package cn.bit.facade.enums;

/**
 * 门禁在线情况
 */
public enum DoorOnlineStatusType {

    OFFLINE(1, "离线"), ONLINE(2, "在线"), UNKNOWN(0, "未知");

    public Integer key;

    public String value;

    DoorOnlineStatusType(Integer key, String value) {
        this.key = key;
        this.value = value;
    }
}
