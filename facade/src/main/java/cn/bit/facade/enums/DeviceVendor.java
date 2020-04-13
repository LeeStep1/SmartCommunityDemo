package cn.bit.facade.enums;

/**
 * 摄像头设备厂商
 */
public enum DeviceVendor {

    UNIVIEW(1, "宇视"), EZVIZ(2, "海康-萤石");

    public Integer key;

    public String value;

    DeviceVendor(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public static DeviceVendor getVendor(int key){
        switch (key){
            case 1:
                return DeviceVendor.UNIVIEW;
            case 2:
                return DeviceVendor.EZVIZ;
            default: return null;
        }
    }
}
