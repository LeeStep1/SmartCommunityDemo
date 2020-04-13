package cn.bit.communityIoT.support.processor;

import cn.bit.facade.model.community.Room;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.communityIoT.elevator.FloorVO;
import cn.bit.facade.vo.mq.ElevatorAuthVO;
import cn.bit.facade.vo.mq.KangTuElevatorAuthVO;
import cn.bit.facade.vo.mq.ThirdPartInfoCallbackVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KangTuElevatorAuthProcessor implements ElevatorAuthProcessor {

    @Autowired
    private RoomFacade roomFacade;
    @Autowired
    private ElevatorFacade elevatorFacade;

    @Override
    public ThirdPartInfoCallbackVO add(ElevatorAuthVO deviceAuthVO) {
        KangTuElevatorAuthVO kangTuElevatorAuthVO = convertElevatorAuthVOToKangTu(deviceAuthVO);
        buildFloorVOFrom(kangTuElevatorAuthVO);
        elevatorFacade.updateIoTElevatorAuth(kangTuElevatorAuthVO);
        return null;
    }

    @Override
    public ThirdPartInfoCallbackVO delete(ElevatorAuthVO deviceAuthVO) {
        KangTuElevatorAuthVO kangTuElevatorAuthVO = convertElevatorAuthVOToKangTu(deviceAuthVO);

        ObjectId target = kangTuElevatorAuthVO.getBuildingList().iterator().next().getRooms().iterator().next();
        if (CollectionUtils.isNotEmpty(kangTuElevatorAuthVO.getOtherRoomsId())
                && existSameFloor(kangTuElevatorAuthVO.getOtherRoomsId(), target)) {
            return null;
        }

        buildFloorVOFrom(kangTuElevatorAuthVO);
        elevatorFacade.deleteIoTElevatorAuth(kangTuElevatorAuthVO);
        return null;
    }

    private KangTuElevatorAuthVO convertElevatorAuthVOToKangTu(ElevatorAuthVO deviceAuthVO) {
        KangTuElevatorAuthVO kangTuElevatorAuthVO = new KangTuElevatorAuthVO();
        BeanUtils.copyProperties(deviceAuthVO, kangTuElevatorAuthVO);
        return kangTuElevatorAuthVO;
    }

    @Override
    public ThirdPartInfoCallbackVO cover(ElevatorAuthVO deviceAuthVO) {
        KangTuElevatorAuthVO kangTuElevatorAuthVO = convertElevatorAuthVOToKangTu(deviceAuthVO);
        buildFloorVOFrom(kangTuElevatorAuthVO);
        elevatorFacade.coverIoTElevatorAuth(kangTuElevatorAuthVO);
        return null;
    }

    private void buildFloorVOFrom(KangTuElevatorAuthVO deviceAuthVO) {
        Set<ObjectId> roomIds = new HashSet<>();
        for (BuildingListVO buildingListVO : deviceAuthVO.getBuildingList()) {
            if (CollectionUtils.isNotEmpty(buildingListVO.getRooms())) {
                roomIds.addAll(buildingListVO.getRooms());
            }
        }

        // 收集房屋楼层参数
        Map<ObjectId, Room> roomMap = null;
        if (CollectionUtils.isNotEmpty(roomIds)) {
            List<Room> roomList = roomFacade.findRoomsByIds(roomIds);
            roomMap = roomList.stream().collect(Collectors.toMap(Room::getId, r -> r));
        }
        Map<ObjectId, FloorVO> map = new HashMap<>(deviceAuthVO.getBuildingList().size());

        for (BuildingListVO buildingListVO : deviceAuthVO.getBuildingList()) {

            // 已有楼栋
            if (map.containsKey(buildingListVO.getBuildingId())) {
                continue;
            }

            FloorVO floorVO = new FloorVO(buildingListVO.getBuildingId().toString());
            floorVO.setFloors(new HashSet<>());
            floorVO.setSubFloors(new HashSet<>());
            map.put(buildingListVO.getBuildingId(), floorVO);
            // 同一个楼栋使用同一个floorVO
            deviceAuthVO.getBuilds().add(map.get(buildingListVO.getBuildingId()));

            // 没有房屋, VO里具体的房间可以为空
            if (buildingListVO.getRooms() == null || roomMap == null) {
                continue;
            }
            // 有具体指定房屋才需要设定楼层号
            for (ObjectId roomId : buildingListVO.getRooms()) {
                // 如果没有主副门设置就主副门都需要给
                if (roomMap.get(roomId).getMainDoor() == null && roomMap.get(roomId).getSubDoor() == null) {
                    floorVO.getFloors().add(roomMap.get(roomId).getFloorCode());
                    floorVO.getSubFloors().add(roomMap.get(roomId).getFloorCode());
                    continue;
                }

                // 主门
                if (roomMap.get(roomId).getMainDoor() != null && roomMap.get(roomId).getMainDoor()) {
                    floorVO.getFloors().add(roomMap.get(roomId).getFloorCode());
                }

                // 副门
                if (roomMap.get(roomId).getSubDoor() != null && roomMap.get(roomId).getSubDoor()) {
                    floorVO.getSubFloors().add(roomMap.get(roomId).getFloorCode());
                }
            }
        }
    }

    /**
     * 查看用户是否存在相同楼层的房间
     * @param roomIds
     * @param target
     * @return
     */
    private boolean existSameFloor(List<ObjectId> roomIds, ObjectId target) {
        return roomFacade.findRoomsInSameFloor(roomIds, target) >= 1;
    }
}
