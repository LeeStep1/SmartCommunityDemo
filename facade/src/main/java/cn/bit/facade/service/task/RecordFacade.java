package cn.bit.facade.service.task;

import cn.bit.facade.model.task.Record;
import cn.bit.facade.vo.task.RecordRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

public interface RecordFacade {

    /**
     * 新增一个 Record 信息
     * @param entity
     * @throws BizException
     * @return
     */
    Record addTaskRecord(Record entity) throws BizException;

    /**
     * 根据ID获取一个 Record 信息
     * @param id
     * @throws BizException
     * @return
     */
    Record findOne(ObjectId id) throws BizException;

    /**
     * 获取社区的 Record
     * @param entity
     * @throws BizException
     * @return
     */
    List<Record> queryList(Record entity) throws BizException;

    /**
     * 根据ID删除一个 Record
     * @param id
     * @throws BizException
     * @return
     */
    boolean deleteTaskRecord(ObjectId id) throws BizException;

    /**
     * 分页
     * @since 20190613 去除没有使用的接口
     * @param entity
     * @param page
     * @param size
     * @throws BizException
     * @return
     */
    @Deprecated
    Page<Record> queryPage(Record entity, int page, int size) throws BizException;

    /**
     * 修改数据状态
     * @param id
     * @throws BizException
     * @return
     */
    boolean changeDataStatus(ObjectId id) throws BizException;

    List<Record> getRecords(RecordRequest recordRequest);

    Page<Record> getRecords(RecordRequest recordRequest, int page, int size);
}
