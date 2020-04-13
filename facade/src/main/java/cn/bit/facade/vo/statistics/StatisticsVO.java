package cn.bit.facade.vo.statistics;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Set;

@Data
public class StatisticsVO implements Serializable {

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 物业公司ID
     */
    private ObjectId companyId;

    /**
     * 角色集合
     */
    private Set<String> roles;

    /**
     * 用户ID集合
     */
    private Set<ObjectId> userIds;
}
