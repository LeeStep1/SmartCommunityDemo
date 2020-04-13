package cn.bit.facade.vo.mq;

import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DeviceAuthVO implements Serializable {

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户ID
     */
    private ObjectId userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 楼栋列表
     */
    private Set<BuildingListVO> buildingList;

    /**
     * 卡类型
     */
    private Integer keyType;

    /**
     * 卡号
     */
    private String keyNo;

    /**
     * 凭证流水号
     */
    private String keyId;

    /**
     * 过期时长
     */
    private Integer processTime;

    /**
     * 使用次数
     */
    private Integer usesTime = 0;

    /**
     * 关联ID
     */
    private ObjectId correlationId;

    /**
     * 住户关系
     */
    private Integer relationship;

    /**
     * 处理次数
     */
    private Integer handleCount;

    /**
     * 性别（0：未知；1：男；2：女）
     */
    private Integer sex;

    /**
     * 米立用户ID
     */
    private Set<Long> outUIds;

    /**
     * 使用者身份 1 : 住户 ；2 : 物业人员
     */
    private Integer userIdentity;

    /**
     * 物业区域
     */
    private Set<ObjectId> districtIds;

    /**
     * 待删除的物业区域
     */
    private Set<ObjectId> delDistrictIds;

    /**
     * 同一楼栋不同的房屋认证
     */
    private List<ObjectId> otherRoomsId;

    private List<ObjectId> otherRoomInCommunity;

    public Set<BuildingListVO> getBuildingList() {
        if(buildingList == null || buildingList.isEmpty()){
            buildingList = new HashSet<>();
        }
        return buildingList;
    }
}
