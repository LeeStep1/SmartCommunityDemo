package cn.bit.facade.enums;

public enum ClientType {

    HOUSEHOLD(1000), PROPERTY(1001), MANAGER_BACKSTAGE(1002), PARTNER_OMS(2000), BUSINESS(3000);

    private int value;

    ClientType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static ClientType fromValue(int value) {
        switch (value) {
            case 1000 :
                return HOUSEHOLD;
            case 1001:
                return PROPERTY;
            case 1002:
                return MANAGER_BACKSTAGE;
            case 2000:
                return PARTNER_OMS;
            case 3000:
                return BUSINESS;
            default:
                return null;
        }
    }

}
