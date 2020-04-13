package cn.bit.facade.enums;

/**
 * 用户禁言状态
 */
public enum SilentStatusType {
    //（0：已解禁，1：禁言中）
    RELIEVE(0), SILENT(1);

    private int key;

    SilentStatusType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
