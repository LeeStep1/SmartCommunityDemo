package cn.bit.communityIoT.service;

import cn.bit.communityIoT.dao.ElevatorRecordRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.service.communityIoT.ElevatorRecordFacade;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorDetailDTO;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorRecordRequest;
import cn.bit.facade.vo.mq.CreateRecordRequest;
import cn.bit.facade.vo.statistics.ElevatorRecordResponse;
import cn.bit.facade.vo.statistics.Section;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.number.AmountUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.COMMUNITY_NULL;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/8
 **/
@Service("elevatorRecordFacade")
@Slf4j
public class ElevatorRecordFacadeImpl implements ElevatorRecordFacade {

    @Autowired
    private ElevatorRecordRepository elevatorRecordRepository;

    @Autowired
    private EsTemplate esTemplate;

    private static final String INDEX_NAME = "cm_device_record";

    private static final String TYPE_NAME = "elevator";

    /**
     * 添加梯禁记录
     * @param entity
     * @return
     */
    @Override
    public ElevatorRecord addElevatorRecord(ElevatorRecord entity) {
        Date now = new Date();
        entity.setCreateAt(now);
        entity.setUpdateAt(now);
        entity.setDataStatus(DataStatusType.VALID.KEY);
        entity = elevatorRecordRepository.insert(entity);
        addElevatorRecord2Es(entity);
        return entity;
    }

    /**
     * 根据id删除梯禁记录
     * @param id
     * @return
     */
    @Override
    public ElevatorRecord deleteElevatorRecordById(ObjectId id) {
        ElevatorRecord entity = new ElevatorRecord();
        entity.setUpdateAt(new Date());
        entity.setDataStatus(DataStatusType.INVALID.KEY);
        return elevatorRecordRepository.updateById(entity, id);
    }

    /**
     * 根据id查询梯禁记录详情
     * @param id
     * @return
     */
    @Override
    public ElevatorRecord findById(ObjectId id) {
        return elevatorRecordRepository.findById(id);
    }

    /**
     * 分页查询梯禁记录
     * @param entity
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<ElevatorRecord> queryPage(ElevatorRecord entity, int page, int size) {
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return elevatorRecordRepository.findPage(entity, page, size, XSort.desc("time"));
    }

    @Override
    public Page<ElevatorRecord> getElevatorRecords(ElevatorRecordRequest elevatorRecordRequest, int page, int size) {
        return elevatorRecordRepository.findAllElevatorRecords(elevatorRecordRequest, page, size);
    }

    /**
     * 批量添加梯禁使用记录
     * @param entities
     */
    @Override
    public void batchAddElevatorRecord(List<ElevatorRecord> entities) {
        elevatorRecordRepository.insertAll(entities);
        entities.forEach(this::addElevatorRecord2Es);
    }

    private void addElevatorRecord2Es(ElevatorRecord elevatorRecord) {
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, elevatorRecord.getId().toString(), generateDoorRecord(elevatorRecord));
    }

    private cn.bit.facade.data.communityIoT.ElevatorRecord generateDoorRecord(ElevatorRecord elevatorRecord) {
        cn.bit.facade.data.communityIoT.ElevatorRecord toAdd = new cn.bit.facade.data.communityIoT.ElevatorRecord();
        toAdd.setCommunityId(elevatorRecord.getCommunityId());
        toAdd.setElevatorId(new ObjectId(elevatorRecord.getDeviceId()));
        toAdd.setMac(elevatorRecord.getMacAddress());
        toAdd.setUseStyle(elevatorRecord.getUseStyle());
        toAdd.setCreateAt(elevatorRecord.getCreateAt());
        return toAdd;
    }

    @Override
    public ElevatorRecordResponse getElevatorRecordStatistics(cn.bit.facade.vo.statistics.ElevatorRecordRequest elevatorRecordRequest) {
        if (elevatorRecordRequest.getCommunityId() == null) {
            throw COMMUNITY_NULL;
        }

        if (elevatorRecordRequest.getStartAt() == null) {
            elevatorRecordRequest.setStartAt(DateUtils.getFirstDateOfMonth(new Date()));
        }

        if (elevatorRecordRequest.getEndAt() == null) {
            elevatorRecordRequest.setEndAt(DateUtils.getLastDateOfMonth(new Date()));
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery("communityId", elevatorRecordRequest.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(elevatorRecordRequest.getStartAt()))
                                        .to(DateUtils.getShortDateStr(elevatorRecordRequest.getEndAt())))))
                .addAggregation(AggregationBuilders.terms("elevator")
                        .field("mac"));

        SearchResponse searchResponse = searchRequestBuilder.get();

        ElevatorRecordResponse response = new ElevatorRecordResponse();
        long total = searchResponse.getHits().getTotalHits();

        List<Section> sections = new LinkedList<>();
        Terms door = searchResponse.getAggregations().get("elevator");
        for (Terms.Bucket bucket : door.getBuckets()) {
            Section section = new Section();
            section.setName(bucket.getKeyAsString());
            section.setCount(bucket.getDocCount());
            section.setProportion(AmountUtil.roundDownStr(section.getCount() * 100.0D / total) + "%");
            sections.add(section);
        }
        response.setElevatorSections(sections);

        return response;
    }

    @Override
    public void addRecordBy(CreateRecordRequest createRecordRequest,
                            Card card,
                            ElevatorDetailDTO elevatorDetail) {
        ElevatorRecord elevatorRecord = new ElevatorRecord();
        // 组装电梯记录对象
        elevatorRecord.buildBy(elevatorDetail, card, createRecordRequest);

        // 先查询记录是否重复，如果重复不需要上传该记录
        if (elevatorRecordRepository.existsByUniqueCodeAndDataStatus(elevatorRecord.getUniqueCode(),
                                                                     DataStatusType.VALID.KEY)) {
            log.info("已存在该记录 : {}", elevatorRecord);
            return;
        }

        // 保存电梯记录
        elevatorRecord = elevatorRecordRepository.insert(elevatorRecord);
        log.info("已保存该电梯记录 : {}", elevatorRecord);
        // 数据同步到es
        addElevatorRecord2Es(elevatorRecord);
    }
}
