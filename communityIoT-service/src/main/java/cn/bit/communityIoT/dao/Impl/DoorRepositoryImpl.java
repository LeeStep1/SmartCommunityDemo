package cn.bit.communityIoT.dao.Impl;

import cn.bit.communityIoT.dao.DoorRepositoryAdvice;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;
import java.util.List;

public class DoorRepositoryImpl extends AbstractMongoDao<Door, ObjectId> implements DoorRepositoryAdvice, MongoDao<Door, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public List<Door> findByDoorRequest(DoorRequest doorRequest) {
        return find(getQuery4DoorRequest(doorRequest), null);
    }

    @Override
    public Page<Door> findByDoorRequest(DoorRequest doorRequest, int page, int size) {
        Query query = getQuery4DoorRequest(doorRequest);
        return findPage(query, page, size, null);
    }

    @Override
    public Door findAndModify(Door door) {
        Query query = new Query(Criteria.where("deviceCode").is(door.getDeviceCode()));

        Update update = new Update();
        if(door.getDoorStatus() != null) {
            update.set("doorStatus", door.getDoorStatus());
        }
        if(door.getOnlineStatus() != null){
            update.set("onlineStatus", door.getOnlineStatus());
        }
        if(door.getDataStatus() != null) {
            update.set("dataStatus", door.getDataStatus());
        }
        if(door.getAlarmStatus() != null){
            update.set("alarmStatus", door.getAlarmStatus());
        }
        // 仅初装状态
        if(door.getName() != null){
            update.set("name", door.getName());
            update.set("createAt", door.getCreateAt());
        }

        update.set("communityId", door.getCommunityId());
        /*update.set("name", door.getName());*/
        update.set("buildingId", door.getBuildingId());
        update.set("rank", 0);
        update.set("doorType", door.getDoorType());
        update.set("deviceId", door.getDeviceId());
        update.set("serviceId", door.getServiceId());
        update.set("brand", door.getBrand());
        update.set("brandNo", door.getBrandNo());
        /*update.set("createAt", door.getCreateAt());*/
        update.set("updateAt", door.getUpdateAt());
        FindAndModifyOptions options = new FindAndModifyOptions();

        options.upsert(true);
        options.returnNew(true);
        return mongoTemplate.findAndModify(query, update, options, Door.class);
    }

    private Query getQuery4DoorRequest(DoorRequest doorRequest) {
        Date after = doorRequest.getAfter();
        doorRequest.setAfter(null);
        Query query = buildExample(doorRequest);
        if (after != null)
            query.addCriteria(Criteria.where("updateAt").gt(after));
        return query;
    }

}
