package cn.bit.facade.model.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 动态点赞表
 */
@Data
@Document(collection = "MOM_PRAISE")
public class Praise implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    @NotNull(message = "动态ID不能为空")
    private ObjectId momentId;

    @Transient
    private String momentContent;

    @Transient
    private List<String> momentPhotos;

    /**
     * 点赞者ID
     */
    private ObjectId creatorId;

    /**
     * 点赞者头像
     */
    @Transient
    private String creatorHeadImg;

    /**
     * 点赞者名称
     */
    @Transient
    private String creatorName;

    /**
     * 点赞时间
     */
    private Date createAt;

    /**
     * 数据状态
     */
    private Integer dataStatus;
}
