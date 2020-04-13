package cn.bit.facade.vo.trade;

import lombok.Data;

import java.io.Serializable;

/**
 * 交易通知实体类
 *
 * @author jianming.fan
 * @date 2018-10-09
 */
@Data
public class Notification implements Serializable {
    /**
     * 交易平台名称
     */
    private String platform;
    /**
     * 支付通知字符串
     */
    private String notifyData;
}
