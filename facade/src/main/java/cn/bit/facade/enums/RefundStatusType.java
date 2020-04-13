package cn.bit.facade.enums;

public enum RefundStatusType {

    UNKNOWN(-1), REFUNDING(0), REFUNDED(1);

    private int value;

    RefundStatusType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
