package cn.bit.facade.model.fees;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "FEES_PROP_FEE_ITEM")
public class PropFeeItem implements Serializable
{
    @Id
    @NotNull(message = "项目ID不能为空", groups = {Update.class})
    private ObjectId id;

    /**
     * 收费项目名称
     */
    @NotBlank(message = "收费项目名称不能为空", groups = {Add.class, Update.class})
    private String itemName;

    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {Add.class, Search.class})
    private ObjectId communityId;

    /**
     * 物业公司ID
     */
    @NotNull(message = "物业公司ID不能为空", groups = {Add.class})
    private ObjectId propertyId;

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
     * 是否自动生成账单 default true
     */
    private Boolean isAutoBill;

    /**
     * 计费规则（1：固定单价；2：固定收费；3：自定义；）
     */
    @NotNull(message = "计费规则不能为空", groups = {Add.class})
    private Integer type;

    public interface Add {
    }
    public interface Update {
    }
    public interface Search {
    }
}
