package cn.bit.facade.vo.mq;

import cn.bit.facade.model.communityIoT.Door;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoorAuthVO extends DeviceAuthVO implements Serializable {

    /**
     * 门禁列表
     */
    private List<Door> doorList;
}
