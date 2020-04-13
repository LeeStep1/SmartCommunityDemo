package cn.bit.user.mq;

import cn.bit.common.facade.company.enums.CompanyTypeEnum;
import cn.bit.common.facade.company.message.EmployeeMessage;
import cn.bit.common.facade.company.model.Company;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.facade.constant.RoleConstants;
import cn.bit.facade.enums.RoleType;
import cn.bit.facade.model.property.Registration;
import cn.bit.facade.service.property.RegistrationFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 新增员工, 目前适用场景: 物业公司创建企业管理员
 */
@Component
@Slf4j
public class CompanyEmployeeAddMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Resource
    private CompanyFacade companyFacade;

    @Resource
    private RegistrationFacade registrationFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                EmployeeMessage employeeMessage = JSON.parseObject(msg.getBody(), EmployeeMessage.class);
                log.info("新增员工 employeeMessage: {}", employeeMessage);
                ObjectId userId = employeeMessage.getUserId();
                ObjectId employeeId = employeeMessage.getId();
                ObjectId companyId = employeeMessage.getCompanyId();
                String phone = employeeMessage.getPhone();
                Set<String> roles = employeeMessage.getAddRoles() == null || employeeMessage.getAddRoles().isEmpty()
                        ? null : new HashSet<>(employeeMessage.getAddRoles());
                if (roles == null) {
                    log.info("当前员工没有分派任何角色，直接返回success");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                Company company = companyFacade.getCompanyByCompanyId(companyId);
                if (company == null) {
                    log.info("企业不存在，直接返回success");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                if (company.getType() != CompanyTypeEnum.PROPERTY.value()) {
                    log.info("企业不是物业公司，直接返回success");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                List<CompanyToCommunity> communities = companyFacade.listCommunitiesByCompanyId(companyId);
                if (communities.isEmpty()) {
                    log.info("企业没有绑定社区，无需处理社区的企业管理员");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                // 转换 企业管理员 角色ID
                if (roles.contains(cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN)
                        || roles.contains(RoleType.COMPANY_ADMIN.name())) {
                    roles.add(RoleConstants.ROLE_STR_COMPANY_ADMIN);
                    roles.remove(cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN);
                    roles.remove(RoleType.COMPANY_ADMIN.name());
                }

                communities.forEach(companyToCommunity -> {
                    // 未注册的企业管理员，需要登记信息
                    if (userId == null) {
                        Registration registration = new Registration();
                        registration.setEmployeeId(employeeId);
                        registration.setPhone(phone);
                        registration.setRoles(roles);
                        registration.setCommunityId(companyToCommunity.getCommunityId());
                        registration.setPartner(employeeMessage.getPartner());
                        registrationFacade.addRegistration(registration);
                    } else {
                        UserToProperty userToProperty = new UserToProperty();
                        userToProperty.setId(employeeId);
                        userToProperty.setPostCode(roles);
                        userToProperty.setPhone(phone);
                        userToProperty.setCommunityId(companyToCommunity.getCommunityId());
                        userToProperty.setPropertyId(companyId);
                        userToProperty.setUserId(userId);
                        // 更新 client-user, cm-user
                        userToPropertyFacade.upsertClientUserAndCMUserForAllocation(
                                employeeMessage.getPartner(), userToProperty);
                    }
                });
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("新增员工消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，新增员工数据非法，ack，移除消息
                log.warn("新增员工数据非法：{}", e);
            }
        }
        log.info("新增员工消息消费完成");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
