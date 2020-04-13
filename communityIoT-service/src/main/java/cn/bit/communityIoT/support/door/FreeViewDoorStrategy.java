package cn.bit.communityIoT.support.door;

import cn.bit.facade.enums.DoorService;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.communityIoT.door.DoorInfo;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class FreeViewDoorStrategy implements DoorStrategy {
    @Autowired
    private DoorFacade doorFacade;
    @Override
    public List<DoorInfo> listDoorInfo(DoorRequest doorRequest) {
        List<DoorInfo> doorInfoList;
        try {
            doorInfoList = doorFacade.getFreeViewAuthDoor(doorRequest.getBuildingId(), doorRequest.getUserId(), doorRequest.getCommunityId());
        } catch (Exception e) {
            log.warn("查询全视通设备异常", e);
            return Collections.emptyList();
        }

        if (doorRequest.getServiceId() != null && doorRequest.getServiceId().contains(DoorService.BLUETOOTH.KEY)) {
            doorInfoList.removeIf(doorInfo -> doorInfo.getHasBluetooth() != null && !doorInfo.getHasBluetooth());
        }
        return doorInfoList;
    }
}
