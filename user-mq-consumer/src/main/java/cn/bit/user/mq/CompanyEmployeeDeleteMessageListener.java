package cn.bit.user.mq;

import cn.bit.common.facade.company.message.EmployeeMessage;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
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
import java.util.List;

/**
 * 注销企业员工
 */
@Component
@Slf4j
public class CompanyEmployeeDeleteMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Resource
    private CompanyFacade companyFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                EmployeeMessage employeeMessage = JSON.parseObject(msg.getBody(), EmployeeMessage.class);
                log.info("注销企业员工 employeeMessage: {}", employeeMessage);
                ObjectId userId = employeeMessage.getUserId();
                ObjectId companyId = employeeMessage.getCompanyId();

                List<CompanyToCommunity> communities = companyFacade.listCommunitiesByCompanyId(companyId);
                if (communities.isEmpty()) {
                    log.info("企业没有绑定社区，无需处理社区下的员工岗位");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                communities.forEach(companyToCommunity -> userToPropertyFacade.deleteCompanyEmployee(
                        employeeMessage.getPartner(), userId, companyId, companyToCommunity.getCommunityId()));
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("注销企业员工消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，注销企业员工数据非法，ack，移除消息
                log.warn("注销企业员工数据非法：{}", e);
            }
        }
        log.info("注销企业员工消息消费完成");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
