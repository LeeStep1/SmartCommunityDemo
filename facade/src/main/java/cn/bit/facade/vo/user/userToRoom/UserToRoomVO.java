package cn.bit.facade.vo.user.userToRoom;

import cn.bit.facade.model.user.UserToRoom;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * userToRoom的key-value
 */
@Data
public class UserToRoomVO implements Serializable {
    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空", groups = {AddOwner.class})
    private ObjectId roomId;

    private ObjectId id;

    // 为兼容旧版app，去掉必填校验 2018/12/10
//    @NotBlank(message = "业主姓名不能为空", groups = {AddOwner.class})
    private String name;

    private String identityCard;

    private Date createAt;

    private String phone;

    private String roomLocation;

    private Integer sex;

    private Integer relationship;

    private Integer auditStatus;

    /**
     * 用户认证请求参数集合（key,value）
     */
    private Map<String, Object> authMap;

    /**
     * 用户认证返回参数列表(label,key,value)
     */
    private List<UserToRoom.AuthParam> authParamList;

    private String headImg;

    public interface AddOwner{}
}
