package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * 新增评论请求参数
 */
@Data
public class CommentVO implements Serializable {
    private ObjectId momentId;

    /**
     * 内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 200, message = "评论内容最大长度为200字符")
    private String content;

    /**
     * 回复谁的评论
     */
    private ObjectId answerTo;
}
