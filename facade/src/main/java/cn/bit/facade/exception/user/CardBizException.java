package cn.bit.facade.exception.user;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by xiaoxi.lao on 2018/4/20.
 */
@Slf4j
public class CardBizException extends BizException {

    public static final CardBizException USER_PHONE_MAC_NULL = new CardBizException(1210001, "该用户在当前社区没有手机蓝牙卡");

    public static final CardBizException CARD_EXIST = new CardBizException(1210002, "一卡通已存在");

    public static final CardBizException CARD_NOT_EXIST = new CardBizException(1210003, "一卡通不存在");

    public static final CardBizException CARD_INFO_LACK = new CardBizException(1210004, "卡片信息不完整");

    public static final CardBizException CARD_KEYTYPE_EXIST = new CardBizException(1210005, "当前物业人员在该社区已有该类型卡片");

    public static final CardBizException QR_CARD_ROOMS_NULL = new CardBizException(1210005, "二维码申请需要指定特定的房间");

    public static final CardBizException QR_CARD_EXIST = new CardBizException(1210005, "该房间已有未过期访客通行证");

    public static final CardBizException PHONE_MAC_CAN_NOT_APPLY = new CardBizException(1210006, "虚拟卡只能由后台生成");

    public static final CardBizException KEYNO_NOT_NULL = new CardBizException(1210007, "卡号不能为空");

    public static final CardBizException IC_CARD_EXIST = new CardBizException(1210008, "卡号重复");

    public static final CardBizException DTU_NET_EXCEPTION = new CardBizException(1210009, "查询DTU网络异常");

    public static final CardBizException KEYNO_ILLEGAL = new CardBizException(1210010, "输入卡号不合法");

    public CardBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public CardBizException(int code, String msg) {
        super(code, msg);
    }

    public CardBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public CardBizException newInstance(String msgFormat, Object... args) {
        return new CardBizException(this.code, msgFormat, args);
    }

    public CardBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new CardBizException(this.code, this.msg);
    }
}
