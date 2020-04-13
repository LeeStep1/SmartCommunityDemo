package cn.bit.facade.vo.user.userToProperty;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserToProperty implements Serializable
{
    @Id
    private ObjectId id;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 社区名称
     */
    private String communityName;

    /**
     * 物业公司ID
     */
    private ObjectId propertyId;

    /**
     * 物业公司名称
     */
    private String propertyName;

    /**
     * 岗位Code
     */
    private Set<String> postCode;

    /**
     * 用户ID
     */
    private ObjectId userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机
     */
    private String phone;

    /**
     * 轮班顺序
     */
    @Transient
    private Integer shiftOrder;

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
     * 数据状态  1：有效；0：无效
     */
    private Integer dataStatus;

    /**
     * 物业人员工号
     */
    private String employeeId;

    /**
     * 性别（0：未知；1：男；2：女）
     */
    private Integer sex;

    /**
     * 楼栋ID集合：用于物业职权范围分配
     */
    private Set<ObjectId> buildingIds;
    /**
     * 区域ID集合：用于物业职权范围分配
     */
    private Set<ObjectId> districtIds;

    /**
     * 用于保存米立用户
     */
    private Set<Long> miliUIds;

    /**
     * 同网易云通信IM的accid
     */
    @Transient
    private String accid;

    /**
     * 员工性质
     */
    private Boolean official;

    public Set<ObjectId> getBuildingIds(){
        if (buildingIds == null){
            buildingIds = new HashSet<>();
        }

        return buildingIds;
    }

    public Set<ObjectId> getDistrictIds(){
        if (districtIds == null){
            districtIds = new HashSet<>();
        }

        return districtIds;
    }

    public Set<Long> getMiliUIds(){
        if (miliUIds == null){
            miliUIds = new HashSet<>();
        }

        return miliUIds;
    }
}
