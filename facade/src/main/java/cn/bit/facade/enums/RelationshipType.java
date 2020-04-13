package cn.bit.facade.enums;

/**
 * 用户关系
 */
public enum RelationshipType {
    // （1：业主；2：家属；3：租客）
    OWNER(1, "业主"), RELATION(2, "家属"), TENANT(3, "租客");

    public Integer KEY;

    public String VALUE;

    RelationshipType(Integer key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }

    public static String getValueByKey(int key){
        if(key == OWNER.KEY){
            return OWNER.VALUE;
        }
        if(key == RELATION.KEY){
            return RELATION.VALUE;
        }
        if(key == TENANT.KEY){
            return TENANT.VALUE;
        }
        return "";
    }

}
