package cn.bit.facade.vo.fees;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class PushBillVO implements Serializable
{
    /**
     * 账单id
     */
    private ObjectId id;

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 会计年月yyyy-MM-dd
     */
    private String accountingDate;

    /**
     * 账单状态 -1：未发布 0: 未缴费 1: 已缴费
     */
    private Integer billStatus;

    /**
     * 业主ID
     */
    private ObjectId proprietorId;

    /**
     * 业主姓名
     */
    private String proprietorName;

    /**
     * 单元编号(房屋位置)
     */
    private String roomLocation;

    /**
     * 超期时间 yyyy-MM-dd
     */
    private String overdueDate;

    /**
     * 总价(保留2位小数)
     */
    private String totalAmount;

}
