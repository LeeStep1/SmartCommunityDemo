package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * Created by fxiao
 * on 2018/4/2
 * 优惠券
 */
@Data
@Document(collection = "BIZ_COUPON")
@CompoundIndex(def = "{'shopId':1,'validityEndAt':1,'maxPrice':-1,'communityIds':1}", background = true, name = "appCoupon")
public class Coupon extends BaseInfo{
    @Id
    @NotNull(message = "优惠券ID不能为空", groups = {Update.class, Search.class})
    private ObjectId id;
    /**
     * 名称
     */
    @NotNull(message = "优惠券名称不能为空", groups = {Add.class, Update.class})
    @Length(max = 30, message = "优惠券名称不能超过30个字")
    private String name;
    /**
     * 商家类型
     */
    private Integer shopType;
    /**
     * 图标
     */
    private String icon;
    /**
     * 优惠券金额
     */
    @Min(value = 0, message = "金额不能为负数")
    @Max(value = 5000, message = "金额不能超5000")
    private Double maxPrice;
    /**
     * 优惠券数量
     */
    @Min(value = 1, message = "优惠券数量不能小于1")
    @Max(value = 10000, message = "优惠券数量不能大于10000")
    private Integer amount;
    /**
     * 已被抢购优惠券数量
     */
    private Integer receiveNum;
    /**
     * 已使用的数量
     */
    private Integer useNum;
    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空", groups = {Add.class, Update.class})
    private ObjectId shopId;
    /**
     * 商家名称
     */
    private String shopName;
    /**
     * 地址
     */
    private String address;
    /**
     * 联系电话
     */
    private String telPhone;
    /**
     * 开始时间
     */
    @NotNull(message = "使用开始时间不能为空", groups = {Add.class, Update.class})
    private Date validityBeginAt;
    /**
     * 结束时间
     */
    @NotNull(message = "使用结束时间不能为空", groups = {Add.class, Update.class})
    private Date validityEndAt;
    /**
     * 商家校验码（由后台生成）
     */
    private String validCode;
    /**
     * 领取限制
     */
    private String couponLimit;
    /**
     * 使用须知
     */
    private String prompt;
    /**
     * 是否有效（0：无效；1：有效）
     */
    @NotNull(message = "缺少状态", groups = Search.class)
    private Integer validStatus;
    /**
     * 经纬度
     */
    @GeoSpatialIndexed
    @Indexed(background = true)
    private Double[] local;
    /**
     * 社区ID集合
     */
    private Set<ObjectId> communityIds;
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
    /**
     * 用户领券的关联表id
     */
    @Transient
    private ObjectId couponToUserId;
    /**
     * 社区ID（预留）
     */
    @Transient
    @JSONField(serialize = false)
    private ObjectId communityId;
    /**
     * 使用状态（0：未使用；1：已使用；-1：已失效）
     */
    @Transient
    private Integer useStatus;

    public interface Add{}

    public interface Update{}

    public interface Search{}
}
