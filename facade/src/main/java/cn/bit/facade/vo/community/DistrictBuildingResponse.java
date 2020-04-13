package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class DistrictBuildingResponse implements Serializable {

    private ObjectId buildingId;

    private String buildingName;

    /**
     * 0为可选楼栋 1为可用楼栋
     */
    private Integer availableType;
}
