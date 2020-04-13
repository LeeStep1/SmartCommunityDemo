package cn.bit.facade.enums;

public enum GoodsType {

    VIRTUAL(0), MATERIAL(1);

    private int value;

    GoodsType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
