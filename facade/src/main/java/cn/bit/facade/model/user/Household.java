package cn.bit.facade.model.user;

import cn.bit.facade.vo.user.userToRoom.EmergencyContactDTO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 住房档案实体
 */
@Data
@Document(collection = "U_HOUSEHOLD")
@CompoundIndex(def = "{'communityId' : 1, 'buildingId' : 1}", background = true)
public class Household implements Serializable {

    @Id
    private ObjectId id;

    private ObjectId userId;

    @NotNull(message = "房间ID不能为空")
    @Indexed(background = true)
    private ObjectId roomId;

    private String roomName;

    private String roomLocation;

    private ObjectId communityId;

    private ObjectId zoneId;

    private ObjectId buildingId;

    /**
     * 设备许可
     */
    private Boolean deviceLicense = true;

    /**
     * 紧急联系人集合
     */
    private List<EmergencyContactDTO> contacts;

    @NotBlank(message = "姓名不能为空")
    private String userName;

    /**
     * 用户关系（1：业主；2：家属；3：租客）
     * {@link cn.bit.facade.enums.RelationshipType}
     */
    @NotNull(message = "用户关系不能为空")
    private Integer relationship;

    private String relationshipDesc;

    private String identityCard;

    @Indexed(background = true)
    private String phone;

    private Integer sex;

    private Date createAt;

    private Date updateAt;

    /**
     * 数据状态（1：有效；0：无效）
     * {@link cn.bit.facade.enums.DataStatusType}
     */
    @Indexed
    private Integer dataStatus;

    private String remark;

    /**
     * 户口类型(农业户口/城镇户口)
     * {@link cn.bit.facade.enums.HouseholdTypeEnum}
     */
    private Integer householdType;

    /**
     * 户籍地址
     */
    private String householdAddress;

    /**
     * 政治面貌(1：群众；2：中共党员(包括预备党员)；3：共青团员；4：民主党派；5：其他；)
     * {@link cn.bit.facade.enums.PoliticsStatusType}
     */
    private Integer politicsStatus;

    private String workUnit;

    /**
     * 是否已激活
     */
    private Boolean activated;
}
