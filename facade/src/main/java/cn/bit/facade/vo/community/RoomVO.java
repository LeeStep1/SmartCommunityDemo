package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 单元房
 */
@Data
public class RoomVO implements Serializable {

    @NotNull(message = "房间ID不能为空", groups = {Update.class})
    private ObjectId id;
    /**
     * 房间名称
     */
    @NotBlank(message = "名称不能为空", groups = {Add.class})
    private String name;

    /**
     * 楼宇ID
     */
    @NotNull(message = "楼宇ID不能为空", groups = {Add.class})
    private ObjectId buildingId;

    /**
     * 楼层
     */
    @NotBlank(message = "所在楼层不能为空", groups = {Add.class})
    private String floorCode;

    /**
     * 面积
     */
    @NotNull(message = "房间面积不能为空", groups = {Add.class})
    private Integer area;

    private Boolean existHousehold;

    public interface Add {
    }

    public interface Update {
    }
}
