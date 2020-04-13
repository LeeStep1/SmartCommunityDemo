package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Collection;

/**
 * 请求参数
 */
@Data
public class RequestVO implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 评论者名称
     */
    private String creatorName;

    /**
     * 评论者ID集合
     */
    @Field("creatorId")
    private Collection<ObjectId> creatorId;

    /**
     * 举报数量
     */
    private Integer reportNum;
}
