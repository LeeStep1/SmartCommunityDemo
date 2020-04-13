package cn.bit.facade.vo.property;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class NoticeRequest implements Serializable {
    /**
     * 社区id
     */
    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;
    /**
     * 开始时间
     */
    private Date startAt;
    /**
     * 结束时间
     */
    private Date endAt;
}
