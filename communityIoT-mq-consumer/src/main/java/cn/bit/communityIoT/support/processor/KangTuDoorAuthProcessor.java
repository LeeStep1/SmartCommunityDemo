package cn.bit.communityIoT.support.processor;

import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.enums.DoorType;
import cn.bit.facade.enums.ManufactureType;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.communityIoT.door.DoorDeviceVO;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DoorAuthVO;
import cn.bit.facade.vo.mq.KangTuDoorAuthVO;
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
public class KangTuDoorAuthProcessor implements DoorAuthProcessor {

    @Autowired
    private DoorFacade doorFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Override
    public ThirdPartInfoCallbackVO add(DoorAuthVO deviceAuthVO) {
        ThirdPartInfoCallbackVO thirdPartInfoCallbackVO = new ThirdPartInfoCallbackVO();
        KangTuDoorAuthVO kangTuDoorAuthVO = populateAuthVOForAddAndCover(deviceAuthVO, thirdPartInfoCallbackVO);
        if (kangTuDoorAuthVO == null) {
            return null;
        }
        doorFacade.updateKangTuDoorAuth(kangTuDoorAuthVO);
        return callBackIfNecessary(kangTuDoorAuthVO, thirdPartInfoCallbackVO);
    }

    private KangTuDoorAuthVO populateAuthVOForAddAndCover(DoorAuthVO deviceAuthVO,
                                                          ThirdPartInfoCallbackVO thirdPartInfoCallbackVO) {
        KangTuDoorAuthVO kangTuDoorAuthVO = convertDoorAuthVOToKangTu(deviceAuthVO);
        Set<ObjectId> buildingIds = new HashSet<>();
        List<Door> doorList = getDoorList(kangTuDoorAuthVO);
        if (CollectionUtils.isEmpty(doorList)) {
            return null;
        }
        kangTuDoorAuthVO.setDoorDevices(doorList.stream().map(door -> {
            buildingIds.add(door.getBuildingId());
            return new DoorDeviceVO(door.getId().toString(), door.getTerminalCode(), door.getTerminalPort());
        }).collect(Collectors.toSet()));

        thirdPartInfoCallbackVO.setBuildingIds(buildingIds);
        return kangTuDoorAuthVO;
    }

    @Override
    public ThirdPartInfoCallbackVO delete(DoorAuthVO deviceAuthVO) {
        KangTuDoorAuthVO kangTuDoorAuthVO = convertDoorAuthVOToKangTu(deviceAuthVO);
        if (CollectionUtils.isNotEmpty(kangTuDoorAuthVO.getOtherRoomsId())) {
            return null;
        }
        List<Door> doorList = getDoorList(kangTuDoorAuthVO);
        if (CollectionUtils.isEmpty(doorList)) {
            return null;
        }
        // 获取同一社区其他房间的楼栋ID集合
        Set<ObjectId> ignoreBuilding = obtainOtherBuildingIds(kangTuDoorAuthVO);

        kangTuDoorAuthVO.setDoorDevices(markDeviceSetIfDelete(kangTuDoorAuthVO, doorList, ignoreBuilding));

        doorFacade.deleteKangTuDoorAuth(kangTuDoorAuthVO);
        return null;
    }

    private Set<DoorDeviceVO> markDeviceSetIfDelete(KangTuDoorAuthVO kangTuDoorAuthVO, List<Door> doorList,
                                                    Set<ObjectId> ignoreBuilding) {
        return doorList.stream()
                // 当前社区没有其他房屋，社区门才能删除
                .filter(door -> (CollectionUtils.isEmpty(kangTuDoorAuthVO.getOtherRoomInCommunity())
                        && DoorType.COMMUNITY_DOOR.getValue() == door.getDoorType())
                        // 当前社区其他房屋不在该楼栋，该楼栋的楼栋门才能删除
                        || (CollectionUtils.isEmpty(ignoreBuilding) || !ignoreBuilding.contains(door.getBuildingId())))
                .map(door -> new DoorDeviceVO(door.getId().toString(), door.getTerminalCode(), door.getTerminalPort()))
                .collect(Collectors.toSet());
    }

    private Set<ObjectId> obtainOtherBuildingIds(KangTuDoorAuthVO kangTuDoorAuthVO) {
        if (CollectionUtils.isEmpty(kangTuDoorAuthVO.getOtherRoomInCommunity())) {
            return Collections.emptySet();
        }
        return roomFacade.findRoomsByIds(new HashSet<>(kangTuDoorAuthVO.getOtherRoomInCommunity()))
                .stream().map(Room::getBuildingId).collect(Collectors.toSet());
    }

    @Override
    public ThirdPartInfoCallbackVO cover(DoorAuthVO deviceAuthVO) {
        ThirdPartInfoCallbackVO thirdPartInfoCallbackVO = new ThirdPartInfoCallbackVO();
        KangTuDoorAuthVO kangTuDoorAuthVO = populateAuthVOForAddAndCover(deviceAuthVO, thirdPartInfoCallbackVO);
        if (kangTuDoorAuthVO == null) {
            return null;
        }
        doorFacade.coverKangTuDoorAuth(kangTuDoorAuthVO);
        return callBackIfNecessary(kangTuDoorAuthVO, thirdPartInfoCallbackVO);
    }

    private List<Door> getDoorList(KangTuDoorAuthVO deviceAuthVO) {
        return doorFacade.getBuildingAndCommunityDoorByBrandNo(
                deviceAuthVO.getBuildingList().stream().map(BuildingListVO::getBuildingId).collect(Collectors.toSet()),
                deviceAuthVO.getCommunityId(), ManufactureType.KANGTU_DOOR.KEY);
    }

    private KangTuDoorAuthVO convertDoorAuthVOToKangTu(DoorAuthVO deviceAuthVO) {
        KangTuDoorAuthVO kangTuDoorAuthVO = new KangTuDoorAuthVO();
        BeanUtils.copyProperties(deviceAuthVO, kangTuDoorAuthVO);
        return kangTuDoorAuthVO;
    }

    private ThirdPartInfoCallbackVO callBackIfNecessary (KangTuDoorAuthVO deviceAuthVO,
                                                        ThirdPartInfoCallbackVO thirdPartInfoCallbackVO) {
        if (deviceAuthVO.getKeyType() != CertificateType.PHONE_MAC.KEY) {
            return null;
        }
        return thirdPartInfoCallbackVO;
    }
}
