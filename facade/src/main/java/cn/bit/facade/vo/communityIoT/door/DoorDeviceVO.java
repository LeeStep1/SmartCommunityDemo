package cn.bit.facade.vo.communityIoT.door;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/14
 **/
@Data
@NoArgsConstructor
public class DoorDeviceVO implements Serializable {

    /**
     * 门禁ID
     */
    private String doorId;

    /**
     * 终端编码
     */
    private String terminalCode;

    /**
     * 终端端口
     */
    private Integer terminalPort;

    private String mac;

    private String terminal;

    private Integer brandNo;

    public DoorDeviceVO(String doorId, String terminalCode, Integer terminalPort) {
        super();
        this.doorId = doorId;
        this.terminalCode = terminalCode;
        this.terminalPort = terminalPort;
    }

    public DoorDeviceVO(Integer terminalPort, String mac, String terminal) {
        this.terminalPort = terminalPort;
        this.mac = mac;
        this.terminal = terminal;
    }
}
