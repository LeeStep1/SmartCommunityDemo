package cn.bit.facade.enums;

/**
 * @author xiaoxi.lao
 * @Description :
 * @Date ： 2018/12/20 10:42
 */
public enum DeviceType {
    ELEVATOR(1, "电梯"), DOOR(2, "门禁"), BARRIER_GATE(3, "车闸"), CAMERA(4, "摄像头");

    public int key;

    private String value;

    DeviceType(int key, String value) {
        this.key = key;
        this.value = value;
    }}
