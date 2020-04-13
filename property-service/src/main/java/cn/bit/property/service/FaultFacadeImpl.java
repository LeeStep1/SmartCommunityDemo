package cn.bit.property.service;

import cn.bit.facade.enums.*;
import cn.bit.facade.model.property.Fault;
import cn.bit.facade.service.property.FaultFacade;
import cn.bit.facade.vo.property.FaultPageQuery;
import cn.bit.facade.vo.statistics.FaultResponse;
import cn.bit.facade.vo.statistics.Section;
import cn.bit.facade.vo.statistics.StatisticsRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.number.AmountUtil;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.property.dao.FaultRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.property.PropertyBizException.*;

/**
 * Created by fxiao
 * on 2018/3/8
 */
@Service("faultFacade")
@Slf4j
public class FaultFacadeImpl implements FaultFacade {

    @Autowired
    private FaultRepository faultRepository;

    @Autowired
    private EsTemplate esTemplate;

    private static final String INDEX_NAME = "cm_accident";

    private static final String TYPE_NAME = "fault";

    @Override
    public Fault addFault(Fault entity) throws BizException {
        switch (FaultItemType.getEntityByKey(entity.getFaultItem())) {
            case ELEVATOR:
                if (entity.getDeviceId() == null) {
                    throw FAULT_ELEVATOR_ID_NULL;
                }
            case DOORCONTROL:
                if (entity.getDeviceId() == null) {
                    throw FAULT_DOOR_ID_NULL;
                }
            default:
                break;
        }
        entity.setHidden(Boolean.FALSE);
        entity.setCreateAt(new Date());
        entity.setPlayTime(entity.getCreateAt());
        entity.setUpdateAt(entity.getCreateAt());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        entity = faultRepository.insert(entity);
        upsertFault2Es(entity);
        return entity;
    }

    /**
     * 更新故障单
     *
     * @param entity
     * @param userId
     * @return
     */
    @Override
    public Fault updateFault(Fault entity, ObjectId userId) {
        Fault item = faultRepository.findById(entity.getId());
        if (item == null || item.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw FAULT_IS_NULL;
        }
        // 只有申请人才能修改单据
        if (!item.getUserId().equals(userId)) {
            throw FAULT_OWNER_ERROR;
        }
        return editFault(entity);
    }

    /**
     * 处理故障单
     *
     * @param entity
     * @param userVO
     * @return
     */
    @Override
    public Fault auditFault(Fault entity, UserVO userVO) {
        Fault item = faultRepository.findById(entity.getId());
        if (item == null || item.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw FAULT_IS_NULL;
        }
        // 已是该状态，直接返回
        if (item.getFaultStatus().equals(entity.getFaultStatus())) {
            throw FAULT_FLOW_STATUS;
        }

        switch (FaultStatusType.getByValue(entity.getFaultStatus())) {
            // 2：受理单据,回填受理人信息
            case WAITALOCATION:
                if (FaultStatusType.WAITACCEPT.key != item.getFaultStatus()) {
                    throw FAULT_FLOW_CHANGE;
                }
                entity.setAcceptId(userVO.getId());
                entity.setAcceptName(userVO.getName());
                entity.setAcceptTime(new Date());
                entity.setFaultStatus(FaultStatusType.WAITALOCATION.key);

                break;
            // -1：驳回单据，回填驳回人信息
            case REJECT:
                if (FaultStatusType.WAITACCEPT.key != item.getFaultStatus()) {
                    throw FAULT_FLOW_CHANGE;
                }
                entity.setRejectReason(entity.getRejectReason());
                entity.setRejectId(userVO.getId());
                entity.setRejectName(userVO.getName());
                entity.setRejectTime(new Date());
                entity.setFaultStatus(FaultStatusType.REJECT.key);
                break;
            // 0：取消订单
            case CANCEL:
                if (FaultStatusType.WAITACCEPT.key != item.getFaultStatus()) {
                    throw FAULT_FLOW_CHANGE;
                } else {
                    if (!item.getUserId().equals(userVO.getId())) {
                        throw FAULT_OWNER_ERROR;
                    }
                    entity.setFaultStatus(FaultStatusType.CANCEL.key);
                }
                break;
            // 4：已完成
            case FINISH:
                if (FaultStatusType.FINISH.key != item.getFaultStatus()) {
                    entity.setFaultStatus(FaultStatusType.FINISH.key);
                    // 修改可评论状态（0：未评价；1：已评价）
                    entity.setEvaluate(0);
                    // 完成时间
                    entity.setFinishTime(new Date());
                }
                break;
            default:
                throw FAULT_FLOW_INVALID;
        }
        return editFault(entity);
    }

