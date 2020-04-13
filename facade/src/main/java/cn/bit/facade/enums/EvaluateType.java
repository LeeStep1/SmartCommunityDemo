package cn.bit.facade.enums;

/**
 * 评价枚举
 */
public enum EvaluateType {

    NOEVALUATION(0, "未评价"), EVALUATION(1, "已评价");

    public int key;

    public String value;

    EvaluateType(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
