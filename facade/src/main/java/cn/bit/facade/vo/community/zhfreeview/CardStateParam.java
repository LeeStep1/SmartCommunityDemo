package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

/**
 * 4.4	卡下发状态
 */
@Data
public class CardStateParam extends FreeViewBaseInfo implements Serializable {
    /**
     * 社区编码
     */
    private String TenantCode;
    /**
     * 设备ID
     */
    private Integer DeviceID;
    /**
     * 卡ID
     */
    private Integer CardID;
    /**
     * 设备本地路径（设备编号，含分机号）
     */
    private String DeviceLocalDirectory;
    /**
     * 卡序列号
     */
    private String CardSerialNumber;
    /**
     * 卡状态(1.发卡成功，2.销卡成功)
     */
    private Byte CardState;
    /**
     * 时间戳
     */
    private Integer Timestamp;
}
