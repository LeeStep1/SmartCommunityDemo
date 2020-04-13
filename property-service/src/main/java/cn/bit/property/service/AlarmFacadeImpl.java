package cn.bit.property.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.ReceiveStatusType;
import cn.bit.facade.model.property.Alarm;
import cn.bit.facade.service.property.AlarmFacade;
import cn.bit.facade.vo.statistics.StatisticsRequest;
import cn.bit.facade.vo.statistics.AlarmResponse;
import cn.bit.facade.vo.statistics.Section;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.number.AmountUtil;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.property.dao.AlarmRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.property.PropertyBizException.*;

@Component("alarmFacade")
@Slf4j
public class AlarmFacadeImpl implements AlarmFacade {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private EsTemplate esTemplate;

    private static final String INDEX_NAME = "cm_accident";

    private static final String TYPE_NAME = "alarm";

    @Override
    public Alarm addRecord(Alarm alarm) throws BizException {
        // 设置为待处理
        alarm.setReceiveStatus(ReceiveStatusType.UNCHECKED.key);
        Date now = new Date();
        alarm.setCallTime(now);
        alarm.setCreateAt(now);
        alarm.setCreatorId(alarm.getCallerId());
        alarm.setUpdateAt(now);
        alarm.setDataStatus(DataStatusType.VALID.KEY);
        alarmRepository.insert(alarm);

        upsertAlarm2Es(alarm);
        return alarm;
    }

    @Override
    public List<Alarm> getAllAlarmRecord() throws BizException {
        return alarmRepository.findAllByDataStatusOrderByCallTimeDescReceiveStatusAsc(DataStatusType.VALID.KEY);
    }

