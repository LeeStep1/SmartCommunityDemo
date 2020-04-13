package cn.bit.facade.enums.fees;

import java.util.HashMap;
import java.util.Map;

/**
 * 收费项目类型枚举
 *
 * @author decai.liu
 * @date 2019-10-29
 */
public enum FeesItemType {
    FIXATION(1, "固定收费"),
    UNIT_PRICE(2, "单价计费"),
    CUSTOM(3, "自定义");

    private static final Map<Integer, FeesItemType> MAP;

    static {
        MAP = new HashMap<>(FeesItemType.values().length);
        for (FeesItemType feesItemType : FeesItemType.values()) {
            MAP.put(feesItemType.value, feesItemType);
        }
    }

    /**
     * 枚举值
     */
    private Integer value;
    /**
     * 枚举叙述
     */
    private String phrase;

    FeesItemType(Integer value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static FeesItemType fromValue(Integer value) {
        return MAP.get(value);
    }

    public Integer value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
