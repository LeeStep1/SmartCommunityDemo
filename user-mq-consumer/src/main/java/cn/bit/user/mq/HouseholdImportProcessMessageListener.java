package cn.bit.user.mq;

import cn.bit.common.facade.system.model.Sign;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.enums.AuditStatusType;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.SmsTempletType;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.communityIoT.MessageParam;
import cn.bit.facade.model.user.ClientUser;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.model.user.User;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.property.PropertyFacade;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.property.Property;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.httpclient.HttpUtils;
import cn.bit.framework.utils.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.bit.facade.exception.CommonBizException.APP_NAME_INVALID;
import static cn.bit.facade.exception.CommonBizException.SIGN_NAME_INVALID;

/**
 * 住户档案业务处理
 */
@Component
@Slf4j
public class HouseholdImportProcessMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    @Autowired
    private PropertyFacade propertyFacade;

    @Resource
    private CommunityFacade communityFacade;

    @Resource
    private SystemFacade systemFacade;

    @Value("${send.msg}")
    private Boolean needSendMsg;

    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.appid}")
    private String smsAppId;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                Map<String, Object> map = JSON.parseObject(msg.getBody(), Map.class);
                Household household = JSONObject.parseObject(map.get("household").toString(), Household.class);
                log.info("住户档案业务处理 household: {}", household);
                Boolean sendMsg = (Boolean) map.get("sendMsg");
                Property property = propertyFacade.findByCommunityId(household.getCommunityId());
                if (needSendMsg && sendMsg) {
                    Community community = communityFacade.findOne(household.getCommunityId());
                    MessageParam.Params params = new MessageParam.Params();
                    params.setPhone(household.getPhone());
                    params.setCompany(property.getName());
                    params.setCommunity(community.getName());
                    params.setHouse(household.getRoomLocation());
                    // 给住户端发送短信，所以用住户端的签名
                    Sign sign = systemFacade.getSignByClientAndPartner(ClientType.HOUSEHOLD.value(), property.getPartner());
                    if (sign == null) {
                        throw SIGN_NAME_INVALID;
                    }
                    if (StringUtil.isBlank(sign.getAppName())) {
                        throw APP_NAME_INVALID;
                    }
                    params.setAppName(sign.getAppName());
                    sendMsg(SmsTempletType.SMS100018.getKey(), household.getPhone(), sign.getName(), params);
                }
                // 需要校验业主是否已经注册
                User user = userFacade.findByPhone(household.getPhone());
                if (user == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                ObjectId userId = user.getId();
                ClientUser clientUser = userFacade.getClientUserByClientAndPartnerAndUserId(ClientType.HOUSEHOLD.value(),
                        property.getPartner(), userId);
                if (clientUser == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                UserToRoom toGet = userToRoomFacade.findOwnerReviewingRecordByRoomIdAndUserId(household.getRoomId(), userId);
                UserToRoom upsert = new UserToRoom();

                if (toGet == null) {
                    BeanUtils.copyProperties(household, upsert);
                    upsert.setId(null);
                    upsert.setUserId(userId);
                    upsert.setProprietorId(userId);
                    upsert.setName(household.getUserName());
                } else {
                    upsert.setId(toGet.getId());
                }
                upsert = userToRoomFacade.upsertAuthOwnerRecord(property.getPartner(), upsert);
                if (upsert == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                Household toUpdate = new Household();
                toUpdate.setId(household.getId());
                toUpdate.setUserId(userId);
                toUpdate.setActivated(true);
                householdFacade.modifyHousehold(toUpdate);
                updateHouseholdNumAndCheckUserRealInfo(upsert);
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("住户档案业务处理消息消费失败，出现异常：", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                // 业务异常，住户档案业务处理数据非法，ack，移除消息
                log.warn("住户档案数据非法：", e);
            }
        }
        log.info("住户档案业务处理消息消费完成");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 发送短信
     *
     * @param tplId
     * @param phone
     * @param signName
     * @param params
     * @return
     * @throws Exception
     */
    private String sendMsg(String tplId, String phone, String signName, MessageParam.Params params) throws Exception {
        MessageParam messageParam = new MessageParam();
        messageParam.setTplId(tplId);
        messageParam.setNumber(phone);
        messageParam.setSignName(signName);
        messageParam.setParams(params);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("charset", "utf-8");
        requestHeaders.put("APPID", smsAppId);
        return HttpUtils.doPost(smsUrl, requestHeaders, JSONObject.toJSONString(messageParam));
    }

    /**
     * 更新社区入住人数，校验业主是否需要实名
     *
     * @param userToRoom
     */
    private void updateHouseholdNumAndCheckUserRealInfo(UserToRoom userToRoom) {
        if (userToRoom.getAuditStatus() == AuditStatusType.REVIEWED.getType()) {
            appendUserRealInfo(userToRoom);
            // 更新社区人口
            Community toUpdate = new Community();
            toUpdate.setHouseholdCnt(1);
            toUpdate.setCheckInRoomCnt(1);
            communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, userToRoom.getCommunityId());
        }
    }

    /**
     * 检测用户是否需要实名认证
     *
     * @param userToRoom
     */
    private void appendUserRealInfo(UserToRoom userToRoom) {
        UserVO userVO = userFacade.findById(userToRoom.getUserId());
        if (userVO != null && StringUtil.isBlank(userVO.getIdentityCard())
                && (StringUtil.isBlank(userVO.getName()) || StringUtil.isNotBlank(userToRoom.getIdentityCard()))) {
            User user = new User();
            user.setId(userToRoom.getUserId());
            user.setName(userToRoom.getName());
            user.setIdentityCard(userToRoom.getIdentityCard());
            userFacade.updateUser(user);
        }
    }
}
