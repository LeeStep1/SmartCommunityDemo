package cn.bit.facade.enums;

/**
 * Created by fxiao
 * on 2018/3/23
 * 待办任务
 */
public enum InHandType {

    FAULT(1, "故障申报");

    public int key;

    public String value;

    InHandType(int key, String value) {
        this.key = key;
        this.value = value;
    }

}
