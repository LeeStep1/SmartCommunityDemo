package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "PROP_NOTICE_TEMPLATE")
public class NoticeTemplate implements Serializable {
    @Id
    @NotNull(message = "模板id不能为空", groups = {Modify.class})
    private ObjectId id;

    /**
     * 社区id
     */
    @NotNull(message = "社区id不能为空", groups = {Add.class})
    private ObjectId communityId;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空", groups = {Add.class})
    @Length(max = 50, message = "名称最大长度为50字符")
    private String name;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空", groups = {Add.class})
    @Length(max = 50, message = "标题最大长度为50字符")
    private String title;

    /**
     * 正文
     */
    @NotBlank(message = "正文不能为空", groups = {Add.class})
    @Length(max = 10000, message = "内容最大长度为10000字符")
    private String body;

    /**
     * 缩略图
     * RUL
     */
    private String thumbnailUrl;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 数据状态(1:有效；0：失效)
     */
    private Integer dataStatus;

    public interface Add {}

    public interface Modify {}
}
