package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by fxiao
 * on 2018/4/2
 * 优惠券、用户关联表
 */
@Data
@Document(collection = "BIZ_COUPONTOUSER")
@CompoundIndex(def = "{'userId':1,'validityEndAt':-1}", background = true, name = "appCouponToUser")
public class CouponToUser extends BaseInfo {
    @Id
    private ObjectId id;
    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空", groups = Add.class)
    private ObjectId couponId;
    /**
     * 商家ID
     */
    @Indexed(background = true)
    private ObjectId shopId;
    /**
     * 商家名称
     */
    private String shopName;
    /**
     * 名称
     */
    private String name;
    /**
     * 商家校验码（由后台生成）
     */
    private String validCode;
    /**
     * 优惠券金额
     */
    private Double maxPrice;
    /**
     * 地址
     */
    private String address;
    /**
     * 联系电话
     */
    private String telPhone;
    /**
     * 领取限制
     */
    private String couponLimit;
    /**
     * 使用须知
     */
    private String prompt;
    /**
     * 用户ID
     */
    @Indexed(background = true)
    private ObjectId userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 有效状态（0：未使用；1：已使用； -1：已失效）
     */
    private Integer useStatus;
    /**
     * 开始时间
     */
    private Date validityBeginAt;
    /**
     * 结束时间
     */
    private Date validityEndAt;

    /**
     * 创建人ID
     */
    private ObjectId createId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 数据状态
     */
    private Integer dataStatus;

    public interface Add{}
}
