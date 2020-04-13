package cn.bit.communityIoT.service;

import cn.bit.communityIoT.dao.DoorRecordRepository;
import cn.bit.communityIoT.dao.DoorRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.service.communityIoT.DoorRecordFacade;
import cn.bit.facade.vo.communityIoT.door.DoorRecordRequest;
import cn.bit.facade.vo.statistics.DoorRecordResponse;
import cn.bit.facade.vo.statistics.Section;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.number.AmountUtil;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.COMMUNITY_NULL;
import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.STARTAT_AFTER_ENDAT;

@Service("doorRecordFacade")
@Slf4j
public class DoorRecordFacadeImpl implements DoorRecordFacade {

    @Autowired
    private DoorRecordRepository doorRecordRepository;

    @Autowired
    private DoorRepository doorRepository;

    @Autowired
    private EsTemplate esTemplate;

    private static final String INDEX_NAME = "cm_device_record";

    private static final String TYPE_NAME = "door";

    /**
     * 添加设备使用记录
     * @param entity
     * @return
     */
    @Override
    public DoorRecord addDoorRecord(DoorRecord entity) {
        Date now = new Date();
        entity.setCreateAt(now);
        entity.setUpdateAt(now);
        entity.setDataStatus(DataStatusType.VALID.KEY);

        Door checkDoor = matchDoorDeviceWithRecord(entity);
        if (checkDoor == null) {
            log.warn("无效的门禁记录 : {}", entity);
            return null;
        }

        log.info("门禁记录对应的设备信息 : {}", checkDoor.toString());
        entity.setDoorId(checkDoor.getId());
        entity = doorRecordRepository.insert(entity);
        addDoorRecord2Es(entity);
        return entity;
    }

    private Door matchDoorDeviceWithRecord(DoorRecord entity) {
        Door checkDoor = null;
        if (StringUtil.isNotBlank(entity.getDeviceLocalDirectory())) {
            checkDoor = doorRepository.findByCommunityIdAndDeviceCodeAndDataStatus(entity.getCommunityId(),
                    entity.getDeviceLocalDirectory(), DataStatusType.VALID.KEY);
        } else if (entity.getDoorId() != null) {
            checkDoor = doorRepository.findByIdAndDataStatus(entity.getDoorId(), DataStatusType.VALID.KEY);
        }
        return checkDoor;
    }

    /**
     * 根据id删除设备使用记录
     * @param id
     * @return
     */
    @Override
    public DoorRecord deleteDoorRecordById(ObjectId id) {
        DoorRecord entity = new DoorRecord();
        entity.setUpdateAt(new Date());
        entity.setDataStatus(DataStatusType.INVALID.KEY);
        return doorRecordRepository.updateById(entity, id);
    }

    /**
     * 根据id查询设备使用记录详情
     * @param id
     * @return
     */
    @Override
    public DoorRecord findById(ObjectId id) {
        return doorRecordRepository.findById(id);
    }

    /**
     * 分页查询设备使用记录
     * @param entity
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<DoorRecord> queryPage(DoorRecord entity, int page, int size) {
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return doorRecordRepository.findPage(entity, page, size, XSort.desc("time"));
    }

    @Override
    public Page<DoorRecord> getDoorRecords(DoorRecordRequest doorRecordRequest, int page, int size) {
        return doorRecordRepository.findAllDoorRecords(doorRecordRequest, page, size);
    }

    @Override
    public void batchAddDoorRecord(List<DoorRecord> entities) {
        List<DoorRecord> toSave = new ArrayList<>();
        for (DoorRecord entity : entities) {
            Door door = null;
            if (!StringUtil.isEmpty(entity.getMacAddress())) {
                door = doorRepository.findByCommunityIdAndMac(entity.getCommunityId(), entity.getMacAddress());
            } else if (entity.getDoorId() != null) {
                door = doorRepository.findByIdAndDataStatus(entity.getDoorId(), DataStatusType.VALID.KEY);
            }
            // 数据库找不到该门的数据就不上报
            if (door == null) {
                continue;
            }
            putDoorInfoInRecord(entity, door);
            toSave.add(entity);
        }
        doorRecordRepository.insertAll(toSave);
        toSave.forEach(this::addDoorRecord2Es);
    }

    private void putDoorInfoInRecord(DoorRecord entity, Door door) {
        entity.setMacAddress(StringUtils.isEmpty(door.getMac()) ? null : door.getMac());
        entity.setDeviceId(door.getDeviceId() + "");
        entity.setDeviceManufacturer(door.getBrand());
        entity.setDeviceName(door.getName());
        entity.setDoorId(door.getId());
    }

    @Override
    public DoorRecordResponse getDoorRecordStatistics(cn.bit.facade.vo.statistics.DoorRecordRequest doorRecordRequest) {
        if (doorRecordRequest.getCommunityId() == null) {
            throw COMMUNITY_NULL;
        }

        if (doorRecordRequest.getStartAt() == null) {
            doorRecordRequest.setStartAt(DateUtils.getFirstDateOfMonth(new Date()));
        }

        if (doorRecordRequest.getEndAt() == null) {
            doorRecordRequest.setEndAt(DateUtils.getLastDateOfMonth(new Date()));
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery("communityId", doorRecordRequest.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(doorRecordRequest.getStartAt()))
                                        .to(DateUtils.getShortDateStr(doorRecordRequest.getEndAt())))))
                .addAggregation(AggregationBuilders.terms("door")
                        .field("doorId"));

        SearchResponse searchResponse = searchRequestBuilder.get();

        DoorRecordResponse response = new DoorRecordResponse();
        long total = searchResponse.getHits().getTotalHits();

        List<Section> sections = new LinkedList<>();
        Terms door = searchResponse.getAggregations().get("door");
        for (Terms.Bucket bucket : door.getBuckets()) {
            Section section = new Section();
            section.setName(bucket.getKeyAsString());
            section.setCount(bucket.getDocCount());
            section.setProportion(AmountUtil.roundDownStr(section.getCount() * 100.0D / total) + "%");
            sections.add(section);
        }
        response.setDoorSections(sections);

        return response;
    }

    /**
     * 统计时间端内某社区的开门次数
     *
     * @param communityId
     * @param startAt
     * @param endAt
     * @return
     */
    @Override
    public Long countByCommunityIdAndDate(ObjectId communityId, Date startAt, Date endAt) {
        if(startAt == null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR, 2);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startAt = calendar.getTime();
        }
        if(endAt == null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtils.addDay(new Date(), 1));
            calendar.set(Calendar.HOUR, 2);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            endAt = calendar.getTime();
        }
        if(startAt.after(endAt)){
            throw STARTAT_AFTER_ENDAT;
        }
        return doorRecordRepository.countByCommunityIdAndTimeGreaterThanEqualAndTimeLessThanAndDataStatus(
                communityId, startAt, endAt, DataStatusType.VALID.KEY);
    }

    private void addDoorRecord2Es(DoorRecord doorRecord) {
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, doorRecord.getId().toString(), generateDoorRecord(doorRecord));
    }

    private cn.bit.facade.data.communityIoT.DoorRecord generateDoorRecord(DoorRecord doorRecord) {
        cn.bit.facade.data.communityIoT.DoorRecord toAdd = new cn.bit.facade.data.communityIoT.DoorRecord();
        toAdd.setCommunityId(doorRecord.getCommunityId());
        toAdd.setDoorId(doorRecord.getDoorId());
        toAdd.setUseStyle(doorRecord.getUseStyle());
        toAdd.setCreateAt(doorRecord.getCreateAt());
        return toAdd;
    }

}
