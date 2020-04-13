package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备协议类型，在线还是离线
 *
 * @author decai.liu
 * @date 2018-08-17
 */
public enum ProtocolTypeEnum {
    ONLINE(1, "在线协议"),
    OFFLINE(0, "离线协议");

    private static final Map<Integer, ProtocolTypeEnum> MAP;

    static {
        MAP = new HashMap<>(ProtocolTypeEnum.values().length);
        for (ProtocolTypeEnum protocolVersionEnum : ProtocolTypeEnum.values()) {
            MAP.put(protocolVersionEnum.value, protocolVersionEnum);
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

    private ProtocolTypeEnum(Integer value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static ProtocolTypeEnum fromValue(Integer value) {
        return MAP.get(value);
    }

    public Integer value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
