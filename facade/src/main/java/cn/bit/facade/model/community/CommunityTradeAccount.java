package cn.bit.facade.model.community;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "CM_TRADE_ACCOUNT")
public class CommunityTradeAccount implements Serializable {
    @Id
    private ObjectId id;
    /**
     * 社区id
     */
    private ObjectId communityId;
    /**
     * 平台（1：微信；2：支付宝）
     */
    private Integer platform;
    /**
     * 交易类型（1：APP；2：WEB）
     */
    private Integer tradeType;
    /**
     * 交易账户id
     */
    private ObjectId tradeAccountId;
    /**
     * 客户端集合
     */
    private Integer client;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 数据状态（1：有效；0：失效）
     */
    private Integer dataStatus;
}
