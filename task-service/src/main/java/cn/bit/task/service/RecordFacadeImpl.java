package cn.bit.task.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.task.Record;
import cn.bit.facade.service.task.RecordFacade;
import cn.bit.facade.vo.task.RecordRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.task.dao.RecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("recordFacade")
@Slf4j
public class RecordFacadeImpl implements RecordFacade {

    @Autowired
    private RecordRepository recordRepository;

    @Override
    public boolean changeDataStatus(ObjectId id) throws BizException {
        Record record = new Record();
        record.setUpdateAt(new Date());
        record.setDataStatus(DataStatusType.INVALID.KEY);
        return recordRepository.updateById(record, id) != null;
    }

    @Override
    public Record addTaskRecord(Record entity) throws BizException {
        Date now = new Date();
        entity.setCreateAt(now);
        entity.setUpdateAt(now);
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return recordRepository.insert(entity);
    }

    @Override
    public Record findOne(ObjectId id) throws BizException {
        return recordRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Record> queryList(Record entity) throws BizException {
        return recordRepository.findByUserNameIgnoreNullAndTaskTypeIgnoreNullAndDataStatusOrderByCreateAtDesc(
                entity.getUserName(), entity.getTaskType(), DataStatusType.VALID.KEY);
    }

    @Override
    public boolean deleteTaskRecord(ObjectId id) throws BizException {
        int i = recordRepository.remove(id);
        return i > 0;
    }

    @Override
    @Deprecated
    public Page<Record> queryPage(Record entity, int page, int size) throws BizException {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Record> resultPage =
                recordRepository.findByUserNameIgnoreNullAndTaskTypeIgnoreNullAndDataStatus(
                        entity.getUserName(), entity.getTaskType(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public List<Record> getRecords(RecordRequest request) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        if (startDate == null) {
            startDate = DateUtils.getFirstDateOfMonth(new Date());
        }
        if (endDate == null) {
            endDate = DateUtils.getLastDateOfMonth(new Date());
        }

        return recordRepository.findByCommunityIdAndUserIdInAndUserNameAndTaskTypeAndPostCodeAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
                request.getCommunityId(), request.getUserIds(), request.getUserName(), request.getTaskType(), request.getRole(),
                startDate, DateUtils.getEndTime(endDate), DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Record> getRecords(RecordRequest request, int page, int size) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        if (startDate == null) {
            startDate = DateUtils.getFirstDateOfMonth(new Date());
        }
        if (endDate == null) {
            endDate = DateUtils.getLastDateOfMonth(new Date());
        }

        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Record> recordPage =
                recordRepository.findByCommunityIdAndUserIdInAndUserNameAndTaskTypeAndPostCodeAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
                        request.getCommunityId(), request.getUserIds(), request.getUserName(), request.getTaskType(), request.getRole(),
                        startDate, DateUtils.getEndTime(endDate), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(recordPage);
    }
}
