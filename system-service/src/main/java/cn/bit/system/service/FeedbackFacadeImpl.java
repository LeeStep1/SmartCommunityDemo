package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.system.Feedback;
import cn.bit.facade.service.system.FeedbackFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.system.dao.FeedbackRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("feedbackFacade")
@Slf4j
public class FeedbackFacadeImpl implements FeedbackFacade {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Feedback addFeedback(Feedback feedback) throws BizException {
        feedback.setCreateAt(new Date());
        feedback.setUpdateAt(feedback.getCreateAt());
        feedback.setDataStatus(DataStatusType.VALID.KEY);
        return feedbackRepository.insert(feedback);
    }

    @Override
    public Feedback updateFeedback(Feedback feedback) throws BizException{
        feedback.setUpdateAt(new Date());
        feedback.setDataStatus(DataStatusType.VALID.KEY);
        return feedbackRepository.updateOne(feedback);
    }

    @Override
    public List<Feedback> getFeedbacksByAppId(ObjectId appId) throws BizException {
        return feedbackRepository.findByAppIdOrderByCreateAtDesc(appId);
    }

    @Override
    public Page<Feedback> getFeedbacksByAppId(ObjectId appId, int page, int size) throws BizException {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Feedback> resultPage = feedbackRepository
                .findByAppIdIgnoreNullAndDataStatus(appId, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public Feedback getFeedbackById(ObjectId id) throws BizException {
        return feedbackRepository.findById(id);
    }

    @Override
    public Feedback deleteFeedbackById(ObjectId id) throws BizException {
        Feedback feedback = new Feedback();
        feedback.setDataStatus(DataStatusType.INVALID.KEY);
        feedback.setUpdateAt(new Date());
        return feedbackRepository.updateById(feedback, id);
    }

}
