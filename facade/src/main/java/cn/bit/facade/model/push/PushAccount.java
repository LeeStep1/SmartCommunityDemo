package cn.bit.facade.model.push;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "PUSH_ACCOUNT")
public class PushAccount implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 应用标识
     */
    private String appKey;
    /**
     * 应用密钥
     */
    private String appSecret;
    /**
     * 提供商（用推送平台的英文名标识名，例如：极光为jpush）
     */
    private String provider;
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
