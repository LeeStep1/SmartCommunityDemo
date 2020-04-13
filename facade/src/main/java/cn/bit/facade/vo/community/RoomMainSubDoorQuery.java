package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * @Description :
 * @Date ： 2019/12/26 10:48
 */
@Data
public class RoomMainSubDoorQuery implements Serializable {
    private Set<ObjectId> roomIds;

    @NotNull(message = "房间id不能为空")
    private ObjectId roomId;

    private Boolean mainDoor = false;

    private Boolean subDoor = false;
}
