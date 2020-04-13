package cn.bit.facade.vo.community.broadcast;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备属性
 */
@Data
public class DeviceSchema implements Serializable {

    /**
     * 品牌/厂商
     */
    private String brand;

    /**
     * 协议类型（0：离线，1：在线）
     */
    private Integer protocolType;

    /**
     * 协议名称，如：4B55
     */
    private String protocol;

}