    @Override
    public Alarm getAlarmRecordById(ObjectId id) throws BizException {
        return alarmRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Alarm receiveAlarm(Alarm alarm) throws BizException {
        Alarm toGet = alarmRepository.findByIdAndDataStatus(alarm.getId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw ALARM_NULL;
        }
        if (toGet.getReceiveStatus() == ReceiveStatusType.RECEIVED.key
                || toGet.getReceiveStatus() == ReceiveStatusType.CHECKED.key) {
            throw ALARM_HANDLED;
        }
        Date now = new Date();
        alarm.setReceiveTime(now);
        alarm.setReceiveStatus(ReceiveStatusType.RECEIVED.key);
        alarm.setUpdateAt(now);
        alarm.setModifierId(alarm.getReceiverId());

        alarm = alarmRepository.updateById(alarm, toGet.getId());
        upsertAlarm2Es(alarm);
        return alarm;
    }

    @Override
    public Alarm troubleShoot(Alarm alarm) throws BizException {
        Alarm toGet = alarmRepository.findByIdAndDataStatus(alarm.getId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw ALARM_NULL;
        }
        if (toGet.getReceiveStatus() == ReceiveStatusType.UNCHECKED.key) {
            throw NOT_RECEIVE_ALARM;
        }
        if (toGet.getReceiveStatus() == ReceiveStatusType.CHECKED.key) {
            throw ALARM_FINISH;
        }
        alarm.setReceiveStatus(ReceiveStatusType.CHECKED.key);
        alarm.setUpdateAt(new Date());
        alarm.setModifierId(toGet.getReceiverId());
        // 设置为已排查
        alarm = alarmRepository.updateById(alarm, toGet.getId());
        upsertAlarm2Es(alarm);
        return alarm;
    }

    @Override
    public Page<Alarm> getAlarmRecord(Alarm entity, int page, int size) throws BizException {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(
                Arrays.asList(
                        new Sort.Order(Sort.Direction.ASC,"receiveStatus"),
                        new Sort.Order(Sort.Direction.DESC,"callTime"))));
        org.springframework.data.domain.Page<Alarm> resultPage =
                alarmRepository.findByCommunityIdAndBuildingIdIgnoreNullAndReceiveStatusIgnoreNullAndCallerNameRegexIgnoreNullAndDataStatus(
                        entity.getCommunityId(), entity.getBuildingId(), entity.getReceiveStatus(),
                        StringUtil.makeQueryStringAllRegExp(entity.getCallerName()), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public Page<Alarm> getProprietorAlarm(ObjectId callerId, int page, int size) throws BizException {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(
                Arrays.asList(
                        new Sort.Order(Sort.Direction.ASC,"receiveStatus"),
                        new Sort.Order(Sort.Direction.DESC,"callTime"))));
        org.springframework.data.domain.Page<Alarm> resultPage =
                alarmRepository.findByCallerIdAndDataStatus(callerId, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    /**
     * 保安按照communityId查找当前社区的待接警数量
     * @param communityId
     * @param receiveStatus
     * @return
     */
    @Override
    public Map<String, Long> findReceiveAlarmNum(ObjectId communityId, Integer receiveStatus) {
        Long unCheckedNum = alarmRepository.countByCommunityIdAndReceiveStatusAndDataStatus(
                communityId, receiveStatus, DataStatusType.VALID.KEY);
        Map<String, Long> map = new HashMap<>();
        map.put("unCheckedNum", unCheckedNum);
        return map;
    }

    @Override
    public AlarmResponse getAlarmStatistics(StatisticsRequest statisticsRequest) {
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
                                .filter(QueryBuilders.matchQuery("communityId", statisticsRequest.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(statisticsRequest.getStartAt()))
                                        .to(DateUtils.getShortDateStr(statisticsRequest.getEndAt())))))
                .addAggregation(AggregationBuilders.filter("receive_alarm",
                        QueryBuilders.existsQuery("receiverId"))
                        .subAggregation(AggregationBuilders.terms("security_group")
                                .field("receiverId")
                                .size(100)))
                .addAggregation(AggregationBuilders.histogram("hour_group")
                        .field("hour")
                        .valueType(ValueType.LONG)
                        .interval(1)
                        .minDocCount(0)
                        .extendedBounds(0L, 23L));

        SearchResponse searchResponse = searchRequestBuilder.get();

        AlarmResponse alarmResponse = new AlarmResponse();
        long total = searchResponse.getHits().getTotalHits();
        alarmResponse.setTotal(total);

        Filter receiveAlarm = searchResponse.getAggregations().get("receive_alarm");
        long dealCount = receiveAlarm.getDocCount();
//        alarmResponse.setDealCount(dealCount);
        Terms securityGroup = receiveAlarm.getAggregations().get("security_group");
        List<Section> securitySections = new LinkedList<>();
        for (Terms.Bucket bucket : securityGroup.getBuckets()) {
            Section section = new Section();
            section.setName(bucket.getKeyAsString());
            section.setCount(bucket.getDocCount());
            section.setProportion(AmountUtil.roundDownStr(section.getCount() * 100.0D / dealCount) + "%");
            securitySections.add(section);
        }
        alarmResponse.setSecuritySections(securitySections);

        List<Section> hourSections = new LinkedList<>();
        Histogram hourGroup = searchResponse.getAggregations().get("hour_group");
        for (Histogram.Bucket bucket : hourGroup.getBuckets()) {
            Section section = new Section();
            section.setName(((Double) bucket.getKey()).intValue() + "");
            section.setCount(bucket.getDocCount());
            section.setProportion(AmountUtil.roundDownStr(section.getCount() * 100.0D / total) + "%");
            hourSections.add(section);
        }
        alarmResponse.setHourSections(hourSections);

        return alarmResponse;
    }

    private void upsertAlarm2Es(Alarm alarm) {
        cn.bit.facade.data.property.Alarm alarmToUpsert = generateAlarm(alarm);
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, alarm.getId().toString(), alarmToUpsert);
    }

    private cn.bit.facade.data.property.Alarm generateAlarm(Alarm alarm) {
        cn.bit.facade.data.property.Alarm genAlarm = new cn.bit.facade.data.property.Alarm();
        genAlarm.setCommunityId(alarm.getCommunityId());
        genAlarm.setReceiverId(alarm.getReceiverId());
        genAlarm.setCreateAt(alarm.getCreateAt());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(alarm.getCreateAt());
        genAlarm.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        genAlarm.setCreateAt(alarm.getCreateAt());
        return genAlarm;
    }
}
