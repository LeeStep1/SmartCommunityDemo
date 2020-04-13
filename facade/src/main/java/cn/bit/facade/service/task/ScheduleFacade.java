package cn.bit.facade.service.task;

import cn.bit.facade.model.task.Class;
import cn.bit.facade.model.task.Schedule;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.facade.vo.task.ScheduleRequest;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ScheduleFacade {

    void deleteSchedules(ScheduleRequest scheduleRequest);

    /**
     * 自动生成班表
     * @param employees 员工集合
     * @param classes 班次信息
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param postCode
     * @return
     */
    List<Schedule> generateShiftSchedule(List<UserToProperty> employees, List<Class> classes, Date startDate, Date endDate, String postCode);

    List<Schedule> generatePeacetimeSchedule(List<UserToProperty> employees, List<Class> classes, Date startDate, Date endDate, String postCode);

    List<Schedule> getSchedules(ScheduleRequest scheduleRequest);

    Page<Schedule> getSchedules(ScheduleRequest scheduleRequest, int page, int size);

    Schedule addSchedule(Schedule entity, ObjectId userId);

    boolean changeDataStatus(ObjectId scheduleId);

    void changeDataStatusByIds(Set<ObjectId> ids);

    Schedule findOne(ObjectId id);

    Schedule updateSchedule(Schedule item);

    /**
     * 查询当值的保安列表
     * @param id
     * @return
     */
    List<Schedule> findDutySecurityByCommunityId(ObjectId id);
}
