package cn.bit.communityIoT.support.processor;

import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.ManufactureType;
import cn.bit.facade.enums.RelationshipType;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.District;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.DistrictFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DoorAuthVO;
import cn.bit.facade.vo.mq.MiliDoorAuthVO;
import cn.bit.facade.vo.mq.ThirdPartInfoCallbackVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MiliDoorAuthProcessor implements DoorAuthProcessor {
    @Autowired
    private DoorFacade doorFacade;
    @Autowired
    private CommunityFacade communityFacade;
    @Autowired
    private RoomFacade roomFacade;
    @Autowired
    private DistrictFacade districtFacade;

    @Override
    public ThirdPartInfoCallbackVO add(DoorAuthVO deviceAuthVO) throws Exception {
        // 实体卡授权
        if (CertificateType.PHONE_MAC.KEY != deviceAuthVO.getKeyType()) {
            log.info("门禁授权 keyType = {}, 不属于手机mac卡 返回 null", deviceAuthVO.getKeyType());
            return null;
        }
        MiliDoorAuthVO miliDoorAuthVO = convertDoorAuthVOToMili(deviceAuthVO);
        List<Door> doorList = getDoorList(miliDoorAuthVO);
        miliDoorAuthVO = fillMiliDoorAuthParam(doorList, miliDoorAuthVO);
        if (CollectionUtils.isEmpty(doorList) || miliDoorAuthVO == null) {
            log.info("add auth doorList({}) is null or millDoorAuthVO({}) is null return null.", doorList, miliDoorAuthVO);
            return null;
        }
        Set<ObjectId> doorBuildingId = doorList.stream().filter(door -> door.getBuildingId() != null)
                .map(Door::getBuildingId).collect(Collectors.toSet());

        Set<Long> ids = doorFacade.updateMiliDoorAuth(miliDoorAuthVO);
        if (CollectionUtils.isEmpty(ids)) {
            log.info("add auth mili ids({}) is null return null.", ids);
            return null;
        }
        return initCallbackVO(miliDoorAuthVO, ids, doorBuildingId);
    }

    private MiliDoorAuthVO fillMiliDoorAuthParam(List<Door> doorList, MiliDoorAuthVO miliDoorAuthVO) {
        if (RelationshipType.OWNER.KEY.equals(miliDoorAuthVO.getRelationship())) {
            miliDoorAuthVO.setDoorList(doorList);
        }
        return putMiliRoomId(miliDoorAuthVO);
    }

    @Override
    public ThirdPartInfoCallbackVO delete(DoorAuthVO deviceAuthVO) throws Exception {
        // 实体卡授权
        if (CertificateType.PHONE_MAC.KEY != deviceAuthVO.getKeyType()) {
            log.info("门禁取消授权 keyType = {}, 不属于手机mac卡 返回 null", deviceAuthVO.getKeyType());
            return null;
        }
        MiliDoorAuthVO miliDoorAuthVO = convertDoorAuthVOToMili(deviceAuthVO);
        List<Door> doorList = getDoorList(miliDoorAuthVO);
        if (CollectionUtils.isEmpty(doorList) || CollectionUtils.isEmpty(miliDoorAuthVO.getOutUIds())) {
            log.info("delete auth doorList({}) is null or miliDoorAuthVO.getOutUIds()({}) is null return null.", doorList, miliDoorAuthVO.getOutUIds());
            return null;
        }

        for (Long miliId : miliDoorAuthVO.getOutUIds()) {
            if (miliId == null) {
                log.info("miliId is null continue;");
                continue;
            }
            doorFacade.deleteMiliDoorAuth(String.valueOf(miliId));
        }
        return null;
    }

    @Override
    public ThirdPartInfoCallbackVO cover(DoorAuthVO deviceAuthVO) throws Exception {
        // 实体卡授权
        if (CertificateType.PHONE_MAC.KEY != deviceAuthVO.getKeyType()) {
            log.info("门禁覆盖授权 keyType = {}, 不属于手机mac卡 返回 null", deviceAuthVO.getKeyType());
            return null;
        }
        MiliDoorAuthVO miliDoorAuthVO = convertDoorAuthVOToMili(deviceAuthVO);
        List<Door> doorList = getDoorList(miliDoorAuthVO);

        if (CollectionUtils.isEmpty(doorList)) {
            log.info("cover auth doorList({}) is null return null.", doorList);
            return null;
        }

        Set<ObjectId> doorBuildingId = doorList.stream().filter(door -> door.getBuildingId() != null)
                .map(Door::getBuildingId).collect(Collectors.toSet());

        buildMiliDoorParamForProperty(miliDoorAuthVO);
        Set<Long> ids = doorFacade.coverMiliDoorAuth(miliDoorAuthVO);
        if (CollectionUtils.isEmpty(ids)) {
            log.info("delete auth mili ids({}) is null return null.", ids);
            return null;
        }
        return initCallbackVO(miliDoorAuthVO, ids, doorBuildingId);
    }

    private ThirdPartInfoCallbackVO initCallbackVO(MiliDoorAuthVO deviceAuthVO, Set<Long> ids,
                                                   Set<ObjectId> doorBuildingId) {
        ThirdPartInfoCallbackVO callbackVO = new ThirdPartInfoCallbackVO();
        callbackVO.setUserIds(ids.stream().map(String::valueOf).collect(Collectors.toSet()));
        callbackVO.setCorrelationId(deviceAuthVO.getCorrelationId());
        callbackVO.setCommunityId(deviceAuthVO.getCommunityId());
        callbackVO.setBuildingIds(doorBuildingId);
        return callbackVO;
    }

    private MiliDoorAuthVO putMiliRoomId(MiliDoorAuthVO miliDoorVO) {

        Community community = communityFacade.findOne(miliDoorVO.getCommunityId());
        miliDoorVO.setMiliCId(community.getMiliCId());
        Set<ObjectId> roomIds = miliDoorVO.getBuildingList().stream()
                .flatMap(buildingListVO -> buildingListVO.getRooms().stream()).collect(Collectors.toSet());
        Set<String> miliRIdSet = new HashSet<>();

        if (miliDoorVO.getUserIdentity() == ClientType.HOUSEHOLD.value()) {
            if (roomIds.isEmpty()) {
                return null;
            }
            List<Room> roomList = roomFacade.findRoomsByIds(roomIds);
            roomList.stream().filter(room -> room.getOutId() != null).forEach(room -> miliRIdSet.add(room.getOutId()));
        }

        if (miliDoorVO.getUserIdentity() == ClientType.PROPERTY.value()) {
            // 拿到唯一的物业房间miliId
            miliDoorVO.setRelationship(RelationshipType.OWNER.KEY);
            District district = districtFacade.findById(miliDoorVO.getDistrictIds().iterator().next());
            miliRIdSet.addAll(district != null ? district.getThirdPartIds() : Collections.emptySet());
            miliDoorVO.setSex(1);
        }

        miliDoorVO.setMiliRId(miliRIdSet);
        return miliDoorVO;
    }

    private void buildMiliDoorParamForProperty(MiliDoorAuthVO miliDoorVO) {
        // 拿到唯一的物业房间miliId 授权为家人
        Community community = communityFacade.findOne(miliDoorVO.getCommunityId());
        miliDoorVO.setMiliCId(community.getMiliCId());
        List<District> districtList = districtFacade.findInIds(miliDoorVO.getDistrictIds());
        miliDoorVO.setRelationship(RelationshipType.RELATION.KEY);
        miliDoorVO.setSex(1);
        if (districtList == null) {
            miliDoorVO.setMiliRId(Collections.emptySet());
            return;
        }
        // 获取区域中的米立物业ID
        miliDoorVO.setMiliRId(districtList.stream()
                .filter(district -> null != district.getBrandNo() && ManufactureType.MILI.KEY == district
                        .getBrandNo() && CollectionUtils.isNotEmpty(district.getThirdPartIds()))
                .flatMap(district -> district.getThirdPartIds().stream()).collect(Collectors.toSet()));
    }

    private MiliDoorAuthVO convertDoorAuthVOToMili(DoorAuthVO deviceAuthVO) {
        MiliDoorAuthVO miliDoorAuthVO = new MiliDoorAuthVO();
        BeanUtils.copyProperties(deviceAuthVO, miliDoorAuthVO);
        return miliDoorAuthVO;
    }

    private List<Door> getDoorList(MiliDoorAuthVO deviceAuthVO) {
        return doorFacade.getBuildingAndCommunityDoorByBrandNo(
                deviceAuthVO.getBuildingList().stream().map(BuildingListVO::getBuildingId).collect(Collectors.toSet()),
                deviceAuthVO.getCommunityId(), ManufactureType.MILI.KEY);
    }
}
