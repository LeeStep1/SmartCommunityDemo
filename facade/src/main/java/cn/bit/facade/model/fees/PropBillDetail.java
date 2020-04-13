package cn.bit.facade.model.fees;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "FEES_PROP_BILL_DETAIL")
@Validated
public class PropBillDetail implements Serializable
{
    @Id
    @NotNull(message = "子账单ID不能为空", groups = {Edit.class})
    private ObjectId id;

    /**
     * 关联的账单ID
     */
    @NotNull(message = "关联账单ID不能为空", groups = {Add.class, Edit.class})
    private ObjectId billId;

    /**
     * 收费项目ID
     */
    @NotNull(message = "收费项目ID不能为空", groups = {Add.class})
    private ObjectId feeItemId;

    /**
     * 收费项目名称
     */
    private String feeItemName;

    /**
     * 上期读数
     */
    private Integer previousRead;

    /**
     * 本期读数
     */
    private Integer currentRead;

    /**
     * 用量
     */
    private Integer used;

    /**
     * 单价(保留四位小数)
     */
    private Integer unitPrice;

    /**
     * 本次费用(保留四位小数)
     */
    @NotNull(message = "本次费用不能为空", groups = {Add.class, Edit.class})
    private Long currentFee;

    /**
     * 往期费用(保留四位小数)
     */
    private Long previousFee;

    /**
     * 违约金(保留四位小数)
     */
    private Long penalties;

    /**
     * 应收小计(保留四位小数)
     */
//    @NotNull(message = "应收小计不能为空", groups = {Add.class})
    private Long subtotal;

    /**
     * 总价(保留2位小数) 前端需要做移位
     */
    private Long totalAmount;

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
     * 计费规则（1：固定单价；2：固定收费；3：自定义；）
     */
    private Integer type;

    /**
     * 数据状态  1：有效；0：无效
     */
    private Integer dataStatus;

    public interface Add{}
    public interface Edit{}
}
