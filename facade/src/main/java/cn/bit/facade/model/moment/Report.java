package cn.bit.facade.model.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 动态举报表
 */
@Data
@Document(collection = "MOM_REPORT")
public class Report implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 被举报的言论ID
     */
    private ObjectId speechId;

    /**
     * 举报言论类型（1：动态，2：评论）
     */
    private Integer type;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 举报者ID
     */
    private ObjectId creatorId;

    /**
     * 举报时间
     */
    private Date createAt;

    /**
     * 被举报总次数
     */
    @Transient
    private Integer reportNum;
}
