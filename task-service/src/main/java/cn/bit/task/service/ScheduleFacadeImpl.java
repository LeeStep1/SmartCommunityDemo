package cn.bit.task.service;

import cn.bit.facade.enums.ClassType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.RoleType;
import cn.bit.facade.exception.task.TaskBizException;
import cn.bit.facade.model.task.Class;
import cn.bit.facade.model.task.Schedule;
import cn.bit.facade.service.task.ScheduleFacade;
import cn.bit.facade.vo.task.ScheduleRequest;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.task.dao.ScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component("scheduleFacade")
@Slf4j
public class ScheduleFacadeImpl implements ScheduleFacade {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public List<Schedule> generateShiftSchedule(List<UserToProperty> employees, List<Class> classes,
                                                Date startDate, Date endDate, String postCode) {
        List<Schedule> schedules = new ArrayList<Schedule>();

        // 排班所需人员数
        long expectedEmployeeNumber = classes.stream()
                .mapToInt(item -> item.getNumber())
                .summaryStatistics()
                .getSum();
        if (expectedEmployeeNumber != employees.size()){
            throw TaskBizException.CLASS_EMPLOYEE_NOT_MATCH;
        }

        // 人员安排shiftCode（初始化）
        int index = 0;
        for (int i = 0; i < classes.size(); i++) {
            for (int j = 0; j < classes.get(i).getNumber(); j++) {
                employees.get(index).setShiftOrder(classes.get(i).getShiftOrder());
                index++;
            }
            expectedEmployeeNumber += classes.get(i).getNumber();
        }

        // 获取shiftCode最大值
        Integer maxShiftCode = classes.stream()
                .mapToInt(item -> item.getShiftOrder())
                .summaryStatistics()
                .getMax();
        Integer minShiftCode = classes.stream()
                .mapToInt(item -> item.getShiftOrder())
                .summaryStatistics()
                .getMin();

        // 获取日期的集合，根据日期数量循环
        for (Date date = startDate;
             date.before(DateUtils.addDay(endDate, 1));
             date = DateUtils.addDay(date, 1)) {

            for (UserToProperty employee : employees) {
                Schedule schedule = new Schedule();
                schedule.setWorkDate(date);
                schedule.setWorkWeek(DateUtils.getWeekIndex(date));

                Class targetClass = classes.stream()
                        .filter(item -> item.getShiftOrder() == employee.getShiftOrder())
                        .findFirst()
                        .get();

                if (targetClass == null) {
                    continue;
                }

                schedule = assembleScheduleFromClass(schedule, targetClass);
                schedule = setAttendOffTimeByString(schedule);

                schedule.setUserId(employee.getUserId());
                schedule.setUserName(employee.getUserName());
                schedule.setPostCode(postCode);
                schedule.setClassType(ClassType.SHIFT.key);
                schedule.setCommunityId(employee.getCommunityId());
                schedule.setCreateAt(new Date());
                schedule.setUpdateAt(schedule.getCreateAt());
                schedule.setDataStatus(DataStatusType.VALID.KEY);

                schedules.add(schedule);

                // 更新shiftCode
                employee.setShiftOrder(
                        employee.getShiftOrder() + 1 > maxShiftCode
                                ? minShiftCode
                                : employee.getShiftOrder() + 1);
            }
        }

        insertSchedules(schedules);

        return schedules;
    }

