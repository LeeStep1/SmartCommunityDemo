package cn.bit.facade.vo.statistics;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

@Data
public class HouseholdRequest implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 年龄区段
     */
    private List<Region> ageRegions;
}
