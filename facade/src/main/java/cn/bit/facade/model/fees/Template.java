package cn.bit.facade.model.fees;

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

/**
 * 账单模板实体
 */
@Data
@Document(collection = "FEES_TEMPLATE")
@CompoundIndex(def = "{'communityId' : 1, 'name' : 1}", background = true)
public class Template implements Serializable {

    @Id
    @NotNull(message = "ID不能为空", groups = {Update.class})
    private ObjectId id;

    /**
     * 模板名称
     */
    @NotBlank(message = "名称不能为空", groups = Add.class)
    private String name;

    /**
     * 账单备注
     */
    private String remark;

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
