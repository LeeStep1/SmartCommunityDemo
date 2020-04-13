package cn.bit.facade.enums;

/**
 * Created by fxiao
 * on 2018/3/12
 * 用户身份
 */
public enum UserStatus {

    RESIDENT(1, "住户"), PROPERTY(2, "物业");

    public Integer key;

    public String value;

    UserStatus(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

}
