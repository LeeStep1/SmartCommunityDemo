package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ElevatorRecordResponse implements Serializable {
    /**
     * 电梯分块集合
     */
    private List<Section> elevatorSections;
}
