package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cecai.liu
 * at 2018/3/12
 * 故障类型
 */
public enum FaultType {

    HOUSEHOLD(1, "住户"), PUBLIC(2, "公共");

    private Integer key;

    private String value;

    private static Map<Integer,String> map = new HashMap();

    static {
        for (FaultType faultType:FaultType.values()){
            map.put(faultType.key(),faultType.value());
        }
    }

    FaultType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int key(){
        return this.key;
    }

    public String value(){
        return this.value;
    }

    public static String getValueByKey(Integer key){
        return map.get(key) == null ? "未知类型" : map.get(key);
    }

}
