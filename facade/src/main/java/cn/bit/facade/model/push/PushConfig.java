package cn.bit.facade.model.push;

import cn.bit.facade.enums.push.PushPointEnum;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "PUSH_CONFIG")
public class PushConfig implements Serializable {

    @Id
    private ObjectId id;

    @NotNull(message = "物业公司ID不能为空")
    @Indexed(background = true)
    private ObjectId companyId;

    @NotNull(message = "推送模板ID不能为空")
    private ObjectId tmplId;

    /**
     * {@link PushPointEnum#name()}
     */
    @NotBlank(message = "推送节点不能为空")
    private String pointId;

    /**
     * {@link PushPointEnum#value()}
     */
    @NotBlank(message = "推送节点名称不能为空")
    private String pointName;

    /**
     * 推送目标范围
     */
    private Set<String> targets;

    private Date createAt;

    private Date updateAt;

    private Integer dataStatus;
}
