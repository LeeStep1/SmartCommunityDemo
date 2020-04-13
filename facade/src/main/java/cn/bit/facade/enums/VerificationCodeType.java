package cn.bit.facade.enums;

/**
 * 手机验证码类型
 */
public enum VerificationCodeType {

    REGISTER("1", "注册"), LOGIN("2", "登录"), EDITPWD("3", "修改密码"), EDITPHONE("4", "修改手机号");

    public String key;

    public String value;

    VerificationCodeType(String key, String value) {
        this.key = key;
        this.value = value;
    }

}
