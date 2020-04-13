package cn.bit.facade.vo.communityIoT.camera;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Data
public class CameraRequest implements Serializable {

    private ObjectId communityId;

    private Set<ObjectId> buildingId = Collections.emptySet();

    private Set<String> mac = Collections.emptySet();

    /**
     * 设备状态（0：未运行；1：正在运行；2：故障；3：未知）
     */
    private Integer cameraStatus;

    private Date after;

    /**
     * 品牌/厂商
     * 1：宇视
     * 2：萤石
     */
    private Integer brandNo;

    /**
     * 摄像头编码
     */
    private String cameraCode;

    /**
     * 监控名称
     */
    private String name;
}
