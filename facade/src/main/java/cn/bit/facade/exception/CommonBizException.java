package cn.bit.facade.exception;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务公共异常类
 */

@Slf4j
public class CommonBizException extends BizException {

    public static final CommonBizException UNKNOWN_ERROR = new CommonBizException(10001, "服务暂不可用，请稍后重试");

    public static final CommonBizException AUTHENCATION_FAILD = new CommonBizException(10002, "无权限操作");

    /**
     * 签名无效
     */
    public static final CommonBizException SIGN_NAME_INVALID = new CommonBizException(10003, "无效的签名");

    /**
     * 应用名称无效
     */
    public static final CommonBizException APP_NAME_INVALID = new CommonBizException(10004, "无效的应用名称");

    public static final CommonBizException DATA_INVALID = new CommonBizException(10005, "数据已失效，请刷新重试");

    public CommonBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public CommonBizException(int code, String msg) {
        super(code, msg);
    }

    public CommonBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public CommonBizException newInstance(String msgFormat, Object... args) {
        return new CommonBizException(this.code, msgFormat, args);
    }

    public CommonBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new CommonBizException(this.code, this.msg);
    }
}
