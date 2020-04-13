package cn.bit.facade.vo.fees;

import cn.bit.facade.model.fees.BillDetail;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 收费模板传输实体
 */
@Data
public class TemplateVO implements Serializable {

    @Id
    @NotNull(message = "收费套餐ID不能为空", groups = Modify.class)
    private ObjectId id;

    /**
     * 名称
     */
    @NotBlank(message = "套餐名称不能为空", groups = Add.class)
    private String name;

    /**
     * 账单备注
     */
    private String remark;

    /**
     * 社区ID
     */
    private ObjectId communityId;

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
     * 修改时间
     */
    private Date updateAt;

    /**
     * 收费项目
     */
    @NotEmpty(message = "收费项目不能为空", groups = Add.class)
    private List<BillDetail> charges;

    public interface Add {
    }

    public interface Modify {
    }
}
