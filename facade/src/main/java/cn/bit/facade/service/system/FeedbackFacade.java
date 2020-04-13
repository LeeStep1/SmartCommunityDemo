package cn.bit.facade.service.system;

import cn.bit.facade.model.system.Feedback;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

public interface FeedbackFacade {

    /**
     * 新增信息
     * @param feedback
     * @return
     */
    Feedback addFeedback(Feedback feedback) throws BizException;

    /**
     * 更新信息
     * @param feedback
     * @return
     */
    Feedback updateFeedback(Feedback feedback) throws BizException;

    /**
     * 根据ID获取反馈信息
     * @param id
     * @return
     */
    Feedback getFeedbackById(ObjectId id) throws BizException;

    /**
     * 根据id删除反馈信息
     * @param id
     */
    Feedback deleteFeedbackById(ObjectId id) throws BizException;

    /**
     * 根据appId获取反馈信息
     * @param appId
     * @return
     */
    List<Feedback> getFeedbacksByAppId(ObjectId appId) throws BizException;

    /**
     * 分页
     * @param appId
     * @param page
     * @param size
     * @return
     */
    Page<Feedback> getFeedbacksByAppId(ObjectId appId, int page, int size) throws BizException;

}
