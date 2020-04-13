package cn.bit.facade.vo.user.userToProperty;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Set;

@Data
public class UserToPropertyResponse implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 物业ID
     */
    private ObjectId propertyId;

    /**
     * 岗位
     */
    private Set<String> postCode;

    /**
     * 员工记录ID
     */
    private ObjectId id;

    /**
     * 员工用户ID
     */
    private ObjectId userId;

    /**
     * 员工姓名
     */
    private String userName;

    /**
     * 电话
     */
    private String phone;

    /**
     * 工号
     */
    private String employeeId;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 员工性质
     */
    private Boolean official;
}
