package cn.bit.facade.vo.communityIoT;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author xiaoxi.lao
 * @Description : 设备管理平台-设备管理详情
 * @Date ： 2018/12/20 10:38
 */
@Data
public class DeviceVO implements Serializable {
    /**
     * 终端号
     */
    private String terminalCode;
    /**
     * 终端端口
     */
    private Integer terminalPort;
    /**
     * 设备ID
     */
    private ObjectId deviceId;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备类型 (1:电梯 ; 2:门禁 ; 3:车闸 ; 4:摄像头)
     */
    private Integer DeviceType;
    /**
     * 设备厂商 (1:米立 ; 2:康途门禁 ; 3:金博 ; 4:康途电梯 ; 5:全视通)
     */
    private Integer manufactureType;
    /**
     * 厂商名
     */
    private String manufactureName;
    /**
     * 在线状态 {@link cn.bit.facade.enums.DoorOnlineStatusType,cn.bit.facade.enums.ElevatorStatusType}
     */
    private Integer onlineStatus;
    /**
     * 设备地址
     */
    private String address;
}
