package cn.bit.facade.vo.user.userToRoom;

import cn.bit.common.facade.constant.RegexConstants;
import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 住户档案入参实体
 */
@Data
public class HouseholdVO implements Serializable {

    // ******************* 房屋信息 *************************************************************************************
    @NotNull(message = "房间ID不能为空")
    private ObjectId roomId;

    private String roomName;

    private String roomLocation;

    /**
     * 房间面积(放大了100倍的)
     */
    private Integer roomArea;

    private ObjectId communityId;

    private ObjectId zoneId;

    private ObjectId buildingId;

    private ObjectId householdId;

    private ObjectId userId;

    @NotBlank(message = "姓名不能为空")
    @Length(max = 16, min = 1, message = "姓名最大长度为16")
    private String userName;

    @NotNull(message = "性别不能为空")
    private Integer sex;

    @NotNull(message = "用户关系不能为空")
    private Integer relationship;

    private String relationshipDesc;

    @Pattern(regexp = RegexConstants.REGEX_IDENTITY_CARD, message = "身份证号码输入不合法")
    @NotBlank(message = "身份证号码不能为空")
    private String identityCard;

    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    @NotBlank(message = "手机号码不能为空")
    private String phone;

    /**
     * 是否激活
     */
    private Boolean activated;

    // ******************* 业主的补充信息 ********************************************************************************
    /**
     * {@link cn.bit.facade.enums.HouseholdTypeEnum}
     */
    private Integer householdType;

    private String householdAddress;

    /**
     * 政治面貌(1：群众；2：中共党员(包括预备党员)；3：共青团员；4：民主党派；5：其他；)
     * {@link cn.bit.facade.enums.PoliticsStatusType}
     */
    private Integer politicsStatus;

    private String workUnit;

    /**
     * 紧急联系人集合
     */
    private List<EmergencyContactDTO> contacts;

    /**
     * 成员信息列表
     */
    private List<MemberDTO> members;

    /**
     * 设备许可
     */
    private Boolean deviceLicense;
}
