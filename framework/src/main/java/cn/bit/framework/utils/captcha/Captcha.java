package cn.bit.framework.utils.captcha;



import cn.bit.framework.utils.string.StrUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terry on 2016/7/7.
 */
public abstract class Captcha implements Serializable {

    private static final long serialVersionUID = 1L;

    private String guid = StrUtil.get32UUID();
    private Date createAt = new Date();
    private String value;

    public Captcha(){}

    public Captcha(String text) {
        this.value = value;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