    @Override
    public List<Schedule> generatePeacetimeSchedule(List<UserToProperty> employees, List<Class> classes,
                                                    Date startDate, Date endDate, String postCode) {
        List<Schedule> schedules = new ArrayList<Schedule>();

        // 检查常班设置（根据业务需求再优化）
        if (classes.size() != 1){
            throw TaskBizException.CLASS_PEACETIME_ERROR;
        }

        Class targetClass = classes.get(0);
        if (targetClass == null) {
            throw TaskBizException.CLASS_PEACETIME_ERROR;
        }

        // 获取日期的集合，根据日期数量循环
        for (Date date = startDate;
             date.before(DateUtils.addDay(endDate, 1));
             date = DateUtils.addDay(date, 1)) {
            int weekIndex = DateUtils.getWeekIndex(date);

            if(targetClass.getRestWeeks() != null && targetClass.getRestWeeks().contains(weekIndex)){
                continue;
            }

            for (UserToProperty employee : employees) {
                Schedule schedule = new Schedule();
                schedule.setWorkDate(date);
                schedule.setWorkWeek(weekIndex);

                schedule = assembleScheduleFromClass(schedule, targetClass);
                schedule = setAttendOffTimeByString(schedule);

                schedule.setUserId(employee.getUserId());
                schedule.setUserName(employee.getUserName());
                schedule.setPostCode(postCode);
                schedule.setClassType(ClassType.PEACETIME.key);
                schedule.setCommunityId(employee.getCommunityId());
                schedule.setCreateAt(new Date());
                schedule.setUpdateAt(schedule.getCreateAt());
                schedule.setDataStatus(DataStatusType.VALID.KEY);

                schedules.add(schedule);
            }
        }

        insertSchedules(schedules);

        return schedules;
    }

    private void insertSchedules(List<Schedule> schedules) {
        scheduleRepository.insertAll(schedules);
    }

