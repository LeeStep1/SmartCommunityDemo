package cn.bit.facade.enums;

/**
 * 电梯运行状态
 */
public enum ElevatorStatusType {
    //电梯状态（0：故障； 1：正常运行）
    OFFLINE(0, "故障"), ONLINE(1, "正常运行");
    public int KEY;

    public String VALUE;

    ElevatorStatusType(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }
}
