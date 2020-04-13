package cn.bit.user.mq;

import cn.bit.common.facade.company.enums.CompanyTypeEnum;
import cn.bit.common.facade.company.message.EmployeeMessage;
import cn.bit.common.facade.company.model.Company;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.common.facade.system.constant.RoleConstants;
import cn.bit.facade.enums.RoleType;
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
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 员工角色变更, 目前适用场景: 物业公司更换企业管理员
 */
@Component
@Slf4j
public class CompanyEmployeeChangeRolesMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Resource
    private CompanyFacade companyFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                EmployeeMessage employeeMessage = JSON.parseObject(msg.getBody(), EmployeeMessage.class);
                log.info("物业公司更换企业管理员 employeeMessage: {}", employeeMessage);
                ObjectId userId = employeeMessage.getUserId();
                ObjectId employeeId = employeeMessage.getId();
                ObjectId companyId = employeeMessage.getCompanyId();
                String phone = employeeMessage.getPhone();
                String addRole = employeeMessage.getAddRoles() == null ? null
                        : employeeMessage.getAddRoles().iterator().next();
                String removeRole = employeeMessage.getRemoveRoles() == null ? null
                        : employeeMessage.getRemoveRoles().iterator().next();
                if (StringUtil.isBlank(addRole) && StringUtil.isBlank(removeRole)) {
                    log.info("当前员工没有任何角色变更，直接返回success");
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
                    log.info("企业没有绑定社区，无需处理社区的岗位");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                for (CompanyToCommunity companyToCommunity : communities) {
                    UserToProperty userToProperty = new UserToProperty();
                    userToProperty.setId(employeeId);
                    userToProperty.setPhone(phone);
                    userToProperty.setCommunityId(companyToCommunity.getCommunityId());
                    userToProperty.setPropertyId(companyId);
                    userToProperty.setUserId(userId);
                    if (!StringUtil.isBlank(removeRole)) {
                        if (RoleConstants.ROLE_STR_TENANT_ADMIN.equals(removeRole)
                                || RoleType.COMPANY_ADMIN.name().equals(removeRole)) {
                            removeRole = cn.bit.facade.constant.RoleConstants.ROLE_STR_COMPANY_ADMIN;
                        }
                        userToProperty.setPostCode(Collections.singleton(removeRole));
                        log.info("需要移除角色({})", userToProperty.getPostCode());
                        // 移除 client-user, cm-user
                        userToPropertyFacade.removeClientUserAndCMUser(employeeMessage.getPartner(), userToProperty);
                    }
                    if (!StringUtil.isBlank(addRole)) {
                        if (RoleConstants.ROLE_STR_TENANT_ADMIN.equals(addRole)
                                || RoleType.COMPANY_ADMIN.name().equals(addRole)) {
                            addRole = cn.bit.facade.constant.RoleConstants.ROLE_STR_COMPANY_ADMIN;
                        }
                        userToProperty.setPostCode(Collections.singleton(addRole));
                        // 增加 client-user, cm-user
                        log.info("需要增加角色({})", userToProperty.getPostCode());
                        userToPropertyFacade.upsertClientUserAndCMUserForAllocation(employeeMessage.getPartner(),
                                userToProperty);
                    }
                }
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("更换企业管理员消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，更换企业管理员数据非法，ack，移除消息
                log.warn("更换企业管理员数据非法：{}", e);
            }
        }
        log.info("更换企业管理员消息消费完成");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
