package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FaultResponse implements Serializable {
    /**
     * 总数（起）
     */
    private Long total;
    /**
     * 完成数量（起）
     */
    private Long finishCount;
    /**
     * 类型分块集合
     */
    private List<cn.bit.facade.vo.statistics.Section> typeSections;

    /**
     * 状态分块集合
     */
    private List<cn.bit.facade.vo.statistics.Section> statusSections;

    /**
     * 每日分块集合
     */
    private List<cn.bit.facade.vo.statistics.Section> dailySections;

    /**
     * 维修工分块集合
     */
    private List<Section> repairSections;

    @Data
    public static class Section extends cn.bit.facade.vo.statistics.Section {
        /**
         * 平均得分
         */
        private String avgScore;
    }
}
