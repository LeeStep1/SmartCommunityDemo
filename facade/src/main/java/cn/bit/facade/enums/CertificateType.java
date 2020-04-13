package cn.bit.facade.enums;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/5
 **/
public enum CertificateType {
    //1:手机MAC；2:蓝牙卡MAC；4:IC卡UID；8:二维码信息号；
    PHONE_MAC(1, "手机MAC"), BLUETOOTH_CARD(2, "蓝牙卡MAC"), IC_CARD(4, "IC卡UID"), QR_CODE(8, "二维码信息号");

    public int KEY;

    public String VALUE;

    CertificateType(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }

    public static String fromValue(int value) {
        switch (value) {
            case 1:
                return PHONE_MAC.VALUE;
            case 2:
                return BLUETOOTH_CARD.VALUE;
            case 4:
                return IC_CARD.VALUE;
            case 8:
                return QR_CODE.VALUE;
            default:
                return null;
        }
    }
}
