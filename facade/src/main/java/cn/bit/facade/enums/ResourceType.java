package cn.bit.facade.enums;

public enum ResourceType {

    API(1), MENU(2);

    private int value;

    ResourceType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
