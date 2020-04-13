package cn.bit.facade.vo.trade;

import lombok.Data;

import java.io.Serializable;

@Data
public class Order implements Serializable {
    /**
     * 交易流水id
     */
    private Long tradeId;
    /**
     * 交易平台类型（1：微信；2：支付宝）
     */
    private Integer platform;
    /**
     * 交易类型（1：APP；2：WEB）
     */
    private Integer tradeType;
    /**
     * 订单信息字符串（暂支付宝专用）
     */
    private String orderInfo;
    /**
     * appid（暂微信专用）
     */
    private String appId;
    /**
     * 商户号（暂微信专用）
     */
    private String partnerId;
    /**
     * 预支付id（暂微信专用）
     */
    private String prepayId;
    /**
     * 扩展字段（暂微信专用）
     */
    private String packageValue;
    /**
     * 随机字符串（暂微信专用）
     */
    private String noncestr;
    /**
     * 时间戳（暂微信专用）
     */
    private String timestamp;
    /**
     * 签名（暂微信专用）
     */
    private String sign;
    /**
     * 支付跳转链接
     */
    private String paymentUrl;
}
