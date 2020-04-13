package cn.bit.facade.vo.user.card;

import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.vo.communityIoT.door.CommunityDoorVO;
import cn.bit.facade.vo.communityIoT.elevator.FloorVO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CardVO implements Serializable {
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {PropertyApply.class})
    private ObjectId userId;
    /**
     * 员工ID
     */
    private ObjectId userToPropertyId;
    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空", groups = {HouseholdApply.class, PropertyApply.class})
    private String name;
    /**
     * 用户联系电话
     */
    private String phone;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 卡片类型  1:手机MAC； 2:蓝牙卡MAC； 4:IC卡UID； 8:二维码信息号
     */
    @NotNull(message = "卡片类型不能为空", groups = {AddOwner.class, DeleteDoors.class, DeleteFloor.class, HouseholdApply.class, PropertyApply.class})
    private Integer keyType;
    /**
     * 卡号
     */
    @NotBlank(message = "卡号不能为空", groups = {DeleteDoors.class, DeleteFloor.class, HouseholdApply.class, PropertyApply.class})
    private String keyNo;
    /**
     * 卡号
     */
    @NotBlank(message = "卡流水号不能为空", groups = {DeleteDoors.class, DeleteFloor.class})
    private String keyId;
    /**
     * 有效时长 以秒为单位
     */
    @NotNull(message = "有效期时长不能为空", groups = {QRCodeApply.class})
    private Integer processTime;
    /**
     * 前端传递楼层资料
     */
    @NotNull(message = "房间信息不能为空", groups = {DeleteFloor.class, QRCodeApply.class, HouseholdApply.class})
    private Set<ObjectId> rooms;

    /**
     * 电梯物联需要的参数
     */
    private Set<FloorVO> builds;

    /**
     * 楼栋ID，用于楼栋授权
     */
    @NotNull(message = "楼栋ID不能为空", groups = {UpdatePermissionWithBuilding.class})
    private Set<ObjectId> buildingIds;

    /**
     * 门禁使用次数
     */
    private Integer usesTime = 0;

    /**
     * 写门禁硬件所需要的参数
     */
    private Set<CommunityDoorVO> houses;

    /**
     * 需要删除的门禁设备ID集合
     */
    @NotNull(message = "门禁ID集合不能为空", groups = {DeleteDoors.class})
    private Set<ObjectId> doors;

    private Set<String> roomName;

    private List<UserToRoom> userToRooms;

    private ObjectId cardId;

    /**
     * add at 2018-11-15 by decai.liu
     * 指定的过期时间
     */
    private Date expireAt;

    /**
     * 时间度量单位 {@link cn.bit.facade.enums.TimeUnitEnum}
     */
    private Integer timeUnit;

    public interface AddOwner {}

    public interface HouseholdApply {}

    public interface PropertyApply {}

    public interface QRCodeApply {}

    public interface UpdatePermissionWithBuilding {}

    public interface DeleteDoors {}

    public interface DeleteFloor {}

    public CardVO() {
    }

    public CardVO(Integer keyType, String keyNo, String keyId) {
        this.keyType = keyType;
        this.keyNo = keyNo;
        this.keyId = keyId;
    }

    public Set<FloorVO> getBuilds() {
        if (builds == null){
            setBuilds(new HashSet<>());
        }
        return builds;
    }

    public Set<ObjectId> getDoors() {
        if (doors == null){
            setDoors(new HashSet<>());
        }
        return doors;
    }

}
