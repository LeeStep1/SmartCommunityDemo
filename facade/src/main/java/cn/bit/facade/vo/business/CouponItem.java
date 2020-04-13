package cn.bit.facade.vo.business;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

@Data
public class CouponItem implements Serializable{

    private ObjectId id;

    private String name;

    private Integer shopType;

    private String shopName;

    private String icon;
    /**
     * 优惠券金额
     */
    private Double maxPrice;
    /**
     * 优惠券数量
     */
    private Integer amount;
    /**
     * 已被抢购优惠券数量
     */
    private Integer receiveNum;
    /**
     * 用户领券的关联表id
     */
    @Transient
    private ObjectId couponToUserId;
    /**
     * 使用状态（0：未使用；1：已使用；-1：已失效）
     */
    @Transient
    private Integer useStatus;

}
