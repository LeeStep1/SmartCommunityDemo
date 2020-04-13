package cn.bit.facade.enums;

public enum TradeStatusType {

    UNKNOWN(-1), NOT_PAY(0), PAID(1), REFUNDED(2), CLOSED(3);

    private int value;

    TradeStatusType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
