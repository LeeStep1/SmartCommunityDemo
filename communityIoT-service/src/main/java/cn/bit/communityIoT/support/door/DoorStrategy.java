package cn.bit.communityIoT.support.door;

import cn.bit.facade.vo.communityIoT.door.DoorInfo;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;

import java.util.List;

public interface DoorStrategy {
    List<DoorInfo> listDoorInfo(DoorRequest doorRequest);
}
