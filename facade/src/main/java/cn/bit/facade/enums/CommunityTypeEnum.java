package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 社区类型枚举类
 *
 * @author jianming.fan
 * @date 2018-07-06
 */
public enum CommunityTypeEnum {
    /**
     * 住宅
     */
    RESIDENCE(1, "住宅"),
    /**
     * 办公楼
     */
    OFFICE(2, "办公楼"),
    /**
     * 学校
     */
    SCHOOL(3, "学校");

    private static final Map<Integer, CommunityTypeEnum> ENUM_MAP;

    static {
        ENUM_MAP = new HashMap<>(CommunityTypeEnum.values().length);
        for (CommunityTypeEnum type : CommunityTypeEnum.values()) {
            ENUM_MAP.put(type.value, type);
        }
    }

    /**
     * 枚举值
     */
    private int value;
    /**
     * 枚举叙述
     */
    private String phrase;

    private CommunityTypeEnum(int value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static CommunityTypeEnum fromValue(int value) {
        return ENUM_MAP.get(value);
    }

    public int value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
