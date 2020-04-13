package cn.bit.facade.model.user;

import cn.bit.common.facade.constant.RegexConstants;
import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by terry on 2018/1/14.
 */
@Data
@Document(collection = "U_USER")
public class User implements Serializable {

    @Id
    @NotNull(message = "用户ID不能为空", groups = {Update.class, RealNameAuthentication.class})
    private ObjectId id;
    /**
     * 真实名称
     */
    /*@NotBlank(message = "用户姓名不能为空")*/
    @NotBlank(message = "姓名不能为空", groups = {RealNameAuthentication.class})
    private String name;
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空", groups = {Add.class})
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    @Indexed(background = true, unique = true)
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空", groups = {Add.class})
    private String password;
    /**
     * 登录帐号
     */
    @Transient
    private String loginName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 座机
     */
    private String telPhone;
    /**
     * 身份证
     */
    @NotBlank(message = "身份证不能为空", groups = {RealNameAuthentication.class})
    @Pattern(regexp = RegexConstants.REGEX_IDENTITY_CARD, message = "身份证号码输入不合法")
    private String identityCard;
    /**
     * 出生年月
     */
    private String birthday;
    /**
     * 年龄
     */
    @Transient
    private Integer age;
    /**
     * 政治面貌(1：群众；2：中共党员(包括预备党员)；3：共青团员；4：少先队员；)
     */
    private Integer politicsStatus;
    /**
     * 头像URL
     */
    private String headImg;
    /**
     * 性别（0：未知；1：男；2：女）
     */
    private Integer sex;
    /**
     * 工作单位
     */
    private String workUnit;
    /**
     * 户口所在地
     */
    private String householdAddress;
    /**
     * 当前住址
     */
    private String currentAddress;
    /**
     * 蓝牙地址
     */
    private String bdaddr;
    /**
     * 身份是否审核通过（0：未审核；1：已审核）
     */
    private Integer verified;
    /**
     * 有效期（权限控制，0：永久有效）
     */
    private Long validity;
    /**
     * 备注
     */
    private String remark;
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
    private Integer dataStatus;
    /**
     * 职位
     */
    private String profession;

    @Transient
    private Set<String> roles = new HashSet<>();

    @Transient
    private Set<String> permissions = new HashSet<>();

    /**
     * 附加信息
     * 最近登录的社区ID：communityId
     * 上次登录时间 lastLoginTime
     * 等等
     * 等等
     */
    @Transient
    private String attach;

    /**
     * 设备信息
     */
    @Transient
    private UserDevice userDevice;

    /**
     * 临时身份证
     */
    @Transient
    private String tempIdentityCard;

    /**
     * 是否内部人员
     */
    private Boolean internal;

    public interface Add {
    }

    public interface Update {
    }

    /**
     * 实名认证校验
     */
    public interface RealNameAuthentication {
    }

}
