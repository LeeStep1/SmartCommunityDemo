package cn.bit.facade.vo.user.userToRoom;

import cn.bit.common.facade.constant.RegexConstants;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 住户档案基本信息dto
 */
@Data
public class MemberDTO implements Serializable {

    private ObjectId householdId;

    @NotBlank(message = "住户姓名不能为空")
    @Length(max = 16, min = 1, message = "姓名最大长度为16")
    private String userName;

    @NotNull(message = "性别不能为空")
    private Integer sex;

    @NotNull(message = "用户关系不能为空")
    private Integer relationship;

    private String relationshipDesc;

    @Pattern(regexp = RegexConstants.REGEX_IDENTITY_CARD, message = "身份证号码输入不合法")
    private String identityCard;

    @Pattern(regexp = "^\\d{0}|1[3456789]\\d{9}$", message = "手机号码格式有误")
    private String phone;

    private Boolean activated;

    /**
     * 设备许可
     */
    private Boolean deviceLicense;
}
