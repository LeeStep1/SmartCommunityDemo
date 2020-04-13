package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

/**
 * @author Dell
 * @date 2018/6/27 11:25
 */
@Data
public class ElevatorFault implements Serializable {
    /**
     * 故障ID
     */
    private ObjectId faultId;
    /**
     * 电梯ID
     */
    private ObjectId elevatorId;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 社区名称
     */
    private String communityName;
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;
    /**
     * 楼栋名称
     */
    private String buildingName;
    /**
     * 故障描述
     */
    private String faultDescription;
    /**
     * 故障图片
     */
    private List<String> images;

}
