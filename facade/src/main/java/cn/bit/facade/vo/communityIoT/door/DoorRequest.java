package cn.bit.facade.vo.communityIoT.door;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
public class DoorRequest implements Serializable {

    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    @NotNull(message = "楼栋id不能为空")
    private Set<ObjectId> buildingId = Collections.emptySet();

    private Set<String> mac = Collections.emptySet();

    private Integer doorType;

    private Integer dataStatus;

    private Date after;

    private Set<Integer> serviceId;

    private String brand;

    private Set<Integer> brandNo;

    private Integer keyType;

    private String keyNo;

    private String keyId;

    private ObjectId userId;
}
