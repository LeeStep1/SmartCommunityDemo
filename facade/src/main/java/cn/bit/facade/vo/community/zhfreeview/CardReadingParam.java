package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 4.8	设备读卡模式下上报读到的卡信息
 */
@Data
public class CardReadingParam extends FreeViewBaseInfo implements Serializable {
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
     * （卡信息。可是单张卡，也可上传多张卡）
     */
    private List<CardInfos> cardInfos;
    /**
     * 时间戳
     */
    private Integer Timestamp;
}
