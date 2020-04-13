package cn.bit.facade.model.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 进出记录
 */
@Data
@Document(collection = "CAR_IN_OUT")
@CompoundIndex(def = "{'carNo' : 1, 'communityId' : 1}", background = true)
public class InOut implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 社区ID
     */
    @Indexed(background = true)
    private ObjectId communityId;
    /**
     * 车牌号码
     */
    private String carNo;

    /**
     * 车辆类型 1=民用  2=军队    3=警用   4=武警
     */
    private Integer carType = 1;

    /**
     * 车牌类型 1=民用  2=军队    3=警用   4=武警
     */
    private Integer carIdentityType = 1;

    /**
     * 收费类型 1=月卡  2=临时车  3=免费车 4=储值卡
     */
    private Integer chargeType = 1;

    /**
     * 进场时间
     */
    private Date enterAt;

    /**
     * 出场时间
     */
    private Date leaveAt;

    /**
     * 进场入口
     */
    private String inGate;

    /**
     * 出场出口
     */
    private String outGate;

    /**
     * 进出场类型 1:进场 ，2：出场
     */
    private Integer type;

    private Date updateAt;

    private Integer dataStatus;

    @JsonProperty("inTime")
    public Date getEnterAt() {
        return enterAt;
    }

    @JsonProperty("outTime")
    public Date getLeaveAt() {
        return leaveAt;
    }

    public String getInGate() {
        return inGate;
    }

    public String getOutGate() {
        return outGate;
    }

    @JsonProperty("inoutType")
    public Integer getType() {
        return type;
    }
}