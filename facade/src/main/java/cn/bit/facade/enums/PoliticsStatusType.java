package cn.bit.facade.enums;

/**
 * 政治党派
 */
public enum PoliticsStatusType {

    Masses(1, "群众"), Communist(2, "中共党员"), League(3, "共青团员"), Democratic(4, "民主党派"), Other(5, "其他");

    public Integer key;

    public String value;

    PoliticsStatusType(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    public static String getValue(Integer key) {
        PoliticsStatusType[] politicsStatusTypes = values();
        for (PoliticsStatusType politicsStatusType : politicsStatusTypes) {
            if (politicsStatusType.getKey().equals(key)) {
                return politicsStatusType.getValue();
            }
        }
        return null;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
