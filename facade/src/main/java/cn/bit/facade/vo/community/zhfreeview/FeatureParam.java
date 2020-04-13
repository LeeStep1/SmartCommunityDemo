package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

@Data
public class FeatureParam extends FreeViewBaseInfo implements Serializable {
    /**
     * 社区编码
     */
    private String TenantCode;
    /**
     * 设备ID
     */
    private Integer DeviceID;
    /**
     * 人体特征ID
     */
    private Integer HumanFeatureID;
    /**
     * 设备本地路径（设备编号，含分机号）
     */
    private String DeviceLocalDirectory;
    /**
     * 人体特征类型
     * 1：人脸
     * 2：指纹
     */
    private Integer FeatureType;
    /**
     * 人体特征码
     */
    private String FeatureCode;
    /**
     * 同步状态，
     * 1:下发特征成功
     * 2:注销特征成功
     */
    private Integer HumanFeatureState;
    /**
     * 时间戳
     */
    private Integer Timestamp;
}
