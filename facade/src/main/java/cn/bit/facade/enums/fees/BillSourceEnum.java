package cn.bit.facade.enums.fees;

/**
 * 账单明细来源枚举
 *
 * @author decai.liu
 * @date 2019-10-29
 */
public enum BillSourceEnum {

    BILL(1, "账单"),
    TEMPLATE(2, "模板");

    /**
     * 枚举值
     */
    private Integer value;

    /**
     * 枚举叙述
     */
    private String phrase;

    BillSourceEnum(Integer value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public Integer value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
