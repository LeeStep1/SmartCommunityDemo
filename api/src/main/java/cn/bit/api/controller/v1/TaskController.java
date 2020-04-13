package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.company.dto.EmployeeDTO;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.facade.enums.ClassType;
import cn.bit.facade.enums.RoleType;
import cn.bit.facade.model.task.Class;
import cn.bit.facade.model.task.Record;
import cn.bit.facade.model.task.Schedule;
import cn.bit.facade.service.task.ClassFacade;
import cn.bit.facade.service.task.RecordFacade;
import cn.bit.facade.service.task.ScheduleFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.vo.ObjectIdsVO;
import cn.bit.facade.vo.task.GenerateRequest;
import cn.bit.facade.vo.task.RecordRequest;
import cn.bit.facade.vo.task.ScheduleRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.EmployeeRequest;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.CopyUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.task.TaskBizException.*;

@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class TaskController {

    @Autowired
    private RecordFacade recordFacade;

    @Autowired
    private ClassFacade classFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Resource
    private CompanyFacade companyFacade;
    // =================================================【record begin】================================================

    /**
     * 新增作业
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "新增作业打卡记录", path = "/task/record/add")
    @Authorization
    public ApiResult<Record> addRecord(@Validated(Record.Add.class) @RequestBody Record entity) {
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                SessionUtil.getTokenSubject().getUid(), entity.getCommunityId(), SessionUtil.getCompanyId());
        if (userToProperty == null) {
            return ApiResult.error(-1, "员工不存在，打卡失败");
        }
        entity.setUserId(userToProperty.getUserId());
        entity.setUserName(userToProperty.getUserName());
        entity.setCreatorId(SessionUtil.getTokenSubject().getUid());

        // 填充岗位
        if (userToProperty.getPostCode() != null) {
            for (String post : userToProperty.getPostCode()) {
                if (!EnumUtils.isValidEnum(RoleType.class, post)) {
                    entity.setPostCode(post);
                    break;
                }
            }
        }
        if (StringUtil.isBlank(entity.getPostCode())) {
            return ApiResult.error(-1, "岗位有误，请联系管理员");
        }

        Record record = recordFacade.addTaskRecord(entity);
        if (record != null) {
            return ApiResult.ok(record);
        }
        return ApiResult.error(-1, "打卡失败");
    }

    /**
     * 删除作业
     *
     * @param recordId
     * @return
     */
    @GetMapping(name = "删除作业打卡记录", path = "/task/record/{recordId}/delete")
    @Authorization
    public ApiResult<Record> deleteTaskRecord(@PathVariable ObjectId recordId) {
        boolean flag = recordFacade.changeDataStatus(recordId);
        if (flag) {
            return ApiResult.ok("删除成功");
        }
        return ApiResult.error(-1, "删除失败");
    }

    /**
     * 获取作业信息
     *
     * @param recordId
     * @return
     */
    @GetMapping(name = "作业打卡记录详情", path = "/task/record/{recordId}/detail")
    @Authorization
    public ApiResult<Record> getTaskRecord(@PathVariable("recordId") ObjectId recordId) {
        Record entity = recordFacade.findOne(recordId);
        if (entity != null) {
            return ApiResult.ok(entity);
        }
        return ApiResult.error(-1, "没有该作业信息");
    }

    /**
     * 分页
     *
     * @param request
     * @param page
     * @return
     */
    @PostMapping(name = "作业打卡记录分页", path = "/task/record/page")
    @Authorization
    public ApiResult queryTaskRecordPage(@RequestBody RecordRequest request,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        request.setCommunityId(SessionUtil.getCommunityId());
        request.setUserIds(request.getUserId() == null ? null : Collections.singleton(request.getUserId()));
        if (request.getUserId() == null && StringUtil.isNotBlank(request.getRole())) {
            // TODO 如果员工列表是无限大的，需要分页轮循处理
            List<EmployeeDTO> dtoList = companyFacade.listEmployeesByCompanyIdAndRoles(
                    SessionUtil.getCompanyId(), Collections.singleton(request.getRole()));
            if (!dtoList.isEmpty()) {
                request.setUserIds(dtoList.stream().map(EmployeeDTO::getUserId).collect(Collectors.toSet()));
            }
        }
        Page<Record> list = recordFacade.getRecords(request, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 获取作业列表
     *
     * @param request
     * @return
     */
    @PostMapping(name = "作业打卡记录列表", path = "/task/record/list")
    @Authorization
    public ApiResult getList(@RequestBody RecordRequest request) {
        request.setCommunityId(SessionUtil.getCommunityId());
        request.setUserIds(request.getUserId() == null ? null : Collections.singleton(request.getUserId()));
        if (request.getUserId() == null && StringUtil.isNotBlank(request.getRole())) {
            // TODO 如果员工列表是无限大的，需要分页轮循处理
            List<EmployeeDTO> dtoList = companyFacade.listEmployeesByCompanyIdAndRoles(
                    SessionUtil.getCompanyId(), Collections.singleton(request.getRole()));
            if (!dtoList.isEmpty()) {
                request.setUserIds(dtoList.stream().map(EmployeeDTO::getUserId).collect(Collectors.toSet()));
            }
        }
        List<Record> list = recordFacade.getRecords(request);
        return ApiResult.ok(list);
    }

    // =================================================【record end】==================================================

    // =================================================【class start】=================================================

    /**
     * 新增班次
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "新增班次", path = "/task/class/add")
    @Authorization
    public ApiResult<Class> addClass(@Validated(Class.Add.class) @RequestBody Class entity) {
        if (entity.getType() == ClassType.SHIFT.key) {
            if (entity.getShiftOrder() == null || entity.getNumber() == null) {
                return ApiResult.error(-1, "轮班顺序和值班人数不能为空");
            }
        }

        UserVO vo = SessionUtil.getCurrentUser();
        entity.setCreatorId(vo == null ? null : vo.getId());
        Class added = classFacade.addTaskClass(entity);
        return added != null ? ApiResult.ok(added) : ApiResult.error(-1, "添加失败");
    }

    /**
     * 删除班次
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除班次", path = "/task/class/{id}/delete")
    @Authorization
    public ApiResult<Class> deleteTaskClass(@PathVariable ObjectId id) {
        boolean flag = classFacade.changeDataStatus(id);
        return flag ? ApiResult.ok("删除成功") : ApiResult.error(-1, "删除失败");
    }

    /**
     * 获取班次信息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "班次详情", path = "/task/class/{id}/detail")
    @Authorization
    public ApiResult<Record> getTaskClass(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(classFacade.findOne(id));
    }

    /**
     * 分页获取班次列表(h5)
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "班次分页", path = "/task/class/page")
    @Authorization
    public ApiResult getList(@RequestBody @Validated(Class.Query.class) Class entity,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        return ApiResult.ok(classFacade.queryPage(entity, page, size));
    }

    /**
     * 修改班次
     *
     * @param entity
     * @return
     **/
    @PostMapping(name = "编辑班次", path = "/task/class/edit")
    @Authorization
    public ApiResult<Class> updateClass(@RequestBody @Validated(Class.Update.class) Class entity) {
        Class updated = classFacade.updateClass(entity);
        return updated != null ? ApiResult.ok(updated) : ApiResult.error(-1, "修改该班次信息失败");
    }
    // =================================================【class end】===================================================

    // =================================================【schedule start】==============================================

    /**
     * 物业自动排班
     *
     * @param gr
     * @return
     */
    @PostMapping(name = "物业自动排班", path = "/task/schedule/generate")
    @Authorization
    public ApiResult<List<Schedule>> generateSchedule(@Validated @RequestBody GenerateRequest gr) {
        Date startDate = gr.getStartDate();
        Date endDate = gr.getEndDate();

        if (DateUtils.compareDate(startDate, new Date(), Calendar.DATE) <= 0) {
            return ApiResult.error(-1, "只能编排明天以后的班表");
        }
        if (DateUtils.compareDate(startDate, endDate, Calendar.DATE) > 0) {
            throw STARTDATE_AFTER_ENDDATE;
        }
        if (DateUtils.getDateDiff(startDate, endDate) > 31) {
            throw DATE_INTERVAL_EXCEEDED;
        }

        EmployeeRequest request = new EmployeeRequest();
        request.setCommunityId(gr.getCommunityId());
        request.setCompanyId(SessionUtil.getCompanyId());
        request.setPartner(SessionUtil.getAppSubject().getPartner());
        request.setRoles(Collections.singleton(gr.getPostCode()));
        List<UserToProperty> employees = userToPropertyFacade.listEmployees(request);

        // 获取班次
        Class toGet = new Class();
        toGet.setCommunityId(gr.getCommunityId());
        toGet.setPostCode(gr.getPostCode());
        toGet.setType(gr.getClassType());
        List<Class> classes = classFacade.queryListByCommunityIdAndPostCodeAndType(
                gr.getCommunityId(), gr.getPostCode(), gr.getClassType());

        if (employees == null || employees.size() == 0) {
            throw UNARRANGED_EMPLOYEE;
        }
        if (classes == null || classes.size() == 0) {
            throw UNARRANGED_CLASS;
        }

        ScheduleRequest sr = new ScheduleRequest();
        CopyUtils.copy(sr, gr, true);
        // 先删除当前岗位所在日期的排班
        scheduleFacade.deleteSchedules(sr);

        List<Schedule> schedules;
        if (gr.getClassType() == ClassType.SHIFT.key) {
            schedules = scheduleFacade.generateShiftSchedule(employees, classes, startDate, endDate, gr.getPostCode());
        } else {
            schedules = scheduleFacade.generatePeacetimeSchedule(employees, classes, startDate, endDate, gr.getPostCode());
        }

        return ApiResult.ok(schedules);
    }

    @PostMapping(name = "工作排班分页", path = "/task/schedule/page")
    @Authorization
    public ApiResult getSchedules(@Validated @RequestBody ScheduleRequest scheduleRequest,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return ApiResult.ok(scheduleFacade.getSchedules(scheduleRequest, page, size));
    }

    @PostMapping(name = "工作排班列表", path = "/task/schedule/list")
    @Authorization
    public ApiResult getSchedules(@RequestBody ScheduleRequest scheduleRequest) {
        scheduleRequest.setCommunityId(SessionUtil.getCommunityId());
        return ApiResult.ok(scheduleFacade.getSchedules(scheduleRequest));
    }

    @PostMapping(name = "当值员工工作排班列表", path = "/task/schedule/current-duty/list")
    public ApiResult getCurrentDutyEmployees(@RequestBody ScheduleRequest scheduleRequest) {
        scheduleRequest.setCommunityId(SessionUtil.getCommunityId());
        if (scheduleRequest.getDutyTime() == null) {
            scheduleRequest.setDutyTime(new Date());
        }
        return ApiResult.ok(scheduleFacade.getSchedules(scheduleRequest));
    }

    /**
     * 新增排班
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "新增排班", path = "/task/schedule/add")
    @Authorization
    public ApiResult<Schedule> addSchedule(@Validated(Schedule.Add.class) @RequestBody Schedule entity) {
        if (DateUtils.compareDate(entity.getWorkDate(), new Date(), Calendar.DATE) < 0) {
            return ApiResult.error(-1, "不可添加历史班表");
        }

        UserVO vo = SessionUtil.getCurrentUser();
        // 功能权限配置，无须校验用户权限
        /*UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                entity.getUserId(), entity.getCommunityId(), entity.getPostCode());
        if(userToProperty == null){
            throw PROPERTY_NOT_EXIST;
        }*/
        entity.setUserName(vo.getNickName());
        Schedule added = scheduleFacade.addSchedule(entity, vo == null ? null : vo.getId());
        return added != null ? ApiResult.ok(added) : ApiResult.error(-1, "添加失败");
    }

    /**
     * 删除排班
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除排班", path = "/task/schedule/{id}/delete")
    @Authorization
    public ApiResult<Schedule> deleteSchedule(@PathVariable("id") ObjectId id) {
        boolean flag = scheduleFacade.changeDataStatus(id);
        return flag ? ApiResult.ok("删除成功") : ApiResult.error(-1, "删除失败");
    }

    /**
     * 批量删除排班
     *
     * @param vo
     * @return
     */
    @PostMapping(name = "批量删除排班", path = "/task/schedule/batch-delete")
    @Authorization
    public ApiResult<String> batchDeleteSchedule(@RequestBody ObjectIdsVO vo) {
        scheduleFacade.changeDataStatusByIds(vo.getIds());
        return ApiResult.ok("删除成功");
    }

    /**
     * 获取单条排班信息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "排班详情", path = "/task/schedule/{id}/detail")
    @Authorization
    public ApiResult<Schedule> getSchedule(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(scheduleFacade.findOne(id));
    }

    /**
     * 修改/编辑排班
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "编辑排班", path = "/task/schedule/edit")
    @Authorization
    public ApiResult<Schedule> updateSchedule(@RequestBody @Validated(Schedule.Update.class) Schedule entity) {
        Schedule updated = scheduleFacade.updateSchedule(entity);
        return updated != null ? ApiResult.ok(updated) : ApiResult.error(-1, "修改失败");
    }

    // =================================================【schedule end】================================================
}
