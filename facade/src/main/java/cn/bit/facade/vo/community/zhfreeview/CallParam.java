package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

@Data
public class CallParam extends FreeViewBaseInfo implements Serializable{
    /**
     * 呼叫消息
     */
    private String CallMsgID;
    /**
     * 社区编号
     */
    private String TenantCode;
    /**
     * 设备本地路径（设备编号，含分机号）
     */
    private String DeviceLocalDirectory;
    /**
     * 设备名称
     */
    private String DeviceName;
    /**
     * 被叫房屋路径（房屋编号）
     */
    private String CalledRoomDirectory;
    /**
     * 被叫房屋
     */
    private String CalledRoom;
    /**
     * 抓拍照片URL
     */
    private String CallPhoto;
    /**
     * 呼叫时间
     */
    private String CallTime;
    /**
     * APP账号集合（被叫房屋里的APP）,
     * 多个账号时用逗号隔开
     */
    private String DevUserNameList;
    /**
     * 时间戳
     */
    private Integer Timestamp;
}
