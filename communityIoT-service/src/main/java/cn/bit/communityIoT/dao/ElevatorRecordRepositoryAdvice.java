package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorRecordRequest;
import cn.bit.framework.data.common.Page;

public interface ElevatorRecordRepositoryAdvice {
    Page<ElevatorRecord> findAllElevatorRecords(ElevatorRecordRequest elevatorRecordRequest, int page, int size);
}
