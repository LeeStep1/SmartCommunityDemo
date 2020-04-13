package cn.bit.facade.vo.communityIoT.elevator;

import cn.bit.facade.model.user.Card;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description :
 * @Date ： 2019/10/30 16:48
 */
@Data
@NoArgsConstructor
public class CertificateElevatorPartKeepRequest implements Serializable {
    /**
     * 终端号
     */
    private List<String> terminalCodes;

    private List<Integer> floors;

    private String keyNo;

    private String keyId;

    private Integer keyType;

    public CertificateElevatorPartKeepRequest(List<String> terminalCodes, Card card) {
        setTerminalCodes(terminalCodes);
        setKeyId(card.getKeyId());
        setKeyNo(card.getKeyNo());
        setKeyType(card.getKeyType());
    }
}
