package cn.bit.facade.data.fees;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
public class PropertyBill extends BaseEntity {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 房间ID
     */
    private ObjectId roomId;
    /**
     * 账单状态 -1：未发布 0: 未缴费 1: 已缴费
     */
    private Integer status;
    /**
     * 总价(保留2位小数)
     */
    private Long totalAmount;
    /**
     * 超期时间
     */
    private Date expireAt;
}
