package cn.bit.facade.model.community;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 房间信息
 */
@Data
@Document(collection = "CM_ROOM")
public class Room implements Serializable {

    @Id
    @NotNull(message = "房间ID不能为空", groups = BindFeesTemplate.class)
    private ObjectId id;
    /**
     * 单元房名称
     */
    private String name;
    /**
     * 单元房编号
     */
    private String code;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 楼宇ID
     */
    private ObjectId buildingId;

    /**
     * 楼层号
     */
    private String floorNo;

    /**
     * 显示楼层
     */
    private String floorCode;
    /**
     * 排序
     */
    private Integer rank;
    /**
     * 跃层面积
     */
    private Integer springlayerArea;
    /**
     * 方位
     */
    private String direction;
    /**
     * 面积
     */
    private Integer area;

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
     * 云对讲业主id
     */
    private Long yun_proprietor_id;

    /**
     * 业主/家人id
     */
    private Long proprietor_id;

    /**
     * 米立ID（现在为第三方ID）
     */
    private String outId;

    /**
     * 物业费套餐ID
     */
    @NotNull(message = "物业费套餐ID不能为空", groups = BindFeesTemplate.class)
    private ObjectId feesTemplateId;

    // =====================================【批量新增房间】================================== //
    /**
     * 房间数量
     */
    @Transient
    private Integer roomNum;

    /**
     * 开始房间号
     */
    @Transient
    private Integer beginNum;

    /**
     * 房间名前缀名称
     */
    @Transient
    private String roomName;

    /**
     * 编码前缀
     */
    @Transient
    private String codeName;

    /**
     * 主门
     */
    private Boolean mainDoor;

    /**
     * 副门
     */
    private Boolean subDoor;

    public interface BindFeesTemplate {
    }
}
