package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@Data
public class DistrictRequest implements Serializable {

    /**
     * 物业人员ID
     */
    private ObjectId userToPropertyId;
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {AvailableDistrictBuilding.class})
    private ObjectId communityId;
    /**
     * 职能区域权限ID集合
     */
    private Set<ObjectId> districtIds;
    /**
     * 职能区域ID
     */
    private ObjectId districtId;

    public interface AvailableDistrictBuilding {
    }
}
