package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.PathBasedRedisIndexDefinition;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "SYS_THIRD_APP_RECORD")
public class ThirdAppRecord implements Serializable {

    @Id
    private ObjectId id;
    /**
     * appId
     */
    private ObjectId appId;
    /**
     * 登录时间（时间戳）
     */
    private Long loginTime;
    /**
     * 登录的IP地址
     */
    private String ipAddress;
}
