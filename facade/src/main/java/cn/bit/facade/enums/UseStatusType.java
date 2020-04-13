package cn.bit.facade.enums;

/**
 * 使用状态
 * 放行条、优惠券等
 */
public enum UseStatusType {

    UNUSED(0,"未使用"), USED(1, "已使用"), EXPIRED(-1, "已过期");

    public int key;

    public String value;

    UseStatusType(int key, String value){
        this.key = key;
        this.value = value;
    }

    public static UseStatusType getByValue(int key){
        for (UseStatusType useStatusType : values()) {
            if (useStatusType.key == key) {
                return useStatusType;
            }
        }
        return null;
    }

}
