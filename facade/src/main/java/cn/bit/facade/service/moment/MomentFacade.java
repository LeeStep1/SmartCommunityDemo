package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.moment.MomentRequestVO;
import cn.bit.facade.vo.moment.MomentVO;
import cn.bit.facade.vo.moment.RequestVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface MomentFacade {

    /**
     * 根据动态ID及用户ID获取动态
     * @param id
     * @param currUserId
     * @param client
     * @return
     */
    Moment findByIdAndCurrentUser(ObjectId id, ObjectId currUserId, Integer client, Integer partner);

    /**
     * 新增一条动态信息
     * @param momentVO
     * @param uid
     * @param autoAudit
     * @return
     */
    Moment addMoment(MomentVO momentVO, Integer partner, ObjectId uid, boolean autoAudit);

    /**
     * 根据ID删除动态
     * @param id
     * @param uid
     * @return
     */
    boolean deleteById(ObjectId id, ObjectId uid);

    /**
     * 分页
     * @param momentRequestVO
     * @param currUserId
     * @param page
     * @param size
     * @return
     */
    Page<Moment> queryPageByMomentRequest(MomentRequestVO momentRequestVO, Integer partner, ObjectId currUserId,
                                          Integer page, Integer size);

    /**
     * 更新动态的相关统计数量（评论数，点赞数，举报数）
     * @param momentId
     * @param fieldName
     * @param num
     */
    void updateNumByIdAndFieldName(ObjectId momentId, String fieldName, int num);

    /**
     * 分页获取被举报过的动态
     * @param requestVO
     * @param page
     * @param size
     * @return
     */
    Page<Moment> queryPageByRequestVO(RequestVO requestVO, Integer partner, int page, int size);

    /**
     * 审核动态
     * @param id
     * @param status
     * @param uid
     * @return
     */
    Moment auditMoment(ObjectId id, Integer status, ObjectId uid);

    /**
     * 增量查询社区动态
     * @param incrementalRequest
     * @param communityId
     * @param currUserId
     * @return
     */
    List<Moment> incrementalMomentList(IncrementalRequest incrementalRequest, Integer partner, ObjectId communityId,
                                       ObjectId currUserId);

    /**
     * 增量查询我自己的动态
     * @param incrementalRequest
     * @param communityId
     * @param currUserId
     * @return
     */
    List<Moment> incrementalMyMomentList(IncrementalRequest incrementalRequest, Integer partner, ObjectId communityId,
                                         ObjectId currUserId);

    /**
     * 根据ID更新一条动态
     * @param toUpdate
     * @return
     */
    Moment updateOne(Moment toUpdate);

    /**
     * 统计用户的动态信息数量
     * @param communityId
     * @param creatorId
     * @return
     */
    Long statisticsMoment(ObjectId communityId, ObjectId creatorId);
}