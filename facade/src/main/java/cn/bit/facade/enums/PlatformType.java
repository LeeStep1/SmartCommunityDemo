package cn.bit.facade.enums;

public enum PlatformType {

    UNKNOWN(-1), WECHAT(1), ALIPAY(2);

    private int value;

    PlatformType(int value) {
        this.value = value;
    }

    public static PlatformType fromValue(int value) {
        switch (value) {
            case 1:
                return WECHAT;
            case 2:
                return ALIPAY;
            default:
                return UNKNOWN;
        }
    }

    public int value() {
        return this.value;
    }

}
