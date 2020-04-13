package cn.bit.facade.exception.fees;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeesBizException extends BizException
{

    public static final FeesBizException FEES_ITEM_NAME_NULL = new FeesBizException(1410001, "收费项目名称不能修改为空");

    public static final FeesBizException FEES_ITEM_NAME_EXISTS = new FeesBizException(1410002, "收费项目已存在");

    public static final FeesBizException FEES_ITEM_NULL = new FeesBizException(1410003, "收费项目不存在");

    public static final FeesBizException FEES_ITEM_DELETE_FAILURE = new FeesBizException(1410004, "收费项目删除失败");

    public static final FeesBizException FEES_RULE_NULL = new FeesBizException(1410005, "收费规则不存在");

    public static final FeesBizException FEES_BILL_NOT_EXISTS = new FeesBizException(1410006, "账单不存在");

    public static final FeesBizException FEES_BILL_ALREADY_PAID = new FeesBizException(1410007, "账单已缴费");

    public static final FeesBizException FEES_BILL_PRICE_NOT_MATCH = new FeesBizException(1410008, "账单金额不一致");

    public static final FeesBizException FEES_BILL_ITEM_NULL = new FeesBizException(1410009, "至少要包含一个收费项目");

    public static final FeesBizException FEES_BILL_STATUS_NOT_ALLOW_MODIFY = new FeesBizException(1410010, "当前状态不允许修改账单信息");

    public static final FeesBizException FEES_BILL_STATUS_NOT_ALLOW_DELETE = new FeesBizException(1410011, "当前状态不允许删除账单");

    public static final FeesBizException FEES_BILL_ALREADY_NOTIFICATION = new FeesBizException(1410012, "已经发过业主缴费通知");

    public static final FeesBizException FEES_BILL_NOT_ALLOW_EXPEDITING = new FeesBizException(1410013, "账单未达到催缴条件");

    public static final FeesBizException FEES_TEMPLATE_NOT_EXISTS = new FeesBizException(1410014, "收费套餐不存在");

    public FeesBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public FeesBizException(int code, String msg) {
        super(code, msg);
    }

    public FeesBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public FeesBizException newInstance(String msgFormat, Object... args) {
        return new FeesBizException(this.code, msgFormat, args);
    }

    public FeesBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new FeesBizException(this.code, this.msg);
    }
}
