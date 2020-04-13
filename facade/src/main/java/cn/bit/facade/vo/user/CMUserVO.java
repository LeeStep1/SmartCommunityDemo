package cn.bit.facade.vo.user;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class CMUserVO implements Serializable {

    private ObjectId id;

    private String name;

    private Integer sex;

    private String birthday;

    private String identityCard;

    private String phone;

    /**
     * 人脸信息录入状态
     */
    private Integer faceStatus;

    /**
     * 指纹信息录入状态
     */
    private Integer fingerprintStatus;
}
