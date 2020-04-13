package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "PROP_NOTICE")
@Validated
@CompoundIndexes({@CompoundIndex(def = "{'communityId':1,'createAt': -1}", background = true)})
public class Notice implements Serializable {
    @Id
    @NotNull(message = "公告消息id不能为空", groups = {Update.class})
    private ObjectId id;

    /**
     * 通知类型（1：公告；2：新闻；3：活动；4：提醒；99：其他）
     */
    @NotNull(message = "通知类型不能为空", groups = {Add.class})
    private Integer noticeType;

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空", groups = {Add.class})
    private String title;

    /**
     * 正文
     */
    @NotBlank(message = "正文不能为空", groups = {Add.class})
    private String body;

    /**
     * 缩略图
     * RUL
     */
    private String thumbnailUrl;

    /**
     * 跳转路径
     */
    private String url;

    /**
     * 消息发布状态（-1：已撤销；0：未发布；1：已发布）
     */
    private Integer publishStatus;

    /**
     * 发布时间
     */
    private Date publishAt;

    /**
     * 编辑人id
     */
    private ObjectId editorId;

    /**
     * 编辑人名称
     */
    private String editorName;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 编辑时间
     */
    private Date updateAt;

    /**
     * 数据状态(1:有效；0：失效)
     */
    private Integer dataStatus;
    /**
     * 推送状态(1:已推送；0：未推送)
     */
    private Integer pushStatus;

    public interface Add {}

    public interface Update {}
}
