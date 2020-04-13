package cn.bit.facade.vo.communityIoT.door;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class DoorInfoResult implements Serializable {
    private Set<DoorInfo> data;

    private int errorCode; //获取错误码

    private String errorMsg;

    private boolean success = false;
}
