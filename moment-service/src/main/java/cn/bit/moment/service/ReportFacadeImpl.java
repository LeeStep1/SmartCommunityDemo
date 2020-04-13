package cn.bit.moment.service;

import cn.bit.facade.enums.CommentStatusType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.MomentStatusType;
import cn.bit.facade.enums.SpeechType;
import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Report;
import cn.bit.facade.service.moment.ReportFacade;
import cn.bit.facade.vo.moment.ReportVO;
import cn.bit.framework.constant.CacheConstant;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.moment.dao.CommentRepository;
import cn.bit.moment.dao.MomentRepository;
import cn.bit.moment.dao.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.moment.MomentException.*;

@Component("reportFacade")
@Slf4j
public class ReportFacadeImpl implements ReportFacade {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    /**
     * 根据用户、社区、举报对象类型查询举报集合
     *
     * @param currUserId
     * @param communityId
     * @param type
     * @return
     */
    @Override
    public List<Report> findByCreatorIdAndCommunityIdAndType(ObjectId currUserId, ObjectId communityId, Integer type) {
        if (currUserId == null || communityId == null || type == null) {
            log.info("请求参数不能为空（用户ID，社区ID，举报对象类型）");
            return Collections.emptyList();
        }
        return reportRepository.findByCreatorIdAndCommunityIdAndType(currUserId, communityId, type);
    }

    /**
     * 新增举报记录
     *
     * @param reportVO
     * @param uid
     * @param maxReportNum
     * @return
     */
    @Override
    public Report addReport(ReportVO reportVO, ObjectId uid, int maxReportNum) {
        if (reportVO.getType() == null) {
            throw TYPE_IS_NULL;
        }
        if (reportVO.getSpeechId() == null) {
            throw SPEECH_ID_IS_NULL;
        }
        if (!StringUtil.isNotNull(reportVO.getReason())) {
            throw REASON_IS_NULL;
        }
        if (maxReportNum <= 0) {
            log.info("最大举报数不合法，重新赋值为10；maxReportNum = " + maxReportNum);
            maxReportNum = 10;
        }
        Report toCheck = null;
        // 举报动态
        if (reportVO.getType() == SpeechType.MOMENT.getKey()) {
            Moment moment = this.checkMoment(reportVO.getSpeechId());
            if (moment.getCreatorId().equals(uid)) {
                throw CAN_NOT_REPORT_ONESELF;
            }
            toCheck = reportRepository.findBySpeechIdAndCreatorIdAndType(moment.getId(), uid, SpeechType.MOMENT.getKey());
            reportVO.setCommunityId(moment.getCommunityId());
        }
        // 举报评论
        if (reportVO.getType() == SpeechType.COMMENT.getKey()) {
            Comment comment = this.checkComment(reportVO.getSpeechId());
            if (comment.getCreatorId().equals(uid)) {
                throw CAN_NOT_REPORT_ONESELF;
            }
            toCheck = reportRepository.findBySpeechIdAndCreatorIdAndType(comment.getId(), uid, SpeechType.COMMENT.getKey());
            reportVO.setCommunityId(comment.getCommunityId());
        }
        if (toCheck != null) {
            throw ALREADY_REPORTED;
        }
        String key = CacheConstant.REPORT_EXPECT_COUNT + uid;
        Long reportCount = RedisTemplateUtil.getLong(key);
        if (reportCount != null && reportCount >= maxReportNum) {
            throw REPORT_LIMIT;
        }
        Report toAdd = new Report();
        BeanUtils.copyProperties(reportVO, toAdd);
        toAdd.setCreateAt(new Date());
        toAdd.setCreatorId(uid);
        toAdd = reportRepository.insert(toAdd);
        if (toAdd == null) {
            log.info("新增举报记录失败，返回null");
            return null;
        }
        // 举报次数写入redis
        if (reportCount == null) {
            RedisTemplateUtil.setLong(key, 1L, DateUtils.getTomorrowZeroSeconds());
        } else {
            RedisTemplateUtil.incrLong(key, 1L);
        }
        int reportNum = 0;
        // 更新举报数（+1）
        if (reportVO.getType() == SpeechType.MOMENT.getKey()) {
            log.info("动态举报数加1");
            // 动态举报数+1
            Moment toUpdate = momentRepository.updateNumByIdAndFieldName(reportVO.getSpeechId(), "reportNum", 1);
            if (toUpdate != null) {
                reportNum = toUpdate.getReportNum();
            }
        }
        // 评论举报数+1
        if (reportVO.getType() == SpeechType.COMMENT.getKey()) {
            log.info("评论举报数加1");
            Comment toUpdate = commentRepository.updateNumByIdAndFieldName(reportVO.getSpeechId(), "reportNum", 1);
            if (toUpdate != null) {
                reportNum = toUpdate.getReportNum();
            }
        }
        toAdd.setReportNum(reportNum);
        return toAdd;
    }

    /**
     * 根据被举报的言论分页查询举报列表
     *
     * @param communityId
     * @param reportVO
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Report> queryPageByCommunityIdAndSpeechIdAndType(ObjectId communityId, ReportVO reportVO,
                                                                 int page, int size) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (reportVO.getSpeechId() == null) {
            throw SPEECH_ID_IS_NULL;
        }
        if (reportVO.getType() == null) {
            throw TYPE_IS_NULL;
        }

        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Report> reportPage = reportRepository.findByCommunityIdAndTypeAndSpeechId(
                communityId, reportVO.getType(), reportVO.getSpeechId(), pageable);
        if (reportPage == null || reportPage.getTotalElements() == 0) {
            log.info("queryPageByCommunityIdAndSpeechIdAndType return null !!!");
            return new Page<>();
        }
        // 举报人不返回（匿名举报）
        reportPage.getContent().forEach(report -> report.setCreatorId(null));
        return PageUtils.getPage(reportPage);
    }

    private Moment checkMoment(ObjectId momentId) {
        if (momentId == null) {
            throw MOMENT_ID_IS_NULL;
        }
        Moment toGet = momentRepository.findById(momentId);
        if (toGet == null || !Arrays.asList(
                MomentStatusType.AUTOREVIEWED.getKey(), MomentStatusType.REVIEWED.getKey()).contains(toGet.getStatus())) {
            log.info("checkMoment end 动态不存在或已被屏蔽");
            throw MOMENT_IS_DELETED;
        }
        return toGet;
    }

    private Comment checkComment(ObjectId commentId) {
        if (commentId == null) {
            throw COMMENT_ID_IS_NULL;
        }
        Comment toGet = commentRepository.findById(commentId);
        if (toGet == null
                || toGet.getDataStatus() == DataStatusType.INVALID.KEY
                || CommentStatusType.NORMAL.getKey() != toGet.getStatus()) {
            log.info("checkComment end 评论不存在或已被屏蔽");
            throw COMMENT_IS_DELETED;
        }
        return toGet;
    }
}
