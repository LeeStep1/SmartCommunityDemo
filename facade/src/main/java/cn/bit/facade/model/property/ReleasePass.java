package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;

@Data
@Document(collection = "PROP_RELEASE_BAR")
@Validated
public class ReleasePass implements Serializable {

    @Id
    @NotNull(message = "放行条id不能为空名", groups = {Update.class})
    private ObjectId id;
    /**
     * 用户id
     */
    private ObjectId userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 业主手机号
     */
    private String phone;
    /**
     * 业主楼栋
     */
    private String building;
    /**
     * 业主房间
     */
    private String room;
    /**
     * 物品清单
     */
    private String items;
    /**
     * 开始时间
     */
    @NotNull(message = "有效开始时间不能为空", groups = {Add.class})
    private Date beginAt;
    /**
     * 结束时间
     */
    @NotNull(message = "有效结束时间不能为空", groups = {Add.class})
    private Date endAt;
    /**
     * 附件（图片）路径
     */
    private LinkedHashSet<String> photos;
    /**
     * 放行条状态（1:已使用；0:未使用；-1：已过期）
     */
    private Integer releaseStatus;
    /**
     * 备注
     */
    private String remark;
    /**
     * 社区id
     */
    @NotNull(message = "社区id不能为空", groups = {Add.class})
    @Indexed(background = true)
    private ObjectId communityId;
    /**
     * 确认人id
     */
    private ObjectId verifierId;
    /**
     * 确认人名称
     */
    private String verifierName;
    /**
     * 创建人id
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
     * 数据状态（0：无效；1：有效）
     */
    private Integer dataStatus;

    public interface Add {
    }

    public interface Update {
    }

}
