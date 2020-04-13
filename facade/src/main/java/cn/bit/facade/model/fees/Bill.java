package cn.bit.facade.model.fees;

import cn.bit.facade.vo.RemarkVO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 物业账单实体
 */
@Data
@Document(collection = "FEES_BILL")
@CompoundIndexes({
        @CompoundIndex(def = "{'communityId' : 1, 'roomId' : 1}", background = true),
        @CompoundIndex(def = "{'communityId' : 1, 'buildingId' : 1}", background = true)})
public class Bill implements Serializable {

    @Id
    @NotNull(message = "账单ID不能为空", groups = Modify.class)
    private ObjectId id;

    /**
     * 名称
     */
    @NotBlank(message = "账单名称不能为空", groups = Add.class)
    private String name;

    /**
     * 账单备注
     */
    private String remark;

    /**
     * 账单状态 -1：未通知 0: 待缴费 1: 已缴费
     *
     * @see cn.bit.facade.enums.BillStatusType
     */
    private Integer status;

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
    @Indexed(background = true)
    private ObjectId communityId;

    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空", groups = Add.class)
    private ObjectId roomId;

    /**
     * 单元编号(房屋位置)
     */
    private String roomLocation;

    /**
     * 物业公司ID
     */
    private ObjectId companyId;

    /**
     * 创建人ID
     */
    private ObjectId creator;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改人ID
     */
    private ObjectId modifier;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 交易订单id
     */
    @Indexed(background = true)
    private Long tradeId;

    /**
     * 总价(保留2位小数)
     */
    private Long totalPrice;

    /**
     * 缴费人
     */
    private String payUser;

    /**
     * 收款方式（1：线上收费；2：人工收费）
     */
    private Integer receiveWay;

    /**
     * 缴费时间
     */
    private Date payAt;

    /**
     * 缴费备注
     */
    private RemarkVO payRemark;

    public interface Add {
    }

    public interface Modify {
    }
}
