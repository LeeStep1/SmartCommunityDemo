package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 品牌厂商枚举类
 *
 * @author decai.liu
 * @date 2019-01-17
 */
public enum BrandsEnum {
    // 门禁厂商 door + brandNo
    MI_LI("door1", "米立"),
    KANG_TU("door2", "康途"),
    JIN_BO("door3", "金博"),
    // 摄像头品牌厂商 camera + brandNo
    UNIVIEW("camera1", "宇视"),
    EZVIZ("camera2", "萤石");

    private static final Map<String, BrandsEnum> MAP;

    static {
        MAP = new HashMap<>(BrandsEnum.values().length);
        for (BrandsEnum brandsEnum : BrandsEnum.values()) {
            MAP.put(brandsEnum.value, brandsEnum);
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

    BrandsEnum(String value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static BrandsEnum fromValue(String value) {
        return MAP.get(value);
    }

    public String value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
