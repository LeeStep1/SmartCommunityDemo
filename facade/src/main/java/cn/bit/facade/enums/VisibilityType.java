package cn.bit.facade.enums;

public enum VisibilityType {

    INVISIBLE(0), VISIBLE(1), AUTH_VISIBLE(2);

    private int value;

    VisibilityType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
