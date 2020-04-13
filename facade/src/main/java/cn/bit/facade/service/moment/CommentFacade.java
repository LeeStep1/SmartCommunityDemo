package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.moment.CommentMsgVO;
import cn.bit.facade.vo.moment.CommentVO;
import cn.bit.facade.vo.moment.RequestVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface CommentFacade {

    /**
     * 根据动态ID分页获取评论
     * @param momentId
     * @param currUserId
     * @param client
     * @param page
     * @param size
     * @return
     */
    Page<Comment> findPageByMomentId(ObjectId momentId, ObjectId currUserId, Integer client, Integer partner, int page,
                                     int size);

    /**
     * 创建一个评论
     * @param commentVO
     * @param creatorId
     * @return
     */
    CommentMsgVO addComment(CommentVO commentVO, Integer partner, ObjectId creatorId);

    /**
     * 删除评论
     * @param id
     * @param uid
     * @return
     */
    Comment deleteById(ObjectId id, ObjectId uid);

    /**
     * 根据社区查询评论列表
     * @param requestVO
     * @param page
     * @param size
     * @param client
     * @return
     */
    Page<Comment> findPageByRequestVO(RequestVO requestVO, int page, int size, Integer client, Integer partner);

    /**
     * 更新对应字段的数量
     * @param commentId
     * @param fieldNme
     * @param num
     */
    void updateNumByIdAndFieldName(ObjectId commentId, String fieldNme, int num);

    /**
     * 根据id获取评论详细
     * @param commentId
     * @return
     */
    Comment findById(ObjectId commentId);

    /**
     * 根据动态id增量获取评论
     * @param incrementalRequest
     * @param currUserId
     * @param client
     * @return
     */
    List<Comment> incrementalCommentList(IncrementalRequest incrementalRequest, ObjectId currUserId, Integer client,
                                         Integer partner);

    /**
     * 更新一条评论
     * @param comment
     * @return
     */
    Comment updateOne(Comment comment);

    /**
     * 增量获取用户的评论列表
     * @param incrementalRequest
     * @param communityId
     * @param uid
     * @return
     */
    List<Comment> incrementalMyCommentList(IncrementalRequest incrementalRequest, Integer partner, ObjectId communityId, ObjectId uid);

    /**
     * 统计评论数量
     * @param communityId
     * @param creatorId
     * @return
     */
    Long statisticsComment(ObjectId communityId, ObjectId creatorId);
}