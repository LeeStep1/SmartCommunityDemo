package cn.bit.facade.vo.community;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 楼宇查询
 */
@Data
public class BuildingRequest implements Serializable {

    /**
     * 账单状态(0：待缴费，1：已缴费，2：已超期)
     */
    private Integer billStatus;

    /**
     * 账单日期(yyyy-MM-dd)，返回当月的账单统计
     */
    private Date billDate;
}
