package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

/**
 * 对应的是Door表
 * 设备状态
 */
@Data
public class DeviceParam extends FreeViewBaseInfo implements Serializable {
    /**
     * tenantCode 社区编码
     * 社区编码
     */
    private String TenantCode;
    /**
     * 对应的是deviceId
     * 设备ID
     */
    private Integer DeviceID;
    /**
     * 对应的是seriaNo
     * 设备本地路径（设备编号，含分机号）
     */
    private String DeviceLocalDirectory;
    /**
     * 对应的是Name
     * 设备名称
     */
    private String DeviceName;
    /**
     * 设备状态类型
     */
    private Byte StateType;
    /**
     * 对应的是doorStatus
     * 设备状态值
     */
    private Byte StateValue;
    /**
     * 对应的是doorType
     * 设备类型
     */
    private Byte DeviceType;
    /**
     * 转化成createAt或updateAt
     * 时间戳
     */
    private Integer Timestamp;
}
