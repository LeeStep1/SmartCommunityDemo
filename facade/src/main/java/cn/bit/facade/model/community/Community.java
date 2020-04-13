package cn.bit.facade.model.community;

import cn.bit.facade.vo.community.broadcast.BroadcastSchema;
import cn.bit.facade.vo.community.broadcast.DeviceSchema;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 小区信息
 */
@Data
@Document(collection = "CM_COMMUNITY")
public class Community implements Serializable {
    @Id
    private ObjectId id;
    /**
     * 社区名称
     */
    private String name;
    /**
     * 类型
     *
     * @see cn.bit.facade.enums.CommunityTypeEnum
     */
    private Integer type;
    /**
     * 社区编码
     */
    //@Indexed(background = true, unique = true)
    private String code;
    /**
     * 地址
     */
    private String address;
    /**
     * 国家
     */
    private String country;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区/县
     */
    private String district;
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
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;

    /**
     * 云对讲小区id（第三方）
     */
    private Long yun_community_id;

    /**
     * 米粒 ID
     */
    private Long miliCId;

    /**
     * 第三方社区 ID
     */
    private Map<String, String> outId;

    /**
     * 面积
     */
    private String area;

    /**
     * 图片
     */
    private String imgUrl;

    /**
     * 入住房数
     */
    private Integer checkInRoomCnt;

    /**
     * 住户人数
     */
    private Integer householdCnt;

    /**
     * 是否开放
     */
    private Boolean open;

    /**
     * 菜单资源
     */
    private Set<ObjectId> menus;

    /**
     * api资源
     */
    private Set<ObjectId> apis;

    /**
     * 物业ID
     */
    private ObjectId propertyId;

    /**
     * 物业名称
     */
    @Transient
    private String propertyName;

    /**
     * 小区编号，协议用的
     */
    private Integer no;

    /**
     * 广播属性
     */
    private BroadcastSchema broadcastSchema;

    /**
     * 楼栋数量
     */
    private Integer buildingNum;

    /**
     * 房间数量
     */
    private Integer roomNum;

    /**
     * 社区实景图片，上限5张
     */
    private List<String> photos;

    /**
     * 接入设备的品牌集合
     */
    private Set<String> deviceBrands;

    /**
     * 读卡器配置：brand：康途、莱卡
     */
    private DeviceSchema cardReaderSchema;

    /**
     * 是否有电梯主副门
     */
    private Boolean hasElevatorSubDoor;
}
