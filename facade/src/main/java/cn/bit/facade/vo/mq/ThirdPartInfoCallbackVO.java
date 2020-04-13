package cn.bit.facade.vo.mq;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Set;

@Data
public class ThirdPartInfoCallbackVO implements Serializable {
    private ObjectId communityId;

    private ObjectId correlationId;

    private Set<String> userIds;

    private Set<ObjectId> buildingIds;
}
