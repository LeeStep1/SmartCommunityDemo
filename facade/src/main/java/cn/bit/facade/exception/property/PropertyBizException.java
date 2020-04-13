package cn.bit.facade.exception.property;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyBizException extends BizException
{
    public static final PropertyBizException NOT_RECEIVE_ALARM = new PropertyBizException(5010001, "该报警记录尚未接警, 请先接警！");

    public static final PropertyBizException ALARM_FINISH = new PropertyBizException(5010002, "该报警记录排查已完成");

    public static final PropertyBizException ALARM_HANDLED = new PropertyBizException(5010003, "该报警记录已被处理, 无法再次接警");

    public static final PropertyBizException FAULT_ID_NULL = new PropertyBizException(5010004, "故障ID为空");

    public static final PropertyBizException FLOW_POURED = new PropertyBizException(5010005, "故障流程没走完，不能删除");

    public static final PropertyBizException NO_UPDATE = new PropertyBizException(5010006, "单据已受理，不能修改");

    public static final PropertyBizException BILL_NOT_EXISTS = new PropertyBizException(501007, "账单不存在");

    public static final PropertyBizException BILL_ALREADY_PAID = new PropertyBizException(501008, "账单已缴费");

    public static final PropertyBizException BILL_PRICE_NOT_MATCH = new PropertyBizException(501009, "账单金额不一致");

    public static final PropertyBizException PLAY_TIME_NULL = new PropertyBizException(501010, "申请的开始时间或结束时间为空");

    public static final PropertyBizException BILL_STATUS_INVALID = new PropertyBizException(501011, "无效的账单状态");

    public static final PropertyBizException NO_BILL_NEED_TO_UPDATE = new PropertyBizException(501012, "没有账单需要更新");

    public static final PropertyBizException BILL_DETAIL_ID_IS_NULL = new PropertyBizException(501013, "子账单ID为空");

    public static final PropertyBizException BILL_DETAIL_FEES_IS_NULL = new PropertyBizException(501014, "子账单金额为空");

    public static final PropertyBizException BILL_ID_IS_NULL = new PropertyBizException(501015, "账单ID为空");

    public static final PropertyBizException RELEASE_BAR_NOT_EXISTS = new PropertyBizException(5010016, "当前社区不存在此放行条");

    public static final PropertyBizException FAULT_IS_NULL = new PropertyBizException(5010017, "故障单不存在");

    public static final PropertyBizException FAULT_OWNER_ERROR = new PropertyBizException(5010018, "非申请人，不能修改此单据");

    public static final PropertyBizException FAULT_HAVE_EVALUATION = new PropertyBizException(5010019, "已评价");

    public static final PropertyBizException FAULT_BEGIN_END_TIME = new PropertyBizException(5010020, "开始时间不能大于结束时间");

    public static final PropertyBizException FAULT_CANNOT_COMMENT = new PropertyBizException(5010021, "还未能评论");

    public static final PropertyBizException FAULT_ONLY_COMMENT_ONESELF = new PropertyBizException(5010022, "只能评论自己的单据");

    public static final PropertyBizException ALARM_NULL = new PropertyBizException(5010023, "报警记录不存在");

    public static final PropertyBizException NOTICE_NULL = new PropertyBizException(5010024, "公告不存在");

    public static final PropertyBizException FAULT_ELEVATOR_ID_NULL = new PropertyBizException(5010025, "电梯ID不能为空");

    public static final PropertyBizException FAULT_DOOR_ID_NULL = new PropertyBizException(5010026, "门禁ID不能为空");

    public static final PropertyBizException FAULT_FLOW_CHANGE = new PropertyBizException(5010027, "故障流程已更变，不能操作");

    public static final PropertyBizException FAULT_FLOW_STATUS = new PropertyBizException(5010028, "故障流程已更变，无需操作");

    public static final PropertyBizException FAULT_FLOW_INVALID = new PropertyBizException(5010029, "无效的流程操作");

    public static final PropertyBizException COMPANY_NOT_EXIST = new PropertyBizException(5010030, "物业公司不存在");

    public static final PropertyBizException COMPLAIN_ID_NULL = new PropertyBizException(5010031, "投诉工单ID为空");
    public static final PropertyBizException COMPLAIN_RESULT_NULL = new PropertyBizException(5010032, "处理结果为空");
    public static final PropertyBizException COMPLAIN_STATUS_INVALID = new PropertyBizException(5010033, "无效的工单状态");
    public static final PropertyBizException COMPLAIN_STATUS_CHANGED = new PropertyBizException(5010034, "工单状态已改变，请刷新后重试");
    public static final PropertyBizException COMPLAIN_NOT_FINISHED = new PropertyBizException(5010035, "工单未办结");

    public PropertyBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public PropertyBizException(int code, String msg) {
        super(code, msg);
    }

    public PropertyBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public PropertyBizException newInstance(String msgFormat, Object... args) {
        return new PropertyBizException(this.code, msgFormat, args);
    }

    public PropertyBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new PropertyBizException(this.code, this.msg);
    }
}
