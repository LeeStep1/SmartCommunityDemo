package cn.bit.facade.vo.user;

import cn.bit.framework.constant.GlobalConstants;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;

@Validated
public class NoteCode {


    @NotBlank(message = "手机号为空")
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    private String phone;

    @NotBlank(message = "业务类型为空")
    private String bizCode;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }
}
