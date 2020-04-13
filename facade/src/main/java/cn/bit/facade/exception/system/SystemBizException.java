package cn.bit.facade.exception.system;

import cn.bit.facade.exception.property.PropertyBizException;
import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by fxiao
 * on 2018/3/2
 */
@Slf4j
public class SystemBizException extends BizException {

    public static final SystemBizException HTTP_HEADER_NULL = new SystemBizException(4010005, "请求头参数为空");

    public static final SystemBizException THIRD_TOKEN_ERROR = new SystemBizException(4010006, "Authorization不正确");

    public static final SystemBizException TIMESTAMP_FORMAT_ERROR = new SystemBizException(4010007, "时间戳格式不正确");

    public static final SystemBizException SESSION_OUTTIME = new SystemBizException(4010008, "请求超时");

    public static final SystemBizException APPID_ERROR = new SystemBizException(4010009, "appid错误");

    public SystemBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public SystemBizException(int code, String msg) {
        super(code, msg);
    }

    public SystemBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public SystemBizException newInstance(String msgFormat, Object... args) {
        return new SystemBizException(this.code, msgFormat, args);
    }

    public PropertyBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new PropertyBizException(this.code, this.msg);
    }

}
