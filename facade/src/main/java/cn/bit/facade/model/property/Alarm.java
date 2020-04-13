package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "PROP_ALARM")
public class Alarm implements Serializable {

    @Id
    @NotNull(message = "记录id不能为空", groups = {TroubleShoot.class, ReceiveAlarm.class})
    private ObjectId id;

    /**
     * 报警人ID
     */
    private ObjectId callerId;

    /**
     * 报警人姓名
     */
    private String callerName;

    /**
     * 报警人电话
     */
    private String callerPhoneNum;

    /**
     * 报警时间
     */
    private Date callTime;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 房间ID
     */
    @NotNull(message = "房间id不能为空", groups = {AddAlarm.class})
    private ObjectId roomId;

    /**
     * 社区名称
     */
    private String communityName;

    /**
     * 楼栋名称
     */
    private String buildingName;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 接警状态   1：待处理；2：已接警；3：已排查
     */
    private Integer receiveStatus;

    /**
     * 接警人ID
     */
    private ObjectId receiverId;

    /**
     * 接警人姓名
     */
    private String receiverName;

    /**
     * 接警人电话
     */
    private String receiverPhoneNum;

    /**
     * 接警时间
     */
    private Date receiveTime;

    /**
     * 排查时间
     */
    @NotNull(message = "排查时间不能为空", groups = {TroubleShoot.class})
    private Date troubleShootingTime;

    /**
     * 排查报告（文字）
     */
    @NotBlank(message = "排查报告不能为空", groups = {TroubleShoot.class})
    private String troubleShootingReport;

    /**
     * 创建人ID
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改人ID
     */
    private ObjectId modifierId;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;

    public interface AddAlarm {
    }

    public interface ReceiveAlarm {
    }

    public interface TroubleShoot {
    }

}
