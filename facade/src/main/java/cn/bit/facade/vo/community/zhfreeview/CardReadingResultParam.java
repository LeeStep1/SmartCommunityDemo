package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

/**
 * 4.7	设备进入读卡模式的结果通知接口
 */
@Data
public class CardReadingResultParam extends FreeViewBaseInfo implements Serializable {
    /**
     * 社区编码
     */
    private String TenantCode;
    /**
     * 设备本地路径（设备编号，含分机号）
     */
    private String DeviceLocalDirectory;
    /**
     * 请求唯一标识
     */
    private String RequestID;
    /**
     * 进入读卡模式的操作结果 true：成功进入读卡模式 false：操作失败
     */
    private Boolean OperationResult;
    /**
     * 时间戳
     */
    private Integer Timestamp;
}
