package cn.bit.facade.vo.task;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
public class RecordRequest implements Serializable {

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 员工用户ID
     */
    private ObjectId userId;

    /**
     * 员工名称
     */
    private String userName;

    /**
     * 任务类型，1:巡更, 2:保洁
     */
    private Integer taskType;

    /**
     * 起始时间
     */
    private Date startDate;

    /**
     * 结束时间
     */
    private Date endDate;

    /**
     * 角色
     */
    private String role;


    /**
     * 员工用户ID集合
     */
    private Set<ObjectId> userIds;
}
