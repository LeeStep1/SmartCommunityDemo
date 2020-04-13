package cn.bit.facade.model.task;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 物业排班-班次
 */
@Data
@Document(collection = "TASK_CLASS")
public class Class implements Serializable {
    @Id
    @NotNull(message = "班次ID不能为空", groups={Update.class})
    private ObjectId id;

    /**
     * 班次名称
     */
    @NotBlank(message = "班次名称不能为空", groups={Add.class})
    private String name;

    /**
     * 班次类型（存在code表，1：轮班、2：常班）
     */
    @NotNull(message = "班次类型不能为空", groups={Add.class})
    private Integer type;

    /**
     * 适用岗位
     */
    @NotBlank(message = "适用岗位不能为空" , groups={Add.class})
    private String postCode;

    /**
     * 轮班安排人数
     */
    private Integer number;

    /**
     * 轮班顺序
     */
    private Integer shiftOrder;

    /**
     * 常班-休息日集合
     */
    private Set<Integer> restWeeks;

    //-----------上班内容------------//
    /**
     * 出勤地点
     */
    private String attendPlace;

    /**
     * 出勤时间
     */
    private String attendTime;

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
    private String offTime;

    /**
     * 工作时长
     */
    private Double workHours;

    /**
     * 备注
     */
    private String remark;
    //-----------上班内容------------//

    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空", groups={Add.class, Query.class})
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

    public interface Add{}
    public interface Query{}
    public interface Update{}
}
