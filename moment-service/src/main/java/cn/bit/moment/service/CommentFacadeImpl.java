package cn.bit.moment.service;

import cn.bit.facade.enums.*;
import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Report;
import cn.bit.facade.service.moment.CommentFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.moment.CommentMsgVO;
import cn.bit.facade.vo.moment.CommentVO;
import cn.bit.facade.vo.moment.RequestVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.moment.dao.CommentRepository;
import cn.bit.moment.dao.MessageRepository;
import cn.bit.moment.dao.MomentRepository;
import cn.bit.moment.dao.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.moment.MomentException.*;

@Component("commentFacade")
@Slf4j
public class CommentFacadeImpl implements CommentFacade {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 根据动态ID分页获取评论
     *
     * @param momentId
     * @param currUserId
     * @param client
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Comment> findPageByMomentId(ObjectId momentId, ObjectId currUserId,
                                            Integer client, Integer partner, int page, int size) {
        Moment toCheck = this.checkMoment(momentId, client);
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Comment> commentPage =
                commentRepository.findByMomentIdAndStatusAndDataStatusAllIgnoreNull(
                        momentId, CommentStatusType.NORMAL.getKey(), DataStatusType.VALID.KEY, pageable);
        if (commentPage == null || commentPage.getTotalElements() == 0) {
            log.info("findPageByMomentId end return null !!!");
            return new Page<>();
        }

        this.packageComment(commentPage.getContent(), null, currUserId, toCheck.getCommunityId(), client, partner);
        return PageUtils.getPage(commentPage);
    }

    /**
     * 创建一个评论
     *
     * @param commentVO
     * @param creatorId
     * @return
     */
    @Override
    public CommentMsgVO addComment(CommentVO commentVO, Integer partner, ObjectId creatorId) {
        if (!StringUtil.isNotNull(commentVO.getContent())) {
            throw CONTENT_IS_NULL;
        }
        CommentMsgVO msgVO = new CommentMsgVO();
        Moment toGetMoment = this.checkMoment(commentVO.getMomentId(), null);
        Comment toAddComment = new Comment();
        BeanUtils.copyProperties(commentVO, toAddComment);
        toAddComment.setCommunityId(toGetMoment.getCommunityId());
        toAddComment.setStatus(CommentStatusType.NORMAL.getKey());
        toAddComment.setDataStatus(DataStatusType.VALID.KEY);
        toAddComment.setCreatorId(creatorId);
        toAddComment.setCreateAt(new Date());
        // 自己不能回复自己
        if (toAddComment.getCreatorId().equals(toAddComment.getAnswerTo())) {
            log.info("回复自己的言论，不记录 answerTo ...");
            toAddComment.setAnswerTo(null);
        }
        toAddComment = commentRepository.insert(toAddComment);
        if (toAddComment == null) {
            log.info("新增评论失败，返回null ");
            return null;
        }
        msgVO.setComment(toAddComment);
        // 更新该动态的评论数量（+1）
        momentRepository.updateNumByIdAndFieldName(toAddComment.getMomentId(), "commentNum", 1);

        // 评论自己的动态不写入消息记录表
        if (!toAddComment.getCreatorId().equals(toGetMoment.getCreatorId()) || toAddComment.getAnswerTo() != null) {
            // 写入消息记录表
            Message toAddMsg = new Message();
            toAddMsg.setType(MomentMessageType.COMMENT.getKey());
            toAddMsg.setCommunityId(toAddComment.getCommunityId());
            toAddMsg.setCreatorId(toAddComment.getCreatorId());
            toAddMsg.setMomentId(toAddComment.getMomentId());
            toAddMsg.setMomentType(toGetMoment.getType());
            toAddMsg.setContent(toAddComment.getContent());
            // 消息创建时间应该与评论时间一致
            toAddMsg.setCreateAt(toAddComment.getCreateAt());
            // 推送给动态发布者
            toAddMsg.setNoticeTo(toGetMoment.getCreatorId());
            // 回复评论
            if (toAddComment.getAnswerTo() != null) {
                // 推送给发布评论的人
                toAddMsg.setNoticeTo(toAddComment.getAnswerTo());
            }
            toAddMsg.setMomentContent(toGetMoment.getContent());
            toAddMsg.setMomentPhotos(toGetMoment.getPhotos());
            toAddMsg.setDataStatus(DataStatusType.VALID.KEY);
            messageRepository.insert(toAddMsg);
            msgVO.setMessage(toAddMsg);
        }
        Set<ObjectId> userIds = new HashSet<>();
        userIds.add(creatorId);
        if (toAddComment.getAnswerTo() != null) {
            userIds.add(toAddComment.getAnswerTo());
        }
        List<UserVO> userVOList = userFacade.listClientUserByClientAndUserIds(ClientType.HOUSEHOLD.value(), partner,
                userIds);
        if (userVOList == null || userVOList.size() == 0) {
            return msgVO;
        }
        Map<ObjectId, UserVO> userVOMap = new HashMap<>();
        for (UserVO userVO : userVOList) {
            userVOMap.put(userVO.getId(), userVO);
        }
        toAddComment.setCreatorName(userVOMap.get(toAddComment.getCreatorId()).getNickName());
        toAddComment.setCreatorHeadImg(userVOMap.get(toAddComment.getCreatorId()).getHeadImg());
        if (toAddComment.getAnswerTo() != null) {
            toAddComment.setAnswerToName(userVOMap.get(toAddComment.getAnswerTo()).getNickName());
        }
        if (msgVO.getMessage() != null && msgVO.getMessage().getCreatorId() != null) {
            msgVO.getMessage().setCreatorName(userVOMap.get(msgVO.getMessage().getCreatorId()).getNickName());
            msgVO.getMessage().setCreatorHeadImg(userVOMap.get(msgVO.getMessage().getCreatorId()).getHeadImg());
        }
        return msgVO;
    }

