package cn.bit.facade.model.communityIoT;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "CIT_CAMERA")
public class Camera implements Serializable{

    @Id
    private ObjectId id;

    /**
     * 摄像头名称
     */
    private String name;

    /**
     * 摄像头编码
     */
    private String cameraCode;

    /**
     * 摄像头
     * (1：门禁；2：车禁；3：梯禁)
     */
    private String cameraType;

    /**
     * 机器硬件地址
     */
    private String mac;

    /**
     * 排序
     */
    private Integer rank;

    /**
     * 品牌/厂商
     */
    private String brand;

    /**
     * 品牌/厂商
     * 1:宇视
     * 2：萤石
     */
    private Integer brandNo;

    /**
     * 登录名
     */
    private String callName;

    /**
     * 登录凭证
     */
    private String callPassword;

    /**
     * 设备状态
     * 设备状态（0：未运行；1：正在运行；2：故障；3：未知）
     */
    private Integer cameraStatus;

    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 创建人ID
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 数据状态
     */
    private Integer dataStatus;

    // =================【第三方产商】================//

    /**
     * 调取接口
     */
    private String callURL;

    /**
     * 设备云帐号（宇视）
     */
    private String cloudUserName;

    /**
     * 设备云帐号密码（宇视）
     */
    private String cloudPassword;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名字
     */
    private String deviceName;
    /**
     * 型号
     */
    private String deviceType;

    /**
     * 设备密码
     * 这个是动态获取密码数据(现在传过来为空就好了)
     */
    private String devicePassword;

    /**
     * 设备云帐号地址（宇视）
     */
    private String cloudServerUrl;

    /**
     * 通道号
     */
    private Integer channelId;

    /**
     * APP key（萤石-海康）
     */
    private String appKey;

    /**
     * 密匙（萤石-海康）
     */
    private String secret;

    @Transient
    private Boolean temporaryAuthorized = false;

}
