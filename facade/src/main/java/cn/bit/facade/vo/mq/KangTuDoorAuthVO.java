package cn.bit.facade.vo.mq;

import cn.bit.facade.vo.communityIoT.door.DoorDeviceVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class KangTuDoorAuthVO extends DoorAuthVO implements Serializable {

    /**
     * 门禁设备
     */
    private Set<DoorDeviceVO> doorDevices;

}