    /**
     * 删除评论
     *
     * @param id
     * @param uid
     * @return
     */
    @Override
    public Comment deleteById(ObjectId id, ObjectId uid) {
        Comment toCheck = this.checkComment(id);
        if (!toCheck.getCreatorId().equals(uid)) {
            throw CAN_NOT_DELETE;
        }
        Comment toUpdateComment = new Comment();
        toUpdateComment.setId(id);
        toUpdateComment.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdateComment.setUpdateAt(new Date());
        toUpdateComment = commentRepository.updateOne(toUpdateComment);
        if (toUpdateComment == null) {
            log.info("删除评论失败，返回null");
            return null;
        }
        //更新该动态的评论数量（-1）
        momentRepository.updateNumByIdAndFieldName(toUpdateComment.getMomentId(), "commentNum", -1);
        //修改消息内容（该评论已删除）
        Message toGetMsg = messageRepository.findByMomentIdAndCreatorIdAndCreateAtAndDataStatus(
                toUpdateComment.getMomentId(), uid, toUpdateComment.getCreateAt(), DataStatusType.VALID.KEY);
        if (toGetMsg != null) {
            Message message = new Message();
            message.setId(toGetMsg.getId());
            message.setIsDeleted(Boolean.TRUE);
            message.setUpdateAt(new Date());
            messageRepository.updateOne(message);
        }
        return toUpdateComment;
    }

