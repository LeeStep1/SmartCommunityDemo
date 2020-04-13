package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * 用于查询用户有权限的电梯列表
 **/
@Data
public class AuthElevatorRequest implements Serializable {

    /**
     * 蓝牙地址集合
     */
    private Set<String> macAddress;

    private ObjectId communityId;

    @NotNull(message = "用户ID不能为空")
    private ObjectId userId;

    private String keyId;

    private Integer keyType;
}
