package cn.bit.facade.vo.business;

import org.bson.types.ObjectId;

import java.io.Serializable;

public class CouponUseVO implements Serializable{

    private ObjectId couponToUserId;
    /**
     * 有效状态（0：未使用；1：已使用；-1：已失效）
     */
    private Integer useStatus;

    public ObjectId getCouponToUserId() {
        return couponToUserId;
    }

    public void setCouponToUserId(ObjectId couponToUserId) {
        this.couponToUserId = couponToUserId;
    }

    public Integer getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(Integer useStatus) {
        this.useStatus = useStatus;
    }
}
