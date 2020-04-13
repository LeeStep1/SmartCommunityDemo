package cn.bit.facade.model.trade;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "TRADE_ACCOUNT")
@CompoundIndex(def = "{'platform' : 1, 'appId' : 1}", background = true)
public class TradeAccount {

    @Id
    private ObjectId id;
    /**
     * 名称
     */
    private String name;
    /**
     * 平台（1：微信；2：支付宝）
     */
    private Integer platform;
    /**
     * 交易类型（1：APP；2：WEB）
     */
    private Integer type;
    /**
     * appid
     */
    private String appId;
    /**
     * 商户号
     */
    private String partnerId;
    /**
     * 支付宝应用公钥 -- RSA/RSA2公钥（平台颁发）
     */
    private String pubKey;
    /**
     * 支付宝应用私钥 -- RSA/RSA2私钥（平台颁发）
     */
    private String pvtKey;
    /**
     * 支付宝公钥 -- RSA/RSA2公钥（支付宝颁发）
     */
    private String alipayPubKey;
    /**
     * 微信密钥
     */
    private String key;
    /**
     * 签名类型
     * <p>
     * 微信：MD5/HMAC-SHA256
     * <p>
     * 支付宝：RSA/RSA2
     */
    private String signType;
    /**
     * 创建人ID
     */
    private ObjectId creatorId;
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
