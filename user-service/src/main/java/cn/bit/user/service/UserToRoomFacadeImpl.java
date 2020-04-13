package cn.bit.user.service;

import cn.bit.common.facade.user.dto.ProfileDTO;
import cn.bit.common.facade.user.model.User;
import cn.bit.facade.enums.*;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.CommunityUser;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DeviceAuthVO;
import cn.bit.facade.vo.user.PrintUserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.redis.lock.RedisLock;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.IdentityCardUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.user.dao.CardRepository;
import cn.bit.user.dao.CommunityUserRepository;
import cn.bit.user.dao.UserToRoomRepository;
import cn.bit.user.support.CardGenerator;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.constant.mq.TagConstant.ADD;
import static cn.bit.facade.constant.mq.TagConstant.DELETE;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_DOOR_AUTH;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH;
import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;
import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.community.CommunityBizException.ROOMID_NULL;
import static cn.bit.facade.exception.user.UserBizException.*;

@Service("userToRoomFacade")
@Slf4j
public class UserToRoomFacadeImpl implements UserToRoomFacade {

    @Resource
    private cn.bit.common.facade.user.service.UserFacade commonUserFacade;

    @Resource
    private CommunityUserRepository communityUserRepository;

    @Resource
    private UserToRoomRepository userToRoomRepository;

    @Autowired
    private CardRepository cardRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private CardGenerator cardGenerator;

    @Autowired
    private DefaultMQProducer producer;

    @Override
    public UserToRoom addOwner(UserToRoom entity) throws BizException {
        User user = commonUserFacade.getUserByUserId(entity.getUserId());
        appendUserInfo(entity, user);
        //业主信息
        entity.setProprietorId(user.getId());

        // 审核信息,默认是审核中（20180517）
        entity.setAuditStatus(AuditStatusType.REVIEWING.getType());
        entity.setRemark("业主提交房屋认证申请，等待物业审核");
        // 用户关系
        entity.setRelationship(RelationshipType.OWNER.KEY);

        entity.setCanApply(Boolean.TRUE);
        entity.setClosed(Boolean.FALSE);
        entity.setCreateId(entity.getUserId());
        entity.setCreateAt(new Date());
        entity.setUpdateAt(entity.getCreateAt());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return userToRoomRepository.insert(entity);
    }

    @Override
    public boolean editUserToRoom(UserToRoom entity) throws BizException {
        entity.setUpdateAt(new Date());
        return userToRoomRepository.updateMultiByUserId(entity, entity.getUserId()) > 0;
    }

