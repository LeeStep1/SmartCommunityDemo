package cn.bit.facade.vo.communityIoT.elevator;

import cn.bit.framework.data.common.Page;
import lombok.Data;

import java.io.Serializable;

@Data
public class ElevatorPageResult implements Serializable {

    private Page<ElevatorVO> data;  //获取调用返回值

    private int errorCode; //获取错误码

    private String errorMsg;

    private boolean success = false;

}
