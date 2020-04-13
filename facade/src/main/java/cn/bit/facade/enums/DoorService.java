package cn.bit.facade.enums;

public enum DoorService {
    //门禁服务（1：离线；2：在线；0：未知）
    BLUETOOTH(1, "蓝牙开门器"), QR_CODE(4,"二维码开门"), WE_CHAT(5,"微信开门"), REMOTE(7, "远程开门");
    public int KEY;

    public String VALUE;

    DoorService(int key, String value) {
        this.KEY = key;
        this.VALUE = value;
    }


}
