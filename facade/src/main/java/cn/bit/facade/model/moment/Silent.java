package cn.bit.facade.model.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 禁言表
 */
@Data
@Document(collection = "MOM_SILENT")
@CompoundIndexes({
        @CompoundIndex(def = "{'communityId':1, 'silentUserId':1, 'status':1}", background = true)
})
public class Silent implements Serializable {
    @Id
    private ObjectId id;

    private ObjectId communityId;

    /**
     * 被禁言用户ID
     */
    private ObjectId silentUserId;

    /**
     * 被禁言者名称
     */
    @Transient
    private String silentUserName;

    /**
     * 禁言有效时长（分钟）
     */
    @NotNull(message = "禁言有效时长不能为空")
    private Long silentMinutes;

    /**
     * 禁言失效时间
     */
    private Date silentEndAt;

    /**
     * 禁言累计时长（分钟）
     */
    private Long totalSilentMinutes;

    /**
     * 累计禁言次数
     */
    private Integer silentTimes;

    /**
     * 已处理的屏蔽动态数量
     */
    @Transient
    private Integer shieldingMomentNum;

    /**
     * 已处理的屏蔽评论数量
     */
    @Transient
    private Integer shieldingCommentNum;

    /**
     * 未处理的屏蔽动态数量
     */
    @Transient
    private Integer newShieldingMomentNum;

    /**
     * 未处理的屏蔽评论数量
     */
    @Transient
    private Integer newShieldingCommentNum;

    /**
     * 未处理的屏蔽动态ID集合
     */
    private Set<ObjectId> newShieldingMomentIds;

    /**
     * 未处理的屏蔽评论ID集合
     */
    private Set<ObjectId> newShieldingCommentIds;

    /**
     * 已处理的屏蔽动态ID集合
     */
    private Set<ObjectId> shieldingMomentIds;

    /**
     * 已处理的屏蔽评论ID集合
     */
    private Set<ObjectId> shieldingCommentIds;

    private ObjectId creatorId;

    private Date createAt;

    private Date updateAt;

    /**
     * 禁言状态（0：已解禁，1：禁言中）
     * 手动解禁：将禁言失效时间设置成为当天时间
     */
    @Transient
    private Integer status;

}
