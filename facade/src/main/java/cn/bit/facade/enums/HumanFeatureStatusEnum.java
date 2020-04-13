package cn.bit.facade.enums;

public enum HumanFeatureStatusEnum {

    SUCCESS(1, "录入成功"),
    EMPTY(0, "未录入"),
    WRITING(-1, "正在录入"),
    FAILURE(-2, "录入失败"),
    DELETE(-3, "已注销");

    HumanFeatureStatusEnum(Integer KEY, String VALUE) {
        this.KEY = KEY;
        this.VALUE = VALUE;
    }

    public Integer KEY;

    public String VALUE;
}
