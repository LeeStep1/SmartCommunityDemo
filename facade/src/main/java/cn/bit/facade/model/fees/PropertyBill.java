package cn.bit.facade.model.fees;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "FEES_PROPERTY_BILL")
public class PropertyBill implements Serializable
{
    @Id
    private ObjectId id;

    /**
     * 会计年月
     */
    private Date accountingDate;

    /**
     * 出单时间
     */
    private Date makeAt;

    /**
     * 账单状态 -1：未发布 0: 未缴费 1: 已缴费
     */
    private Integer billStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 业主ID
     */
    private ObjectId proprietorId;

    /**
     * 业主姓名
     */
    private String proprietorName;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 房间ID
     */
    private ObjectId roomId;

    /**
     * 单元编号(房屋位置)
     */
    private String roomLocation;

    /**
     * 物业公司ID
     */
    private ObjectId propertyId;

    /**
     * 物业公司名称
     */
    private String propertyName;

    /**
     * 超期时间
     */
    private Date overdueDate;

    /**
     * 创建人ID
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改人ID
     */
    private ObjectId modifierId;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 数据状态  1：有效；0：无效
     */
    private Integer dataStatus;

    /**
     * 总价(保留四位小数)
     */
    private Long totalPrice;

    /**
     * 交易订单id
     */
    @Indexed(background = true)
    private Long tradeId;

    /**
     * 收款方式（1：线上收费；2：人工收费）
     */
    private Integer receiveWay;

    /**
     * 总价(保留2位小数)
     */
    private Long totalAmount;

    /**
     * 物业费title(物业费年月 + roomLocation)
     */
    private String title;

    /**
     * 缴费时间
     */
    private Date payAt;

}
