package cn.bit.communityIoT.dao.Impl;

import cn.bit.communityIoT.dao.ElevatorRecordRepositoryAdvice;
import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorRecordRequest;
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

public class ElevatorRecordRepositoryImpl extends AbstractMongoDao<ElevatorRecord, ObjectId> implements MongoDao<ElevatorRecord, ObjectId>, ElevatorRecordRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Page<ElevatorRecord> findAllElevatorRecords(ElevatorRecordRequest elevatorRecordRequest, int page, int size) {
        Query query = getQueryElevatorRecordRequest(elevatorRecordRequest);
        return findPage(query, page, size, XSort.desc("time"));
    }

    private Query getQueryElevatorRecordRequest(ElevatorRecordRequest elevatorRecordRequest) {
        Date startDate = elevatorRecordRequest.getStartDate();
        Date endDate = elevatorRecordRequest.getEndDate();
        Date now = new Date();
        if (startDate != null && endDate == null) {
            endDate = DateUtils.getLastDateOfMonth(now);
        }else if (startDate == null && endDate != null) {
            startDate = DateUtils.getFirstDateOfMonth(now);
        }
        elevatorRecordRequest.setStartDate(null);
        elevatorRecordRequest.setEndDate(null);
        Query query = buildExample(elevatorRecordRequest);
        if(startDate != null && endDate != null){
            query.addCriteria(Criteria.where("time").gte(startDate).lte(DateUtils.getEndTime(endDate)));
        }
        return query;
    }
}
