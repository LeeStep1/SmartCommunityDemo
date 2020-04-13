package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 禁言请求参数
 */
@Data
public class SilentVO implements Serializable {
    @NotNull(message = "被禁言用户ID不能为空")
    private ObjectId silentUserId;

    @NotNull(message = "禁言有效时长不能为空")
    private Long silentMinutes;
}
