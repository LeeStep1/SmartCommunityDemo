package cn.bit.facade.enums;

/**
 * Created by zuen.su
 * 班次类型
 * @description 用于区分班次的类型
 * @create: 2018/3/29
 **/
public enum ClassType {

    //1：轮班：轮替的方式自动安排班表
    //2：常班：固定上班内容、时间、规律

    SHIFT(1, "轮班"),PEACETIME(2, "常班");

    public int key;

    public String value;

    ClassType(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
