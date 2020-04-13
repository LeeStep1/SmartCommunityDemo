package cn.bit.facade.model.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 车闸
 */
@Data
@Document(collection = "CAR_GATE")
public class Gate implements Serializable
{
    /**
     * 车闸ID
     */
    @Id
    private ObjectId id;

    /**
     * 车闸编号
     */
    private String no;

    /**
     * 车闸名称
     */
    private String name;

    /**
     * 出入标记（1：入口；2：出口）
     */
    private Integer type;

    private ObjectId communityId;

    private Integer dataStatus;

    @JsonProperty("gateNO")
    public String getNo() {
        return no;
    }

    @JsonProperty("gateName")
    public String getName() {
        return name;
    }

    @JsonProperty("inOutTag")
    public Integer getType() {
        return type;
    }

    @JsonProperty("gateName")
    public void setName(String name) {
        this.name = name;
    }
}
