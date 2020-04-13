package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class Structure implements Serializable{
    /**
     * 区域编码
     */
    @NotNull(message = "区域编码不能为空")
    private String AreaID;
    /**
     * 社区名称
     */
    @NotNull(message = "社区名称不能为空")
    private String VillageName;
    /**
     * 期数
     */
    private Integer PeriodNum = 0;
    /**
     * 范围、地域
     */
    private Integer RegionNum = 0;
    /**
     * 楼栋数量
     */
    @NotNull(message = "楼栋数量不能为空")
    private Integer BuildingNum;
    /**
     * 楼栋开始数
     */
    private Integer BuildingNumStart = 1;
    /**
     * 单元数，默认1
     */
    private Integer UnitNum;
    /**
     * 单元开始数
     */
    private Integer UnitNumStart = 1;
    /**
     * 楼层数
     */
    @NotNull(message = "楼层数量不能为空")
    private Integer FloorNum;
    /**
     * 楼层开始数
     */
    private Integer FloorNumStart = 1;
    /**
     * 房间数
     */
    @NotNull(message = "房间数量不能为空")
    private Integer RoomNum;
    /**
     * 开始房间数
     */
    private Integer RoomNumStart = 1;

    @NotNull(message = "地表层不能为空")
    @Min(value = 1, message = "地表层数量不能小于1")
    @Max(value = 200, message = "请输入正常的楼层数量")
    private Integer overGround;

    @Min(value = 0, message = "地表层数为自然数")
    @Max(value = 10, message = "请输入正常的楼层数量")
    private Integer underGround;

    @NotNull(message = "面积不能为空")
    private Integer area;
    /**
     * 楼栋名称
     */
    private String buildingName;

    /**
     * 社区类型（1：“住宅”；2：“办公楼”；3：“学校”）
     */
    @NotNull(message = "社区类型不能为空")
    private Integer type;

}
