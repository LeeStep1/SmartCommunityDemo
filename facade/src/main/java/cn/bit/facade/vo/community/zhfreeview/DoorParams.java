package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 楼栋
 */
@Data
public class DoorParams implements Serializable {
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID为空")
    private ObjectId communityId;
    /**
     * 社区编码
     */
    private String tenantCode;
    /**
     * 社区结构不能为空
     */
    private String parentDirectory;

    private Integer buildingNum = 1;

    private Integer unitNum = 1;

    private Integer buildingNumStart = 1;

    private Integer unitNumStart = 1;

    private String buildingDisplay;

    private String unitDisplay;
    /**
     * 层数
     */
    private Integer nodeNum;
    /**
     * 开始楼层数
     */
    @NotNull(message = "开始楼层数为空")
    private Integer nodeNumStart;

    @NotNull(message = "楼面层数不能为空")
    private Integer overGround;

    @NotNull(message = "地下层数不能为空")
    private Integer underGround;

    @NotNull(message = "房间数量不能为空")
    private Integer roomNum;
}
