package cn.bit.facade.model.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 动态信息表
 */
@Data
@Document(collection = "MOM_MOMENT")
@CompoundIndexes({//组合索引
        @CompoundIndex(def = "{'communityId': 1, 'status': 1}", background = true)
})
public class Moment implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 动态类型（1：邻里社交，2：悬赏求助，3：二手交易）
     */
    @NotNull(message = "动态类型不能为空")
    private Integer type;

    /**
     * 内容
     */
    @Length(max = 500, message = "内容最大长度为500字符")
    private String content;

    /**
     * 照片
     */
    @Size(max = 9, message = "最多只能发布9张照片")
    private List<String> photos;

    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空")
    private ObjectId communityId;

    /**
     * 状态（0：待审核,1：审核通过,2:自动通过,-1：未通过,-2:系统自动屏蔽,-3：管理员屏蔽）
     */
    private Integer status;

    /**
     * 审核人ID
     */
    private ObjectId auditorId;

    /**
     * 审核人名称
     */
    @Transient
    private String auditorName;

    /**
     * 审核时间
     */
    private Date auditAt;

    /**
     * 点赞数
     */
    private Integer praiseNum;

    /**
     * 是否已点赞
     */
    @Transient
    private Boolean isPraised;

    /**
     * 评论数
     */
    private Integer commentNum;

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
     * 屏蔽原因(物业人员屏蔽的时候需要填写)
     */
    private String shieldingReason;

    /**
     * 屏蔽时间
     */
    private Date shieldingAt;

    /**
     * 发布者头像
     */
    @Transient
    private String creatorHeadImg;

    /**
     * 发布者ID
     */
    private ObjectId creatorId;

    /**
     * 发布者名称
     */
    @Transient
    private String creatorName;

    /**
     * 发布时间
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
