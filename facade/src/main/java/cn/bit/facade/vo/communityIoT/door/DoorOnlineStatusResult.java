package cn.bit.facade.vo.communityIoT.door;

import cn.bit.facade.vo.communityIoT.door.freeview.DoorOnlineStatusVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class DoorOnlineStatusResult implements Serializable {
    private Set<DoorOnlineStatusVO> data;

    private int errorCode; //获取错误码

    private String errorMsg;

    private boolean success = false;
}
