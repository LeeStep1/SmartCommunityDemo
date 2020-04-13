package cn.bit.facade.service.communityIoT;

import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.vo.communityIoT.door.DoorRecordRequest;
import cn.bit.facade.vo.statistics.DoorRecordResponse;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 *
 * @description
 * @create: 2018/3/8
 **/
public interface DoorRecordFacade {
    /**
     * 添加门禁记录
     * @param entity
     * @return
     */
    DoorRecord addDoorRecord(DoorRecord entity);

    /**
     * 根据id删除门禁记录
     * @param id
     * @return
     */
    DoorRecord deleteDoorRecordById(ObjectId id);

    /**
     * 根据id查询门禁记录详情
     * @param id
     * @return
     */
    DoorRecord findById(ObjectId id);

    /**
     * 分页门禁使用记录
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<DoorRecord> queryPage(DoorRecord entity, int page, int size);

    Page<DoorRecord> getDoorRecords(DoorRecordRequest doorRecordRequest, int page, int size);

    /**
     * 批量添加门禁使用记录
     * @param entities
     * @return
     */
    void batchAddDoorRecord(List<DoorRecord> entities);

    /**
     * 门禁使用记录统计
     * @param doorRecordRequest
     * @return
     */
    DoorRecordResponse getDoorRecordStatistics(cn.bit.facade.vo.statistics.DoorRecordRequest doorRecordRequest);

    /**
     * 统计时间端内某社区的开门次数
     * @param communityId
     * @param startAt
     * @param endAt
     * @return
     */
    Long countByCommunityIdAndDate(ObjectId communityId, Date startAt, Date endAt);
}
