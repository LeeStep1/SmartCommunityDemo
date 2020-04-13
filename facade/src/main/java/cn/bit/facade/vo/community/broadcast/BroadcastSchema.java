package cn.bit.facade.vo.community.broadcast;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 广播设备属性
 */
@Data
public class BroadcastSchema implements Serializable {

    /**
     * 广播类型（normal：正常, analog：模拟信号, mix：混合广播）
     */
    private String type;

    /**
     * 正常广播-设备协议
     */
    private List<DeviceSchema> deviceProtocols;

}
