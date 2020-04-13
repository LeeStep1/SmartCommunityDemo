package cn.bit.moment.service;

import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.MomentStatusType;
import cn.bit.facade.enums.SpeechType;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Praise;
import cn.bit.facade.model.moment.Report;
import cn.bit.facade.service.moment.MomentFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.moment.MomentRequestVO;
import cn.bit.facade.vo.moment.MomentVO;
import cn.bit.facade.vo.moment.RequestVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.moment.dao.MomentRepository;
import cn.bit.moment.dao.PraiseRepository;
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
import static cn.bit.facade.exception.user.UserBizException.USER_ID_NULL;

@Component("momentFacade")
@Slf4j
public class MomentFacadeImpl implements MomentFacade {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private PraiseRepository praiseRepository;

    @Autowired
    private ReportRepository reportRepository;

    /**
     * 根据动态ID及用户ID获取动态
     *
     * @param id
     * @param currUserId
     * @param client
     * @return
     */
    @Override
    public Moment findByIdAndCurrentUser(ObjectId id, ObjectId currUserId, Integer client, Integer partner) {
        Moment toGet = momentRepository.findById(id);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw MOMENT_IS_DELETED;
        }
        if (currUserId != null && client == ClientType.HOUSEHOLD.value()) {
            // 查询当前用户是否已对此动态点赞
            Praise praise = praiseRepository.findByMomentIdAndCreatorIdAndDataStatus(id,
                    currUserId, DataStatusType.VALID.KEY);
            if (praise != null) {
                toGet.setIsPraised(Boolean.TRUE);
            } else {
                toGet.setIsPraised(Boolean.FALSE);
            }
        }
        UserVO userVO = userFacade.getUserById(ClientType.HOUSEHOLD.value(), partner, toGet.getCreatorId());
        if (userVO != null) {
            toGet.setCreatorHeadImg(userVO.getHeadImg());
            toGet.setCreatorName(client == ClientType.HOUSEHOLD.value() ? userVO.getNickName() : userVO.getName());
        }
        return toGet;
    }

    /**
     * 新增一条动态信息
     *
     * @param momentVO
     * @param uid
     * @param autoAudit
     * @return
     */
    @Override
    public Moment addMoment(MomentVO momentVO, Integer partner, ObjectId uid, boolean autoAudit) {

        if (momentVO.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (momentVO.getType() == null) {
            throw MOMENT_TYPE_IS_NULL;
        }
        if (!StringUtil.isNotNull(momentVO.getContent())
                && (momentVO.getPhotos() == null || momentVO.getPhotos().isEmpty())) {
            throw CONTENT_IS_NULL;
        }
        if (momentVO.getPhotos() != null && momentVO.getPhotos().size() > 9) {
            throw PHOTO_COUNT_OUTOFRANGE;
        }
        log.info("start insert moment...");
        Moment toAdd = new Moment();
        BeanUtils.copyProperties(momentVO, toAdd);
        toAdd.setCreatorId(uid);
        toAdd.setCreateAt(new Date());
        toAdd.setDataStatus(DataStatusType.VALID.KEY);
        toAdd.setStatus(MomentStatusType.UNREVIEWED.getKey());
        // 物业是否开启自动审核
        if (autoAudit) {
            log.info("auto audit moment ... status is " + MomentStatusType.AUTOREVIEWED.getKey());
            toAdd.setStatus(MomentStatusType.AUTOREVIEWED.getKey());
            toAdd.setAuditAt(new Date());
            // default value
            toAdd.setAuditorName("auto-audit");
        }
        toAdd = momentRepository.insert(toAdd);
        UserVO userVO = userFacade.getUserById(ClientType.HOUSEHOLD.value(), partner, toAdd.getCreatorId());
        if (userVO != null) {
            toAdd.setCreatorHeadImg(userVO.getHeadImg());
            toAdd.setCreatorName(userVO.getNickName());
        }
        return toAdd;
    }

    /**
     * 根据ID删除动态
     *
     * @param id
     * @param uid
     * @return
     */
    @Override
    public boolean deleteById(ObjectId id, ObjectId uid) {
        if (id == null) {
            throw MOMENT_ID_IS_NULL;
        }
        Moment toGet = momentRepository.findById(id);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DATA_NOT_EXIST;
        }
        if (!toGet.getCreatorId().equals(uid)) {
            throw CAN_NOT_DELETE;
        }
        Moment toUpdate = new Moment();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate.setUpdateAt(new Date());
        toUpdate = momentRepository.updateById(toUpdate, id);
        return toUpdate != null;
    }

    /**
     * 分页查询动态
     *
     * @param requestVO
     * @param currUserId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Moment> queryPageByMomentRequest(MomentRequestVO requestVO, Integer partner,
                                                 ObjectId currUserId, Integer page, Integer size) {
        if (requestVO.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }
        Date start = requestVO.getCreateStart() == null ? null : DateUtils.getStartTime(requestVO.getCreateStart());
        Date end = requestVO.getCreateEnd() == null ? null : DateUtils.getEndTime(requestVO.getCreateEnd());
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Moment> momentPage =
                momentRepository.findByCommunityIdAndTypeAndStatusAndCreatorIdInAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
                        requestVO.getCommunityId(), requestVO.getType(), requestVO.getStatus(), requestVO.getCreatorId(),
                        start, end, DataStatusType.VALID.KEY, pageable);
        if (momentPage == null || momentPage.getTotalElements() == 0) {
            log.info("queryPageByMomentRequest end return null !!!");
            return new Page<>();
        }

        this.packageMoment(momentPage.getContent(), requestVO.getCreatorId(), currUserId,
                requestVO.getCommunityId(), ClientType.PROPERTY.value(), partner);

        return PageUtils.getPage(momentPage);
    }

    // 根据用户、社区查询点赞过的动态集合
    private Set<ObjectId> findPraiseByCreatorIdAndCommunityId(ObjectId currUserId, ObjectId communityId) {
        if (currUserId == null) {
            log.info("用户ID为空，不需要查询点过赞的动态集合");
            return Collections.emptySet();
        }
        List<Praise> praiseList = praiseRepository.findByCreatorIdAndCommunityIdAndDataStatus(currUserId,
                communityId, DataStatusType.VALID.KEY);
        if (praiseList == null || praiseList.size() == 0) {
            log.info("用户没有为任何动态点过赞");
            return Collections.emptySet();
        }
        return praiseList.stream().map(Praise::getMomentId).collect(Collectors.toSet());
    }

    // 根据用户、社区查询举报过的动态集合
    private Set<ObjectId> findReportByCreatorIdAndCommunityId(ObjectId currUserId, ObjectId communityId) {
        if (currUserId == null) {
            log.info("用户ID为空，不需要查询举报过的动态集合");
            return Collections.emptySet();
        }
        List<Report> reportList = reportRepository.findByCreatorIdAndCommunityIdAndType(currUserId,
                communityId, SpeechType.MOMENT.getKey());
        if (reportList == null || reportList.size() == 0) {
            log.info("用户没有举报过任何动态");
            return Collections.emptySet();
        }
        return reportList.stream().map(Report::getSpeechId).collect(Collectors.toSet());
    }

    /**
     * 更新动态的相关统计数量（评论数，点赞数，举报数）
     *
     * @param momentId
     * @param fieldName
     * @param num
     */
    @Override
    public void updateNumByIdAndFieldName(ObjectId momentId, String fieldName, int num) {
        if (momentId == null) {
            return;
        }
        if (!StringUtil.isNotNull(fieldName)) {
            return;
        }
        if (num == 0) {
            return;
        }
        momentRepository.updateNumByIdAndFieldName(momentId, fieldName, num);
    }

    /**
     * 分页获取被举报过的动态
     *
     * @param requestVO
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Moment> queryPageByRequestVO(RequestVO requestVO, Integer partner, int page, int size) {
        if (requestVO.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Moment> momentPage =
                momentRepository.findByCommunityIdAndStatusAndCreatorIdInAndReportNumGreaterThanEqualAndDataStatusAllIgnoreNull(
                        requestVO.getCommunityId(), requestVO.getStatus(), requestVO.getCreatorId(),
                        requestVO.getReportNum(), DataStatusType.VALID.KEY, pageable);
        if (momentPage == null || momentPage.getTotalElements() == 0) {
            return new Page<>();
        }
        this.packageMoment(momentPage.getContent(), requestVO.getCreatorId(), null,
                requestVO.getCommunityId(), ClientType.PROPERTY.value(), partner);
        return PageUtils.getPage(momentPage);
    }

    /**
     * 审核动态
     *
     * @param id
     * @param status
     * @param auditorId
     * @return
     */
    @Override
    public Moment auditMoment(ObjectId id, Integer status, ObjectId auditorId) {
        if (status == null) {
            throw AUDIT_STATUS_IS_NULL;
        }
        if (id == null) {
            throw MOMENT_ID_IS_NULL;
        }

        Moment toCheck = momentRepository.findById(id);
        if (toCheck == null || toCheck.getDataStatus() == DataStatusType.INVALID.KEY
                || MomentStatusType.UNREVIEWED.getKey() != toCheck.getStatus()) {
            throw DATA_NOT_EXIST;
        }
        Moment toAudit = new Moment();
        toAudit.setStatus(status);
        toAudit.setAuditorId(auditorId);
        toAudit.setAuditAt(new Date());
        toAudit.setUpdateAt(toAudit.getAuditAt());
        return momentRepository.updateById(toAudit, id);
    }

    /**
     * 增量查询社区动态
     *
     * @param incrementalRequest
     * @param communityId
     * @param currUserId
     * @return
     */
    @Override
    public List<Moment> incrementalMomentList(IncrementalRequest incrementalRequest, Integer partner,
                                              ObjectId communityId, ObjectId currUserId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        List<Moment> momentList = momentRepository.findByIncrementalRequestAndCommunityId(
                incrementalRequest, communityId);

        if (momentList == null || momentList.size() == 0) {
            return momentList;
        }

        this.packageMoment(momentList, null, currUserId, communityId, ClientType.HOUSEHOLD.value(), partner);
        return momentList;
    }

    private void packageMoment(List<Moment> momentList, Collection<ObjectId> userIds,
                               ObjectId currUserId, ObjectId communityId, Integer client, Integer partner) {
        // 如果已经有值，说明是通过发布者名称筛选过了
        if (userIds == null || userIds.isEmpty()) {
            userIds = momentList.stream().map(Moment::getCreatorId).collect(Collectors.toSet());
            if (userIds == null || userIds.isEmpty()) {
                log.info("userIds is null, not need to append user info, return");
                return;
            }
        }
        List<UserVO> userVOList = null;
        if (ClientType.HOUSEHOLD.value() == client) {
            userVOList = userFacade.listClientUserByClientAndUserIds(client, partner, new HashSet<>(userIds));
        } else {
            userVOList = userFacade.findByIds(new HashSet<>(userIds));
        }
        if (userVOList == null || userVOList.isEmpty()) {
            log.info("userVOList is null, packageMoment end return momentList without any creator info !!!");
            return;
        }
        Map<ObjectId, UserVO> userVOMap = new HashMap<>();
        userVOList.forEach(userVO -> userVOMap.put(userVO.getId(), userVO));
        // 点赞的动态id集合
        Set<ObjectId> praisedMomentIds = this.findPraiseByCreatorIdAndCommunityId(currUserId, communityId);
        // 举报的动态id集合
        Set<ObjectId> reportedMomentIds = this.findReportByCreatorIdAndCommunityId(currUserId, communityId);
        // 封装动态发布者的个人信息（头像，名字）,是否已点赞，已举报
        for (Moment moment : momentList) {
            moment.setIsPraised(Boolean.FALSE);
            if (praisedMomentIds.contains(moment.getId())) {
                moment.setIsPraised(Boolean.TRUE);
            }
            moment.setIsReported(Boolean.FALSE);
            if (reportedMomentIds.contains(moment.getId())) {
                moment.setIsReported(Boolean.TRUE);
            }
            UserVO userVO = userVOMap.get(moment.getCreatorId());
            if (userVO != null) {
                String creator = null;
                if (client == ClientType.HOUSEHOLD.value()) {
                    creator = userVO.getNickName();
                } else {
                    creator = StringUtil.isNotNull(userVO.getName()) ? userVO.getName() : userVO.getPhone();
                }
                moment.setCreatorName(creator);
                moment.setCreatorHeadImg(userVO.getHeadImg());
            }
        }
    }

    /**
     * 增量查询我自己的动态
     *
     * @param incrementalRequest
     * @param communityId
     * @param currUserId
     * @return
     */
    @Override
    public List<Moment> incrementalMyMomentList(IncrementalRequest incrementalRequest, Integer partner,
                                                ObjectId communityId, ObjectId currUserId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (currUserId == null) {
            throw USER_ID_NULL;
        }
        List<Moment> momentList = momentRepository.findByIncrementalRequestAndCommunityIdAndCreatorId(
                incrementalRequest, communityId, currUserId);

        if (momentList == null || momentList.size() == 0) {
            log.info("incrementalMyMomentList end return null !!!");
            return momentList;
        }

        this.packageMoment(momentList, null, currUserId, communityId, ClientType.HOUSEHOLD.value(), partner);
        return momentList;
    }

    /**
     * 根据ID更新一条动态
     *
     * @param toUpdate
     * @return
     */
    @Override
    public Moment updateOne(Moment toUpdate) {
        return momentRepository.updateOne(toUpdate);
    }

    @Override
    public Long statisticsMoment(ObjectId communityId, ObjectId creatorId) {
        return momentRepository.countByCommunityIdAndCreatorIdAndStatusNotInAndDataStatus(
                communityId, creatorId,
                Arrays.asList(MomentStatusType.AUDOSHIELDING.getKey(), MomentStatusType.HANDSHIELDING.getKey()),
                DataStatusType.VALID.KEY);
    }
}
