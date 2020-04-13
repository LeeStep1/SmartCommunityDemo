package cn.bit.facade.enums;

public enum IdentityStatus {
    VALID(1, "正常"), INVALID(0, "过期");

    public int KEY;

    public String VALUE;

    IdentityStatus(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }
}
