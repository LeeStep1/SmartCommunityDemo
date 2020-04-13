package cn.bit.facade.enums;

/**
 * 数据的有效性
 */
public enum DataStatusType {

    VALID(1, "有效"), INVALID(0,"无效");

    public int KEY;

    public String VALUE;

    DataStatusType(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }

}
