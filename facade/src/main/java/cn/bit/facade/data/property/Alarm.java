package cn.bit.facade.data.property;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Alarm extends BaseEntity {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 接警人ID
     */
    private ObjectId receiverId;
    /**
     * 发生的小时（24小时制）
     */
    private Integer hour;
}