    @Override
    public void deleteSchedules(ScheduleRequest request) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        Schedule toDelete = new Schedule();
        toDelete.setDataStatus(DataStatusType.INVALID.KEY);
        toDelete.setUpdateAt(new Date());
        scheduleRepository.updateByCommunityIdAndPostCodeAndWorkDateGreaterThanEqualAndWorkDateLessThanEqual(
                toDelete, request.getCommunityId(), request.getPostCode(), startDate, endDate);
    }

    @Override
    public List<Schedule> getSchedules(ScheduleRequest request) {
        Date dutyTime = request.getDutyTime();
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        if (dutyTime != null) {
            startDate = DateUtils.getFirstDateOfMonth(dutyTime);
            endDate = DateUtils.getLastDateOfMonth(dutyTime);
        } else {
            // 设置当月第一天及最后一天
            if(startDate == null && endDate == null){
                startDate = DateUtils.getFirstDateOfMonth(new Date());
                endDate = DateUtils.getLastDateOfMonth(new Date());
            }else{
                if(startDate == null && endDate != null){
                    startDate = DateUtils.getFirstDateOfMonth(endDate);
                }else{
                    if(startDate != null && endDate == null){
                        endDate = DateUtils.getLastDateOfMonth(startDate);
                    }
                }
            }
        }

        return scheduleRepository.findByCommunityIdAndUserIdAndPostCodeAndDataStatusAndWorkDateGreaterThanEqualAndWorkDateLessThanEqualAndAttendTimeLessThanEqualAndOffTimeGreaterThanEqualAllIgnoreNull(
                request.getCommunityId(), request.getUserId(), request.getPostCode(), DataStatusType.VALID.KEY,
                startDate, endDate, dutyTime, dutyTime);
    }

    @Override
    public Page<Schedule> getSchedules(ScheduleRequest request, int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size,
                new Sort(Sort.Direction.DESC, "workDate").and(new Sort(Sort.Direction.DESC, "_id")));
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        // 设置当月第一天及最后一天
        if(startDate == null && endDate == null){
            startDate = DateUtils.getFirstDateOfMonth(new Date());
            endDate = DateUtils.getLastDateOfMonth(new Date());
        }else{
            if(startDate == null && endDate != null){
                startDate = DateUtils.getFirstDateOfMonth(endDate);
            }else{
                if(startDate != null && endDate == null){
                    endDate = DateUtils.getLastDateOfMonth(startDate);
                }
            }
        }
        org.springframework.data.domain.Page<Schedule> pageList = scheduleRepository
                .findByCommunityIdAndPostCodeIgnoreNullAndUserIdIgnoreNullAndDataStatusAndWorkDateGreaterThanEqualAndWorkDateLessThanEqual(
                        request.getCommunityId(), request.getPostCode(),request.getUserId(), DataStatusType.VALID.KEY,
                        startDate, endDate, pageable);
        return PageUtils.getPage(pageList);
    }

    @Override
    public Schedule addSchedule(Schedule schedule, ObjectId userId) {
        schedule.setWorkWeek(DateUtils.getWeekIndex(schedule.getWorkDate()));
        schedule = setAttendOffTimeByString(schedule);
        schedule.setCreatorId(userId);
        schedule.setCreateAt(new Date());
        schedule.setDataStatus(DataStatusType.VALID.KEY);
        return scheduleRepository.insert(schedule);
    }

    @Override
    public boolean changeDataStatus(ObjectId scheduleId) {
        Schedule toUpdate = new Schedule();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate = scheduleRepository.updateById(toUpdate, scheduleId);
        return toUpdate != null;
    }

    @Override
    public void changeDataStatusByIds(Set<ObjectId> ids){
        Schedule toUpdate = new Schedule();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        scheduleRepository.updateByIdIn(toUpdate, ids);
    }

    @Override
    public Schedule findOne(ObjectId id) {
        return scheduleRepository.findOne(id);
    }

    @Override
    public Schedule updateSchedule(Schedule schedule) {
        schedule = setAttendOffTimeByString(schedule);

        schedule.setUserId(null);
        schedule.setUserName(null);
        schedule.setEmployeeId(null);
        schedule.setPostCode(null);
        schedule.setClassType(null);
        schedule.setWorkDate(null);
        schedule.setWorkWeek(null);
        schedule.setCommunityId(null);
        schedule.setDataStatus(null);

        schedule.setUpdateAt(new Date());
        schedule = scheduleRepository.updateOne(schedule, "attendPlace",
                "attendTime","task","offPlace","offTime","remark","attendTimeStr","offTimeStr");
        return schedule;
    }

    /**
     * 查询当值的保安列表
     *
     * @param communityId
     * @return
     */
    @Override
    public List<Schedule> findDutySecurityByCommunityId(ObjectId communityId) {
        Date now =new Date();
        return scheduleRepository.findByCommunityIdAndPostCodeAndDataStatusAndAttendTimeLessThanEqualAndOffTimeGreaterThanEqual(
                communityId, RoleType.SECURITY.name(), DataStatusType.VALID.KEY, now, now);
    }

    private Schedule setAttendOffTimeByString(Schedule schedule){
        if (StringUtil.isNotBlank(schedule.getAttendTimeStr()) && StringUtil.isNotBlank(schedule.getOffTimeStr())) {
            schedule.setAttendTime(DateUtils.addByString(schedule.getWorkDate(), schedule.getAttendTimeStr(), "HH:mm"));
            schedule.setOffTime(DateUtils.addByString(schedule.getWorkDate(), schedule.getOffTimeStr(), "HH:mm"));
            // 修正跨天
            if (!schedule.getAttendTime().before(schedule.getOffTime())) {
                schedule.setOffTime(DateUtils.addDay(schedule.getOffTime(), 1));
            }
            schedule.setRemark(String.format("%s~%s", schedule.getAttendTimeStr(), schedule.getOffTimeStr()));
        }else {
            schedule.setRemark(null);
        }
        return schedule;
    }

    private Schedule assembleScheduleFromClass(Schedule schedule, Class targetClass){
        schedule.setClassId(targetClass.getId());
        schedule.setClassName(targetClass.getName());
        // 上班信息
        schedule.setAttendPlace(targetClass.getAttendPlace());
        schedule.setOffPlace(targetClass.getOffPlace());
        schedule.setTask(targetClass.getTask());
        schedule.setAttendTimeStr(targetClass.getAttendTime());
        schedule.setOffTimeStr(targetClass.getOffTime());
        schedule.setWorkHours(targetClass.getWorkHours());

        return schedule;
    }
}