    /**
     * 根据社区查询评论列表
     *
     * @param requestVO
     * @param page
     * @param size
     * @param client
     * @return
     */
    @Override
    public Page<Comment> findPageByRequestVO(RequestVO requestVO, int page, int size, Integer client, Integer partner) {
        if (requestVO.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Comment> commentPage =
                commentRepository.findByCommunityIdAndStatusAndReportNumGreaterThanEqualAndDataStatusAllIgnoreNull(
                        requestVO.getCommunityId(), requestVO.getStatus(), requestVO.getReportNum(),
                        DataStatusType.VALID.KEY, pageable);
        if (commentPage == null || commentPage.getTotalElements() == 0) {
            log.info("findPageByRequestVO end return null !!!");
            return new Page<>();
        }
        this.packageComment(commentPage.getContent(), requestVO.getCreatorId(), null,
                requestVO.getCommunityId(), client, partner);
        return PageUtils.getPage(commentPage);
    }

    /**
     * 更新对应字段的数量(举报)
     *
     * @param commentId
     * @param fieldName
     * @param num
     */
    @Override
    public void updateNumByIdAndFieldName(ObjectId commentId, String fieldName, int num) {
        if (commentId == null) {
            return;
        }
        if (!StringUtil.isNotNull(fieldName)) {
            return;
        }
        if (num == 0) {
            return;
        }
        commentRepository.updateNumByIdAndFieldName(commentId, fieldName, num);
    }

    /**
     * 根据id获取评论详细
     *
     * @param commentId
     * @return
     */
    @Override
    public Comment findById(ObjectId commentId) {
        if (commentId == null) {
            throw COMMENT_ID_IS_NULL;
        }
        return commentRepository.findById(commentId);
    }

    /**
     * 根据动态id增量获取评论
     *
     * @param incrementalRequest
     * @param currUserId
     * @param client
     * @return
     */
    @Override
    public List<Comment> incrementalCommentList(IncrementalRequest incrementalRequest,
                                                ObjectId currUserId, Integer client, Integer partner) {
        ObjectId momentId = incrementalRequest.getMomentId();
        if (momentId == null) {
            throw MOMENT_ID_IS_NULL;
        }
        Moment toCheck = this.checkMoment(momentId, client);
        List<Comment> commentList = commentRepository.incrementalCommentList(incrementalRequest);
        if (commentList == null || commentList.size() == 0) {
            log.info("incrementalCommentList end return null !!!");
            return commentList;
        }

        this.packageComment(commentList, null, currUserId, toCheck.getCommunityId(), client, partner);
        return commentList;
    }

    /**
     * 更新一条评论
     *
     * @param comment
     * @return
     */
    @Override
    public Comment updateOne(Comment comment) {
        return commentRepository.updateOne(comment);
    }

    /**
     * 增量获取用户的评论列表
     *
     * @param incrementalRequest
     * @param communityId
     * @param uid
     * @return
     */
    @Override
    public List<Comment> incrementalMyCommentList(IncrementalRequest incrementalRequest, Integer partner,
                                                  ObjectId communityId, ObjectId uid) {
        List<Comment> commentList = commentRepository.incrementalMyCommentList(incrementalRequest, communityId, uid);
        if (commentList == null || commentList.size() == 0) {
            log.info("incrementalMyCommentList end return null !!!");
            return commentList;
        }
        Set<ObjectId> momentIds = commentList.stream().map(Comment::getMomentId).collect(Collectors.toSet());
        List<Moment> momentList = momentRepository.findByIdIn(momentIds);
        Map<ObjectId, Moment> momentMap = new HashMap<>();
        momentList.forEach(moment -> momentMap.put(moment.getId(), moment));
        UserVO userVO = userFacade.getUserById(ClientType.HOUSEHOLD.value(), partner, uid);
        if (userVO != null) {
            commentList.forEach(comment -> {
                comment.setCreatorName(userVO.getNickName());
                comment.setCreatorHeadImg(userVO.getHeadImg());
            });
        }
        if (momentMap.size() > 0) {
            for (Comment comment : commentList) {
                Moment moment = momentMap.get(comment.getMomentId());
                if (moment == null) {
                    // 可以设置主体被删除的标识
                    continue;
                }
                comment.setMomentContent(moment.getContent());
                comment.setMomentPhotos(moment.getPhotos());
            }
        }
        return commentList;
    }

    @Override
    public Long statisticsComment(ObjectId communityId, ObjectId creatorId) {
        return commentRepository.statisticsComment(communityId, creatorId);
    }

    private void packageComment(List<Comment> commentList, Collection<ObjectId> userIds,
                                ObjectId currUserId, ObjectId communityId, Integer client, Integer partner) {
        if (userIds == null || userIds.isEmpty()) {
            userIds = new HashSet<>();
            // 遍历得到评论者的id集合
            for (Comment comment : commentList) {
                userIds.add(comment.getCreatorId());
                // 被回复人的ID
                if (comment.getAnswerTo() != null) {
                    userIds.add(comment.getAnswerTo());
                }
            }
            if (userIds == null || userIds.isEmpty()) {
                log.info("userIds is null, packageComment end return pageList without any creator info !!!");
                return;
            }
        }
        List<UserVO> userVOList = null;
        // 根据id集合查询评论者的信息集合
        if (ClientType.HOUSEHOLD.value() == client) {
            userVOList = userFacade.listClientUserByClientAndUserIds(client, partner, new HashSet<>(userIds));
        } else {
            userVOList = userFacade.findByIds(new HashSet<>(userIds));
        }

        if (userVOList == null || userVOList.isEmpty()) {
            log.info("userList is null, packageComment end return pageList without any creator info !!!");
            return;
        }
        Map<ObjectId, UserVO> userVOMap = new HashMap<>();
        userVOList.forEach(userVO -> userVOMap.put(userVO.getId(), userVO));

        Set<ObjectId> reportedCommentIds = null;
        // 物业人员查看是不需要传userId
        if (currUserId != null) {
            reportedCommentIds = this.findReportByCreatorIdAndCommunityId(currUserId, communityId);
        }
        // 封装评论者的个人信息（头像，名字）, 是否已经举报过评论
        for (Comment comment : commentList) {
            comment.setIsReported(Boolean.FALSE);
            if (reportedCommentIds != null && reportedCommentIds.contains(comment.getId())) {
                comment.setIsReported(Boolean.TRUE);
            }
            // 创建人
            UserVO userVO = userVOMap.get(comment.getCreatorId());
            if (userVO != null) {
                comment.setCreatorName(client == ClientType.HOUSEHOLD.value() ? userVO.getNickName() : userVO.getName());
                comment.setCreatorHeadImg(userVO.getHeadImg());
            }
            // 被回复人
            if (comment.getAnswerTo() != null && userVOMap.get(comment.getAnswerTo()) != null) {
                userVO = userVOMap.get(comment.getAnswerTo());
                comment.setAnswerToName(client == ClientType.HOUSEHOLD.value() ? userVO.getNickName()
                        : StringUtil.isBlank(userVO.getName()) ? userVO.getPhone() : userVO.getName());
            }
        }
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
        if (client == null || client == ClientType.HOUSEHOLD.value()) {
            if (!Arrays.asList(MomentStatusType.AUTOREVIEWED.getKey(), MomentStatusType.REVIEWED.getKey())
                    .contains(toGet.getStatus())) {
                log.info("checkMoment end 动态已被屏蔽");
                throw MOMENT_IS_DELETED;
            }
        }
        return toGet;
    }

    private Comment checkComment(ObjectId id) {
        if (id == null) {
            throw COMMENT_ID_IS_NULL;
        }
        Comment toGet = commentRepository.findById(id);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            log.info("checkComment end 评论不存在");
            throw COMMENT_IS_DELETED;
        }
        return toGet;
    }

    // 根据用户、社区查询举报过的评论集合
    private Set<ObjectId> findReportByCreatorIdAndCommunityId(ObjectId currUserId, ObjectId communityId) {
        if (currUserId == null) {
            log.info("用户ID为空，不需要查询举报过的评论集合");
            return Collections.emptySet();
        }

        List<Report> reportList = reportRepository.findByCreatorIdAndCommunityIdAndType(
                currUserId, communityId, SpeechType.COMMENT.getKey());
        if (reportList == null || reportList.size() == 0) {
            log.info("用户没有举报过任何评论");
            return Collections.emptySet();
        }
        Set<ObjectId> reportedCommentIds = reportList.stream().map(Report::getSpeechId).collect(Collectors.toSet());
        return reportedCommentIds;
    }

}
