package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cecai.liu
 * at 2018/3/12
 * 故障项目类型
 */
public enum FaultItemType {

    UTILITIES(1, "水电燃气"),
    BUILDINGSTRUCTURE(2, "房屋结构"),
    FIRESECURITY(3,"消防安防"),
    NOMOTHER(9, "个人的其他"),
    ELEVATOR(10,"电梯"),
    DOORCONTROL(11,"门禁"),
    OTHER(99,"其它");

    private int key;

    private String value;

    private static Map<Integer,String> map = new HashMap();

    static {
        for (FaultItemType faultItemType:FaultItemType.values()){
            map.put(faultItemType.key(),faultItemType.value());
        }
    }


    FaultItemType(int key, String value) {
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
        return map.get(key) == null ? OTHER.value : map.get(key);
    }

    public static FaultItemType getEntityByKey(Integer key) {
        for (FaultItemType faultItemType : values()) {
            if (faultItemType.key == key) {
                return faultItemType;
            }
        }
        return null;
    }

}
