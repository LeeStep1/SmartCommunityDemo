package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

@Data
public class KeyNoListElevatorVO implements Serializable {
    private ObjectId id;

    private Integer macType;

    private String macAddress;

    private String keyNo;

    private Boolean writeSuccess;

    private String name;

    private ObjectId buildId;

    /**
     * 终端编号，存在则说明这个设备是在线的
     */
    private String deviceNum;

    @Transient
    private String protocolKey;
}
