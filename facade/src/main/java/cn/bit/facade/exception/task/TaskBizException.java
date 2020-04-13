package cn.bit.facade.exception.task;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskBizException extends BizException
{

    public static final TaskBizException STARTDATE_AFTER_ENDDATE = new TaskBizException(6010001, "开始时间不能晚于结束时");

    public static final TaskBizException DATE_INTERVAL_EXCEEDED = new TaskBizException(6010002, "时间跨度不能超过一个月");

    public static final TaskBizException POST_CODE_NULL = new TaskBizException(6010003, "岗位码不能为空");

    public static final TaskBizException UNARRANGED_EMPLOYEE = new TaskBizException(6010004, "该岗位尚未安排工作人员");

    public static final TaskBizException UNARRANGED_CLASS = new TaskBizException(6010005, "该岗位尚未安排班次信息");

    public static final TaskBizException CLASS_ALREADY_EXIST = new TaskBizException(6010006, "该岗位已存在同名班次");

    public static final TaskBizException CLASS_EMPLOYEE_NOT_MATCH = new TaskBizException(6010007, "工作人员数与班次需求不匹配，请设置班次人数");

    public static final TaskBizException CLASS_PEACETIME_ERROR = new TaskBizException(6010008, "该岗位的常班数不正确，请查看班次");

    public static final TaskBizException CLASS_PEACETIME_ALREADY_EXIST = new TaskBizException(6010009, "该岗位已存在常班");

    public TaskBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public TaskBizException(int code, String msg) {
        super(code, msg);
    }

    public TaskBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public TaskBizException newInstance(String msgFormat, Object... args) {
        return new TaskBizException(this.code, msgFormat, args);
    }

    public TaskBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new TaskBizException(this.code, this.msg);
    }
}
