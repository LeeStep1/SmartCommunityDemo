package cn.bit.job.handler;

import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.vo.user.UserCommunityVO;
import cn.bit.framework.data.common.Page;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cn.bit.facade.constant.mq.TagConstant.ELEVATOR;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITYIOT_UNIVERSAL_CERTIFICATE;

/**
 * @Description : 定时生成万能凭证
 * @author xiaoxi.lao
 * @date 2018/9/11 10:02
 */

@Component
@Slf4j
@JobHandler(value = "certificateJobHandler")
public class CertificateJobHandler extends IJobHandler {

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private DefaultMQProducer deviceAuthProducer;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        int page = 1;
        Page<UserCommunityVO> communityVOPage;

        do {
            communityVOPage = communityFacade.queryPage(null, null, null, true, page++, 1000);
            for (UserCommunityVO community : communityVOPage.getRecords()) {
                Message doorMessage = new Message(TOPIC_COMMUNITYIOT_UNIVERSAL_CERTIFICATE, ELEVATOR,
                        community.getId().toHexString().getBytes());
                doorMessage.setKeys(community.getId().toHexString());
                try {
                    deviceAuthProducer.send(doorMessage);
                } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                    XxlJobLogger.log("create " + community.getName() + " universal certificate failed");
                }
            }
        } while (communityVOPage.hasNextPage());

        return ReturnT.SUCCESS;
    }
}
