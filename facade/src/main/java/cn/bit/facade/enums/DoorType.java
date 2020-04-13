package cn.bit.facade.enums;

import cn.bit.framework.exceptions.BizException;

/**
 * 门类型(1:社区门，2:楼栋门)
 */
public enum DoorType {
    COMMUNITY_DOOR(1, "社区门"),
    BUILDING_DOOR(2, "楼栋门"),
    UNKNOWN(0, "未录入");

    private int value;
    private String desc;

    private DoorType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static void validate(int value) {
        if (COMMUNITY_DOOR.value != value && BUILDING_DOOR.value != value) {
            throw new BizException("DoorType 不正确");
        }
    }
}
