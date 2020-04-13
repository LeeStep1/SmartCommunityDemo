package cn.bit.facade.model.task;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 作业记录表
 */
@Data
@Document(collection = "TASK_RECORD")
@CompoundIndex(def = "{'communityId' : 1, 'createAt' : 1, 'taskType' : 1}", background = true)
public class Record implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户id不能为空", groups = {Add.class})
    private ObjectId userId;

    /**
     * 用户名字
     */
    private String userName;

    /**
     * 社区ID
     */
    @NotNull(message = "社区id不能为空", groups = {Add.class, Search.class})
    private ObjectId communityId;

    /**
     * 作业类型（存在code表，1：巡更；2：保洁）
     */
    /*@NotNull(message = "作业类型不能为空", groups = {Add.class})*/
    private Integer taskType;

    /**
     * 岗位（打卡有针对性，因此不需要支持多岗位）
     */
    private String postCode;

    /**
     * 作业备注
     */
    private String remark;

    /**
     * OSS目录
     */
    private String buckName;

    /**
     * OSS标识
     */
    private String key;

    /**
     * 附件
     */
    private String url;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 坐标
     */
    private String coordinate;

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

    public interface Add {
    }

    public interface Search {
    }

}
