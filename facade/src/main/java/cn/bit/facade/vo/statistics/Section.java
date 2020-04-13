package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;

/**
 * 分块，用作统计分组的返回
 */
@Data
public class Section implements Serializable {
    /**
     * 名称
     */
    private String name;
    /**
     * 图标
     */
    private String icon;
    /**
     * 总数
     */
    private Long count;
    /**
     * 占比
     */
    private String proportion;
}
