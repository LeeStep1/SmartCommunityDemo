package cn.bit.facade.service.property;

import cn.bit.facade.model.property.Alarm;
import cn.bit.facade.vo.statistics.StatisticsRequest;
import cn.bit.facade.vo.statistics.AlarmResponse;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface AlarmFacade
{
    /**
     * 添加报警记录
     * @param alarm
     * @return
     * @throws BizException
     */
    Alarm addRecord(Alarm alarm) throws BizException;

    /**
     * 查询所有的报警记录
     * @return
     * @throws BizException
     */
    List<Alarm> getAllAlarmRecord() throws BizException;

    /**
     * 根据ID查报警记录
     * @param id
     * @return
     * @throws BizException
     */
    Alarm getAlarmRecordById(ObjectId id) throws BizException;

    /**
     * 物业接警
     * @param alarm
     * @return
     * @throws BizException
     */
    Alarm receiveAlarm(Alarm alarm) throws BizException;

    /**
     * 故障排查
     * @param alarm
     * @return
     * @throws BizException
     */
    Alarm troubleShoot(Alarm alarm) throws BizException;

    /**
     * 物业查看报警数据
     * @param entity
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    Page<Alarm> getAlarmRecord(Alarm entity, int page, int size) throws BizException;

    /**
     * 业主查看报警数据
     * @param callerId
     * @return
     * @throws BizException
     */
    Page<Alarm> getProprietorAlarm(ObjectId callerId, int page, int size) throws BizException;

    /**
     * 查找待接警数量
     * @param communityId
     * @return
     */
    Map<String, Long> findReceiveAlarmNum(ObjectId communityId, Integer receiveStatus);

    /**
     * 警报统计
     * @param statisticsRequest
     * @return
     */
    AlarmResponse getAlarmStatistics(StatisticsRequest statisticsRequest);
}
