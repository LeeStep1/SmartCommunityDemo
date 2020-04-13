package cn.bit.facade.data.user;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class TenantApplication extends BaseEntity implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;
}
