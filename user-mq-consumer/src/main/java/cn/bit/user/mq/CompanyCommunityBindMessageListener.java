package cn.bit.user.mq;

import cn.bit.common.facade.company.message.CompanyCommunityBindingMessage;
import cn.bit.common.facade.company.model.Employee;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.common.facade.system.constant.RoleConstants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 企业绑定社区，需要新增企业管理员到指定社区
 */
@Component
@Slf4j
public class CompanyCommunityBindMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Resource
    private RegistrationFacade registrationFacade;

    @Resource
    private CompanyFacade companyFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                CompanyCommunityBindingMessage bindMessage = JSON.parseObject(msg.getBody(), CompanyCommunityBindingMessage.class);
                log.info("企业绑定社区 bindMessage: {}", bindMessage);
                Employee employee = companyFacade.getCompanyManagerByCompanyId(bindMessage.getCompanyId());
                if (employee == null) {
                    log.error("企业管理员不存在，直接返回success. ");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                Set<String> roles = null;
                if (employee.getRoles() != null && !employee.getRoles().isEmpty()) {
                    roles = new HashSet<>(employee.getRoles().size());
                    for (String role : employee.getRoles()) {
                        // 系统层企业管理员需要转换成业务层企业管理员
                        if (RoleConstants.ROLE_STR_TENANT_ADMIN.equals(role)
                                || RoleType.COMPANY_ADMIN.name().equals(role)) {
                            role = cn.bit.facade.constant.RoleConstants.ROLE_STR_COMPANY_ADMIN;
                        }
                        roles.add(role);
                    }
                }
                // 未注册的企业管理员，需要登记信息
                if (employee.getUserId() == null) {
                    Registration registration = new Registration();
                    registration.setEmployeeId(employee.getId());
                    registration.setPhone(employee.getPhone());
                    registration.setRoles(roles);
                    registration.setCommunityId(bindMessage.getCommunityId());
                    registration.setPartner(bindMessage.getPartner());
                    registrationFacade.addRegistration(registration);
                } else {
                    // 已注册的企业管理员直接分派角色
                    UserToProperty userToProperty = new UserToProperty();
                    userToProperty.setId(employee.getId());
                    userToProperty.setPostCode(roles);
                    userToProperty.setPhone(employee.getPhone());
                    userToProperty.setCommunityId(bindMessage.getCommunityId());
                    userToProperty.setPropertyId(bindMessage.getCompanyId());
                    userToProperty.setUserId(employee.getUserId());
                    // 更新 client-user, cm-user
                    userToPropertyFacade.upsertClientUserAndCMUserForAllocation(bindMessage.getPartner(), userToProperty);
                }
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("企业绑定社区消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，企业绑定社区数据非法，ack，移除消息
                log.warn("企业绑定社区数据非法：{}", e);
            }
        }
        log.info("企业绑定社区消息消费完成");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
