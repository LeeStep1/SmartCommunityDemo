package cn.bit.facade.enums;

/**
 * 厂商类型
 */
public enum ManufactureType {
    MILI(1, "米立"), KANGTU_DOOR(2, "康途门禁"), JINBO(3, "金博"), KANGTU_ELEVATOR(4, "康途电梯"), FREEVIEW_DOOR(5, "全视通"), UNKNOWN(-1, "未知");

    public int KEY;

    public String VALUE;

    ManufactureType(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }

    public int getKEY() {
        return KEY;
    }

    public String getVALUE() {
        return VALUE;
    }

    public static String getValueByKey(int key) {
        switch (key) {
            case 1:
                return MILI.getVALUE();
            case 2:
                return KANGTU_DOOR.getVALUE();
            case 3:
                return JINBO.getVALUE();
            case 4:
                return KANGTU_ELEVATOR.getVALUE();
            case 5:
                return FREEVIEW_DOOR.getVALUE();
            default:
                return "";
        }
    }

    public static ManufactureType fromValue(int key) {
        switch (key) {
            case 1 :
                return MILI;
            case 2:
                return KANGTU_DOOR;
            case 3:
                return JINBO;
            case 4:
                return KANGTU_ELEVATOR;
            case 5:
                return FREEVIEW_DOOR;
            default:
                return UNKNOWN;
        }
    }
}
