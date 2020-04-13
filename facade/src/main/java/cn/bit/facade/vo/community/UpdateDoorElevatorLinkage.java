package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description :
 * @Date ： 2019/12/26 16:08
 */
@Data
public class UpdateDoorElevatorLinkage implements Serializable {
    private List<LinkageMap> auths;

    private ObjectId communityId;

    @Data
    public static class LinkageMap implements Serializable {
        @NotNull(message = "楼栋不能为空")
        private ObjectId buildingId;

        @NotNull(message = "联动设置项不能为空")
        private Boolean doorElevatorLinkage;
    }
}
