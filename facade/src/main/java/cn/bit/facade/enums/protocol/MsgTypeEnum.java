package cn.bit.facade.enums.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备协议节点信息类型
 *
 * @author decai.liu
 * @date 2018-08-21
 */
public enum MsgTypeEnum {
    COMMUNITY(0, "社区设备"),
    ZONE(1, "区域设备"),
    UNIT(2, "单元设备"),
    BUILDING(3, "楼栋设备");

    private static final Map<Integer, MsgTypeEnum> MAP;

    static {
        MAP = new HashMap<>(MsgTypeEnum.values().length);
        for (MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()) {
            MAP.put(msgTypeEnum.value, msgTypeEnum);
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

    private MsgTypeEnum(Integer value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static MsgTypeEnum fromValue(Integer value) {
        return MAP.get(value);
    }

    public Integer value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
