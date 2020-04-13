package cn.bit.facade.enums;

/**
 * Created by decai
 * on 2018/5/15
 * 维修工类型
 */
public enum FaultRepairerType {

    PROPERTY(1, "物业维修工"), PERSONAL(2, "私人维修工");

    private Integer type;

    private String value;

    FaultRepairerType(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public Integer getType() {
        return type;
    }
}
