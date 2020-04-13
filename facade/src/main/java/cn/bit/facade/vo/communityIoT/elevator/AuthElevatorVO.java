package cn.bit.facade.vo.communityIoT.elevator;

import cn.bit.facade.model.communityIoT.Elevator;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AuthElevatorVO implements Serializable {

    private List<Elevator> elevatorInfo;

    private String keyId;
}
