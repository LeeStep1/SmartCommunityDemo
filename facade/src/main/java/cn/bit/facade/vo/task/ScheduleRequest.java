package cn.bit.facade.vo.task;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class ScheduleRequest implements Serializable {

    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    private ObjectId userId;

    private String postCode;

    private Integer dataStatus;

    private Date startDate;

    private Date endDate;

    /**
     * 用于获取某时间正在值班人员
     */
    private Date dutyTime;

}
