package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 投诉工单状态枚举类
 *
 * Created by decai.liu
 * at 2019/1/16
 */
public enum ComplainStatusEnum {

    TO_ACCEPT(0, "待受理"),
    PENDING(1, "待处理"),
    PROCESSED(2, "待评价"),
    EVALUATED(3, "已评价"),
    REJECTED(-1, "已驳回");

    public int value;

    public String phrase;

    private static Map<Integer, String> map = new HashMap();

    static {
        for (ComplainStatusEnum complainStatusEnum : ComplainStatusEnum.values()){
            map.put(complainStatusEnum.value, complainStatusEnum.phrase);
        }
    }

    ComplainStatusEnum(int value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    public static ComplainStatusEnum getByValue(int value){
        for (ComplainStatusEnum complainStatusEnum : values()) {
            if (complainStatusEnum.value == value) {
                return complainStatusEnum;
            }
        }
        return null;
    }

    public static String getPhrase(Integer value){
        return map.get(value) == null ? "未知状态" : map.get(value);
    }
}
