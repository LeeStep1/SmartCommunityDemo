package cn.bit.facade.model.communityIoT;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorDetailDTO;
import cn.bit.facade.vo.mq.CreateRecordRequest;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/7
 **/
@Data
@Document(collection = "CIT_ELEVATOR_RECORD")
public class ElevatorRecord implements Serializable {
    @Id
    private ObjectId id;
    /**
     * 用户ID
     */
    // 因为部分实体卡是没有用户档案的，不存在userId，当前限制先去除，at 2019.10.08 by xiaoxi.lao
    //@NotNull(message = "使用者id不能为空")
    private ObjectId userId;
    /**
     * 用户姓名
     */
    private String   userName;
    /**
     * 用户身份
     */
    private Integer  userStatus;
    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;
    /**
     * 社区名字
     */
    private String   communityName;
    /**
     * 用户手机
     */
    private String   phone;
    /**
     * 用户头像
     */
    private String   headImg;
    /**
     * 设备id
     */
    private String   deviceId;
    /**
     * 设备名称
     */
    private String   deviceName;
    /**
     * 设备厂商
     */
    private String   deviceManufacturer;
    /**
     * MAC地址
     */
    private String   macAddress;
    /**
     * 操作方式
     */
    @NotNull(message = "操作方式不能为空")
    private Integer  useStyle;
    /**
     * 凭证
     */
    @NotBlank(message = "卡号不能为空")
    private String   keyNo;
    /**
     * 操作时间
     */
    @NotNull(message = "操作时间不能为空")
    private Date     time;
    /**
     * 操作结果
     */
    @NotNull(message = "操作结果不能为空")
    private String   result;
    /**
     * 结果码 (-1：失败； 0：未知； 1：成功)
     */
    @NotNull(message = "操作结果码不能为空")
    private Integer  resultCode;
    /**
     * 操作指令
     */
    private String   userCommand;
    /**
     * 创建人ID
     */
    private ObjectId creatorId;
    /**
     * 创建时间
     */
    private Date     createAt;
    /**
     * 修改时间
     */
    private Date     updateAt;
    /**
     * 数据状态
     */
    private Integer  dataStatus;
    /**
     * 设备上传的记录唯一编号，防止重复上传
     * 使用稀疏索引防止因uniqueCode为空导致启动报错
     */
    @Indexed(unique = true, background = true, sparse = true)
    private String   uniqueCode;

    /**
     * 组装对象
     *
     * @param detail              电梯信息
     * @param card                卡信息
     * @param createRecordRequest 设备发送过来的记录
     */
    public void buildBy(ElevatorDetailDTO detail, Card card, CreateRecordRequest createRecordRequest) {
        setUserInfo(card);
        setCommunityInfo(detail);
        setRecordResult(createRecordRequest);
        setDevice(detail);
        setCreatorId(card.getUserId());
        setDataStatus(DataStatusType.VALID.KEY);
        signNow();
        Date accessTime = createRecordRequest.getAccessTime();
        Calendar accessCalendar = Calendar.getInstance();
        accessCalendar.setTime(accessTime);
        // 组装记录唯一码
        setUniqueCode(accessCalendar.get(Calendar.YEAR) +
                      accessCalendar.get(Calendar.MONTH) +
                      accessCalendar.get(Calendar.DAY_OF_MONTH) +
                      String.valueOf(createRecordRequest.getRecordId()));
    }

    private void setCommunityInfo(ElevatorDetailDTO detail) {
        setCommunityId(detail.getCommunityId());
        setCommunityName(detail.getCommunityName());
    }

    private void setUserInfo(Card card) {
        setUserId(card.getUserId());
        setUserName(card.getName());
        setPhone(card.getPhone());
        setKeyNo(card.getKeyNo());
    }

    private void setDevice(ElevatorDetailDTO detail) {
        setDeviceId(detail.getId().toHexString());
        setDeviceName(detail.getName());
        setDeviceManufacturer(detail.getBrandName());
        setMacAddress(detail.getMacAddress());
    }

    private void setRecordResult(CreateRecordRequest createRecordRequest) {
        setTime(createRecordRequest.getAccessTime());

        if (createRecordRequest.getAccess()) {
            setResultCode(1);
            setResult("开梯成功");
        } else {
            setResultCode(-1);
            setResult("开梯失败");
        }

        setUseStyle(Integer.valueOf(createRecordRequest.getType()));
    }

    private void signNow() {
        setCreateAt(new Date());
        setUpdateAt(new Date());
    }

    public interface Add {
    }

    public interface Query {
    }
}
