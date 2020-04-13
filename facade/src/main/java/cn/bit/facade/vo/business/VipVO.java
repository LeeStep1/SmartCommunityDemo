package cn.bit.facade.vo.business;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by fxiao
 * on 2018/4/9
 */
@Data
public class VipVO implements Serializable {
    /**
     * 商家信息
     */
    private ShopItem shop;
    /**
     * 优惠券信息
     */
    private CouponItem coupon;
}
