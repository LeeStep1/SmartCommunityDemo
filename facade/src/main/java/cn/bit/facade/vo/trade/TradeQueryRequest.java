package cn.bit.facade.vo.trade;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class TradeQueryRequest implements Serializable {
    /**
     * 订单号
     */
    private String tradeNo;
    /**
     * 交易账号id
     */
    private ObjectId tradeAccountId;
    /**
     * 收单机构订单号
     */
    private String agtTradeNo;
}
