package cn.bit.facade.enums.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 协议版本枚举类
 *
 * @author decai.liu
 * @date 2018-08-17
 */
public enum ProtocolVersionEnum {

    P_4B55("1.0", "Protocol4B55");

    private static final Map<String, ProtocolVersionEnum> MAP;

    static {
        MAP = new HashMap<>(ProtocolVersionEnum.values().length);
        for (ProtocolVersionEnum protocolVersionEnum : ProtocolVersionEnum.values()) {
            MAP.put(protocolVersionEnum.value, protocolVersionEnum);
        }
    }

    /**
     * 枚举值
     */
    private String value;
    /**
     * 枚举叙述
     */
    private String phrase;

    private ProtocolVersionEnum(String value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static ProtocolVersionEnum fromValue(String value) {
        return MAP.get(value);
    }

    public String value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
