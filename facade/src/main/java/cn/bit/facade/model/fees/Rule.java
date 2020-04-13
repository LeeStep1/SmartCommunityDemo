package cn.bit.facade.model.fees;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "FEES_RULE")
public class Rule implements Serializable {
    @Id
    @NotNull(message = "收费规则ID不能为空", groups = {Update.class})
    private ObjectId id;

    /**
     * 规则名称
     */
//    @NotBlank(message = "规则名称不能为空", groups = {AddOne.class, AddAll.class})
    private String name;

    /**
     * 规则内容（将设计成JS模板等，目前先填写物业费单价值[保留四位小数]，用于计算）
     */
    @NotBlank(message = "规则内容不能为空", groups = {AddOne.class, AddAll.class})
    private String content;

    /**
     * 单价(保留四位小数)
     */
    @NotNull(message = "价格不能为空", groups = {AddOne.class, AddAll.class, Update.class})
    private Integer unitPrice;

    /**
     * 收费项目ID
     */
    @NotNull(message = "收费项目ID不能为空", groups = {AddOne.class, AddAll.class})
    private ObjectId feeItemId;

    /**
     * 收费项目名称
     */
    private String feeItemName;

    /**
     * 楼栋id
     */
    @Indexed(background = true)
    @NotNull(message = "楼栋id不能为空", groups = {AddOne.class})
    private ObjectId buildingId;

    /**
     * 楼栋名称
     */
    private String buildingName;

    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {AddOne.class, AddAll.class})
    private ObjectId communityId;

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

    public interface AddOne {

    }
    public interface AddAll {
    }

    public interface Update {
    }
}
