package cn.bit.facade.model.communityIoT;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/7
 **/
@Data
@Document(collection = "CIT_DOOR_RECORD")
public class DoorRecord implements Serializable {
    @Id
    private ObjectId id;
    /**
     * 用户ID
     */
    @NotNull(message = "使用者id不能为空")
    private ObjectId userId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户身份(1:住户；2：物业)
     */
    private Integer userStatus;
    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;
    /**
     * 社区名字
     */
    private String communityName;
    /**
     * 用户手机
     */
    private String phone;
    /**
     * 用户头像
     */
    private String headImg;
    /**
     * 门禁id
     */
    private ObjectId doorId;
    /**
     * 设备id
     */
    private String deviceId;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备厂商
     */
    private String deviceManufacturer;
    /**
     * MAC地址
     */
    private String macAddress;
    /**
     * 操作方式  1：蓝牙；2远程；
     */
    @NotNull(message = "操作方式不能为空")
    private Integer useStyle;
    /**
     * 凭证
     */
    @NotBlank(message = "卡号不能为空")
    private String keyNo;
    /**
     * 操作时间
     */
    @NotNull(message = "操作时间不能为空")
    private Date time;
    /**
     * 操作结果
     */
    @NotBlank(message = "操作结果不能为空")
    private String result;
    /**
     * 结果码 (-1：失败； 0：未知； 1：成功)
     */
    @NotNull(message = "操作结果码不能为空")
    private Integer resultCode;
    /**
     * 操作指令
     */
    private String userCommand;
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

    // ===========[新增]==============
    /**
     * 身份证
     */
    private String identityCard;
    /**
     * 开门抓拍照片URL,多张照片时用逗号隔开
     */
    private String openDoorPhotoList;
    /**
     * 开门方式
     */
    private String useStyleValue;
    /**
     * 进出方向（1：进，2：出）
     */
    private Integer direction;

    /**
     * 用于查找全视通门禁
     */
    @Transient
    private String deviceLocalDirectory;

    public interface Add {}

    public interface Query {}
}
