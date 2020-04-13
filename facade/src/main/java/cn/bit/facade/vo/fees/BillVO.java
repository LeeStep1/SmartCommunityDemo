package cn.bit.facade.vo.fees;

import cn.bit.facade.enums.ReceiveWayType;
import cn.bit.facade.model.fees.BillDetail;
import cn.bit.facade.vo.RemarkVO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 物业账单传输实体
 */
@Data
public class BillVO implements Serializable {

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
     * 收费账单的ID集合
     */
    @NotEmpty(message = "至少需要包含一个账单", groups = Pay.class)
    private List<ObjectId> billIds;

    /**
     * 缴费人
     */
    @NotBlank(message = "缴费人不能为空", groups = Pay.class)
    private String payUser;

    /**
     * 收款方式（1：线上收费；2：现金；3：转账；4：微信；5：支付宝；6：其他）
     * @see ReceiveWayType
     */
    @NotNull(message = "缴费方式不能为空", groups = Pay.class)
    private Integer receiveWay;

    /**
     * 缴费时间
     */
    private Date payAt;

    /**
     * 缴费备注
     */
    private RemarkVO payRemark;

    /**
     * 收费项目
     */
    @NotEmpty(message = "收费项目不能为空", groups = Add.class)
    private List<BillDetail> charges;

    /**
     * 是否保存为收费模板
     */
    private Boolean saveTemplate = false;

    /**
     * 是否通知业主缴费
     */
    private Boolean notify = false;

    public interface Add {
    }

    public interface Modify {
    }

    public interface Pay {
    }
}
