package cn.bit.facade.enums;

public enum TradeBizType {

    UNKNOWN(-1), PROPERTY_BILL(1);

    private int value;

    private TradeBizType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static TradeBizType fromValue(int value) {
        switch (value) {
            case 1 :
                return PROPERTY_BILL;
            default:
                return UNKNOWN;
        }
    }

}
