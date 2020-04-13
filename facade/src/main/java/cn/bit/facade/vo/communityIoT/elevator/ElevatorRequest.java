package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author jianjun.cheng
 * @description 通过楼房Id和蓝牙mac地址查询电梯列表
 * @create 2018-02-11
 **/
@Data
public class ElevatorRequest implements Serializable {

    @NotBlank(message = "电梯Id不能为空")
    private String elevatorId;
}
