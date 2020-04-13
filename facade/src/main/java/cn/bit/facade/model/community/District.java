package cn.bit.facade.model.community;

import cn.bit.facade.enums.ManufactureType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "CM_DISTRICT")
public class District implements Serializable {
    @Id
    @NotNull(message = "职能区域ID不能为空", groups = {EditDistrict.class})
    private ObjectId id;
    /**
     * 职能区域名称
     */
    @NotNull(message = "职能区域名称不能为空", groups = {AddDistrict.class})
    private String name;
    /**
     * @since 2018-06-21
     * 米立设备对应房间id
     */
    private String thirdPartId;
    /**
     * 第三方设备对应id
     */
    @NotNull(message = "第三方ID不能为空", groups = {AddThirdPartInfo.class})
    private List<String> thirdPartIds;
    /**
     * 楼栋id集合
     */
    @NotNull(message = "楼栋ID集合不能为空", groups = {AddDistrict.class})
    private Set<ObjectId> buildingIds;
    /**
     * 设备厂商
     */
    @NotNull(message = "第三方厂商不能为空", groups = {AddThirdPartInfo.class})
    private Integer brandNo = ManufactureType.UNKNOWN.KEY;
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {AddDistrict.class, ListDistrict.class})
    @Indexed(background = true)
    private ObjectId communityId;
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
     * 职能区域是否开放使用
     */
    private Boolean open;

    /**
     * 区域编号，协议用的
     */
    private Integer no;

    public interface AddDistrict {}

    public interface EditDistrict {}

    public interface ListDistrict {}

    public interface AddThirdPartInfo {}
}
