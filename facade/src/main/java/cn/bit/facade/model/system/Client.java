package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "SYS_CLIENT")
public class Client implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 客户端类型
     */
    @Indexed(background = true)
    private Integer type;
    /**
     * 合作伙伴代码
     */
    private Integer partner;
    /**
     * 客户端名称
     */
    private String name;
    /**
     * 推送账号ID
     */
    private ObjectId pushAccountId;
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
