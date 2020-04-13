package cn.bit.facade.enums;

/**
 * 收费规则类型
 */
public enum FeesRuleType {

    CONSTANTUNITPRICE(1,"单价*面积"), CONSTANTFEES(2,"固定收费"), CUSTOM(3,"自定义");

    private Integer key;
    private String value;

    FeesRuleType(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    public Integer getKey(){
        return this.key;
    }

    public String getValue(){
        return this.value;
    }
}
