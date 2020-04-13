package cn.bit.facade.model.community;

import cn.bit.common.facade.data.Location;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 楼宇信息表
 */
@Data
@Document(collection = "CM_BUILDING")
public class Building implements Serializable {
    @Id
    @NotNull(message = "楼栋ID不能为空")
    private ObjectId id;
    /**
     * 楼宇名称
     */
    private String name;
    /**
     * 楼宇编号
     */
    private String code;
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空")
    private ObjectId communityId;
    /**
     * 楼面层数
     */
    private Integer overGround;
    /**
     * 地下层数
     */
    private Integer underGround;
    /**
     * 房间数量
     */
    private Integer roomNum;
    /**
     * 坐标，格式（x,y）
     */
    private String coordinate;
    /**
     * 排序
     */
    private Integer rank;

    /**
     * 创建人ID
     */
    private ObjectId createId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 数据状态
     */
    private Integer dataStatus;

    /**
     * 米立ID
     */
    private Long miliBId;

    /**
     * 第三方设备（key-value）
     * DeviceVendor.class
     */
    private String outId;

    /**
     * 楼宇对照表
     */
    private Map<String, String> floorMap;

    /**
     * 已录入房间数
     */
    @Transient
    private Long inputRoomNum;

    /**
     * 计数
     */
    @Transient
    private Long tempNum;

    /**
     * 是否开放
     */
    private Boolean open;

    private Integer no;

    @Transient
    private ObjectId zoneId;

    @Transient
    private List<Location> locations;

    /**
     * 门禁招梯联动
     */
    private Boolean doorElevatorLinkage;

}