    @Override
    public Fault editFault(Fault entity) throws BizException {
        ObjectId id = entity.getId();
        if (id == null) {
            throw FAULT_ID_NULL;
        }
        entity.setId(null);
        entity.setUpdateAt(new Date());
        entity = faultRepository.updateById(entity, id);
        if (entity.getFaultStatus() == FaultStatusType.WAITALOCATION.key
                || entity.getFaultStatus() == FaultStatusType.WAITRECONDTION.key
                || entity.getFaultStatus() == FaultStatusType.REJECT.key
                || entity.getFaultStatus() == FaultStatusType.FINISH.key) {
            upsertFault2Es(entity);
        } else if (entity.getFaultStatus() == FaultStatusType.CANCEL.key) {
            removeFaultsFromEs(Collections.singleton(id));
        }
        return entity;
    }

    /**
     * 评价故障单
     *
     * @param entity
     * @param uid
     * @return
     */
    @Override
    public Fault faultComment(Fault entity, ObjectId uid) {
        Fault item = faultRepository.findById(entity.getId());
        if (item == null || item.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw FAULT_IS_NULL;
        }
        // 待评价
        if (item.getFaultStatus() != FaultStatusType.FINISH.key) {
            throw FAULT_CANNOT_COMMENT;
        }
        if (item.getEvaluate() == 1) {
            throw FAULT_HAVE_EVALUATION;
        }
        if (!item.getUserId().equals(uid)) {
            throw FAULT_ONLY_COMMENT_ONESELF;
        }
        entity.setEvaluate(1);
        entity.setEvaluationTime(new Date());
        // 如果为空，默认5星
        entity.setEvaluationGrade(entity.getEvaluationGrade() == null ? 5 : entity.getEvaluationGrade());
        entity = faultRepository.updateById(entity, entity.getId());
        // 更新es的评分
        upsertFault2Es(entity);
        return entity;
    }

