package cn.bit.framework.utils.captcha;

/**
 * Created by terry on 2016/7/7.
 */
public class ImageCaptcha extends Captcha {

    private String url;

    public ImageCaptcha(){}

    public ImageCaptcha(String value, String url) {
        super(value);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
