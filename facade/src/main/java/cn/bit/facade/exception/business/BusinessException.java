package cn.bit.facade.exception.business;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by fxiao
 * on 2018/4/2
 */
@Slf4j
public class BusinessException extends BizException {

    public static final BusinessException BIZ_SLIDE_NULL = new BusinessException(1310010, "轮播图信息不存在");

    public static final BusinessException BIZ_SLIDE_ID_NULL = new BusinessException(1310011, "轮播图ID为空");

    public static final BusinessException BIZ_SHOP_ID_NULL = new BusinessException(1310012, "商家ID为空");

    public static final BusinessException BIZ_SHOP_NULL = new BusinessException(1310013, "商家信息不存在");

    public static final BusinessException BIZ_COUPON_ID_NULL = new BusinessException(1310014, "优惠券ID为空");

    public static final BusinessException BIZ_COUPON_NULL = new BusinessException(1310015, "该优惠券不存在");

    public static final BusinessException BIZ_ID_NULL = new BusinessException(1310016, "优惠券ID为空");

    public static final BusinessException BIZ_USERID_NULL = new BusinessException(1310017, "用户ID为空");

    public static final BusinessException BIZ_COMMUNITYID_NULL = new BusinessException(1310018, "社区ID为空");

    public static final BusinessException BIZ_COUPON_EXIST = new BusinessException(1310019, "已获取该优惠券");

    public static final BusinessException BIZ_COUPON_FULL = new BusinessException(1310020, "优惠券已领完");

    public static final BusinessException BIZ_SLIDE_REJECT_EDIT = new BusinessException(1310021, "轮播图处于发布状态，不能修改");

    public static final BusinessException BIZ_SLIDE_MAX_NUM = new BusinessException(1310022, "轮播图最多能发布5个");

    public static final BusinessException BIZ_GOODS_NULL = new BusinessException(1310023, "商品信息不存在");

    public static final BusinessException BIZ_GOODS_ID_NULL = new BusinessException(1310024, "商品ID为空");

    public static final BusinessException BIZ_COUPON_INVALID_USE_DATE = new BusinessException(1310025, "使用结束时间不能小于当前时间");

    public static final BusinessException BIZ_COUPON_NUM_LESS_THAN_BEFORE = new BusinessException(1310026, "更变的优惠券数量不能小于之前的数量");

    public static final BusinessException BIZ_COUPON_DATE_LESS_THAN_BEFORE = new BusinessException(1310027, "变更时间不能小于先前时间");

    public static final BusinessException BIZ_COUPON_VALID_CODE_NULL = new BusinessException(1310028, "校验码不能为空");

    public static final BusinessException BIZ_COUPON_DATE_INVALID = new BusinessException(1310029, "请在有效的时间内使用");

    public static final BusinessException BIZ_COUPON_CODE_INVALID = new BusinessException(1310030, "校验码错误，请重新输入");

    public static final BusinessException BIZ_COUPON_USED = new BusinessException(1310031, "优惠券已使用");

    public static final BusinessException BIZ_COUPON_OUTDATED = new BusinessException(1310032, "优惠券已过期");

    public BusinessException(Integer code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public BusinessException(Integer code, String msgFormat) {
        super(code, msgFormat);
    }

    public BusinessException() {
        super();
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public BusinessException newInstance(String msgFormat, Object... args) {
        return new BusinessException(this.code, msgFormat, args);
    }

    public BusinessException print() {
        log.info(" ==> TradeException, code:" + this.code + ", msg:" + this.msg);
        return new BusinessException(this.code, this.msg);
    }
}
