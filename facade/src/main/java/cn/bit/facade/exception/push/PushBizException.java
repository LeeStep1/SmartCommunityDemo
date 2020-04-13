package cn.bit.facade.exception.push;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushBizException extends BizException {

    public static final PushBizException PUSH_POINT_INVALID = new PushBizException(10010001, "无效的功能节点");

    public static final PushBizException PUSH_CONFIG_NOT_EXISTS = new PushBizException(10010002, "功能节点推送配置不存在");

    public static final PushBizException PUSH_CONFIG_EXISTS = new PushBizException(10010003, "功能节点推送配置已存在");


    public PushBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public PushBizException(int code, String msg) {
        super(code, msg);
    }

    public PushBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public PushBizException newInstance(String msgFormat, Object... args) {
        return new PushBizException(this.code, msgFormat, args);
    }

    public PushBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new PushBizException(this.code, this.msg);
    }
}
