package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class KeyNoListElevatorVOResponse implements Serializable {
    private List<KeyNoListElevatorVO> data;

    private Integer errorCode;

    private String errorMsg;

    private Boolean success;
}
