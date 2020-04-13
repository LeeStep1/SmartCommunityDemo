package cn.bit.facade.vo.communityIoT.elevator;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CallElevatorRequest implements Serializable {
    @NotNull(message = "指定房间ID不能为空")
    private ObjectId roomId;

    @NotNull(message = "指定楼栋ID不能为空")
    private ObjectId buildingId;

    /**
     * 呼梯方式 1: 回家  2: 下楼
     */
    @NotNull(message = "远程呼梯方式不能为空")
    private Integer remoteType;

    /**
     * 当前楼层
     */
    @NotNull(message = "当前楼层不能为空")
    private String currentFloor;

    /**
     * 电梯方向  1: 上行  2: 下行
     */
    @NotNull(message = "电梯方向不能为空")
    private Integer hallCallDirection;

    @JSONField(name = "buildId")
    public ObjectId getBuildingId() {
        return this.buildingId;
    }
}
