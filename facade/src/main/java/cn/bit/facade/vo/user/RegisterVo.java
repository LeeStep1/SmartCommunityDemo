package cn.bit.facade.vo.user;

import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 注册请求参数实体
 * Created by decai.liu At 20180906
 */
@Data
public class RegisterVo implements Serializable {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 16, message = "密码长度需为6 ~ 16个字符")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;

}
