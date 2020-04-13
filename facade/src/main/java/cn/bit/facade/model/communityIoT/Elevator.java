package cn.bit.facade.model.communityIoT;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class Elevator implements Serializable {
    /**
     * 凭证类型
     */
    private Integer keyType;
    /**
     * 凭证
     */
    private String keyNo;
    /**
     * 电梯名字
     */
    private String name;
    /**
     * 电梯编号
     */
    private String elevatorNum;
    /**
     * 电梯MAC地址
     */
    private String macAddress;
    /**
     * 蓝牙类别  1: 金博 2: 康途
     */
    private Integer macType;
    /**
     * 是否临时
     */
    private Boolean temporary;
    /**
     * 设备运行状态  0：故障； 1：正常运行
     */
    private Integer elevatorStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Elevator elevator = (Elevator) o;
        return this.getElevatorNum().equals(elevator.getElevatorNum());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), elevatorNum);
    }

/*    @Override
    public int compareTo(Elevator o) {
        return this.getElevatorNum().compareTo(o.getElevatorNum());
    }*/
}
