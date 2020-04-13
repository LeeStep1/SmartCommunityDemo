package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TenantApplicationResponse implements Serializable {
    /**
     * 总数（次）
     */
    private Long total;
    /**
     * 日期分块集合
     */
    private List<Section> daySections;
}
