package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fxiao
 * on 2018/3/26
 */
@Data
@Document(collection = "PROP_GTASKS")
@CompoundIndex(name = "community_createtime", def = "{'communityId':1, 'createAt':-1}")
public class Gtaskzs implements Serializable{
    @Id
    private String id;
    /**
     * 标题
     */
    private String title;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 外键ID
     */
    @Indexed(background = true)
    private ObjectId otherId;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 任务类型
     * 0：没有
     * 1：故障报修
     * 、、、
     * 其它待补充
     * @return
     */
    private Integer taskType;
    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;
}
