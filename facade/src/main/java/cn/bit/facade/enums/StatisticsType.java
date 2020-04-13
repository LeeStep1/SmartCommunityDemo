package cn.bit.facade.enums;

import lombok.val;

/**
 * Created by fxiao
 * on 2018/3/27
 * 统计类型
 */
public enum StatisticsType {

    HOUSEHOLD(1, "住户"), FAULT(2, "故障"), REVENUE(3, "营收"), DEVICE(4, "设备");

    public Integer key;

    public String value;

    StatisticsType(Integer key, String value) {
        this.key = key;
        this.value = value;
    }
}
