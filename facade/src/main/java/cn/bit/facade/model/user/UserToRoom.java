package cn.bit.facade.model.user;

import cn.bit.common.facade.constant.RegexConstants;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "U_USER_TO_ROOM")
public class UserToRoom implements Serializable {

    @Id
    @NotNull(message = "用户认证ID不能为空", groups = {AuditOwner.class, AuditAuxiliary.class})
    private ObjectId id;
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {AddArea.class})
    private ObjectId userId;
    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空", groups = {AddOwner.class, AddAuxiliary.class, AddArea.class})
    private ObjectId roomId;
    /**
     * 房间名称
     */
    private String roomName;
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {GetUserRooms.class})
    private ObjectId communityId;
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;
    /**
     * 用户姓名
     */
    // 为兼容旧版app，去掉必填校验 2018/12/10
//    @NotBlank(message = "姓名不能为空", groups = {AddOwner.class, AddAuxiliary.class})
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 合同
     */
    /*@NotBlank(message = "合同编号不能为空", groups = {AddOwner.class})*/
    private String contract;
    /**
     * 入户时间
     */
    /*@NotNull(message = "入户时间不能为空", groups = {AddOwner.class})*/
    private Date checkInTime;
    /**
     * 用户关系（1：业主；2：家属；3：租客）
     */
    @NotNull(message = "用户关系不能为空", groups = {AddAuxiliary.class})
    private Integer relationship;
    /**
     * 身份证
     */
    /*@NotBlank(message = "身份证不能为空", groups = {AddOwner.class})*/
    @Pattern(regexp = RegexConstants.REGEX_IDENTITY_CARD, message = "身份证号码输入不合法")
    private String identityCard;
    /**
     * 座机
     */
    private String telPhone;
    /**
     * 房屋位置
     */
    private String roomLocation;
    /**
     * 用户与该房间的关系是否审核通过
     */
    @NotNull(message = "审核状态不能为空", groups = {AuditOwner.class})
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private ObjectId auditorId;

    /**
     * 审核人（点击通过 ‘审核’ 的人的名称）
     */
    @Transient
    private String auditor;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 业主ID，如果该用户是该房间的业主，则为该用户ID
     */
    private ObjectId proprietorId;

    /**
     * 是否开放申请
     */
    private Boolean canApply;

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
     * 数据状态（1：有效；0：无效）
     */
    @Indexed
    private Integer dataStatus;

    /**
     * 政治面貌(1：群众；2：中共党员(包括预备党员)；3：共青团员；4：少先队员；)
     */
    private Integer politicsStatus;

    /**
     * 工作单位
     */
    private String workUnit;

    /**
     * 户口所在地
     */
    private String householdAddress;
    /**
     * 现住地址
     */
    private String currentAddress;

    /**
     * 合同手机号
     */
    private String contractPhone;

    /**
     * 是否隐藏（关闭显示），业主端被驳回、已注销，可在房间列表中隐藏该条记录
     */
    private Boolean closed;
    /**
     * 更新者
     */
    private ObjectId updateBy;

    /**
     * 更新者（主要记录注销操作的人）
     */
    @Transient
    private String updater;

    /**
     * 性别（0：未知；1：男；2：女）
     */
    private Integer sex;
    /**
     * 出生年月
     */
    private String birthday;
    /**
     * 米立ID
     */
    private Long miliUId;

    /**
     * 审核操作备注
     */
    private String remark;
    /**
     * 房屋面积(放大100倍)
     */
    private Integer area;

    /**
     * 用户认证参数列表(label,key,value)
     */
    private List<AuthParam> authParamList;

    @Transient
    private String headImg;

    /**
     * 是否常住房屋
     */
    private Boolean inCommonUse;

    /**
     * 房屋位置编码
     */
    private String locationCode;

    @Data
    public static class AuthParam implements Serializable {
        private String key;
        private String label;
        private Object value;
    }

    public interface Update {
    }

    public interface AddOwner {
    }

    public interface AddArea {
    }

    public interface AuditOwner {
    }

    public interface AuditAuxiliary {
    }

    public interface AddAuxiliary {
    }

    public interface GetUserRooms {
    }

}
