package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间度量单位枚举
 *
 * @author decai.liu
 * @date 2018-11-15
 */
public enum TimeUnitEnum {
    MILLISECOND(0),
    SECOND(1),
    MINUTE(2),
    HOUR(3),
    DAY(4),
    MONTH(5),
    YEAR(6);

    private static final Map<Integer, TimeUnitEnum> MAP;

    static {
        MAP = new HashMap<>(TimeUnitEnum.values().length);
        for (TimeUnitEnum protocolVersionEnum : TimeUnitEnum.values()) {
            MAP.put(protocolVersionEnum.value, protocolVersionEnum);
        }
    }

    /**
     * 枚举值
     */
    private Integer value;

    private TimeUnitEnum(Integer value) {
        this.value = value;
    }

    public static TimeUnitEnum fromValue(Integer value) {
        return MAP.get(value);
    }

    public Integer value() {
        return value;
    }
}
