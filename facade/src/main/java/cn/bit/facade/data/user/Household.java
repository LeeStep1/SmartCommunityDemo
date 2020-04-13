package cn.bit.facade.data.user;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class Household extends BaseEntity implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 用户关系（1：业主；2：家属；3：re租客）
     */
    private Integer relationship;
    /**
     * 出生日期（yyyy-MM-dd）
     */
    private String birthday;
    /**
     * 性别（0：未知；1：男；2：女）
     */
    private Integer sex;
}
