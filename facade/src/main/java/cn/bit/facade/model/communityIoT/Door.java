package cn.bit.facade.model.communityIoT;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "CIT_DOOR")
@CompoundIndexes({
        @CompoundIndex(def = "{'communityId': 1, 'buildingId': 1}", background = true),
        @CompoundIndex(def = "{'deviceCode': 1, 'brandNo': -1}", background = true)
})
public class Door implements Serializable {
    @Id
    @NotNull(message = "门禁ID不能为空", groups = {EditDoor.class, RemoteOpen.class, BindDoorToCommunity.class})
    private ObjectId id;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 门禁名称
     */
    @NotNull(message = "门禁名称不能为空", groups = {BindDoorToCommunity.class})
    private String name;
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;
    /**
     * 门禁设备蓝牙MAC地址
     */
    private String mac;
    /**
     * 蓝牙PIN码
     */
    private String pin;
    /**
     * 排序
     */
    private int rank;
    /**
     * 终端编码
     */
    private String terminalCode;
    /**
     * 终端端口
     */
    private Integer terminalPort;
    /**
     * 微动开关
     */
    private Integer guardSwitch;
    /**
     * 门禁状态（1：可用；0：停用）
     */
    private Integer doorStatus;
    /**
     * 门类型(1:社区门，2:楼栋门)
     */
    private Integer doorType;
    /**
     * 设备ID
     */
    private Long deviceId;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备序列号
     */
    private String serialNo;
    /**
     * 设备类型 1 : 蓝牙开门器  4 : 二维码开门  5 : 微信开门  7 : 云对讲
     */
    private Set<Integer> serviceId;
    /**
     * 设备编号
     */
    private String deviceCode;
    /**
     * 在线状态（1：离线 ; 2: 在线）
     * {@link cn.bit.facade.enums.DoorOnlineStatusType}
     */
    private Integer onlineStatus;
    /**
     * 报警状态（1：报警；0：正常）
     */
    private Integer alarmStatus;
    /**
     * 云对讲设备id
     */
    private Long yunDeviceId;
    /**
     * 品牌/厂商
     */
    private String brand;
    /**
     * 型号
     */
    private String deviceType;
    /**
     * 门禁厂商 (1: 米立  2：康途  3：金博)
     */
    @NotNull(message = "厂商类型不能为空", groups = {AddOwner.class})
    private Integer brandNo;
    /**
     * 住户编码（全视通）
     */
    private String tenantCode;
    /**
     * 创建人ID
     */
    private ObjectId creatorId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 修改时间
     */
    private Date updateAt;
    /**
     * 数据状态
     */
    private Integer dataStatus;

    public interface AddOwner {
    }

    public interface BindDoorToCommunity {
    }

    public interface EditDoor {
    }

    public interface RemoteOpen {
    }
}

