package cn.bit.facade.vo.user;

import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 登录请求
 * Created by Administrator on 2018/1/16 0016.
 */
@Data
public class SignInByCode implements Serializable {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 第三方平台的用户授权码
     */
    private String authCode;
}
