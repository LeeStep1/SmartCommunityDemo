package cn.bit.facade.model.user;

import cn.bit.facade.enums.ClientType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 网易云IM信息
 */
@Data
@Document(collection = "U_IM_USER")
public class IMUser implements Serializable {

    /**
     * 同网易云通信IM的accid
     */
    @Id
    private ObjectId id;

    /**
     * 用户id
     */
    @NotNull(message = "userId不能为空")
    private ObjectId userId;

    /**
     * RoleType.name  目前只需要区别HOUSEHOLD和SUPPORTSTAFF这2个角色
     */
    @NotNull(message = "role不能为空")
    private String role;

    private String token;

    /**
     * 各端登录状态
     */
    private Set<String> loginClient;

    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;


    public IMUser() {
    }

    public IMUser(ObjectId userId, String role, String token, Integer dataStatus) {
        this.userId = userId;
        this.role = role;
        this.token = token;
        this.dataStatus = dataStatus;
    }
}
