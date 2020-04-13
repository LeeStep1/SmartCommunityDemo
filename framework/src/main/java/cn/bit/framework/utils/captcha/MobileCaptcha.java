package cn.bit.framework.utils.captcha;

/**
 * Created by terry on 2016/7/7.
 */
public class MobileCaptcha extends Captcha {

    private String mobile;


    public MobileCaptcha(){}
    public MobileCaptcha(String value, String mobile) {
        super(value);
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
