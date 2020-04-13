package cn.bit.framework.enums;

/**
 * Created by terry on 2016/7/7.
 */
public enum CaptchaTextType {
    DEFAULT(0), NUMBER(1), ARABIC(2), CHINESE(3), FIVE_LETTER_FIRSTNAME(4);
    private int type;

    CaptchaTextType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}