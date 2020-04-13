package cn.bit.communityIoT.dao.Impl;

import cn.bit.communityIoT.dao.DoorRecordRepositoryAdvice;
import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.vo.communityIoT.door.DoorRecordRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.framework.utils.DateUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;


public class DoorRecordRepositoryImpl extends AbstractMongoDao<DoorRecord, ObjectId> implements MongoDao<DoorRecord, ObjectId>, DoorRecordRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Page<DoorRecord> findAllDoorRecords(DoorRecordRequest doorRecordRequest, int page, int size) {
        Query query = getQueryDoorRecordRequest(doorRecordRequest);
        return findPage(query, page, size, XSort.desc("time"));
    }

    private Query getQueryDoorRecordRequest(DoorRecordRequest doorRecordRequest) {
        Date now = new Date();
        Date startDate = doorRecordRequest.getStartDate();
        Date endDate = doorRecordRequest.getEndDate();
        if (startDate != null && endDate == null) {
            endDate = DateUtils.getLastDateOfMonth(now);
        }else if (startDate == null && endDate != null) {
            startDate = DateUtils.getFirstDateOfMonth(now);
        }
        doorRecordRequest.setStartDate(null);
        doorRecordRequest.setEndDate(null);
        Query query = buildExample(doorRecordRequest);
        if(startDate != null && endDate != null){
            query.addCriteria(Criteria.where("time").gte(startDate).lte(DateUtils.getEndTime(endDate)));
        }
        return query;
    }
}
