package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.vo.communityIoT.door.DoorRecordRequest;
import cn.bit.framework.data.common.Page;

public interface DoorRecordRepositoryAdvice {

    Page<DoorRecord> findAllDoorRecords(DoorRecordRequest doorRecordRequest, int page, int size);
}
