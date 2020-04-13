package cn.bit.facade.enums;

/**
 * 推送状态
 */
public enum PushStatusType {

    PUSHED(1, "已推送"), UNPUSHED(0,"未推送");

    private int key;

    private String value;

    PushStatusType(int key, String value){
        this.key = key;
        this.value = value;
    }

    public int key(){
        return this.key;
    }

    public String value(){
        return this.value;
    }

}
