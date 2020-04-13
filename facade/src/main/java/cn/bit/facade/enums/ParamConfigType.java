package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置项的类型
 * parameter type
 */
public enum ParamConfigType {

    BILL(1, "物业账单配置"),
    MOMENT(2, "社区动态配置"),
    HOUSEHOLD_AUTH(3, "住房认证配置"),
    COMPLAIN(4, "投诉报事配置"),
    OTHER(9, "其他配置");

    private Integer key;
    private String value;

    private static Map<Integer,String> map = new HashMap<>();

    static {
        for (ParamConfigType paramConfigType : ParamConfigType.values()) {
            map.put(paramConfigType.getKey(), paramConfigType.getValue());
        }
    }

    ParamConfigType(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
    public Integer getKey(){
        return this.key;
    }
}
