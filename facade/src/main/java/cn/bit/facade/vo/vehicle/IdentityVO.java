package cn.bit.facade.vo.vehicle;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 车牌
 */
@Data
public class IdentityVO implements Serializable {
    @Id
    private ObjectId id;
    /**
     * 车牌号
     */
    private String carNo;
    /**
     * 车主姓名
     */
    private String owner;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 放行时间
     */
    private Date passAt;
    /**
     * 住址
     */
    private String address;
    /**
     * 开始时间
     */
    private Date beginAt;
    /**
     * 结束时间
     */
    private Date endAt;

    private Integer status;
    /**
     * 车辆类型
     */
    private Integer carType;
    /**
     * 车牌类型  1=民用  2=军队    3=警用   4=武警
     */
    private Integer type;
    /**
     * 收费类型  1=月卡  2=临时车  3=免费车 4=储值卡
     */
    private Integer chargeType;

    private Integer dataStatus;
}
