package cn.bit.facade.model.fees;

import cn.bit.facade.enums.fees.FeesItemType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 账单收费项目详情实体
 */
@Data
@Document(collection = "FEES_BILL_DETAIL")
public class BillDetail implements Serializable {

    @Id
    @NotNull(message = "账单项目ID不能为空", groups = {Update.class})
    private ObjectId id;

    /**
     * 账单ID/模板ID
     */
    @NotNull(message = "关联ID不能为空", groups = {Add.class})
    @Indexed(background = true)
    private ObjectId relateId;

    /**
     * 来源（1：账单，2：模板）
     */
    private Integer source;

    /**
     * 源收费项目ID
     */
    private ObjectId itemId;

    /**
     * 收费项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = {Add.class})
    private String itemName;

    /**
     * 项目类型（1：固定收费；2：单价计费；3：自定义；）
     *
     * @see FeesItemType
     */
    @NotNull(message = "项目类型不能为空", groups = {Add.class})
    private Integer itemType;

    /**
     * 单价(保留2位小数)，前端传参放大100倍
     */
    private Integer unitPrice;

    /**
     * 计费单位
     */
    private String units;

    /**
     * 数量(保留2位小数)，前端传参放大100倍
     */
    private Integer quantity;

    /**
     * 收费价格(保留2位小数)，前端传参放大100倍
     */
    private Integer totalPrice;

    /**
     * 创建人ID
     */
    private ObjectId creator;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 最后修改时间
     */
    private Date updateAt;

    public interface Add {
    }

    public interface Update {
    }
}
