package cn.bit.facade.vo.communityIoT.camera;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Data
public class EzvizTokenVO implements Serializable {

    /**
     * accessToken
     */
    private String accessToken;

    /**
     * expireTime
     */
    private long expireTime;

    /**
     * 默认构造函数
     */
    public EzvizTokenVO(){

    }

    /**
     * 构造函数
     * @param accessToken
     * @param expireTime
     */
    public EzvizTokenVO(String accessToken, long expireTime){
        this.accessToken = accessToken;
        this.expireTime = expireTime;
    }

}
