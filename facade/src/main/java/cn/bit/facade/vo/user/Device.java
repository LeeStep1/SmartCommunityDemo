package cn.bit.facade.vo.user;

import java.io.Serializable;

/**
 * 设备
 */
public class Device implements Serializable {
    /**
     * 设备ID
     */
    private Long id;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备序列号
     */
    private String serial_no;
    /**
     * 设备编号
     */
    private String device_code;
    /**
     * 设备运行状态
     */
    private int online_status;
    /**
     * 云对讲设备id
     */
    private Long yun_device_id;
    /**
     * 蓝牙PIN码
     */
    private String pin;
    /**
     * 创建时间
     */
    private String create_time;

    /**
     * 设备类型（1：蓝牙开门器；2：微信蓝牙开门器；3：大门公共终端；4：云对讲设备）
     */
    private String item_type_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial_no() {
        return serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public String getDevice_code() {
        return device_code;
    }

    public void setDevice_code(String device_code) {
        this.device_code = device_code;
    }

    public int getOnline_status() {
        return online_status;
    }

    public void setOnline_status(int online_status) {
        this.online_status = online_status;
    }

    public Long getYun_device_id() {
        return yun_device_id;
    }

    public void setYun_device_id(Long yun_device_id) {
        this.yun_device_id = yun_device_id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
