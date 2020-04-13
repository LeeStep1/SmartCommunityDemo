package cn.bit.facade.vo.trade;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

@Data
public class TradeOrder implements Serializable {
    /**
     * 交易id
     */
    private Long tradeId;
    /**
     * 交易类型（1：APP；2：WEB）
     */
    private Integer tradeType;
    /**
     * 业务类型
     */
    private Integer bizType;
    /**
     * 用户id
     */
    private ObjectId userId;
    /**
     * 订单标题
     */
    private String title;
    /**
     * 订单详情
     */
    private String detail;
    /**
     * 交易账号id
     */
    private ObjectId tradeAccountId;
    /**
     * 商品类型（0：虚拟类商品；1：实物类商品）
     */
    private Integer goodsType;
    /**
     * 总金额（单位：分）
     */
    private Long totalAmount;
    /**
     * 开始时间
     */
    private Date startAt;
    /**
     * 到期时间
     */
    private Date expireAt;
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    /**
     * 用户的实际IP
     */
    private String userIp;
    /**
     * 附加信息
     */
    private String attach;
}
