package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AlarmResponse implements Serializable {
    /**
     * 总数（起）
     */
    private Long total;
//    /**
//     * 处理总数
//     */
//    private Long dealCount;
//    /**
//     * 保安总数
//     */
//    private Integer securityCount;
//    /**
//     * 人均处理数量
//     */
//    private String perCapita;
    /**
     * 保安分块集合
     */
    private List<Section> securitySections;
    /**
     * 时间点（24小时制）集合
     */
    private List<Section> hourSections;
}
