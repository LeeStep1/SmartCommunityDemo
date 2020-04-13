package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description :
 * @Date ： 2019/9/12 15:03
 */
@Data
public class ElevatorDetailQO implements Serializable {
    /**
     * 终端编码
     */
    private String terminalCode;

    /**
     * 终端端口
     */
    private Integer terminalPort;

    public ElevatorDetailQO(String terminalCode, Integer terminalPort) {
        setTerminalCode(terminalCode);
        setTerminalPort(terminalPort);
    }
}
