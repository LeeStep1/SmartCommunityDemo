package cn.bit.facade.model.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 动态评论表
 */
@Data
@Document(collection = "MOM_COMMENT")
@CompoundIndexes({
        @CompoundIndex(def = "{'momentId':1,'status':1}", background = true)
})
public class Comment implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    @NotNull(message = "动态ID不能为空")
    @Indexed(background = true)
    private ObjectId momentId;

    @Transient
    private String momentContent;

    @Transient
    private List<String> momentPhotos;

    /**
     * 内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 200, message = "评论内容最大长度为200字符")
    private String content;

    /**
     * 被回复人的ID(仅当回复他人时会记录)
     */
    private ObjectId answerTo;

    /**
     * 被回复人的名字
     */
    @Transient
    private String answerToName;

    /**
     * 举报数
     */
    private Integer reportNum;

    /**
     * 是否已举报
     */
    @Transient
    private Boolean isReported;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 屏蔽原因
     */
    private String shieldingReason;

    /**
     * 屏蔽时间
     */
    private Date shieldingAt;

    /**
     * 评论者头像
     */
    @Transient
    private String creatorHeadImg;

    /**
     * 评论者ID
     */
    private ObjectId creatorId;

    /**
     * 评论者名称
     */
    @Transient
    private String creatorName;

    /**
     * 评论时间
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
}
