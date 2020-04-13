package cn.bit.facade.model.task;

import cn.bit.framework.utils.DateUtils;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 物业排班表
 */
@Data
@Document(collection = "TASK_SCHEDULE")
@CompoundIndex(def = "{'communityId' : 1, 'workDate' : 1}", background = true)
public class Schedule implements Serializable {
    @Id
    @NotNull(message = "班表ID不能为空", groups = {Update.class})
    private ObjectId id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {Add.class})
    private ObjectId userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 物业人员工号
     */
    private String employeeId;

    /**
     * 岗位
     */
    @NotBlank(message = "岗位不能为空", groups = {Add.class})
    private String postCode;

    /**
     * 班次ID
     */
    private ObjectId classId;

    /**
     * 班次名称
     */
    @NotBlank(message = "班次名称不能为空", groups = {Add.class, Update.class})
    private String className;

    /**
     * 班次类型
     */
    @NotNull(message = "班次类型不能为空", groups = {Add.class})
    private Integer classType;

    /**
     * 上班日期
     */
    private Date workDate;

    /**
     * 上班星期
     */
    private Integer workWeek;

    //-----------上班内容，来自选择的班次------------//
    /**
     * 出勤地点
     */
    private String attendPlace;

    /**
     * 出勤时间
     */
    private Date attendTime;

    /**
     * 工作任务
     */
    private String task;

    /**
     * 退勤地点
     */
    private String offPlace;

    /**
     * 退勤时间
     */
    private Date offTime;

    /**
     * 工作时长
     */
    private Double workHours;

    /**
     * 备注
     */
    private String remark;
    //-----------上班内容，来自选择的班次------------//

    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空", groups = {Add.class})
    private ObjectId communityId;

    /**
     * 创建人ID
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 数据状态
     */
    private Integer dataStatus;

    /**
     * 测试用
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%s %s %s %s", DateUtils.formatDate(workDate, "yyyy-MM-dd"), workWeek, userName, className);
    }

    //------------时间字符串-----------//
    private String attendTimeStr;

    private String offTimeStr;

    //------------时间字符串-----------//

    public interface Add {
    }

    public interface Update {
    }
}
