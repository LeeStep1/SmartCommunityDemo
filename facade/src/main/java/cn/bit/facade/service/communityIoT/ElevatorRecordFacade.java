package cn.bit.facade.service.communityIoT;

import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorDetailDTO;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorRecordRequest;
import cn.bit.facade.vo.mq.CreateRecordRequest;
import cn.bit.facade.vo.statistics.ElevatorRecordResponse;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

/**
 *
 * @description
 * @create: 2018/3/8
 **/
public interface ElevatorRecordFacade {
    /**
     * 添加梯禁记录
     * @param entity
     * @return
     */
    ElevatorRecord addElevatorRecord(ElevatorRecord entity);

    /**
     * 根据id删除梯禁记录
     * @param id
     * @return
     */
    ElevatorRecord deleteElevatorRecordById(ObjectId id);

    /**
     * 根据id查询梯禁记录详情
     * @param id
     * @return
     */
    ElevatorRecord findById(ObjectId id);

    /**
     * 分页梯禁使用记录
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<ElevatorRecord> queryPage(ElevatorRecord entity, int page, int size);

    Page<ElevatorRecord> getElevatorRecords(ElevatorRecordRequest elevatorRecordRequest, int page, int size);

    /**
     * 批量添加梯禁使用记录
     * @param entities
     */
    void batchAddElevatorRecord(List<ElevatorRecord> entities);

    /**
     * 电梯使用记录统计
     * @param elevatorRecordRequest
     * @return
     */
    ElevatorRecordResponse getElevatorRecordStatistics(cn.bit.facade.vo.statistics.ElevatorRecordRequest elevatorRecordRequest);

    /**
     * 根据硬件上报信息添加记录
     * @param createRecordRequest
     * @param card
     * @param elevatorDetail
     */
    void addRecordBy(CreateRecordRequest createRecordRequest, Card card, ElevatorDetailDTO elevatorDetail);
}
