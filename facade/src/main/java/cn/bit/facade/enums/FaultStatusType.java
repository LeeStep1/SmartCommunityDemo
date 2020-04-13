package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fxiao
 * on 2018/3/8
 */
public enum FaultStatusType {

    CANCEL(0, "已取消"), WAITACCEPT(1, "待受理"), WAITALOCATION(2, "待分派"), WAITRECONDTION(3, "待检修"), FINISH(4, "已完成"), REJECT(-1, "已驳回");

    public int key;

    public String value;

    private static Map<Integer, String> map = new HashMap();

    static {
        for (FaultStatusType faultStatusType : FaultStatusType.values()){
            map.put(faultStatusType.key, faultStatusType.value);
        }
    }

    FaultStatusType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static FaultStatusType getByValue(int key){
        for (FaultStatusType faultStatusType : values()) {
            if (faultStatusType.key == key) {
                return faultStatusType;
            }
        }
        return null;
    }

    public static String getValueByKey(Integer key){
        return map.get(key) == null ? "未知状态" : map.get(key);
    }
}
