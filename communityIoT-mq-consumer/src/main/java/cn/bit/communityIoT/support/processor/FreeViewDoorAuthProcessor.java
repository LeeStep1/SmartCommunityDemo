package cn.bit.communityIoT.support.processor;


import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.ManufactureType;
import cn.bit.facade.model.community.District;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.service.community.DistrictFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DoorAuthVO;
import cn.bit.facade.vo.mq.FreeViewDoorAuthVO;
import cn.bit.facade.vo.mq.ThirdPartInfoCallbackVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FreeViewDoorAuthProcessor implements DoorAuthProcessor {

    @Autowired
    private DoorFacade doorFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private DistrictFacade districtFacade;

    @Override
    public ThirdPartInfoCallbackVO add(DoorAuthVO deviceAuthVO) throws Exception {
        // 实体卡授权
        if (CertificateType.PHONE_MAC.KEY != deviceAuthVO.getKeyType()) {
            return null;
        }
        FreeViewDoorAuthVO freeViewDoorAuthVO = convertDoorAuthVOToFreeView(deviceAuthVO);

        if (CollectionUtils.isEmpty(getDoorList(deviceAuthVO))) {
            return null;
        }

        if (freeViewDoorAuthVO.getUserIdentity() == ClientType.HOUSEHOLD.value()) {
            getRoomOutIdForHouseHold(freeViewDoorAuthVO);
        }

        if (freeViewDoorAuthVO.getUserIdentity() == ClientType.PROPERTY.value()) {
            getRoomOutIdForProperty(freeViewDoorAuthVO);
        }
        doorFacade.updateFreeViewDoorAuth(freeViewDoorAuthVO);
        return null;
    }

    private void getRoomOutIdForHouseHold(FreeViewDoorAuthVO freeViewDoorAuthVO) {
        Set<ObjectId> roomIds = new HashSet<>();
        for (BuildingListVO buildingListVO : freeViewDoorAuthVO.getBuildingList()) {
            if (CollectionUtils.isNotEmpty(buildingListVO.getRooms())) {
                roomIds.addAll(buildingListVO.getRooms());
            }
        }
        if (roomIds.isEmpty()) {
            return;
        }
        List<Room> roomList = roomFacade.findRoomsByIds(roomIds);
        freeViewDoorAuthVO.setOutRoomCodes(roomList.stream().filter(room -> Objects.nonNull(room.getOutId()))
                .map(room -> String.valueOf(room.getOutId())).collect(Collectors.toSet()));
    }

    @Override
    public ThirdPartInfoCallbackVO delete(DoorAuthVO deviceAuthVO) throws Exception {
        // 实体卡授权
        if (CertificateType.PHONE_MAC.KEY != deviceAuthVO.getKeyType()) {
            return null;
        }
        FreeViewDoorAuthVO freeViewDoorAuthVO = convertDoorAuthVOToFreeView(deviceAuthVO);

        if (CollectionUtils.isEmpty(getDoorList(deviceAuthVO))) {
            return null;
        }

        Set<ObjectId> roomIds = new HashSet<>();
        for (BuildingListVO buildingListVO : deviceAuthVO.getBuildingList()) {
            if (CollectionUtils.isNotEmpty(buildingListVO.getRooms())) {
                roomIds.addAll(buildingListVO.getRooms());
            }
        }

        if (roomIds.isEmpty()) {
            return null;
        }

        List<Room> roomList = roomFacade.findRoomsByIds(roomIds);
        freeViewDoorAuthVO.setOutRoomCodes(
                roomList.stream().filter(room -> Objects.nonNull(room.getOutId())).map(Room::getOutId)
                        .collect(Collectors.toSet()));

        doorFacade.deleteFreeViewDoorAuth(freeViewDoorAuthVO);

        return null;
    }

    @Override
    public ThirdPartInfoCallbackVO cover(DoorAuthVO deviceAuthVO) throws Exception {
        // 实体卡授权
        if (CertificateType.PHONE_MAC.KEY != deviceAuthVO.getKeyType()) {
            return null;
        }
        FreeViewDoorAuthVO freeViewDoorAuthVO = convertDoorAuthVOToFreeView(deviceAuthVO);

        Set<ObjectId> addDistrictIds = new HashSet<>(freeViewDoorAuthVO.getDistrictIds());
        addDistrictIds.addAll(freeViewDoorAuthVO.getDelDistrictIds());
        List<District> districtList = districtFacade.findInIds(addDistrictIds);
        if (CollectionUtils.isEmpty(districtList) || CollectionUtils.isEmpty(getDoorDistrictList(districtList))) {
            return null;
        }
        Set<String> addOutCodeSet = new HashSet<>();
        districtList.stream()
                .filter(district -> freeViewDoorAuthVO.getDistrictIds().contains(district.getId())
                        && CollectionUtils.isNotEmpty(district.getThirdPartIds())).map(District::getThirdPartIds)
                .forEach(addOutCodeSet::addAll);
        freeViewDoorAuthVO.setOutRoomCodes(addOutCodeSet);

        Set<String> delOutCodeSet = new HashSet<>();
        districtList.stream()
                .filter(district -> freeViewDoorAuthVO.getDelDistrictIds().contains(district.getId())
                        && CollectionUtils.isNotEmpty(district.getThirdPartIds())).map(District::getThirdPartIds)
                .forEach(delOutCodeSet::addAll);
        freeViewDoorAuthVO.setDelOutRoomCodes(delOutCodeSet);
        doorFacade.coverFreeViewDoorAuth(freeViewDoorAuthVO);

        return null;
    }

    private FreeViewDoorAuthVO convertDoorAuthVOToFreeView(DoorAuthVO deviceAuthVO) {
        FreeViewDoorAuthVO freeViewDoorAuthVO = new FreeViewDoorAuthVO();
        BeanUtils.copyProperties(deviceAuthVO, freeViewDoorAuthVO);
        return freeViewDoorAuthVO;
    }

    private List<Door> getDoorDistrictList(List<District> districtList) {
        Set<ObjectId> buildingIds = new HashSet<>();
        districtList.stream().map(District::getBuildingIds).forEach(buildingIds::addAll);
        return doorFacade.getBuildingAndCommunityDoorByBrandNo(buildingIds, districtList.get(0).getCommunityId(),
                ManufactureType.FREEVIEW_DOOR.KEY);
    }

    private List<Door> getDoorList(DoorAuthVO deviceAuthVO) {
        Set<ObjectId> buildingIdSet = deviceAuthVO.getBuildingList().stream().map(BuildingListVO::getBuildingId)
                .collect(Collectors.toSet());
        return doorFacade.getBuildingAndCommunityDoorByBrandNo(buildingIdSet, deviceAuthVO.getCommunityId(),
                ManufactureType.FREEVIEW_DOOR.KEY);
    }

    private void getRoomOutIdForProperty(FreeViewDoorAuthVO freeViewDoorAuthVO) {
        List<District> toGetOutIds = districtFacade.findInIds(freeViewDoorAuthVO.getDistrictIds());
        Set<String> outIds = new HashSet<>();
        toGetOutIds.stream().filter(district -> null != district.getBrandNo()
                && ManufactureType.FREEVIEW_DOOR.KEY == district.getBrandNo()
                && CollectionUtils.isNotEmpty(district.getThirdPartIds()))
                .forEach(district -> outIds.addAll(district.getThirdPartIds()));
        freeViewDoorAuthVO.setOutRoomCodes(outIds);
    }
}
