package cn.bit.facade.vo.communityIoT.elevator;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class ElevatorVO implements Serializable {

    @NotNull(message = "id不能为空", groups = {ControlStatus.class})
    private String id;

    private String buildingId;

    private String buildingName;

    private String brandName;

    private String elevatorId;

    /**
     * 终端编号，存在则说明这个设备是在线的
     */
    private String deviceNum;

    /**
     * 蓝牙类型 1：金博，2：康途
     */
    private Integer macType;

    private String macAddress;

    private String elevatorNum;

    private String name;

    private Integer elevatorStatus;

    private String communityId;

    private String communityName;

    private String elevatorTypeName;

    private Integer controllerStatus;

    @JSONField(name = "buildId")
    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @JSONField(name = "buildName")
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    @JSONField(name = "houseName")
    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public interface ControlStatus {}
}
