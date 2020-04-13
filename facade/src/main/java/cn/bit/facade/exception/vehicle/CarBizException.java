package cn.bit.facade.exception.vehicle;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CarBizException extends BizException
{

    public static final CarBizException CAR_AUDITED = new CarBizException(7010001, "该车牌已审核成功, 无法再次审核");

    public static final CarBizException CARNO_APPLIED = new CarBizException(7010002, "该车牌已被申请");

    public static final CarBizException CARNO_APPLIED_OR_REAPPLIED = new CarBizException(7010003, "该车牌已被他人申请或者重复申请同一个车牌");

    public static final CarBizException CAR_ID_NULL = new CarBizException(7010004, "车牌记录ID为空");

    public CarBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public CarBizException(int code, String msg) {
        super(code, msg);
    }

    public CarBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public CarBizException newInstance(String msgFormat, Object... args) {
        return new CarBizException(this.code, msgFormat, args);
    }

    public CarBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new CarBizException(this.code, this.msg);
    }
}
