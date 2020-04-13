package cn.bit.facade.vo.communityIoT.elevator;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.Set;

/**
 * @author jianjun.cheng
 * @description 通过楼房Id和蓝牙mac地址查询电梯列表
 * @create 2018-02-11
 **/
@Data
public class FindElevatorListRequest implements Serializable {
    @NotBlank(message = "社区Id不能为空")
    private String communityId;
    /**
     * 楼栋列表
     */
    private Set<String> buildingIds;
    /**
     * 模糊查询(电梯名或设备ID模糊查询)
     */
    private String name;

    /**
     * 品牌ID
     */
    private String brandId;

    /**
     * 电梯编号
     */
    private String elevatorId;

    /**
     * 梯号
     */
    private String elevatorNum;

    /**
     * 蓝牙地址列表
     */
    private Set<String> macAddress;

    /**
     * 数据状态集合
     */
    private Set<Integer> dataStatus;

    /**
     * elevatorStatusList
     *
     * 电梯状态（0：正常  1：检修  2：终端断开  3：故障 ）
     */
    private Set<Integer> elevatorStatusList;

    private Integer page;

    private Integer size;

    @JSONField(name = "houseId")
    public String getCommunityId() {
        return communityId;
    }

    @JSONField(name = "build")
    public Set<String> getBuildingIds() {
        return buildingIds;
    }
}
