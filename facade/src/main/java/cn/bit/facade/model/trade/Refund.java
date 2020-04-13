package cn.bit.facade.model.trade;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "TRADE_REFUND")
public class Refund {
    @Id
    private Long id;
    /**
     * 交易平台类型（1：微信；2：支付宝）
     */
    private Integer platform;
    /**
     * appid（暂微信专用）
     */
    private String appId;
    /**
     * 商户号
     */
    private String partnerId;
    /**
     * 收单机构退款订单号（支付宝的退款单号与退款单id一致）
     */
    private String agtRefundNo;
    /**
     * 用户id
     */
    private ObjectId userId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 退款时间
     */
    private Date refundAt;
    /**
     * 退款状态（0：退款中；1：已退款）
     */
    private Integer status;
    /**
     * 退款金额（单位：分）
     */
    private Integer refundAmount;
}
