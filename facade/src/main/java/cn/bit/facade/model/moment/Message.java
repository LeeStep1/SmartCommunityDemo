package cn.bit.facade.model.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 动态消息记录表
 */
@Data
@Document(collection = "MOM_MESSAGE")
@CompoundIndexes({
        @CompoundIndex(def = "{'communityId':1,'noticeTo':1,'createAt':1}", background = true)
})
public class Message implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 内容
     */
    private String content;

    private ObjectId momentId;

    /**
     * 动态类型（1：邻里社交，2：悬赏求助，3：二手交易）
     */
    private Integer momentType;

    /**
     * 动态内容
     */
    private String momentContent;

    /**
     * 照片
     */
    private List<String> momentPhotos;

    /**
     * 消息通知对象ID（动态发布者/评论者）
     */
    private ObjectId noticeTo;

    /**
     * 消息类型（1：评论（包括评论动态、回复评论）；2：点赞；3：屏蔽动态；4：屏蔽评论；5：禁言；）
     */
    private Integer type;

    /**
     * 主体（动态/评论）是否已删除（default:false）
     */
    private Boolean isDeleted;

    /**
     * 消息创建人
     */
    private ObjectId creatorId;

    /**
     * 创建人头像
     */
    @Transient
    private String creatorHeadImg;

    /**
     * 创建人名称
     */
    @Transient
    private String creatorName;

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
}
