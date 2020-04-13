package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import cn.bit.framework.data.common.Page;

import java.util.List;

public interface DoorRepositoryAdvice {

    List<Door> findByDoorRequest(DoorRequest doorRequest);

    Page<Door> findByDoorRequest(DoorRequest doorRequest, int page, int size);

    Door findAndModify(Door door);
}
