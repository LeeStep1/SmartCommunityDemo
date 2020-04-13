package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;

/**
 * 区段，用作统计的入参
 */
@Data
public class Region implements Serializable, Comparable<Region> {
    /**
     * 名称
     */
    private String name;
    /**
     * 起始
     */
    private Comparable from;
    /**
     * 结束
     */
    private Comparable to;

    @Override
    public int compareTo(Region o) {
        return from == null || o.to == null ? -1
                : to == null || o.from == null ? 1
                : from.getClass() != o.from.getClass() ? 0
                : from.compareTo(o.from);
    }
}
