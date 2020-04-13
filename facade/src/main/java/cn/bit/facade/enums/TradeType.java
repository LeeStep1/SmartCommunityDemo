package cn.bit.facade.enums;

public enum TradeType {

    UNKNOWN(-1), APP(1), WEB(2);

    private int value;

    private TradeType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static TradeType fromValue(int value) {
        switch (value) {
            case 1:
                return APP;
            case 2:
                return WEB;
            default:
                return UNKNOWN;
        }
    }

}
