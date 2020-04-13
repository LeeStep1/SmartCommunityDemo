package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HouseholdResponse implements Serializable {
    /**
     * 住户总数
     */
    private Long total;
    /**
     * 住户类型分块
     */
    private List<Section> relationshipSections;
    /**
     * 年龄分块集合
     */
    private List<Section> ageSections;
    /**
     * 性别分块集合
     */
    private List<Section> sexSections;
}
