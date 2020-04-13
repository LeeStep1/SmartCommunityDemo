package cn.bit.facade.exception.trade;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeException extends BizException {

    public static final TradeException UNSUPPORTED_PLATFORM = new TradeException(1010001, "不支持的平台类型");

    public static final TradeException ACCOUNT_NOT_EXISTS = new TradeException(1010002, "账号不存在");

    public static final TradeException INVALID_ALIPAY_PUBLIC_KEY = new TradeException(1010003, "无效的支付宝公钥");

    public static final TradeException INVALID_SIGN = new TradeException(1010004, "无效的签名");

    public static final TradeException INVALID_WECHAT_PAY_NOTIFICATION = new TradeException(1010005, "无效的微信支付回调");

    public static final TradeException INVALID_TRADE = new TradeException(1010006, "无效的交易");

    public static final TradeException TRADE_NOT_EXISTS = new TradeException(1010007, "交易单不存在");

    public static final TradeException TOTAL_AMOUNT_NOT_MATCH = new TradeException(1010008, "金额不一致");

    public static final TradeException MISSING_TRADE_ACCOUNT = new TradeException(1010009, "丢失交易账户信息");

    public static final TradeException TRADE_ALREADY_DONE = new TradeException(1010010, "交易单已完成");

    public static final TradeException UNSUPPORTED_TRADE_TYPE = new TradeException(1010011, "不支持的交易类型");

    public static final TradeException INVALID_ALIPAY_PAY_NOTIFICATION = new TradeException(1010012, "无效的支付宝支付回调");

    public TradeException(Integer code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public TradeException(Integer code, String msgFormat) {
        super(code, msgFormat);
    }

    public TradeException() {
        super();
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public TradeException newInstance(String msgFormat, Object... args) {
        return new TradeException(this.code, msgFormat, args);
    }

    public TradeException print() {
        log.info(" ==> TradeException, code:" + this.code + ", msg:" + this.msg);
        return new TradeException(this.code, this.msg);
    }
}
