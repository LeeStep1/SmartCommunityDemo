package cn.bit.facade.model.business;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @autor xiaoyu.fang
 * @date 2018/11/8 20:57
 */
@Data
public class CouponTemplate implements Serializable {
    /**
     * ID
     */
    @NotNull(message = "缺少优惠券ID", groups = Modify.class)
    private ObjectId id;
    /**
     * 优惠券名称
     */
    @NotBlank(message = "缺少优惠券名称", groups = {Create.class, Modify.class})
    private String name;
    /**
     * 优惠券图标-没有默认商家logo
     */
    private String icon;
    /**
     * 优惠券类型（1：折扣卷；2：满减券；）
     */
    @NotNull(message = "缺少优惠券类型", groups = {Create.class})
    private Integer couponType;
    /**
     * 面额/折扣
     * <p>
     * couponType为1（折扣券）时，单位为百分比
     * <p>
     * couponType为2（满减券）时，单位为分
     */
    @NotNull(message = "缺少优惠券模板面值", groups = {Create.class})
    private String parValue;
    /**
     * 门槛，满多少才可使用优惠，单位为分。为null表示无门槛
     */
    private String threshold;
    /**
     * 优惠券发行数量（null表示数量无限，非null表示有限数量）
     */
    private Integer quantity;
    /**
     * 已被领取的优惠券数量，默认为0
     */
    private Integer receivedNum;
    /**
     * 已使用数量，默认为0
     */
    private Integer usedNum;
    /**
     * 企业ID
     */
    @Indexed(background = true)
    private ObjectId companyId;
    /**
     * 商家ID
     */
    private ObjectId shopId;
    /**
     * 商标（企业优惠券使用企业名称，单店优惠券使用店铺名称）
     */
    private String brandName;
    /**
     * 开始时间
     */
    private Date beginAt;
    /**
     * 结束时间
     */
    private Date endAt;
    /**
     * 优惠券模板状态（0：未发布；1：未开始；2：进行中；3：已结束）
     * <p>
     */
    private Integer status;
    /**
     * 使用须知
     */
    @NotBlank(message = "缺少优惠券模板使用须知", groups = {Create.class})
    @Max(
            value = 200,
            message = "超过优惠券模板使用须知的最大长度"
    )
    private String prompt;
    /**
     * 期限类型
     * 0：FIXED_RANGE
     * 1：FIXED_TERM
     */
    @NotNull(message = "缺少优惠券期限类型", groups = {Create.class})
    private Integer termType;
    /**
     * 优惠券生效时间
     */
    /*@Future(message = "优惠券生效时间必须大于当前时间")*/
    private Date couponEnableAt;
    /**
     * 优惠券失效时间
     */
    @Future(message = "优惠券失效时间必须大于当前时间")
    private Date couponDisableAt;
    /**
     * 有效天数
     */
    private Integer days;
    /**
     * 领取的优惠券持续有效时长（秒）
     */
    private Long couponDuration;
    /**
     * 领取的优惠券延时生效时长（秒）
     * <p>
     * 默认为0，表示领取当天开始有效
     */
    private Long couponDelay;
    /**
     * 该优惠券能被同一个账号领取的数量，默认是：1
     */
    private Integer receiveLimit;
    /**
     * 创建人ID
     */
    private ObjectId creator;

    public interface Create {
    }

    public interface Modify {
    }

}
