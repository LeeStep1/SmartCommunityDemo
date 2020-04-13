package cn.bit.facade.enums;

import cn.bit.framework.utils.string.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 账单状态
 */
public enum BillStatusType {

    UNPUBLISHED(-1,"未通知"),
    UNPAYMENT(0,"待缴费"),
    PAYMENT(1,"已缴费"),
    //这个项只用于前端展示不入库
    OVERDUE(2,"待缴费(已超期)");

    private Integer key;
    private String value;

    private static Map<Integer,String> map = new HashMap<>();

    static {
        for (BillStatusType billStatusType: BillStatusType.values()) {
            map.put(billStatusType.getKey(),billStatusType.getValue());
        }
    }

    BillStatusType(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    public Integer getKey(){
        return this.key;
    }

    public String getValue(){
        return this.value;
    }

//    public static String getValueByKey(Integer key){
//        return map.get(key);
//    }

    public static boolean checkBillStatus(Integer billStatus) {
        if(billStatus == 2){//不入库的状态
            return Boolean.FALSE;
        }
        return StringUtil.isNotNull(map.get(billStatus));
    }
}
