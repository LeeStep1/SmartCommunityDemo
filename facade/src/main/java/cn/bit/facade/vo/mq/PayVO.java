package cn.bit.facade.vo.mq;

import lombok.Data;

import java.io.Serializable;

@Data
public class PayVO implements Serializable {
    /**
     * 交易平台类型（1：微信；2：支付宝）
     */
    private Integer platform;
    /**
     * 支付通知字符串
     */
    private String notifyData;
    /**
     * 处理次数
     */
    private Integer handleCount;
}
