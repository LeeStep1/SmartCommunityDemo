package cn.bit.facade.model.user;

import cn.bit.facade.model.communityIoT.Elevator;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "U_USER_TO_ELEVATOR")
@CompoundIndex(def = "{'userId' : 1, 'communityId' : 1}", background = true)
public class UserToElevator implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 电梯流水号
     */
    @Indexed(background = true)
    private String keyId;
    /**
     * 电梯请求参数
     */
    private List<Elevator> elevators;
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = AuditOwner.class)
    private ObjectId userId;
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = AuditOwner.class)
    private ObjectId communityId;
    /**
     * 起效时间
     */
    private Date startAt;
    /**
     * 有效时间
     */
    private Long processTime;
    /**
     * 创建人ID
     */
    private ObjectId createId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 更新者
     */
    private ObjectId updateBy;
    /**
     * 数据状态（1：有效；0：无效）
     */
    @Indexed(background = true)
    private Integer dataStatus;
    /**
     * 房屋认证ID
     */
    private ObjectId userToRoomId;

    public interface AuditOwner {}
}
