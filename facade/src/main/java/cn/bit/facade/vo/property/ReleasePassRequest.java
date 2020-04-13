package cn.bit.facade.vo.property;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
public class ReleasePassRequest implements Serializable {

    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;
    /**
     * 业主id
     */
    private ObjectId userId;
    /**
     * 业主名字
     */
    private String userName;
    /**
     * 业主手机号
     */
    private String phone;
    /**
     * 确认人id
     */
    private ObjectId verifierId;
    /**
     * 确认人名称
     */
    private String verifierName;
    /**
     * 放行条状态（1:已使用；0:未使用；-1：已过期）
     */
    private Integer releaseStatus;
    /**
     * 数据状态（0：无效；1：有效）
     */
    private Integer dataStatus;

}
