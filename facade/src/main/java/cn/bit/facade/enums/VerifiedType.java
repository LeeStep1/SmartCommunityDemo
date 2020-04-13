package cn.bit.facade.enums;

public enum VerifiedType {

    UNREVIEWED(0, "未审核"), REVIEWED(1, "已审核"), REJECT(-1, "已拒绝"), CANCELLED(2, "已注销");

    private int KEY;

    private String VALUE;

    VerifiedType(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }

    public int getKEY()
    {
        return KEY;
    }

    public String getVALUE()
    {
        return VALUE;
    }
}
