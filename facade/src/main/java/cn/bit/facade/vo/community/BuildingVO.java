package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新楼宇
 */
@Data
public class BuildingVO implements Serializable {
    @NotNull(message = "楼宇ID不能为空", groups = {Update.class})
    private ObjectId id;
    /**
     * 楼宇名称
     */
    @NotBlank(message = "名称不能为空", groups = {Add.class})
    private String name;

    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {Add.class})
    private ObjectId communityId;

    /**
     * 楼面层数
     */
    @NotNull(message = "楼面层数不能为空", groups = {Add.class})
    private Integer overGround;

    /**
     * 地下层数
     */
    @NotNull(message = "地下层数不能为空", groups = {Add.class})
    private Integer underGround;

    /**
     * 房间数量
     */
    @NotNull(message = "房间数量不能为空", groups = {Add.class})
    private Integer roomNum;

    private ObjectId buildId;

    public interface Add {
    }

    public interface Update {
    }
}
