package cn.bit.facade.model.user;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "U_CM_USER")
public class CommunityUser implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 用户ID
     */
    @Indexed(background = true)
    private ObjectId userId;
    /**
     * 客户端类型集合
     */
    private Set<Integer> clients;
    /**
     * 角色集合
     */
    private Set<String> roles;
    /**
     * 人脸信息是否录入 -2 : 录入失败  -1 : 正在录入  0 : 未录入  1 : 录入成功
     */
    private Integer faceStatus;
    /**
     * 人脸信息特征码
     */
    private String faceCode;
    /**
     * 指纹信息是否录入 -2 : 录入失败  -1 : 正在录入  0 : 未录入  1 : 录入成功
     */
    private Integer fingerprintStatus;
    /**
     * 人脸信息特征码
     */
    private String fingerprintCode;
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

    //--------------------- 20180912 将 UserToProperty 迁移至此 -------------------cecai.liu----------------------------
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
