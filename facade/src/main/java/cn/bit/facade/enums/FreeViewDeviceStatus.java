package cn.bit.facade.enums;

/**
 * Author: suzuen
 * Name: 全视通-设备状态
 * Desc: 包含校验位
 */
public enum FreeViewDeviceStatus {

    ONLINE(1, "在线"), OFFLINE(0, "离线"),
    ALARM(1, "报警"), NORMAL(0,"正常"),
    ENABLED(1, "可用"), DISABLED(0, "停用");

    public static final byte ONLINE_CHECK  = 1;
    public static final byte ALARM_CHECK  = 2;
    public static final byte ENABLED_CHECK = 4;

    public Integer key;
    public String value;

    FreeViewDeviceStatus(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int key() {
        return this.key;
    }

    public String value() {
        return this.value;
    }
}
