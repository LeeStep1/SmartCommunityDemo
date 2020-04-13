package cn.bit.facade.vo.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class FreeViewDoorAuthVO extends DoorAuthVO implements Serializable {
    private Set<String> outRoomCodes;

    private Set<String> delOutRoomCodes;

    private String tenantCode;
}
