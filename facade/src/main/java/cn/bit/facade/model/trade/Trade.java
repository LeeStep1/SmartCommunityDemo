package cn.bit.facade.model.trade;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "TRADE_ORDER")
public class Trade implements Serializable {
    @Id
    private Long id;
    /**
     * 收单机构订单号
     */
    private String agtTradeNo;
    /**
     * 交易平台类型（1：微信；2：支付宝）
     */
    private Integer platform;
    /**
     * 业务类型
     */
    private Integer bizType;
    /**
     * appid
     */
    private String appId;
    /**
     * 商户号
     */
    private String partnerId;
    /**
     * 订单标题
     */
    private String title;
    /**
     * 订单详情
     */
    private String detail;
    /**
     * 商品类型（0：虚拟类商品；1：实物类商品）
     */
    private Integer goodsType;
    /**
     * 用户id
     */
    private ObjectId userId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 到期时间
     */
    private Date expireAt;
    /**
     * 支付时间
     */
    private Date payAt;
    /**
     * 交易状态（0：未支付；1：已支付；2：发生退款；3：已关闭；）
     */
    private Integer status;
    /**
     * 总金额（单位：分）
     */
    private Long totalAmount;
    /**
     * 退款订单号集合
     */
    private List<Long> refunds;
    /**
     * 累计退款金额（单位：分）
     */
    private Integer totalRefundAmount;
}
