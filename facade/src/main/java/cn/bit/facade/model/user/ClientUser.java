package cn.bit.facade.model.user;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.*;

@Data
@Document(collection = "U_CLIENT_USER")
public class ClientUser implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 用户ID
     */
    @Indexed(background = true)
    private ObjectId userId;
    /**
     * 客户端（1000：住户端；1001：物业端；1002：后台管理）
     */
    private Integer client;
    /**
     * 密码（独立密码，预留，暂时与用户信息中的密码统一）
     */
    private String password;
    /**
     * 登录帐号
     */
//    @Indexed(background = true)
    private String loginName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 登录设备
     */
    private Set<UserDevice> userDevices;
    /**
     * 登录状态（0：离线；1：在线）
     */
    private Integer loginStatus;
    /**
     * 上次登录时间
     */
    private Date lastLoginAt;
    /**
     * 上次登录设备
     */
    private String lastLoginDeviceId;
    /**
     * 上次登录当前设备推送唯一标识（以第三方推送服务的设备ID为准）
     */
    private String lastPushId;
    /**
     * 附加信息
     * 最近登录的社区ID：communityId
     * 上次登录时间 lastLoginTime
     * 等等
     * 等等
     */
    private String attach;
    /**
     * 所在社区集合
     */
    @Indexed(background = true)
    private Set<ObjectId> communityIds;
    /**
     * 所包含权限集合
     */
    private Set<String> roles;
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

    @Transient
    public UserDevice getLastLoginDevice() {
        if (lastLoginDeviceId == null || userDevices.size() == 0) {
            return null;
        }

        Map<String, UserDevice> deviceMap = new HashMap<>();
        for (UserDevice device : userDevices) {
            deviceMap.put(device.getDeviceId(), device);
        }

        return deviceMap.get(lastLoginDeviceId);
    }

    public Set<UserDevice> getUserDevices() {
        if (userDevices == null) {
            userDevices = new HashSet<>();
        }

        return userDevices;
    }

    public Set<ObjectId> getCommunityIds() {
        if (communityIds == null) {
            communityIds = new HashSet<>();
        }

        return communityIds;
    }

}
