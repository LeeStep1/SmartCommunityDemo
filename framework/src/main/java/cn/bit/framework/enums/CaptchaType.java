package cn.bit.framework.enums;

/**
 * Created by terry on 2016/7/7.
 */
public enum CaptchaType {

    IMAGE_CAPTCHA("image"),MOBILE_CAPTCHA("mobile");

    private String type;
    CaptchaType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


}
