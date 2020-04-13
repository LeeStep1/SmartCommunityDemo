package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Message;
import cn.bit.facade.model.moment.Praise;
import cn.bit.facade.vo.IncrementalRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface PraiseFacade {

    /**
     * 点赞
     * @param momentId
     * @param creatorId
     * @return
     */
    Message addPraise(ObjectId momentId, Integer partner, ObjectId creatorId);

    /**
     * 取消点赞
     * @param momentId
     * @param creatorId
     * @return
     */
    int deletePraise(ObjectId momentId, ObjectId creatorId);

    /**
     * 根据用户/社区查询点赞过的动态集合
     * @param currUserId
     * @param communityId
     * @return
     */
    List<Praise> findByCreatorIdAndCommunityId(ObjectId currUserId, ObjectId communityId);

    /**
     * 根据动态id增量获取点赞列表
     * @param incrementalRequest
     * @param client
     * @return
     */
    List<Praise> incrementalPraiseList(IncrementalRequest incrementalRequest, Integer client, Integer partner);

    /**
     * 根据用户ID/动态ID查询是否已经点赞
     * @param currUserId
     * @param id
     * @return
     */
    Praise findByMomentIdAndCreatorId(ObjectId currUserId, ObjectId id);

    /**
     * 增量查询自己点过赞的列表
     * @param incrementalRequest
     * @param communityId
     * @param uid
     * @return
     */
    List<Praise> incrementalMyPraiseList(IncrementalRequest incrementalRequest, Integer partner, ObjectId communityId, ObjectId uid);

    /**
     * 获取用户的点赞数量
     * @param communityId
     * @param creatorId
     * @return
     */
    Long statisticsPraise(ObjectId communityId, ObjectId creatorId);
}