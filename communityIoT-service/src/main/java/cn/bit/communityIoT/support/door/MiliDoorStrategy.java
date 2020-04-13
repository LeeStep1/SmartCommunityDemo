package cn.bit.communityIoT.support.door;

import cn.bit.facade.enums.ManufactureType;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.communityIoT.door.DoorInfo;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class MiliDoorStrategy implements DoorStrategy {
    @Autowired
    private DoorFacade doorFacade;

    @Override
    public List<DoorInfo> listDoorInfo(DoorRequest entity) {
        List<DoorInfo> doorInfoList = new ArrayList<>();
        DoorRequest doorRequest = new DoorRequest();
        doorRequest.setCommunityId(entity.getCommunityId());
        doorRequest.setBuildingId(entity.getBuildingId());
        doorRequest.setServiceId(entity.getServiceId());
        doorRequest.setBrandNo(Collections.singleton(ManufactureType.MILI.KEY));
        List<Door> doors;
        try {
            doors = doorFacade.getBuildingAndCommunityDoorByDoorRequest(doorRequest);
        } catch (Exception e) {
            log.warn("查询米立设备异常", e);
            return Collections.emptyList();
        }
        doors.forEach(door -> doorInfoList.add(new DoorInfo(door, null, true)));
        return doorInfoList;
    }
}
