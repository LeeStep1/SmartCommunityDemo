package cn.bit.facade.enums;

public enum LoginStatusType {

    OFFLINE("离线"), ONLINE("在线");

    private String value;

    LoginStatusType(String value){
        this.value = value;
    }

    public String value() {
        return value;
    }
}
