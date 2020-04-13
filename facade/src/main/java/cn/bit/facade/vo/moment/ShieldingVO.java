package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 屏蔽请求参数
 */
@Data
public class ShieldingVO implements Serializable {

    @NotNull(message = "言论的ID不能为空")
    private ObjectId speechId;

    @NotNull(message = "言论类型不能为空")
    private Integer type;

    /**
     * 举报原因
     */
    @NotBlank(message = "原因不能为空")
    private String reason;
}
