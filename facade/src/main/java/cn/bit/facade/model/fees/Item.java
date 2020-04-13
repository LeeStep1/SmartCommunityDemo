package cn.bit.facade.model.fees;

import cn.bit.facade.enums.fees.FeesItemType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "FEES_ITEM")
@CompoundIndex(def = "{'communityId' : 1, 'name' : 1}", background = true)
public class Item implements Serializable {
    @Id
    @NotNull(message = "项目ID不能为空", groups = {Modify.class})
    private ObjectId id;

    /**
     * 收费项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = {Add.class})
    private String name;

    /**
     * 社区ID
     */
    @Indexed(background = true)
    private ObjectId communityId;

    /**
     * 物业公司ID
     */
    private ObjectId companyId;

    /**
     * 项目类型（1：固定收费；2：单价计费；3：自定义；）
     * @see FeesItemType
     */
    @NotNull(message = "项目类型不能为空", groups = {Add.class})
    private Integer type;

    /**
     * 单价(保留2位小数)
     */
    private Integer unitPrice;

    /**
     * 计费单位
     */
    private String units;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 收费价格(保留2位小数)
     */
    private Integer totalPrice;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private ObjectId creator;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 数据状态  1：有效；0：无效
     */
    private Integer dataStatus;

    public interface Add {
    }

    public interface Modify {
    }
}
