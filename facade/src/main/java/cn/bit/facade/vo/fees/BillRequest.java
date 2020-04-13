package cn.bit.facade.vo.fees;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
public class BillRequest implements Serializable {
    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    /**
     * 物业ID
     */
    private ObjectId propertyId;

    /**
     * 房屋ID
     */
    private ObjectId roomId;

    /**
     * 房号
     */
    private String roomNo;

    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 账单状态 -1：未发布 0: 未缴费 1: 已缴费
     */
    private Set<Integer> billStatusSet;

    /**
     * 业主ID
     */
    private ObjectId proprietorId;

    /**
     * 收款方式（1：线上收费；2：人工收费）
     */
    private Integer receiveWay;

    /**
     * 出单时间 yyyyMMdd
     */
    private Date makeBillAt;

    /**
     * 缴费日期 yyyyMMdd
     */
    private Date payAt;
}
