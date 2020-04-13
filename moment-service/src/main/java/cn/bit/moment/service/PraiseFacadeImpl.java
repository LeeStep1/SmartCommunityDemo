package cn.bit.moment.service;

import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.MomentMessageType;
import cn.bit.facade.enums.MomentStatusType;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Praise;
import cn.bit.facade.service.moment.PraiseFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.moment.dao.MessageRepository;
import cn.bit.moment.dao.MomentRepository;
import cn.bit.moment.dao.PraiseRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.moment.MomentException.*;

@Component("praiseFacade")
@Slf4j
public class PraiseFacadeImpl implements PraiseFacade {

    @Autowired
    private PraiseRepository praiseRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 点赞
     *
     * @param momentId
     * @param creatorId
     * @return
     */
    @Override
    public Message addPraise(ObjectId momentId, Integer partner, ObjectId creatorId) {
        Moment moment = this.checkMoment(momentId, null);
        Date createAt = new Date();
        Praise praise = new Praise();
        praise.setCommunityId(moment.getCommunityId());
        praise.setCreatorId(creatorId);
        praise.setMomentId(momentId);
        praise.setDataStatus(DataStatusType.VALID.KEY);
        praise.setCreateAt(createAt);
        Integer result = praiseRepository.insertPraiseWithChecked(praise);
        if (result == null) {
            throw OPERATION_FAILURE;
        }
        // 重复点赞
        if (result == 0) {
            log.info("addPraise 重复点赞");
            return null;
        }
        // 更新动态的点赞数量（+1）
        momentRepository.updateNumByIdAndFieldName(momentId, "praiseNum", 1);
        // 第一次点赞插入点赞记录，并且不是自己点赞自己
        if (result == 1 && !creatorId.equals(moment.getCreatorId())) {
            // 写入消息记录表
            Message toAddMsg = new Message();
            toAddMsg.setType(MomentMessageType.PRAISE.getKey());
            toAddMsg.setCommunityId(moment.getCommunityId());
            toAddMsg.setNoticeTo(moment.getCreatorId());
            toAddMsg.setMomentContent(moment.getContent());
            toAddMsg.setMomentPhotos(moment.getPhotos());
            toAddMsg.setMomentType(moment.getType());
            toAddMsg.setCreatorId(creatorId);
            toAddMsg.setMomentId(momentId);
            toAddMsg.setCreateAt(createAt);//消息创建时间应该与点赞时间一致
            toAddMsg.setDataStatus(DataStatusType.VALID.KEY);
            toAddMsg = messageRepository.insert(toAddMsg);
            if (toAddMsg != null && toAddMsg.getCreatorId() != null) {
                UserVO userVO = userFacade.getUserById(ClientType.HOUSEHOLD.value(), partner, toAddMsg.getCreatorId());
                if (userVO != null) {
                    toAddMsg.setCreatorName(userVO.getNickName());
                    toAddMsg.setCreatorHeadImg(userVO.getHeadImg());
                }
            }
            return toAddMsg;
        }
        return null;
    }

    /**
     * 取消点赞
     *
     * @param momentId
     * @param creatorId
     * @return
     */
    @Override
    public int deletePraise(ObjectId momentId, ObjectId creatorId) {
        this.checkMoment(momentId, null);
        Praise toUpdate = new Praise();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        int result = praiseRepository.updateMultiByMomentIdAndCreatorId(toUpdate, momentId, creatorId);
        if (result < 1) {
            log.info("没有已点赞需要取消的记录，不更新动态的点赞数量");
            return result;
        }
        // 更新动态的点赞数量（-1）
        momentRepository.updateNumByIdAndFieldName(momentId, "praiseNum", -1);
        return result;
    }

