package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Silent;
import cn.bit.facade.vo.moment.SilentRequest;
import cn.bit.facade.vo.moment.SilentVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

public interface SilentFacade {

    /**
     * 禁言用户
     * @param silentVO
     * @param communityId
     * @param operatorId
     * @return
     */
    Message silentUser(SilentVO silentVO, ObjectId communityId, ObjectId operatorId);

    /**
     * 解除禁言
     * @param id
     * @return
     */
    boolean relieveSilentUser(ObjectId id);

    /**
     * 分页查询禁言列表
     * @param silentRequest
     * @param page
     * @param size
     * @return
     */
    Page<Silent> findPageBySilentRequest(SilentRequest silentRequest, int page, int size);

    /**
     * 检查禁言状态
     * @param uid
     * @param communityId
     * @return
     */
    boolean checkSilentUser(ObjectId uid, ObjectId communityId);

    /**
     * 根据禁言记录ID查询被屏蔽的评论
     * @param id
     * @param page
     * @param size
     * @return
     */
    Page<Comment> queryShieldingCommentById(ObjectId id, Integer page, Integer size);

    /**
     * 根据禁言记录ID查询被屏蔽的动态
     * @param id
     * @param page
     * @param size
     * @return
     */
    Page<Moment> queryShieldingMomentById(ObjectId id, Integer page, Integer size);
}