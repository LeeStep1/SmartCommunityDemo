package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collection;

/**
 * 禁言列表请求参数
 */
@Data
public class SilentRequest implements Serializable {

    /**
     * 禁言状态(0:已解禁，1：禁言中)
     */
    private Integer status;

    /**
     * 被禁言者名称
     */
    private String silentUserName;

    /**
     * 被禁言用户的ID集合
     */
    private Collection<ObjectId> silentUserId;

    private ObjectId communityId;
}
