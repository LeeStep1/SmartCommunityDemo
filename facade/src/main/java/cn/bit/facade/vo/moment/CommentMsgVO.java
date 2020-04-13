package cn.bit.facade.vo.moment;

import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Message;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增评论数据的封装实体
 */
@Data
public class CommentMsgVO implements Serializable {
    private Comment comment;
    private Message message;
}