    @Override
    public UserToRoom addAuxiliary(UserToRoom toSave) throws BizException {
        if(toSave.getRoomId() == null){
            throw ROOMID_NULL;
        }
        // 不能申请业主
        if (toSave.getRelationship() == null || toSave.getRelationship() == RelationshipType.OWNER.KEY) {
            throw IDENTITY_INVALID;
        }

        boolean existBean = existApplication(toSave.getUserId(), toSave.getRoomId());
        if (existBean) {
            throw APPLY_EXIST;
        }

        // 获取房屋业主认证信息
        UserToRoom proprietorToRoom = userToRoomRepository.findByRoomIdAndRelationshipAndAuditStatusAndDataStatus(
                toSave.getRoomId(), RelationshipType.OWNER.KEY, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        // 房屋未认证业主,未开放申请
        if (proprietorToRoom == null || (proprietorToRoom.getCanApply() != null && !proprietorToRoom.getCanApply())) {
            throw CAN_NOT_APPLY;
        }
        // 获取用户信息（姓名、身份证、电话、昵称）
        User auxiliary = commonUserFacade.getUserByUserId(toSave.getUserId());
        appendUserInfo(toSave, auxiliary);

        toSave.setProprietorId(proprietorToRoom.getUserId());
        toSave.setBuildingId(proprietorToRoom.getBuildingId());
        toSave.setRoomName(proprietorToRoom.getRoomName());
        toSave.setCommunityId(proprietorToRoom.getCommunityId());
        toSave.setRoomLocation(proprietorToRoom.getRoomLocation());
        toSave.setArea(proprietorToRoom.getArea());
        toSave.setCreateId(toSave.getUserId());
        toSave.setCreateAt(new Date());
        toSave.setUpdateAt(toSave.getCreateAt());
        // 初始申请状态
        toSave.setAuditStatus(AuditStatusType.UNREVIEWED.getType());
        toSave.setRemark("等待业主审核");
        toSave.setClosed(Boolean.FALSE);
        // 数据状态
        toSave.setDataStatus(DataStatusType.VALID.KEY);
        //房屋位置编码
        toSave.setLocationCode(proprietorToRoom.getLocationCode());
        // 保存数据
        return userToRoomRepository.insert(toSave);
    }

    /**
     * 校验并填充用户的基本信息
     * @param entity
     * @param user
     */
    private void appendUserInfo(UserToRoom entity, User user){
        // 为兼容旧版app，从实名信息中获取名字 2018/12/10
        if(StringUtil.isBlank(entity.getName())){
            entity.setName(user.getName());
            if(StringUtil.isBlank(entity.getName())){
                throw USER_INFO_INCOMPLETE;
            }
        }
        String identityCard = entity.getIdentityCard();
        if(StringUtil.isBlank(identityCard)){
            identityCard = user.getIdentityCard();
            entity.setIdentityCard(identityCard);
        }

        // 通过身份证获取性别出生日期
        IdentityCardUtils.IdentityCardMeta identityCardMeta = null;
        if (StringUtil.isNotBlank(identityCard)
                && (identityCardMeta = IdentityCardUtils.getIdentityCardMeta(identityCard)) == null) {
            throw IDENTITY_CARD_ILLEGAL;
        }
        if (identityCardMeta != null) {
            entity.setBirthday(identityCardMeta.getBirthday());
            entity.setSex(identityCardMeta.getSex());
        }
        entity.setId(null);
        entity.setPhone(user.getPhone());
        entity.setCreateId(user.getId());
    }

    @Override
    public boolean existOwner(ObjectId roomId) throws BizException {
        if (roomId == null) {
            throw ROOMID_NULL;
        }
        // 获取房屋认证的信息
        UserToRoom userToRoom = userToRoomRepository.findByRoomIdAndRelationshipAndAuditStatusAndDataStatus(roomId,
                RelationshipType.OWNER.KEY, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        return userToRoom != null;
    }

    @Override
    public boolean existApplication(ObjectId userId, ObjectId roomId) throws BizException {
        UserToRoom toGet = userToRoomRepository.findByUserIdAndRoomIdAndAuditStatusInAndDataStatus(
                userId,
                roomId,
                Arrays.asList(AuditStatusType.REVIEWED.getType(), AuditStatusType.UNREVIEWED.getType(), AuditStatusType.REVIEWING.getType()),
                DataStatusType.VALID.KEY);
        return toGet != null;
    }

    @Override
    public boolean isCheckIn(ObjectId userId, ObjectId roomId) throws BizException {
        UserToRoom toGet = userToRoomRepository.findByUserIdAndRoomIdAndAuditStatusInAndDataStatus(
                userId,
                roomId,
                Collections.singletonList(AuditStatusType.REVIEWED.getType()),
                DataStatusType.VALID.KEY);
        return toGet != null;
    }

    @Override
    public List<UserToRoom> findByUserIdAndRooms(ObjectId userId, Collection<ObjectId> roomId) throws BizException {
        return userToRoomRepository.findByUserIdAndRoomIdInAndDataStatusAndAuditStatus(userId, roomId,
                DataStatusType.VALID.KEY, AuditStatusType.REVIEWED.getType());
    }

    @Override
    public UserToRoom findById(ObjectId id) throws BizException {
        if (id == null){
            throw USER_TO_ROOM_ID_NULL;
        }
        UserToRoom toGet = userToRoomRepository.findOne(id);
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }
        Set<ObjectId> userIds = new HashSet<>();
        userIds.add(toGet.getUserId());
        if(toGet.getAuditorId() != null){
            userIds.add(toGet.getAuditorId());
            // 已注销的需要查询注销人姓名
            if(toGet.getUpdateBy() != null && AuditStatusType.CANCELLED.getType() == toGet.getAuditStatus()){
                userIds.add(toGet.getUpdateBy());
            }
        }
        List<User> userList = commonUserFacade.listUsersByUserIds(userIds);
        if(userList == null || userList.size() == 0){
            return null;
        }
        Map<ObjectId, User> userMap = new HashMap<>();
        userList.stream().filter(user -> user != null).forEach(user -> userMap.put(user.getId(), user));
        User user = userMap.get(toGet.getUserId());
        User auditor = userMap.get(toGet.getAuditorId());
        if(user == null){
            return null;
        }
        toGet.setHeadImg(user.getAvatar());
        if(auditor != null){
            toGet.setAuditor(auditor.getName());
        }
        if(toGet.getUpdateBy() != null && AuditStatusType.CANCELLED.getType() == toGet.getAuditStatus()){
            User updater = userMap.get(toGet.getUpdateBy());
            if(updater != null){
                toGet.setUpdater(updater.getName());
            }
        }
        if(toGet.getAuditStatus() == AuditStatusType.REVIEWED.getType() && StringUtil.isBlank(toGet.getAuditor())){
            toGet.setAuditor("系统");
        }
        return toGet;
    }

    @Override
    public UserToRoom findOwnerByRoomId(ObjectId roomId) throws BizException {
        return userToRoomRepository.findByRoomIdAndRelationshipAndAuditStatusAndDataStatus(
                roomId, RelationshipType.OWNER.KEY, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    @Override
    public UserToRoom auditAuxiliary(Integer partner, ObjectId id, ObjectId currUserId, Integer auditStatus, boolean Level2Audit)
            throws BizException{

        if(id == null){
            throw USER_TO_ROOM_ID_NULL;
        }
        UserToRoom target = userToRoomRepository.findById(id);
        // 检查当前数据是否有效
        if (target == null || target.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DATA_INVALID;
        }
        if (target.getAuditStatus() != AuditStatusType.UNREVIEWED.getType()) {
            throw APPLICATION_AUDITED;
        }
        // 检查当前用户是否为该房间的业主
        if (!currUserId.equals(target.getProprietorId())) {
            throw PROPRIETOR_MISMATCH;
        }

        UserToRoom owner = userToRoomRepository.findByRoomIdAndRelationshipAndProprietorIdAndDataStatus(
                target.getRoomId(), RelationshipType.OWNER.KEY, currUserId, DataStatusType.VALID.KEY);
        // 检查房间状态是否正常
        if (owner == null) {
            throw ROOM_STATUS_EXCEPTION;
        }
        // 更新当前用户房间关系id的审核状态
        UserToRoom userToRoom = new UserToRoom();
        // 如果是开启二级审核,审核通过就更改为 “审核中” 状态，其它状态不改变
        if (Level2Audit && auditStatus == AuditStatusType.REVIEWED.getType()) {
            auditStatus = AuditStatusType.REVIEWING.getType();
        }
        if(auditStatus == AuditStatusType.REVIEWED.getType()){//没有开放管理员审核则通过审核时，需要检测/设定常住房屋
            //校验是否已有常住房屋
            Boolean inCommonUse = userToRoomRepository.existsByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
                    target.getCommunityId(), target.getUserId(), AuditStatusType.REVIEWED.getType(), Boolean.TRUE, DataStatusType.VALID.KEY);
            userToRoom.setInCommonUse(inCommonUse ? Boolean.FALSE : inCommonUse);
        }
        userToRoom.setAuditStatus(auditStatus);
        userToRoom.setId(id);
        userToRoom.setAuditorId(currUserId);
        userToRoom.setRemark("被业主审核通过");
        if(auditStatus == AuditStatusType.REJECT.getType()){
            userToRoom.setRemark("被业主驳回");
        }
        userToRoom.setAuditTime(new Date());
        userToRoom.setUpdateBy(currUserId);
        userToRoom.setUpdateAt(new Date());
        userToRoom = userToRoomRepository.updateOne(userToRoom);
		// 审核通过则更新其他关联信息
        auditReviewedHandle(partner, userToRoom);

        return userToRoom;
    }

    @Override
    public UserToRoom approvalOwner(Integer partner, ObjectId id, ObjectId auditorId, Integer auditStatus) throws BizException {
        // 驳回
        if(auditStatus == AuditStatusType.REJECT.getType()){
            return this.rejectOwner(id, auditorId, auditStatus);
        }
        // 通过
        if(auditStatus == AuditStatusType.REVIEWED.getType()){
            return this.auditOwner(partner, id, auditorId, auditStatus);
        }
        log.info("业主认证审核状态异常。。。");
        return null;
    }

    /**
     * 审核通过业主认证
     * @param id
     * @param auditorId
     * @param auditStatus
     * @return
     */
    private UserToRoom auditOwner(Integer partner, ObjectId id, ObjectId auditorId, Integer auditStatus) {
        // 获取当前信息
        UserToRoom item = userToRoomRepository.findById(id);
        // 校验数据合法性
        if (item == null || item.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DATA_INVALID;
        }

        if (item.getAuditStatus() != AuditStatusType.REVIEWING.getType()) {
            throw APPLICATION_AUDITED;
        }

        // 防止并发，true则表示可以修改数据
        RedisLock lock = RedisTemplateUtil.getRedisLock(id.toString(), null, null);
        try {
            if (!lock.lock()) {
                throw DATA_LOCKED;
            }
            // 判断是否是业主
            if (item.getRelationship() == RelationshipType.OWNER.KEY) {
                // 验证当前房间是否已认证其他业主
                UserToRoom toCheck = userToRoomRepository.findByRoomIdAndRelationshipAndAuditStatusAndDataStatus(
                        item.getRoomId(), RelationshipType.OWNER.KEY, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
                if (toCheck != null) {
                    throw PROPRIETOR_EXIST;
                }
            }

            // 校验是否已有常住房屋
            Boolean inCommonUse = userToRoomRepository.existsByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
                    item.getCommunityId(), item.getUserId(), AuditStatusType.REVIEWED.getType(),
                    Boolean.TRUE, DataStatusType.VALID.KEY);
            // 更新当前用户房间关系id的审核状态
            UserToRoom userToRoom = new UserToRoom();
            userToRoom.setAuditStatus(auditStatus);
            userToRoom.setUpdateAt(new Date());
            userToRoom.setUpdateBy(auditorId);
            // 审核人信息
            userToRoom.setAuditorId(auditorId);
            userToRoom.setAuditTime(userToRoom.getUpdateAt());
            userToRoom.setRemark("被管理员审核通过");

            userToRoom.setInCommonUse(inCommonUse == null ? Boolean.TRUE : !inCommonUse);
            userToRoom = userToRoomRepository.updateById(userToRoom, id);

            // 审核通过则更新其他关联信息
            auditReviewedHandle(partner, userToRoom);
            return userToRoom;
        } catch (InterruptedException e) {
            log.error("InterruptedException:", e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * 驳回业主认证
     * @param id
     * @param auditorId
     * @param auditStatus
     * @return
     */
    private UserToRoom rejectOwner(ObjectId id, ObjectId auditorId, Integer auditStatus) {
        // 获取当前信息
        UserToRoom item = userToRoomRepository.findById(id);
        // 校验数据合法性
        if (item == null || item.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DATA_INVALID;
        }
        if (item.getAuditStatus() != AuditStatusType.REVIEWING.getType()) {
            throw APPLICATION_AUDITED;
        }
        RedisLock lock = RedisTemplateUtil.getRedisLock(id.toString(), null, null);
        try {
            if (!lock.lock()){
                throw DATA_LOCKED;
            }
            UserToRoom userToRoom = new UserToRoom();
            userToRoom.setAuditStatus(auditStatus);
            userToRoom.setUpdateAt(new Date());
            userToRoom.setUpdateBy(auditorId);
            // 审核人信息
            userToRoom.setAuditorId(auditorId);
            userToRoom.setAuditTime(userToRoom.getUpdateAt());
            userToRoom.setRemark("被管理员驳回");
            userToRoom = userToRoomRepository.updateById(userToRoom, id);
            return userToRoom;
        } catch (InterruptedException e) {
            log.error("InterruptedException:", e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public int relieveOwner(Integer partner, ObjectId roomId, ObjectId operator) throws BizException {
        if (roomId == null) {
            throw ROOMID_NULL;
        }

        List<UserToRoom> userToRooms = userToRoomRepository
                .findByRoomIdAndAuditStatusAndDataStatusOrderByRelationshipAsc(
                        roomId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        UserToRoom owner = userToRooms.stream()
                .filter(userToRoom -> RelationshipType.OWNER.KEY.equals(userToRoom.getRelationship()))
                .findFirst()
                .orElse(null);
        // 业主已被注销
        if(owner == null){
            log.info("业主不存在或已被注销");
            return 0;
        }
        //注销房间下的所有成员
        UserToRoom userToRoom = new UserToRoom();
        userToRoom.setUpdateBy(operator);
        userToRoom.setUpdateAt(new Date());
        userToRoom.setRemark("被管理员注销");
        userToRoom.setAuditStatus(AuditStatusType.CANCELLED.getType());
        int result = userToRoomRepository.updateByCommunityIdAndRoomIdAndAuditStatusInAndDataStatus(
                userToRoom, owner.getCommunityId(), roomId,
                Collections.singleton(AuditStatusType.REVIEWED.getType()), DataStatusType.VALID.KEY);
        log.info(roomId + "_房屋注销成功，注销用户数：" + result);
        if(result <= 0){
            throw DATA_INVALID;
        }

        // 清理该房屋下的所有未处理的申请记录
        UserToRoom record = new UserToRoom();
        record.setUpdateAt(new Date());
        record.setRemark("房屋已注销，此记录被清除");
        record.setAuditStatus(AuditStatusType.REJECT.getType());
        int unReviewedRecord = userToRoomRepository.updateByCommunityIdAndRoomIdAndAuditStatusInAndDataStatus(
                record, owner.getCommunityId(), roomId,
                Arrays.asList(AuditStatusType.UNREVIEWED.getType(), AuditStatusType.REVIEWING.getType()),
                DataStatusType.VALID.KEY);
        log.info(roomId + "_房屋注销成功，清理未处理完成的申请记录条数：" + unReviewedRecord);
        // 删除系统用户中社区ID标签
        List<ObjectId> userIds = userToRooms.stream().map(UserToRoom::getUserId).collect(Collectors.toList());
        removeRoleAndClientFromCommunityUser(owner.getCommunityId(), userIds);
        removeCommunityIdAndRoleFromClientUserForHouseHolder(partner, owner.getCommunityId(), userIds);

        List<UserToRoom> notOwner = userToRooms.stream()
                .filter(u -> RelationshipType.OWNER.KEY.equals(u.getRelationship()))
                .collect(Collectors.toList());
        // 先解绑家人和租客
        notOwner.forEach(this::sendRelieveMessage2Device);
        // 再解绑业主设备
        this.sendRelieveMessage2Device(owner);
        return result;
    }

    @Override
    public Page<UserToRoom> queryByCommunityId(ObjectId communityId, String buildingId,
                                               Integer relationship, Integer auditStatus,
                                               String contractPhone,String name,
                                               int page, int size) throws BizException {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }

        UserToRoom userToRoom = new UserToRoom();
        userToRoom.setCommunityId(communityId);
        userToRoom.setDataStatus(DataStatusType.VALID.KEY);
        if(relationship != null){
            userToRoom.setRelationship(relationship);
        }
        if (buildingId != null) {
            userToRoom.setBuildingId(new ObjectId(buildingId));
        }
        if(auditStatus != null){
            userToRoom.setAuditStatus(auditStatus);
        }
        if(StringUtil.isNotNull(contractPhone)){
            userToRoom.setContractPhone(contractPhone);
        }
        if(StringUtil.isNotNull(name)){
            userToRoom.setName(name);
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC,"createAt"));
        org.springframework.data.domain.Page<UserToRoom> pageList = userToRoomRepository
                .findByCommunityIdAndDataStatusAndRelationshipIgnoreNullAndBuildingIdIgnoreNullAndAuditStatusIgnoreNullAndContractPhoneIgnoreNullAndNameIgnoreNull(
                communityId, DataStatusType.VALID.KEY, relationship, buildingId==null?null:new ObjectId(buildingId), auditStatus, contractPhone, name, pageable);
        Page<UserToRoom> resultPage = PageUtils.getPage(pageList);
        this.packageHeadImg(resultPage.getRecords());
        return resultPage;
    }

    private void packageHeadImg(List<UserToRoom> userToRoomList) {
        if(userToRoomList == null || userToRoomList.size() == 0){
            return;
        }
        Set<ObjectId> userIds = new HashSet<>();
        for(UserToRoom userToRoom : userToRoomList){
            userIds.add(userToRoom.getUserId());
            if(userToRoom.getAuditorId() != null){
                userIds.add(userToRoom.getAuditorId());
            }
        }
        List<User> userList = commonUserFacade.listUsersByUserIds(userIds);
        Map<ObjectId, User> userMap = new HashMap<>();
        for(User user : userList){
            if(user == null){
                continue;
            }
            userMap.put(user.getId(), user);
        }
        for(UserToRoom userToRoom : userToRoomList){
            User user = userMap.get(userToRoom.getUserId());
            User auditor = userMap.get(userToRoom.getAuditorId());
            if(user != null){
                userToRoom.setHeadImg(user.getAvatar());
            }
            if(auditor != null){
                userToRoom.setAuditor(auditor.getName());
            }
            if(userToRoom.getAuditStatus() == AuditStatusType.REVIEWED.getType() && auditor == null){
                userToRoom.setAuditor("系统");
            }
        }
    }

    @Override
    public Page<UserToRoom> queryByRoomId(ObjectId roomId, Integer auditStatus, ObjectId userId,
                                          Integer client, Integer page, Integer size) throws BizException {
        if (roomId == null) {
            throw ROOMID_NULL;
        }
        Boolean closed = null;
        ObjectId proprietorId = null;
        if(client == ClientType.HOUSEHOLD.value()){
            closed = Boolean.FALSE;
            proprietorId = userId;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<UserToRoom> userToRooms = userToRoomRepository
                .findByRoomIdAndAuditStatusIgnoreNullAndProprietorIdIgnoreNullAndClosedIgnoreNullAndDataStatus(
                roomId, auditStatus, proprietorId, closed, DataStatusType.VALID.KEY, pageable);
        Page<UserToRoom> pageList = PageUtils.getPage(userToRooms);
        this.packageHeadImg(pageList.getRecords());
        return pageList;
    }

    @Override
    public Set<ObjectId> getCommunityIdsByUserId(ObjectId userId) {
        if (userId == null) {
            throw USER_ID_NULL;
        }
        List<UserToRoom> u2cList = userToRoomRepository.findByUserIdAndAuditStatusAndDataStatus(
                userId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        return u2cList.stream().map(UserToRoom::getCommunityId).collect(Collectors.toSet());
    }

    @Override
    public Set<ObjectId> getBuildingsByUserId(ObjectId communityId, ObjectId userId) {
        if (userId == null) {
            log.error("USER_ID_NULL:", USER_ID_NULL);
            throw USER_ID_NULL;
        }
        if (communityId == null) {
            log.error("COMMUNITY_ID_NULL:", COMMUNITY_ID_NULL);
            throw COMMUNITY_ID_NULL;
        }

        List<UserToRoom> u2rList = userToRoomRepository.findByCommunityIdAndUserIdInAndAuditStatusAndDataStatus(
                communityId, Collections.singleton(userId), AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        return u2rList.stream().map(UserToRoom::getBuildingId).collect(Collectors.toSet());
    }

    @Override
    public List<UserToRoom> getRoomsByUserId(UserToRoom userToRoom) {
        if(userToRoom.getCommunityId() == null){
            throw COMMUNITY_ID_NULL;
        }
        userToRoom.setDataStatus(DataStatusType.VALID.KEY);
        List<UserToRoom> list = userToRoomRepository.find(userToRoom, XSort.desc("createAt"));
        this.packageHeadImg(list);
        return list;
    }

    @Override
    public boolean updatePhoneByUserId(ObjectId userId, String phone) {
        UserToRoom userToRoom = new UserToRoom();
        userToRoom.setPhone(phone);
        return userToRoomRepository.updateMultiByUserId(userToRoom, userId) > 0;
    }

    @Override
    public boolean disableAuxiliaryApply(ObjectId id, ObjectId userId, Boolean canApply) throws BizException{
        // 用户房间关联ID不能为空
        if (id == null) {
            throw USER_TO_ROOM_ID_NULL;
        }

        // 用户ID不能为空
        if (userId == null) {
            throw USER_ID_NULL;
        }

        // 获取关联表
        UserToRoom entity = userToRoomRepository.findById(id);
        if (entity == null || AuditStatusType.REVIEWED.getType() != entity.getAuditStatus()) {
            throw DATA_INVALID;
        }
        // 判断该房间的业主是否等于userId
        if (!userId.equals(entity.getProprietorId())) {
            log.info("此房屋认证业主与当前登录用户不一致");
            throw DATA_INVALID;
        }

        // 设置房间是否开放给家属或者租客申请，默认false
        UserToRoom toUpdate = new UserToRoom();
        if(canApply == null){
            canApply = false;
        }
        toUpdate.setId(entity.getId());
        toUpdate.setCanApply(canApply);
        toUpdate.setUpdateAt(new Date());
        return userToRoomRepository.updateOne(toUpdate) != null;
    }

    @Override
    public UserToRoom deleteAuxiliary(Integer partner, ObjectId id, ObjectId operator, Integer client) throws BizException {
        if (id == null) {
            throw USER_TO_ROOM_ID_NULL;
        }

        // 获取申请记录
        UserToRoom entity = userToRoomRepository.findById(id);
        if (entity == null || entity.getDataStatus() == DataStatusType.INVALID.KEY
                || entity.getAuditStatus() != AuditStatusType.REVIEWED.getType()) {
            throw DATA_INVALID;
        }

        // 判断当前用户是否为业主
        if(!operator.equals(entity.getProprietorId()) && ClientType.HOUSEHOLD.value() == client){
            throw AUTHENCATION_FAILD;
        }

        // 解绑用户
        UserToRoom toUpdate = new UserToRoom();
        toUpdate.setUpdateBy(operator);
        String remark = "";
        if(client == ClientType.HOUSEHOLD.value()){
            remark = "被业主注销";
        }else {
            remark = "被管理员注销";
        }
        remark += RelationshipType.getValueByKey(entity.getRelationship()) + "身份";
        toUpdate.setRemark(remark);
        toUpdate.setUpdateAt(new Date());
        toUpdate.setAuditStatus(AuditStatusType.CANCELLED.getType());//注销

        UserToRoom userToRoom = userToRoomRepository.updateById(toUpdate, id);

        if (userToRoom == null || userToRoom.getAuditStatus() != AuditStatusType.CANCELLED.getType()) {
            throw DATA_INVALID;
        }
        // 删除系统用户中社区ID标签
        removeRoleAndClientFromCommunityUser(
                userToRoom.getCommunityId(), Collections.singletonList(userToRoom.getUserId()));
        removeCommunityIdAndRoleFromClientUserForHouseHolder(partner,
                userToRoom.getCommunityId(), Collections.singletonList(userToRoom.getUserId()));
        sendRelieveMessage2Device(userToRoom);
        return userToRoom;
    }

    @Override
    public boolean hiddenUserToRoomApplyById(ObjectId id, ObjectId userId, Boolean closed) throws BizException {
        // 获取关联表
        UserToRoom entity = userToRoomRepository.findById(id);
        // 判断该房间的使用者是否是当前用户
        if (entity == null || entity.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DATA_INVALID;
        }
        if(!userId.equals(entity.getUserId())){
            throw AUTHENCATION_FAILD;
        }
        if(!Arrays.asList(AuditStatusType.CANCELLED.getType(), AuditStatusType.REJECT.getType(),
                AuditStatusType.RELEASED.getType()).contains(entity.getAuditStatus())){
            log.info("当前记录不能被关闭");
            return false;
        }
        UserToRoom toUpdate = new UserToRoom();
        toUpdate.setClosed(closed);
        toUpdate.setUpdateBy(userId);
        toUpdate.setUpdateAt(new Date());
        UserToRoom userToRoom = userToRoomRepository.updateById(toUpdate, id);
        return userToRoom != null;
    }

    @Override
    public Page<UserToRoom> queryByBuildingId(ObjectId buildingId, Integer relationship, Integer auditStatus,
                                              Integer client, int page, int size) throws BizException {
        if (buildingId == null) {
            throw BUILDING_ID_NULL;
        }

        Boolean notClosed = null;
        if(client == ClientType.HOUSEHOLD.value()){
            notClosed = Boolean.TRUE;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC,"createAt"));
        org.springframework.data.domain.Page<UserToRoom> pageList =
                userToRoomRepository.findByBuildingIdAndRelationshipAndAuditStatusAndClosedNotAndDataStatusAllIgnoreNull(
                buildingId, relationship, auditStatus, notClosed, DataStatusType.VALID.KEY, pageable);
        Page<UserToRoom> resultPage = PageUtils.getPage(pageList);
        this.packageHeadImg(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public PrintUserVO getContractInfoById(ObjectId id) throws BizException {
        if(id == null){
            throw USER_TO_ROOM_ID_NULL;
        }
        UserToRoom userToRoom = userToRoomRepository.findById(id);
        if(userToRoom == null || userToRoom.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }

        PrintUserVO printUserVO = new PrintUserVO();
        printUserVO.setUserToRoom(userToRoom);

        List<UserToRoom> userToRoomList = userToRoomRepository.findByRoomIdAndAuditStatusAndDataStatusOrderByRelationshipAsc(
                userToRoom.getRoomId(), AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        printUserVO.setInhabitantList(userToRoomList);
        return printUserVO;
    }

    @Override
    public List<Object> proprietorsStatistics(ObjectId communityId) throws BizException {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("communityId").is(communityId)
                        .and("dataStatus").is(DataStatusType.VALID.KEY)
                        .and("relationship").is(RelationshipType.OWNER.KEY)
                        .and("auditStatus").is(AuditStatusType.REVIEWED.getType())
                ),
                Aggregation.lookup("CM_BUILDING","buildingId","_id","buildingEntity"),
                Aggregation.group("communityId", "buildingEntity").count().as("total"));
        List<Object> list = mongoTemplate.aggregate(agg, "U_USER_TO_ROOM", Object.class).getMappedResults();
        return list;
    }

    @Override
    public Map<String, Long> countByCommunityIdAndAuditStatus(ObjectId communityId, Integer auditStatus)
            throws BizException {

        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        long unReviewedNum = userToRoomRepository.countByCommunityIdAndDataStatusAndAuditStatus(
                communityId, DataStatusType.VALID.KEY, auditStatus);
        log.info("未审核的用户数量 unReviewedNum:" + unReviewedNum);
        Map map = new HashMap();
        map.put("unReviewedNum", unReviewedNum);
        return map;
    }

    @Override
    public Map<String, Long> countUnReviewedProprietorsByCommunityId(ObjectId communityId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        long unReviewedNum = userToRoomRepository.countByCommunityIdAndDataStatusAndAuditStatus(
                communityId, DataStatusType.VALID.KEY, AuditStatusType.REVIEWING.getType());
        log.info("待审核的用户数量 unReviewedNum:" + unReviewedNum);
        Map map = new HashMap();
        map.put("unReviewedNum", unReviewedNum);
        return map;
    }

    /**
     * 根据社区统计已审核的用户数量
     *
     * @param communityId
     * @return
     */
    @Override
    public Map<String, Long> countReviewedProprietorsByCommunityId(ObjectId communityId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        long reviewedNum = userToRoomRepository.countByCommunityIdAndDataStatusAndAuditStatus(
                communityId, DataStatusType.VALID.KEY, AuditStatusType.REVIEWED.getType());
        log.info("已审核的用户数量 reviewedNum:" + reviewedNum);
        Map map = new HashMap();
        map.put("reviewedNum", reviewedNum);
        return map;
    }

    @Override
    public boolean updateMiliUIdById(UserToRoom userToRoom, Long newMiliUId) {
        return userToRoomRepository.updateMiliUIdById(userToRoom, newMiliUId) > 0;
    }

    @Override
    public List<UserToRoom> findByBuildingIdAndUserId(ObjectId buildingId, ObjectId userId) {
        return userToRoomRepository.findByBuildingIdAndUserIdAndAuditStatusAndDataStatus(
                buildingId, userId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    @Override
    public String getIdentityCardMetaBirthday(UserToRoom userToRoom) {
        IdentityCardUtils.IdentityCardMeta identityCardMeta = null;
        if (StringUtil.isNotBlank(userToRoom.getIdentityCard())
                && (identityCardMeta = IdentityCardUtils.getIdentityCardMeta(userToRoom.getIdentityCard())) == null) {
            throw IDENTITY_CARD_ILLEGAL;
        }
        return identityCardMeta.getBirthday();
    }

    @Override
    public List<UserToRoom> findByRoomId(ObjectId roomId) {
        return userToRoomRepository.findByRoomIdAndAuditStatusAndDataStatusOrderByRelationshipAsc(
                roomId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    @Override
    public List<UserToRoom> countUserInfo(ObjectId communityId) {
        return userToRoomRepository.findByCommunityIdAndAuditStatusAndDataStatus(
                communityId, AuditStatusType.REVIEWED.getType(),DataStatusType.VALID.KEY);
    }

    @Override
    public int countUserByTime(ObjectId communityId, Date beginDate, Date endDate) {
        List<UserToRoom> list = userToRoomRepository.findByCommunityIdAndAuditStatusAndDataStatusAndCreateAtGreaterThanEqualAndCreateAtLessThan(
                communityId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY, beginDate, endDate);
        Set<String> identity = list.stream().map(UserToRoom::getIdentityCard).collect(Collectors.toSet());
        return identity.size();
    }

    @Override
    public Page<UserToRoom> findValidProprietorsByCommunityIdIn(Collection<ObjectId> communityIds, int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size);
        org.springframework.data.domain.Page<UserToRoom> userToRoomPage = userToRoomRepository
                .findByCommunityIdInAndRelationshipAndAuditStatusAndDataStatus(communityIds, RelationshipType.OWNER.KEY,
                        AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(userToRoomPage);
    }

    @Override
    public List<UserToRoom> getProprietorsByRoomIds(Collection<ObjectId> roomIds) {
        if (CollectionUtils.isEmpty(roomIds)) {
            return Collections.emptyList();
        }

        return userToRoomRepository.findByRoomIdInAndRelationshipAndAuditStatusAndDataStatus(roomIds,
                RelationshipType.OWNER.KEY, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    /**
     * 分页查询用户认证列表
     *
     * @param userToRoom
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<UserToRoom> queryPageByCommunityId(UserToRoom userToRoom, Integer page, Integer size) {
        Page<UserToRoom> pageList = userToRoomRepository.findPageByUserToRoom(userToRoom, page, size);
        this.packageHeadImg(pageList.getRecords());
        return pageList;
    }

    /**
     * 物业二级审核非业主的认证
     *
     * @param id
     * @param operatorId
     * @param auditStatus
     * @return
     */
    @Override
    public UserToRoom auditAuxiliaryByProperty(Integer partner, ObjectId id, ObjectId operatorId, Integer auditStatus) {
        UserToRoom toGet = userToRoomRepository.findById(id);
        //检查当前数据是否有效
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY
                || toGet.getAuditStatus() != AuditStatusType.REVIEWING.getType()){
            throw DATA_INVALID;
        }

        if(AuditStatusType.REVIEWED.getType() == auditStatus){//审核通过之前需要验证房间状态
            UserToRoom owner = userToRoomRepository.findByRoomIdAndRelationshipAndProprietorIdAndDataStatus(
                    toGet.getRoomId(), RelationshipType.OWNER.KEY, toGet.getProprietorId(), DataStatusType.VALID.KEY);
            // 检查房间状态是否正常
            if (owner == null) {
                throw ROOM_STATUS_EXCEPTION;
            }
        }

        //校验是否已有常住房屋
        Boolean inCommonUse = userToRoomRepository.existsByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
                toGet.getCommunityId(), toGet.getUserId(), AuditStatusType.REVIEWED.getType(), Boolean.TRUE, DataStatusType.VALID.KEY);

        // 更新当前用户房间关系id的审核状态
        UserToRoom userToRoom = new UserToRoom();
        userToRoom.setAuditStatus(auditStatus);
        userToRoom.setAuditorId(operatorId);
        userToRoom.setRemark("被管理员审核通过");
        if(AuditStatusType.REJECT.getType() == auditStatus){
            userToRoom.setRemark("被管理员驳回");
        }
        userToRoom.setAuditTime(new Date());
        userToRoom.setUpdateBy(operatorId);
        userToRoom.setUpdateAt(new Date());

        userToRoom.setInCommonUse(inCommonUse == null ? Boolean.TRUE : !inCommonUse);
        userToRoom = userToRoomRepository.updateById(userToRoom, id);
        if(userToRoom == null){
            throw DATA_INVALID;
        }
        //审核通过则更新其他关联信息
        auditReviewedHandle(partner, userToRoom);
        return userToRoom;
    }

    /**
     * 根据房间查询已被注销的用户认证列表
     *
     * @param roomId
     * @return
     */
    @Override
    public List<UserToRoom> findCancelledListByRoomId(ObjectId roomId) {
        return userToRoomRepository.findByRoomIdAndAuditStatusAndDataStatusOrderByRelationshipAsc(
                roomId, AuditStatusType.CANCELLED.getType(), DataStatusType.VALID.KEY);
    }

    /**
     * 根据社区ID分页获取待审核的用户认证
     *
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<UserToRoom> queryUnReviewPageByCommunityId(ObjectId communityId, Integer page, Integer size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC,"createAt"));
        org.springframework.data.domain.Page<UserToRoom> pageList =
                userToRoomRepository.findByCommunityIdAndDataStatusAndAuditStatus(
                communityId, DataStatusType.VALID.KEY, AuditStatusType.REVIEWING.getType(), pageable);
        Page<UserToRoom> resultPage = PageUtils.getPage(pageList);
        this.packageHeadImg(resultPage.getRecords());
        return resultPage;
    }

    /**
     * 业主获取房间的待审核列表
     *
     * @param roomId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<UserToRoom> queryNonProprietorUnReviewPageForRoom(ObjectId roomId, Integer page, Integer size) {
        if (roomId == null) {
            throw ROOMID_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC,"createAt"));
        org.springframework.data.domain.Page<UserToRoom> pageList =
                userToRoomRepository.findPageByRoomIdAndDataStatusAndRelationshipInAndAuditStatus(
                roomId,
                DataStatusType.VALID.KEY,
                Arrays.asList(RelationshipType.RELATION.KEY, RelationshipType.TENANT.KEY),
                AuditStatusType.UNREVIEWED.getType(),
                pageable);
        Page<UserToRoom> resultPage = PageUtils.getPage(pageList);
        this.packageHeadImg(resultPage.getRecords());
        return resultPage;
    }

    /**
     * 根据楼栋获取已认证用户列表
     *
     * @param buildingId
     * @return
     */
    @Override
    public List<UserToRoom> queryListByBuildingId(ObjectId buildingId) {
        if(buildingId == null){
            throw BUILDING_ID_NULL;
        }
        List<UserToRoom> list =
                userToRoomRepository.findByBuildingIdAndAuditStatusAndDataStatusOrderByRoomIdAscRelationshipAsc(
                buildingId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        this.packageHeadImg(list);
        return list;
    }

    /**
     * 根据楼栋ID查询用户已认证列表
     *
     * @param buildingIds
     * @return
     */
    @Override
    public List<UserToRoom> findByBuildingIdsIn(Collection<ObjectId> buildingIds) {
        if(buildingIds == null || buildingIds.isEmpty()){
            log.info("findByBuildingIdsIn：楼栋id集合为空，return null");
            return new ArrayList<UserToRoom>();
        }
        return userToRoomRepository.findByBuildingIdInAndAuditStatusAndDataStatus(
                buildingIds, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    /**
     * 根据房间查询认证通过的住户
     * @param roomId
     * @return
     */
    @Override
    public List<UserToRoom> findReviewedListByRoomId(ObjectId roomId) {
        return userToRoomRepository.findByRoomIdAndAuditStatusAndDataStatusOrderByRelationshipAsc(
                roomId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    @Override
    public void applyUserToMili(ObjectId id) {
        UserToRoom userToRoom = this.findById(id);
        if (userToRoom.getAuditStatus() == AuditStatusType.REVIEWED.getType()
                && userToRoom.getMiliUId() == null) {
            this.sendAuditMessage2Device(userToRoom);
        }
    }

    /**
     * 获取用户第一个认证的房间
     *
     * @param communityId
     * @param userId
     * @return
     */
    @Override
    public UserToRoom findTop1ByCommunityIdAndUserId(ObjectId communityId, ObjectId userId) {
        return userToRoomRepository.findTop1ByCommunityIdAndUserIdAndAuditStatusAndDataStatusOrderByAuditTimeAsc(
                communityId, userId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    /**
     * 设置常住房屋
     *
     * @param communityId
     * @param userId
     * @param userToRoomId
     * @return
     */
    @Override
    public UserToRoom editInCommonUse(ObjectId communityId, ObjectId userId, ObjectId userToRoomId) {
        //根据id查询住房信息，并校验communityId, userId
        UserToRoom toGet = userToRoomRepository.findByIdAndCommunityIdAndUserIdAndAuditStatusAndDataStatus(
                userToRoomId, communityId, userId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        if(toGet == null){
            log.info("住房信息不存在，或者不属于这个社区，或者不属于当前用户");
			throw DATA_INVALID;
        }
        //检查当前常住房屋id是否与要设置的一致
    	UserToRoom toCheck = userToRoomRepository.findByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
                communityId, userId, AuditStatusType.REVIEWED.getType(), Boolean.TRUE, DataStatusType.VALID.KEY);
        if(toCheck != null && toCheck.getId().equals(userToRoomId)){
            throw ALREADY_IN_COMMON_USE;
        }
        UserToRoom toUpdate = new UserToRoom();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setUpdateBy(userId);
        if(toCheck != null){
            toUpdate.setInCommonUse(Boolean.FALSE);
            UserToRoom result = userToRoomRepository.updateById(toUpdate, toCheck.getId());
            if(result == null){
                log.info("设置常用住房失败");
                throw OPERATION_FAILURE;
            }
        }

	    toUpdate.setInCommonUse(Boolean.TRUE);
        return userToRoomRepository.updateById(toUpdate, userToRoomId);
    }

    /**
     * 查询用户住房
     *
     * @param communityId
     * @param userId
     * @param roomId
     * @return
     */
    @Override
    public UserToRoom findByCommunityIdAndUserIdAndRoomId(ObjectId communityId, ObjectId userId, ObjectId roomId) {
        return userToRoomRepository.findByCommunityIdAndRoomIdAndUserIdAndAuditStatusAndDataStatus(
                communityId, roomId, userId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    /**
     * 查找常用住房
     *
     * @param communityId
     * @param userId
     * @return
     */
    @Override
    public UserToRoom findInCommonUseByCommunityIdAndUserId(ObjectId communityId, ObjectId userId) {
        return userToRoomRepository.findByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
                communityId, userId, AuditStatusType.REVIEWED.getType(), Boolean.TRUE, DataStatusType.VALID.KEY);
    }

    /**
     * 更新
     *
     * @param toUpdate
     * @param id
     * @return
     */
    @Override
    public UserToRoom updateById(UserToRoom toUpdate, ObjectId id) {
        return userToRoomRepository.updateById(toUpdate, id);
    }

    /**
     * 根据communityId及userId获取已拥有的房屋列表
     *
     * @param communityId
     * @param userId
     * @return
     */
    @Override
    public List<UserToRoom> findByCommunityIdAndUserId(ObjectId communityId, ObjectId userId) {
        return userToRoomRepository.findByCommunityIdAndUserIdAndAuditStatusAndDataStatus(
                communityId, userId, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
    }

    /**
     * upsert 业主认证记录
     *
     * @param userToRoom
     * @return
     */
    @Override
    public UserToRoom upsertAuthOwnerRecord(Integer partner, UserToRoom userToRoom) {
        if(userToRoom == null){
            return null;
        }
        userToRoom.setUpdateAt(new Date());
        userToRoom.setAuditTime(userToRoom.getUpdateAt());
        userToRoom.setAuditStatus(AuditStatusType.REVIEWED.getType());
        userToRoom.setRemark("存在住户档案，自动匹配业主手机号认证通过");
        userToRoom.setCanApply(Boolean.TRUE);
        userToRoom.setClosed(Boolean.FALSE);

        if(userToRoom.getId() == null){
            userToRoom.setRelationship(RelationshipType.OWNER.KEY);
            userToRoom.setCreateAt(userToRoom.getUpdateAt());
            userToRoom.setDataStatus(DataStatusType.VALID.KEY);
            userToRoom = userToRoomRepository.insert(userToRoom);
        }else{
            userToRoom = userToRoomRepository.updateById(userToRoom, userToRoom.getId());
        }
        // 认证通过后设备授权
        auditReviewedHandle(partner, userToRoom);
        return userToRoom;
    }

    /**
     * 查询房间有效的业主申请记录（审核中/审核通过）
     *
     * @param roomId
     * @return
     */
    @Override
    public List<UserToRoom> getOwnerApplyRecordsByRoomId(ObjectId roomId) {
        return userToRoomRepository.findByRoomIdAndRelationshipAndAuditStatusInAndDataStatus(
                roomId,
                RelationshipType.OWNER.KEY,
                Arrays.asList(AuditStatusType.REVIEWING.getType(), AuditStatusType.REVIEWED.getType()),
                DataStatusType.VALID.KEY);
    }

    /**
     * 根据房间、用户查询审核中的业主申请记录
     *
     * @param roomId
     * @param userId
     * @return
     */
    @Override
    public UserToRoom findOwnerReviewingRecordByRoomIdAndUserId(ObjectId roomId, ObjectId userId) {
        return userToRoomRepository.findByRoomIdAndUserIdAndRelationshipAndAuditStatusAndDataStatus(
            roomId, userId, RelationshipType.OWNER.KEY, AuditStatusType.REVIEWING.getType(), DataStatusType.VALID.KEY);
    }

    private void addCommunityUser(UserToRoom userToRoom) {
        CommunityUser communityUser = new CommunityUser();
        communityUser.setUserId(userToRoom.getUserId());
        communityUser.setCommunityId(userToRoom.getCommunityId());
        communityUser.setClients(new HashSet<>());
        communityUser.getClients().add(ClientType.HOUSEHOLD.value());
        communityUser.setRoles(new HashSet<>());
        communityUser.getRoles().add(RoleType.HOUSEHOLD.name());
        communityUser.setCreateAt(userToRoom.getCreateAt());
        communityUser.setUpdateAt(communityUser.getCreateAt());
        communityUser.setDataStatus(DataStatusType.VALID.KEY);
        communityUserRepository.upsertWithAddToSetClientsAndRolesByCommunityIdAndUserIdAndDataStatus(
                communityUser, userToRoom.getCommunityId(), userToRoom.getUserId(), DataStatusType.VALID.KEY);
    }

    private void removeRoleAndClientFromCommunityUser(ObjectId communityId, Collection<ObjectId> userIds) {
        if (communityId == null || userIds == null || userIds.isEmpty()) {
            return;
        }

        Set<ObjectId> userIdsToUpdate = new HashSet<>(userIds);
        List<UserToRoom> userToRoomList = userToRoomRepository.findByCommunityIdAndUserIdInAndAuditStatusAndDataStatus(
                communityId,userIds, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        userToRoomList.forEach(userToRoom -> userIdsToUpdate.remove(userToRoom.getUserId()));

        if (userIdsToUpdate.isEmpty()) {
            return;
        }

        CommunityUser communityUser = new CommunityUser();
        communityUser.setRoles(new HashSet<>(Collections.singleton(RoleType.HOUSEHOLD.name())));
        communityUser.setClients(new HashSet<>(Collections.singleton(ClientType.HOUSEHOLD.value())));
        communityUserRepository.pullAllByCommunityIdAndUserId(communityUser, communityId, userIdsToUpdate);
    }

    private void addCommunityIdAndRoleToClientUser(Integer partner, UserToRoom userToRoom) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setClient(ClientType.HOUSEHOLD.value());
        profileDTO.setPartner(partner);
        profileDTO.setUserId(userToRoom.getUserId());
        profileDTO.setTags(Collections.singleton(userToRoom.getCommunityId()));
        profileDTO.setRoles(Collections.singleton(RoleType.HOUSEHOLD.name()));
        commonUserFacade.appendProfileByClientAndPartnerAndUserId(profileDTO);
    }

    private void removeCommunityIdAndRoleFromClientUserForHouseHolder(Integer partner,
            ObjectId communityId, Collection<ObjectId> userIds) {
        if (communityId == null || userIds == null || userIds.isEmpty()) {
            return;
        }

        Set<ObjectId> userIdsToUpdate = new HashSet<>(userIds);
        List<UserToRoom> userToRoomList = userToRoomRepository.findByCommunityIdAndUserIdInAndAuditStatusAndDataStatus(
                communityId,userIds, AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        userToRoomList.forEach(userToRoom -> userIdsToUpdate.remove(userToRoom.getUserId()));

        if (userIdsToUpdate.isEmpty()) {
            return;
        }

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setClient(ClientType.HOUSEHOLD.value());
        profileDTO.setPartner(partner);
        profileDTO.setTags(Collections.singleton(communityId));
        profileDTO.setRoles(Collections.singleton(RoleType.HOUSEHOLD.name()));
        userIdsToUpdate.forEach(userId -> {
            profileDTO.setUserId(userId);
            commonUserFacade.removeProfileByClientAndPartnerAndUserId(profileDTO);
        });
    }

    /**
     * 房屋信息审核通过相关操作
     * @param userToRoom
     */
    private void auditReviewedHandle(Integer partner, UserToRoom userToRoom) {
        if(userToRoom != null && userToRoom.getAuditStatus() == AuditStatusType.REVIEWED.getType()){
            addCommunityUser(userToRoom);
            addCommunityIdAndRoleToClientUser(partner, userToRoom);
            sendAuditMessage2Device(userToRoom);
        }
    }

    private void sendAuditMessage2Device(UserToRoom userToRoom) {
        User user = commonUserFacade.getUserByUserId(userToRoom.getUserId());
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setUserId(userToRoom.getUserId());
        deviceAuthVO.setCommunityId(userToRoom.getCommunityId());
        deviceAuthVO.setName(userToRoom.getName());
        deviceAuthVO.setPhone(user.getPhone());
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(userToRoom.getBuildingId());
        buildingListVO.setRooms(Collections.singleton(userToRoom.getRoomId()));
        Set<BuildingListVO> vos = Collections.singleton(buildingListVO);
        deviceAuthVO.setBuildingList(vos);
        Card card = cardGenerator.applyUserCard(userToRoom.getUserId(), userToRoom.getCommunityId(), userToRoom.getName());
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setKeyNo(card.getKeyNo());
        deviceAuthVO.setKeyId(card.getKeyId());
        deviceAuthVO.setRelationship(userToRoom.getRelationship());
        Date startDate = new Date();
        deviceAuthVO.setProcessTime((int) DateUtils.secondsBetween(startDate, DateUtils.addYear(startDate, 50)));
        // 使用次数暂时设定为0
        deviceAuthVO.setUsesTime(0);
        deviceAuthVO.setCorrelationId(userToRoom.getId());
        deviceAuthVO.setHandleCount(0);
        deviceAuthVO.setSex(user.getSex());
        deviceAuthVO.setUserIdentity(ClientType.HOUSEHOLD.value());
        List<DeviceAuthVO> allDeviceAuthVOS = getPhysicalCardsDeviceAuthVOS(deviceAuthVO);
        for (DeviceAuthVO authVO : allDeviceAuthVOS) {
            Message doorMessage = new Message(TOPIC_COMMUNITY_IOT_DOOR_AUTH, ADD, JSON.toJSONString(authVO).getBytes());
            Message elevatorMessage = new Message(TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH, ADD, JSON.toJSONString(authVO).getBytes());
            try {
                producer.send(doorMessage);
                producer.send(elevatorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("设备权限新增信息发送异常 : ", e);
            }
        }
    }

    private List<DeviceAuthVO> getPhysicalCardsDeviceAuthVOS(DeviceAuthVO deviceAuthVO) {
        List<Card> physicalCards = getUserPhysicalCardInCommunity(deviceAuthVO.getUserId(), deviceAuthVO.getCommunityId());
        List<DeviceAuthVO> deviceAuthVOS = new ArrayList<>(physicalCards.size() + 1);
        deviceAuthVOS.add(deviceAuthVO);
        deviceAuthVOS.addAll(physicalCards.stream().map(physicalCard -> {
            DeviceAuthVO physicalDeviceAuthVO = new DeviceAuthVO();
            BeanUtils.copyProperties(deviceAuthVO, physicalDeviceAuthVO);
            physicalDeviceAuthVO.setKeyType(physicalCard.getKeyType());
            physicalDeviceAuthVO.setKeyId(physicalCard.getKeyId());
            physicalDeviceAuthVO.setKeyNo(physicalCard.getKeyNo());
            return physicalDeviceAuthVO;
        }).collect(Collectors.toSet()));
        return deviceAuthVOS;
    }

    private void sendRelieveMessage2Device(UserToRoom userToRoom) {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setUserId(userToRoom.getUserId());
        deviceAuthVO.setCommunityId(userToRoom.getCommunityId());
        deviceAuthVO.setName(userToRoom.getName());
        deviceAuthVO.setPhone(userToRoom.getPhone());
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(userToRoom.getBuildingId());
        buildingListVO.setRooms(Collections.singleton(userToRoom.getRoomId()));
        Set<BuildingListVO> vos = Collections.singleton(buildingListVO);
        deviceAuthVO.setBuildingList(vos);

        // 查找用户虚拟卡
        Card card = cardRepository.findByUserIdAndCommunityIdAndKeyTypeAndDataStatus(
                userToRoom.getUserId(), userToRoom.getCommunityId(), CertificateType.PHONE_MAC.KEY, DataStatusType.VALID.KEY);
        if (card == null) {
            return;
        }

        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setKeyNo(card.getKeyNo());
        deviceAuthVO.setKeyId(card.getKeyId());
        deviceAuthVO.setCorrelationId(userToRoom.getId());
        deviceAuthVO.setHandleCount(0);
        deviceAuthVO.setOutUIds(Collections.singleton(userToRoom.getMiliUId()));

        // 判断同一楼栋下的其他房屋认证
        List<UserToRoom> otherRoomAudit = userToRoomRepository.findByBuildingIdAndUserIdAndAuditStatusAndDataStatus(
                userToRoom.getBuildingId(), userToRoom.getUserId(), AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        deviceAuthVO.setOtherRoomsId(otherRoomAudit.stream().map(UserToRoom::getRoomId).collect(Collectors.toList()));

        List<UserToRoom> otherCommunityRoom = userToRoomRepository
                .findByCommunityIdAndUserIdAndAuditStatusAndDataStatus(userToRoom.getCommunityId(),
                        userToRoom.getUserId(), AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        deviceAuthVO.setOtherRoomInCommunity(otherCommunityRoom.stream().map(UserToRoom::getRoomId).collect(Collectors.toList()));
        //amqpTemplate.convertAndSend(EXCHANGE_HOUSEHOLD, ROUTING_KEY_HOUSEHOLD_DELETE, deviceAuthVO);
        List<DeviceAuthVO> deviceAuthVOS = getPhysicalCardsDeviceAuthVOS(deviceAuthVO);

        for (DeviceAuthVO authVO : deviceAuthVOS) {
            Message doorMessage = new Message(TOPIC_COMMUNITY_IOT_DOOR_AUTH, DELETE, JSON.toJSONString(authVO).getBytes());
            Message elevatorMessage = new Message(TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH, DELETE, JSON.toJSONString(authVO).getBytes());
            try {
                producer.send(doorMessage);
                producer.send(elevatorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("设备权限删除信息发送异常 : ", e);
            }
        }
    }

    private List<Card> getUserPhysicalCardInCommunity(ObjectId userId, ObjectId communityId) throws BizException {
        return cardRepository.findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(userId, communityId,
                Arrays.asList(CertificateType.BLUETOOTH_CARD.KEY, CertificateType.IC_CARD.KEY), DataStatusType.VALID.KEY);
    }
}
