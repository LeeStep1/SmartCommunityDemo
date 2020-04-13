package cn.bit.facade.vo.push;

import cn.bit.facade.enums.push.PushPointEnum;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@Data
public class PushConfigVO implements Serializable {

    @NotNull(message = "ID不能为空", groups = Modify.class)
    private ObjectId id;

    @NotNull(message = "物业公司ID不能为空", groups = Add.class)
    private ObjectId companyId;

    @NotNull(message = "推送模板ID不能为空", groups = Add.class)
    private ObjectId tmplId;

    /**
     * {@link PushPointEnum#name()}
     */
    @NotBlank(message = "推送节点不能为空", groups = Add.class)
    private String pointId;

    /**
     * {@link PushPointEnum#value()}
     */
    private String pointName;

    /**
     * 推送目标范围
     */
    @NotEmpty(message = "推送目标范围不能为空", groups = Add.class)
    private Set<String> targets;

    public interface Add {
    }

    public interface Modify {
    }
}
