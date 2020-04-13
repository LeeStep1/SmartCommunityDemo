package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

/**
 * 门禁记录同步
 */
@Data
public class DoorRecordParam extends FreeViewBaseInfo implements Serializable{
    /**
     * 社区编码
     */
    private String TenantCode;
    /**
     * 刷卡开门消息ID
     */
    private String OpenDoorMsgID;
    /**
     * 设备ID
     */
    private Integer DeviceID;
    /**
     * 设备本地路径（设备编号，含分机号）
     */
    private String DeviceLocalDirectory;
    /**
     * 设备名称
     */
    private String DeviceName;
    /**
     * 开门人ID
     */
    private Integer PersonnelID;
    /**
     * 开门人姓名
     */
    private String PersonnelName;
    /**
     * 开门人证件号码（如身份证）
     */
    private String CertificateCardNo;
    /**
     * 开门人手机号码
     */
    private String Mobile;
    /**
     * 开门抓拍照片URL,多张照片时用逗号隔开
     */
    private String OpenDoorPhotoList;
    /**
     * 开门时间
     */
    private String OpenDoorTime;
    /**
     * 开锁方式
     */
    private Byte AccessWay;
    /**
     * 进出方向（1：进，2：出）
     */
    private Byte Direction;
    /**
     * 卡序列号（即卡ID）
     */
    private String CardSerialNumber;
    /**
     * 结果，是否开锁
     */
    private Boolean AccessResult;
    /**
     * 门禁记录图片存储的服务器地址与端口号。
     */
    private String PhotoHost;
    /**
     * 开门抓拍视频URL,多个视频时用逗号隔开
     */
    private String OpenDoorVideoList;
    /**
     * 门禁ID
     */
    private String ResquestId;
    /**
     * 时间戳
     */
    private Integer Timestamp;
}
