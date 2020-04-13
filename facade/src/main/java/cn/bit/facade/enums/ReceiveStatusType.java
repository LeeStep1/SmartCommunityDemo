package cn.bit.facade.enums;

/**
 * 接警状态
 */
public enum ReceiveStatusType {
    // （1：待处理；2：已接警；3：已排查）
    UNCHECKED(1, "待处理"), RECEIVED(2,"已接警"), CHECKED(3,"已排查");

    public int key;

    public String value;

    ReceiveStatusType(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
