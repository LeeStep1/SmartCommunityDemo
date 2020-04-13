package cn.bit.facade.vo.property;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ElevatorFaultRequest implements Serializable {
    private Integer source;

    private String communityId;

    private String communityName;

    private String buildingId;

    private String buildingName;

    private String faultDescription;

    private List<String> images;

    @JSONField(name = "houseId")
    public String getCommunityId() {
        return communityId;
    }

    @JSONField(name = "houseName")
    public String getCommunityName() {
        return communityName;
    }

    @JSONField(name = "buildId")
    public String getBuildingId() {
        return buildingId;
    }

    @JSONField(name = "buildName")
    public String getBuildingName() {
        return buildingName;
    }
}
