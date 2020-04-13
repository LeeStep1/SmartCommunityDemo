package cn.bit.facade.enums;

/**
 * 收款方式
 */
public enum ReceiveWayType {

    ONLINE(1, "线上收费"),
    CASH(2, "现金"),
    EFT(3, "转账"),
    WECHAT(4, "微信"),
    ALIPAY(5, "支付宝"),
    OTHER(6, "其他");

    private Integer key;
    private String value;

    ReceiveWayType(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
