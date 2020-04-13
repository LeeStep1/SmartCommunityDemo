package cn.bit.facade.enums;

public enum CardStatusType {

    VALID(1, "有效"), INVALID(0,"无效"), APPLYING(2, "申请中");

    public int KEY;

    public String VALUE;

    CardStatusType(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }
}