    private Moment checkMoment(ObjectId momentId, Integer client) {
        if (momentId == null) {
            throw MOMENT_ID_IS_NULL;
        }
        Moment toGet = momentRepository.findById(momentId);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            log.info("checkMoment end 动态不存在");
            throw MOMENT_IS_DELETED;
        }
        // 住户端
        if ((client == null || client == ClientType.HOUSEHOLD.value())
                && !Arrays.asList(MomentStatusType.AUTOREVIEWED.getKey(), MomentStatusType.REVIEWED.getKey())
                .contains(toGet.getStatus())) {
            log.info("checkMoment end 动态已被屏蔽");
            throw MOMENT_IS_DELETED;
        }
        return toGet;
    }

    private void packagePraise(List<Praise> praiseList, List<UserVO> userList, Integer client, Integer partner) {
        if (userList == null || userList.size() == 0) {
            // 遍历得到点赞者的id集合
            Set<ObjectId> userIds = praiseList.stream().map(Praise::getCreatorId).collect(Collectors.toSet());
            if (userIds == null) {
                log.info("userIds is null, packagePraise end return pageList without any creator info !!!");
                return;
            }

            // 根据id集合查询点赞者的信息集合
            if (ClientType.HOUSEHOLD.value() == client) {
                userList = userFacade.listClientUserByClientAndUserIds(client, partner, userIds);
            } else {
                userList = userFacade.findByIds(userIds);
            }

        }

        if (userList == null || userList.size() == 0) {
            log.info("userList is null, packagePraise end return pageList without any creator info !!!");
            return;
        }
        Map<ObjectId, UserVO> userVOMap = new HashMap<>();
        userList.forEach(userVO -> userVOMap.put(userVO.getId(), userVO));
        // 封装点赞者的个人信息（头像，名字）
        for (Praise praise : praiseList) {
            UserVO userVO = userVOMap.get(praise.getCreatorId());
            if (userVO != null) {
                praise.setCreatorName(client == ClientType.HOUSEHOLD.value() ? userVO.getNickName() :
                        !StringUtil.isNotNull(userVO.getName()) ? userVO.getPhone() : userVO.getName());
                praise.setCreatorHeadImg(userVO.getHeadImg());
            }
        }
    }

    /**
     * 根据用户、社区查询点赞过的动态集合
     *
     * @param currUserId
     * @param communityId
     * @return
     */
    @Override
    public List<Praise> findByCreatorIdAndCommunityId(ObjectId currUserId, ObjectId communityId) {
        if (currUserId == null || communityId == null) {
            log.info("用户ID或者社区ID为空");
            return Collections.emptyList();
        }
        return praiseRepository.findByCreatorIdAndCommunityIdAndDataStatus(currUserId, communityId, DataStatusType.VALID.KEY);
    }

    /**
     * 根据动态id增量获取点赞列表
     *
     * @param incrementalRequest
     * @param client
     * @return
     */
    @Override
    public List<Praise> incrementalPraiseList(IncrementalRequest incrementalRequest, Integer client, Integer partner) {
        ObjectId momentId = incrementalRequest.getMomentId();
        this.checkMoment(momentId, client);
        List<Praise> praiseList = praiseRepository.incrementalPraiseList(incrementalRequest);
        if (praiseList == null || praiseList.isEmpty()) {
            return praiseList;
        }
        this.packagePraise(praiseList, null, client, partner);
        return praiseList;
    }

    /**
     * 根据用户ID、动态ID查询是否已经点赞
     *
     * @param momentId
     * @param currUserId
     * @return
     */
    @Override
    public Praise findByMomentIdAndCreatorId(ObjectId momentId, ObjectId currUserId) {
        if (momentId == null) {
            throw MOMENT_ID_IS_NULL;
        }
        return praiseRepository.findByMomentIdAndCreatorIdAndDataStatus(momentId, currUserId, DataStatusType.VALID.KEY);
    }

    /**
     * 增量查询自己点过赞的列表
     *
     * @param incrementalRequest
     * @param communityId
     * @param uid
     * @return
     */
    @Override
    public List<Praise> incrementalMyPraiseList(IncrementalRequest incrementalRequest, Integer partner,
                                                ObjectId communityId, ObjectId uid) {
        List<Praise> praiseList = praiseRepository.incrementalMyPraiseList(incrementalRequest, communityId, uid);
        if (praiseList == null || praiseList.size() == 0) {
            log.info("incrementalMyPraiseList end return null !!!");
            return praiseList;
        }
        Set<ObjectId> momentIds = praiseList.stream().map(Praise::getMomentId).collect(Collectors.toSet());
        List<Moment> momentList = momentRepository.findByIdIn(momentIds);
        Map<ObjectId, Moment> momentMap = new HashMap<>();
        momentList.forEach(moment -> momentMap.put(moment.getId(), moment));
        UserVO userVO = userFacade.getUserById(ClientType.HOUSEHOLD.value(), partner, uid);
        if (userVO != null) {
            praiseList.forEach(praise -> {
                praise.setCreatorName(userVO.getNickName());
                praise.setCreatorHeadImg(userVO.getHeadImg());
            });
        }
        if (momentMap.size() > 0) {
            for (Praise praise : praiseList) {
                Moment moment = momentMap.get(praise.getMomentId());
                if (moment == null) {
                    // 可以设置主体被删除的标识
                    continue;
                }
                praise.setMomentContent(moment.getContent());
                praise.setMomentPhotos(moment.getPhotos());
            }
        }
        return praiseList;
    }

    @Override
    public Long statisticsPraise(ObjectId communityId, ObjectId creatorId) {
        return praiseRepository.statisticsPraise(communityId, creatorId);
    }
}
