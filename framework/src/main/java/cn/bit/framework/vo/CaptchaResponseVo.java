package cn.bit.framework.vo;


import cn.bit.framework.enums.CaptchaType;
import cn.bit.framework.utils.captcha.Captcha;
import cn.bit.framework.utils.captcha.ImageCaptcha;
import cn.bit.framework.utils.captcha.MobileCaptcha;

import java.io.Serializable;

/**
 * Created by terry on 2016/7/7.
 */
public class CaptchaResponseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String guid;
    private String type;
    private String mobile;
    private String url;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static CaptchaResponseVo build(Captcha captcha) {
        CaptchaResponseVo vo = new CaptchaResponseVo();
        vo.setGuid(captcha.getGuid());

        if (captcha.getClass() == ImageCaptcha.class) {
            vo.setType(CaptchaType.IMAGE_CAPTCHA.getType());
            vo.setUrl(((ImageCaptcha) captcha).getUrl());
        } else if (captcha.getClass() == MobileCaptcha.class) {
            vo.setType(CaptchaType.MOBILE_CAPTCHA.getType());
        }

        return vo;
    }

}
