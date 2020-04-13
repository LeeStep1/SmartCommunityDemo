package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by fxiao
 * on 2018/4/2
 * 商家
 */
@Data
@Document(collection = "BIZ_SHOP")
@CompoundIndex(def = "{'dataStatus':1,'popularity':-1,'communityIds':1,'type':1}", background = true, name = "appShop")
public class Shop extends BaseInfo implements Serializable {
    @Id
    @NotNull(message = "商家ID不能为空", groups = {Update.class})
    private ObjectId id;
    /**
     * 商家名称
     */
    @NotNull(message = "店名不能为空", groups = {Add.class, Update.class})
    private String name;
    /**
     * 商家LOGO
     */
    private String logo;
    /**
     * 商家图片
     */
    private List<String> picture;
    /**
     * 商家类型（10：餐饮美食；20：百货商店；30：生鲜果蔬；40：休闲娱乐 等等）
     */
    @NotNull(message = "商家类型不能为空", groups = {Add.class, Update.class})
    private Integer type;
    /**
     * 商家类型名称（冗余字段）
     */
    private String typeName;
    /**
     * 标签（中餐/西餐/火锅/川菜/等等）
     */
    private Set<String> tag;
    /**
     * 运营时间（开始时间）
     */
    private String serviceBeginAt;
    /**
     * 运营时间（结束时间）
     */
    private String serviceEndAt;
    /**
     * 联系电话
     */
    private String telPhone;
    /**
     * 所在区域（省-市-区三级）
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区/县
     */
    private String district;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 经纬度
     */
    @GeoSpatialIndexed
    private Double[] local;
    /**
     * 关联社区ID
     * 初期还是要关联社区
     */
    private Set<ObjectId> communityIds;
    /**
     * 热度
     * （每张优惠券完成交易就加一）
     * 其它等等
     */
    private Long popularity;
    /**
     * 最新两张优惠券
     */
    private List<String> couponNames;
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
     * 优惠券
     */
    @Transient
    private List<Coupon> coupons;
    /**
     * 商品
     */
    @Transient
    private List<Goods> goods;
    /**
     * 优惠券数量（有效期的）
     */
    @Transient
    private Long couponNum;
    /**
     * 社区ID
     */
    @Transient
    @JSONField(serialize = false)
    private ObjectId communityId;

    public interface Add{}

    public interface Update{}
}