    /**
     * 故障记录插入es
     *
     * @param fault
     */
    private void upsertFault2Es(Fault fault) {
        cn.bit.facade.data.property.Fault toUpsert = new cn.bit.facade.data.property.Fault();
        toUpsert.setCommunityId(fault.getCommunityId());
        toUpsert.setRepairId(fault.getRepairId());
        toUpsert.setType(fault.getFaultType());
        toUpsert.setStatus(fault.getFaultStatus());
        toUpsert.setScore(fault.getEvaluationGrade());
        toUpsert.setCreateAt(fault.getCreateAt());
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, fault.getId().toString(), toUpsert);
    }

    /**
     * 根据id，移除es记录
     *
     * @param faultIds
     */
    private void removeFaultsFromEs(Collection<ObjectId> faultIds) {
        faultIds.forEach(id -> esTemplate.deleteAsync(INDEX_NAME, TYPE_NAME, id.toString()));
    }

    @Override
    public Fault hiddenById(ObjectId id) throws BizException {
        if (id == null) {
            throw FAULT_ID_NULL;
        }
        Fault entity = faultRepository.findById(id);
        if (entity.getFaultStatus() > 0 && entity.getFaultStatus() < 4) {
            throw FLOW_POURED;
        }
        Fault toUpdate = new Fault();
        toUpdate.setHidden(Boolean.TRUE);
        toUpdate.setUpdateAt(new Date());
        return faultRepository.updateById(toUpdate, id);
    }

    @Override
    public Fault findOne(ObjectId id) throws BizException {
        return faultRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Fault> queryFaultPage(Fault entity, Integer client, int page, int size) throws BizException {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        // 时间查询
        Date startTime = null;
        Date endTime = null;
        if (entity.getPlayTimeBegin() != null) {
            startTime = DateUtils.getStartTime(entity.getPlayTimeBegin());
        }
        if (entity.getPlayTimeEnd() != null) {
            endTime = DateUtils.getEndTime(entity.getPlayTimeEnd());
        }
        if (entity.getPlayTimeBegin() != null && entity.getPlayTimeEnd() != null) {
            if (entity.getPlayTimeEnd().before(entity.getPlayTimeBegin())) {
                throw FAULT_BEGIN_END_TIME;
            }
        }
        if (client == ClientType.HOUSEHOLD.value()
                || client == ClientType.PROPERTY.value() && entity.getUserId() != null) {
            // 住户端需过滤掉已经隐藏的记录
            entity.setHidden(Boolean.TRUE);
        }

        org.springframework.data.domain.Page<Fault> faultPage =
                faultRepository.findByCommunityIdAndUserIdAndUserNameRegexAndRepairIdAndFaultStatusAndPlayTimeGreaterThanEqualAndPlayTimeLessThanAndHiddenNotAndDataStatusAllIgnoreNull(
                        entity.getCommunityId(), entity.getUserId(),
                        StringUtil.makeQueryStringAllRegExp(entity.getUserName()), entity.getRepairId(),
                        entity.getFaultStatus(), startTime, endTime,
                        entity.getHidden(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(faultPage);
    }

    @Override
    public List<Fault> getFaultList(Fault entity) throws BizException {
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return faultRepository.find(entity, XSort.desc("userId", "createAt"));
    }

    @Override
    public Long countFaultByCommunityId(ObjectId communityId) throws BizException {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        return faultRepository.countByCommunityIdAndFaultStatusAndDataStatus(
                communityId, FaultStatusType.FINISH.key, DataStatusType.VALID.KEY);
    }

    @Override
    public Long countFaultByItem(ObjectId communityId, Integer faultItem) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        return faultRepository.countByCommunityIdAndFaultStatusAndFaultItemAndDataStatus(
                communityId, FaultStatusType.FINISH.key, faultItem, DataStatusType.VALID.KEY);
    }

    @Override
    public Long countFaultByEvaluationGrade(ObjectId communityId, Integer star) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        return faultRepository.countByCommunityIdAndFaultStatusAndEvaluationGradeAndDataStatus(
                communityId, FaultStatusType.FINISH.key, star, DataStatusType.VALID.KEY);
    }

    @Override
    public Long countFaultByTime(ObjectId communityId) throws BizException {
        // 获取当月的开始时间
        Date beginTime = DateUtils.getMonthStart(new Date());
        return faultRepository.countByCommunityIdAndFinishTimeGreaterThanEqualAndFinishTimeLessThan(
                communityId, beginTime, new Date());
    }

    /**
     * 获取社区下待处理的故障数量
     *
     * @param communityId
     * @param faultStatus
     * @return
     */
    @Override
    public Map<String, Long> countUnRepairedFault(ObjectId communityId, Integer faultStatus) {
        Long unRepairedNum = faultRepository.countByCommunityIdAndFaultStatusAndDataStatus(
                communityId, faultStatus, DataStatusType.VALID.KEY);
        Map<String, Long> map = new HashMap<>();
        map.put("unRepairedNum", unRepairedNum == null ? 0 : unRepairedNum);
        return map;
    }

    /**
     * 维修工:获取指派给自己的待检修故障数量
     *
     * @param communityId
     * @param repairId
     * @param faultStatus
     * @return
     */
    @Override
    public Map<String, Long> queryFaultCountByCommunityIdAndRepairId(ObjectId communityId,
                                                                     ObjectId repairId, Integer faultStatus) {
        Long count = faultRepository.countByCommunityIdAndFaultStatusAndRepairIdAndDataStatus(
                communityId, faultStatus, repairId, DataStatusType.VALID.KEY);
        Map<String, Long> map = new HashMap<>();
        map.put("toRepairNum", count == null ? 0 : count);
        return map;
    }

    @Override
    public FaultResponse getFaultStatistics(StatisticsRequest statisticsRequest) {
        if (statisticsRequest.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        if (statisticsRequest.getStartAt() == null) {
            statisticsRequest.setStartAt(DateUtils.getFirstDateOfMonth(new Date()));
        }

        if (statisticsRequest.getEndAt() == null) {
            statisticsRequest.setEndAt(DateUtils.getLastDateOfMonth(new Date()));
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery(
                                        "communityId", statisticsRequest.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("status")
                                        .gt(FaultStatusType.WAITACCEPT.key)
                                        .lte(FaultStatusType.FINISH.key))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(statisticsRequest.getStartAt()))
                                        .to(DateUtils.getShortDateStr(statisticsRequest.getEndAt())))))

                .addAggregation(AggregationBuilders.terms("type_group")
                        .field("type")
                        .valueType(ValueType.LONG)
                        .order(Terms.Order.term(true)))
                .addAggregation(AggregationBuilders.filter("finish",
                        QueryBuilders.matchQuery("status", FaultStatusType.FINISH.key))
                        .subAggregation(AggregationBuilders.terms("repair_group")
                                .field("repairId")
                                .size(100)
                                .subAggregation(
                                        AggregationBuilders.filter("score", QueryBuilders.existsQuery("score"))
                                                .subAggregation(AggregationBuilders.avg("avg_score")
                                                        .field("score")
                                                        .format("0.0")))));

        SearchResponse searchResponse = searchRequestBuilder.get();

        FaultResponse faultResponse = new FaultResponse();
        long total = searchResponse.getHits().getTotalHits();
        faultResponse.setTotal(total);

        List<Section> typeSections = new LinkedList<>();
        Terms typeGroup = searchResponse.getAggregations().get("type_group");
        for (int i = 0; i < FaultType.values().length; i++) {
            boolean exists = typeGroup.getBuckets().size() > i;
            Section section = new Section();
            section.setName(FaultType.getValueByKey(i + 1));
            section.setCount(exists ? typeGroup.getBuckets().get(i).getDocCount() : 0L);
            section.setProportion(
                    (exists ? AmountUtil.roundDownStr(section.getCount() * 100.0D / total) : "0.00") + "%");
            typeSections.add(section);
        }
        faultResponse.setTypeSections(typeSections);

        Filter finish = searchResponse.getAggregations().get("finish");
        long finishCount = finish.getDocCount();
        faultResponse.setFinishCount(finishCount);
        List<FaultResponse.Section> avgScoreSections = new LinkedList<>();
        Terms repairGroup = finish.getAggregations().get("repair_group");
        for (Terms.Bucket bucket : repairGroup.getBuckets()) {
            FaultResponse.Section avgScoreSection = new FaultResponse.Section();
            avgScoreSection.setName(bucket.getKeyAsString());
            avgScoreSection.setCount(bucket.getDocCount());
            avgScoreSection.setProportion(
                    AmountUtil.roundDownStr(avgScoreSection.getCount() * 100.0D / finishCount) + "%");
            Filter score = bucket.getAggregations().get("score");
            Avg avgScore = score.getAggregations().get("avg_score");
            avgScoreSection.setAvgScore("\ufffd".equals(avgScore.getValueAsString()) ? null : avgScore.getValueAsString());

            avgScoreSections.add(avgScoreSection);
        }
        faultResponse.setRepairSections(avgScoreSections);

        return faultResponse;
    }

    /**
     * 统计当前故障工单
     *
     * @param request
     * @return
     */
    @Override
    public FaultResponse getFaultStatisticsForBigScreen(StatisticsRequest request) {
        if (request.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        if (request.getStartAt() == null) {
            request.setStartAt(DateUtils.getFirstDateOfMonth(new Date()));
        }

        if (request.getEndAt() == null) {
            request.setEndAt(DateUtils.getLastDateOfMonth(new Date()));
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery().filter(
                                QueryBuilders.matchQuery("communityId", request.getCommunityId().toString()))))
                .addAggregation(AggregationBuilders.terms("type_group")
                        .field("type")
                        .valueType(ValueType.LONG)
                        .order(Terms.Order.term(true)))
                .addAggregation(AggregationBuilders.terms("status_group")
                        .field("status")
                        .valueType(ValueType.LONG)
                        .order(Terms.Order.term(true)))
                .addAggregation(AggregationBuilders.filter("lately", QueryBuilders.rangeQuery("createAt")
                        .from(DateUtils.getShortDateStr(DateUtils.addDay(new Date(), -6))))
                        .subAggregation(AggregationBuilders.dateHistogram("lately_group")
                                .field("createAt")
                                .dateHistogramInterval(DateHistogramInterval.DAY)
                                .format("yyyy-MM-dd")
                                .minDocCount(0)
                                .extendedBounds(new ExtendedBounds(
                                        DateUtils.getShortDateStr(DateUtils.addDay(new Date(), -6)),
                                        DateUtils.getShortDateStr()))
                                .subAggregation(AggregationBuilders.filter("finish", QueryBuilders.boolQuery()
                                        .should(QueryBuilders.matchQuery("status", FaultStatusType.FINISH.key))
                                        .should(QueryBuilders.matchQuery("status", FaultStatusType.REJECT.key)))
                                        .subAggregation(AggregationBuilders.terms("finish_group")
                                                .field("createAt")
                                                .valueType(ValueType.LONG)))));
        SearchResponse searchResponse = searchRequestBuilder.get();
        long total = searchResponse.getHits().getTotalHits();
        FaultResponse faultResponse = new FaultResponse();
        // 故障类型统计
        List<Section> typeSections = new LinkedList<>();
        Terms typeGroup = searchResponse.getAggregations().get("type_group");
        for (int i = 0; i < FaultType.values().length; i++) {
            boolean exists = typeGroup.getBuckets().size() > i;
            Section typeSection = new Section();
            typeSection.setName(FaultType.getValueByKey(i + 1));
            typeSection.setCount(exists ? typeGroup.getBuckets().get(i).getDocCount() : 0L);
            typeSection.setProportion(
                    (exists ? AmountUtil.roundDownStr(typeSection.getCount() * 100.0D / total) : "0.00") + "%");
            typeSections.add(typeSection);
        }
        faultResponse.setTypeSections(typeSections);
        // 故障状态统计
        final String waitAccepted = "未办";
        final String accepted = "办理中";
        final String finished = "办结";
        List<Section> statusSections = new LinkedList<>();
        Terms statusGroup = searchResponse.getAggregations().get("status_group");

        Section waitAcceptedSection = initEmptySection(waitAccepted);
        Section acceptedSection = initEmptySection(accepted);
        Section finishedSection = initEmptySection(finished);
        for (Terms.Bucket bucket : statusGroup.getBuckets()) {
            if (bucket.getKeyAsNumber().intValue() == FaultStatusType.WAITALOCATION.key
                    || bucket.getKeyAsNumber().intValue() == FaultStatusType.WAITRECONDTION.key) {
                acceptedSection.setCount(acceptedSection.getCount() + bucket.getDocCount());
                continue;
            }
            if (bucket.getKeyAsNumber().intValue() == FaultStatusType.FINISH.key
                    || bucket.getKeyAsNumber().intValue() == FaultStatusType.REJECT.key) {
                finishedSection.setCount(finishedSection.getCount() + bucket.getDocCount());
                continue;
            }
            if (bucket.getKeyAsNumber().intValue() == FaultStatusType.WAITACCEPT.key) {
                waitAcceptedSection.setCount(waitAcceptedSection.getCount() + bucket.getDocCount());
                continue;
            }
        }

        waitAcceptedSection.setProportion(AmountUtil.roundDownStr(waitAcceptedSection.getCount() * 100.0D / total) + "%");
        statusSections.add(waitAcceptedSection);
        acceptedSection.setProportion(AmountUtil.roundDownStr(acceptedSection.getCount() * 100.0D / total) + "%");
        statusSections.add(acceptedSection);
        finishedSection.setProportion(AmountUtil.roundDownStr(finishedSection.getCount() * 100.0D / total) + "%");
        statusSections.add(finishedSection);

        faultResponse.setStatusSections(statusSections);

        Filter lately = searchResponse.getAggregations().get("lately");
        List<Section> dailySections = new LinkedList<>();
        Histogram latelyGroup = lately.getAggregations().get("lately_group");
        for (Histogram.Bucket bucket : latelyGroup.getBuckets()) {
            Section dailySection = new Section();
            dailySection.setName(bucket.getKeyAsString());
            dailySection.setCount(bucket.getDocCount());
            Filter finish = bucket.getAggregations().get("finish");
            Long finishCount = finish.getDocCount();
            if (dailySection.getCount() == 0) {
                dailySection.setProportion("0.00%");
            } else {
                dailySection.setProportion(
                        AmountUtil.roundDownStr((finishCount == null ? 0L : finishCount) * 100.0D / dailySection.getCount()) + "%");
            }
            dailySections.add(dailySection);
        }
        faultResponse.setDailySections(dailySections);
        return faultResponse;
    }

    @Override
    public Page<Fault> listFaults(FaultPageQuery query) {
        Pageable pageable = new PageRequest(
                query.getPage() == null ? 0 : query.getPage() - 1, query.getSize() == null ? 10 : query.getSize(),
                new Sort(Sort.Direction.DESC, "createAt"));
        // 时间查询
        Date startTime = null;
        Date endTime = null;
        if (query.getStartAt() != null) {
            startTime = DateUtils.getStartTime(query.getStartAt());
        }
        if (query.getEndAt() != null) {
            endTime = DateUtils.getEndTime(query.getEndAt());
        }
        if (query.getStartAt() != null && query.getEndAt() != null && query.getEndAt().before(query.getStartAt())) {
            throw FAULT_BEGIN_END_TIME;
        }
        org.springframework.data.domain.Page<Fault> faultPage =
                faultRepository.findByCommunityIdAndUserIdAndUserNameRegexAndRepairIdAndFaultStatusAndPlayTimeGreaterThanEqualAndPlayTimeLessThanAndHiddenNotAndDataStatusAllIgnoreNull(
                        query.getCommunityId(), query.getUserId(),
                        StringUtil.makeQueryStringAllRegExp(query.getUserName()),
                        query.getRepairId(), query.getFaultStatus(), startTime, endTime,
                        query.getHidden(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(faultPage);
    }

    /**
     * 初始化空section
     *
     * @param name
     * @return
     */
    private Section initEmptySection(String name) {
        Section section = new Section();
        section.setName(name);
        section.setCount(0L);
        section.setProportion("0.00%");
        return section;
    }
}
