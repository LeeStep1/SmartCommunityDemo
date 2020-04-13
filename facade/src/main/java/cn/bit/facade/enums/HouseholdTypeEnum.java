package cn.bit.facade.enums;

/**
 * 户口类型
 */
public enum HouseholdTypeEnum {

    Village(1, "农业户口"), Town(2, "城镇户口");

    public Integer value;

    public String phrase;

    HouseholdTypeEnum(Integer value, String phrase){
        this.value = value;
        this.phrase = phrase;
    }

    public static String getPhrase(Integer value) {
        HouseholdTypeEnum[] politicsStatusTypes = values();
        for (HouseholdTypeEnum politicsStatusType : politicsStatusTypes) {
            if (politicsStatusType.value().equals(value)) {
                return politicsStatusType.phrase();
            }
        }
        return null;
    }

    public Integer value() {
        return value;
    }

    public String phrase() {
        return phrase;
    }
}
