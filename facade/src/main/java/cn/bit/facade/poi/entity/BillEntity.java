package cn.bit.facade.poi.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;

/**
 * 物业账单导出实体
 */
@Data
public class BillEntity implements Serializable {

    @Excel(name = "id", width = 1, isColumnHidden = true)
    private String id;

    /**
     * 房间地址
     */
    @Excel(name = "房屋", width = 20)
    private String roomLocation;

    /**
     * 账单名称
     */
    @Excel(name = "账单名称", width = 25)
    private String billName;

    /**
     * 账单状态 -1：未通知 0: 待缴费 1: 已缴费
     * 暂时不需要导出此字段
     *
     * @see cn.bit.facade.enums.BillStatusType
     */
//    @Excel(name = "缴费状态", width = 10, replace = {"未通知_-1", "待缴费_0", "已缴费_1"}, isColumnHidden = true)
    private String status;

    /**
     * 业主姓名
     * 暂时不需要导出此字段
     */
//    @Excel(name = "业主", width = 10, isColumnHidden = true)
    private String proprietorName;

    /**
     * 收费项目名称
     */
    @Excel(name = "收费项目", width = 25)
    private String itemName;

    /**
     * 项目类型（1：固定收费；2：单价计费；3：自定义；）
     * 暂时不需要导出此字段
     */
//    @Excel(name = "计费类型", width = 10, replace = {"固定收费_1", "单价计费_2", "自定义_3"})
    private String itemType;

    /**
     * 单价(保留2位小数)
     */
    @Excel(name = "单价", width = 10)
    private String unitPrice;

    /**
     * 计费单位
     */
    @Excel(name = "单位", width = 10)
    private String units;

    /**
     * 数量(保留2位小数)
     */
    @Excel(name = "数量", width = 10)
    private String quantity;

    /**
     * 价格(保留2位小数)
     */
    @Excel(name = "价格", width = 10)
    private String price;

    /**
     * 总价(保留2位小数)
     */
    @Excel(name = "合计", width = 10)
    private String total;
}
