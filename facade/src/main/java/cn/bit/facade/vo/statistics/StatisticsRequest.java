package cn.bit.facade.vo.statistics;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class StatisticsRequest implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private Date startAt;
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private Date endAt;
}
