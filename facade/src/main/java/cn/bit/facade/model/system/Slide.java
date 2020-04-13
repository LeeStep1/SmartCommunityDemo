package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "SYS_SLIDE_SHOW")
public class Slide implements Serializable {

    @Id
    @NotNull(message = "轮播图id不能为空", groups = {Slide.UpdateSlide.class})
    private ObjectId id;
    /**
     * 标题
     */
    private String title;

    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 素材链接（图片）
     */
    @NotBlank(message = "素材链接不能为空", groups = {Slide.AddSlide.class})
    private String materialUrl;
    /**
     * 客户端类型（1000：住户端；1001：物业端；1002：WEB后台）
     */
    @NotNull(message = "客户端类型", groups = {Slide.AddSlide.class})
    private Integer client;
    /**
     * 标签（食品、工具等标签）
     */
    private String[] tags;
    /**
     * 跳转链接(点击素材后的跳转链接)
     */
    private String href;
    /**
     * 开始时间
     */
    private Date beginAt;
    /**
     * 失效时间
     */
    private Date deadline;
    /**
     * 排序
     */
    private Integer rank;
    /**
     * 发布状态（1：已发布；0：未发布）
     */
    private Integer published;
    /**
     * 发布时间
     */
    private Date publishAt;
    /**
     * 创建人id
     */
    private ObjectId creatorId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 修改时间
     */
    private Date updateAt;
    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;

    public interface AddSlide {};

    public interface UpdateSlide {};

}
