package cn.bit.facade.enums;

public enum InOutTypeEnum {
    IN(1, "进场"), OUT(2, "出场");

    public int KEY;

    public String VALUE;

    InOutTypeEnum(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }
}
