package cn.bit.facade.vo.communityIoT.elevator;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @Description :
 * @Date ： 2019/9/12 15:36
 */
@Data
public class ElevatorDetailDTO implements Serializable {
    private String   brandName;
    private ObjectId buildId;
    private String buildName;
    private ObjectId communityId;
    private Integer controllerStatus;
    private Integer deviceConnectStatus;
    private String deviceNum;
    private Integer devicePort;
    private String elevatorId;
    private String elevatorNum;
    private Integer elevatorStatus;
    private String elevatorTypeName;


    private String communityName;
    private ObjectId id;
    private String macAddress;
    private Integer macType;
    private String name;

    @JSONField(name = "houseName")
    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }
}
