package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;

@Data
public class PropertyBillSummaryResponse implements Serializable {
    /**
     * 有效账单数
     */
    private Long validCount;
    /**
     * 应收总额(保留2位小数)
     */
    private Long validAmount;
    /**
     * 已缴费账单数
     */
    private Long paidCount;
    /**
     * 已缴费金额(保留2位小数)
     */
    private Long paidAmount;
    /**
     * 待缴费账单数
     */
    private Long unpaidCount;
    /**
     * 待缴费金额(保留2位小数)
     */
    private Long unpaidAmount;
}
