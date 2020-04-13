package cn.bit.facade.vo.fees;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ItemRuleRequest implements Serializable {
    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    /**
     * 物业公司ID
     */
    @NotNull(message = "物业公司ID不能为空")
    private ObjectId propertyId;

    /**
     * 收费项目名称
     */
    @NotBlank(message = "收费项目名称不能为空")
    private String itemName;

    /**
     * 是否自动生成账单 default true
     */
    private Boolean isAutoBill;

    /**
     * 计费规则（1：固定单价；2：固定收费；3：自定义；）
     */
    @NotNull(message = "计费规则不能为空")
    private Integer type;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则内容（将设计成JS模板等，目前先填写物业费单价值[保留四位小数]，用于计算）
     */
    private String content;

    /**
     * 单价(保留四位小数)
     */
    @NotNull(message = "单价不能为空")
    private Integer unitPrice;
}
