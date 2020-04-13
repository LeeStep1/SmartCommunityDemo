package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;

@Data
public class ElevatorSummaryResponse implements Serializable {
    /**
     * 总数
     */
    Long total;
    /**
     * 故障数量
     */
    Long faultCount;
}
