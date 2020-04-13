package cn.bit.facade.model.business;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @autor xiaoyu.fang
 * @date 2018/11/7 20:47
 */
@Data
public class ShopApproval implements Serializable {
    /**
     * ID
     */
    @NotNull(message = "缺少店铺ID参数", groups = {Modify.class})
    private ObjectId id;
    /**
     * 名称
     */
    /*@NotBlank(message = "缺少店铺名称参数", groups = {Create.class, Apply.class})*/
    private String name;
    /**
     * 身份证照片（正反两张）
     */
    private List<String> identityCards;
    /**
     * 营业执照
     */
    private List<String> bizLicenses;
    /**
     * 商家LOGO-图片
     */
    private String logo;
    /**
     * 商家图片
     */
    private List<String> images;
    /**
     * 店铺类型（10：餐饮美食；20：百货商店；30：生鲜果蔬；40：休闲娱乐 等等）
     */
    @NotNull(message = "缺少店铺类型参数", groups = {Create.class, Apply.class, Modify.class})
    private Integer type;
    /**
     * 标签（中餐/西餐/火锅/川菜/等等）
     */
    private List<String> tags;
    /**
     * 提供服务
     */
    private List<String> serviceTags;
    /**
     * 运营时间（开始时间）
     */
    @NotBlank(message = "缺少店铺营业开始时间参数", groups = {Create.class, Apply.class})
    private String openAt;
    /**
     * 运营时间（结束时间）
     */
    @NotBlank(message = "缺少店铺营业结束时间参数", groups = {Create.class, Apply.class})
    private String closeAt;
    /**
     * 店铺-联系方式（如果为空，默认contactPhone）
     */
    private List<String> phone;
    /**
     * 联系电话
     */
    @NotBlank(message = "缺少店铺联系人方式参数", groups = {Create.class})
    private String contactPhone;
    /**
     * 简介
     */
    private String description;
    /**
     * 国家
     */
    @NotNull(message = "缺少店铺所在国家参数", groups = {Create.class, Apply.class})
    private String country;
    /**
     * 省份
     */
    @NotNull(message = "缺少店铺所在省份参数", groups = {Create.class, Apply.class})
    private String province;
    /**
     * 城市
     *
     * 台湾省没有城市这一级
     */
    private String city;
    /**
     * 区县
     *
     * 台湾省没有区县这一级
     */
    private String district;
    /**
     * 详细地址
     */
    @NotNull(message = "缺少店铺详细地址参数", groups = {Create.class, Apply.class})
    private String address;
    /**
     * 行政区划代码
     */
    // @NotNull(message = "缺少店铺行政区划代码参数", groups = {Create.class, Apply.class})
    private Integer adCode;
    /**
     * 位置（经纬度）
     */
    @NotNull(message = "缺少店铺位置参数", groups = {Create.class, Apply.class})
    private Double lng;
    /**
     * 位置（经纬度）
     */
    @NotNull(message = "缺少店铺位置参数", groups = {Create.class, Apply.class})
    private Double lat;
    /**
     * 所属公司ID（预留）
     */
    private ObjectId companyId;
    /**
     * 合伙伙伴代码（默认0）
     */
    private Integer partner;
    /**
     * 申请人（用户ID）
     */
    /*@NotNull(message = "缺少店铺申请人参数", groups = {Create.class, Apply.class})*/
    private ObjectId proposer;
    /**
     * 邀请人（用户ID）
     */
    /*@NotNull(message = "缺少邀请人联系方式", groups = {Create.class})*/
    private String inviterPhone;
    /**
     * 审核状态
     */
    private Integer status;

    public interface Create {}

    public interface Apply {}

    public interface Modify {}
}
