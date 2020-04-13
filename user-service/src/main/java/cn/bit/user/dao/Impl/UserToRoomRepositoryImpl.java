package cn.bit.user.dao.Impl;

import cn.bit.facade.enums.AuditStatusType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.user.dao.UserToRoomRepositoryAdvice;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.Date;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;

@Slf4j
public class UserToRoomRepositoryImpl extends AbstractMongoDao<UserToRoom, ObjectId>
        implements UserToRoomRepositoryAdvice, MongoDao<UserToRoom, ObjectId>
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public int updateMultiByUserId(UserToRoom userToRoom, ObjectId userId) {
        return updateMultiBy(userToRoom, userId, DataStatusType.VALID.KEY);
    }

    @Override
    public int updateMiliUIdById(UserToRoom userToRoom, Long newMiliUId) {
        Query query = buildExample(userToRoom);
        userToRoom.setMiliUId(newMiliUId);
        userToRoom.setUpdateAt(new Date());
        return updateByExample(query, userToRoom);
    }

    @Override
    public Page<UserToRoom> findPageByUserToRoom(UserToRoom userToRoom, Integer page, Integer size) {
        if(userToRoom.getCommunityId() == null){
            throw COMMUNITY_ID_NULL;
        }
        UserToRoom toQuery = new UserToRoom();
        toQuery.setCommunityId(userToRoom.getCommunityId());
        toQuery.setRoomId(userToRoom.getRoomId());
        toQuery.setBuildingId(userToRoom.getBuildingId());
        toQuery.setRelationship(userToRoom.getRelationship());
//        toQuery.setName(userToRoom.getName());
        toQuery.setPhone(userToRoom.getPhone());
        toQuery.setContractPhone(userToRoom.getContractPhone());
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        Query query = buildExample(toQuery);
        if(StringUtil.isNotBlank(userToRoom.getName())){
            query.addCriteria(Criteria.where("name").regex(userToRoom.getName()));
        }
        if(StringUtil.isNotBlank(userToRoom.getRoomLocation())){
            query.addCriteria(Criteria.where("roomLocation").regex(userToRoom.getRoomLocation()));
        }
        if(userToRoom.getAuditStatus() != null){
            if(userToRoom.getAuditStatus() == AuditStatusType.CANCELLED.getType()){//已注销
                query.addCriteria(Criteria.where("auditStatus").in(
                        Arrays.asList(AuditStatusType.CANCELLED.getType(), AuditStatusType.RELEASED.getType())));
            }else{
                query.addCriteria(Criteria.where("auditStatus").is(userToRoom.getAuditStatus()));
            }
        }else{
            query.addCriteria(Criteria.where("auditStatus").ne(AuditStatusType.UNREVIEWED.getType()));
        }
        log.info("findPageByUserToRoom query:" + query);
        return findPage(query, page, size, XSort.desc("createAt"));
    }
}
