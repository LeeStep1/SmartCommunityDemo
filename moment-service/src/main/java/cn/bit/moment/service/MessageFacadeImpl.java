package cn.bit.moment.service;

import cn.bit.facade.enums.ClientType;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.service.moment.MessageFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.moment.dao.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.moment.MomentException.ILLEGAL_PARAMETER;
import static cn.bit.facade.exception.user.UserBizException.USER_ID_NULL;

@Component("messageFacade")
@Slf4j
public class MessageFacadeImpl implements MessageFacade {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserFacade userFacade;

    private void packageMessage(List<Message> messageList, Integer partner) {
        List<UserVO> userVOList = userFacade.listClientUserByClientAndUserIds(
                ClientType.HOUSEHOLD.value(), partner,
                messageList.stream().map(Message::getCreatorId).collect(Collectors.toSet()));
        Map<ObjectId, UserVO> userVOMap = new HashMap<>();
        userVOList.forEach(userVO -> userVOMap.put(userVO.getId(), userVO));
        // 封装动态创建消息的个人信息（头像，名字）
        for (Message message : messageList) {
            UserVO userVO = userVOMap.get(message.getCreatorId());
            if (userVO != null) {
                message.setCreatorName(userVO.getNickName());
                message.setCreatorHeadImg(userVO.getHeadImg());
            }
        }
    }

    /**
     * 增量查询消息列表
     *
     * @param incrementalRequest
     * @param communityId
     * @param uid
     * @return
     */
    @Override
    public List<Message> findByIncrementalRequest(IncrementalRequest incrementalRequest, Integer partner,
                                                  ObjectId communityId, ObjectId uid) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (uid == null) {
            throw USER_ID_NULL;
        }
        if (incrementalRequest == null
                || (incrementalRequest.getStartAt() != null && incrementalRequest.getSort() == null)) {
            throw ILLEGAL_PARAMETER;
        }
        List<Message> messageList =
                messageRepository.findByIncrementalRequestAndCommunityIdAndUserId(incrementalRequest, communityId, uid);
        if (messageList == null || messageList.size() == 0) {
            log.info("没有找到对应的消息记录");
            return messageList;
        }
        this.packageMessage(messageList, partner);
        return messageList;
    }
}
