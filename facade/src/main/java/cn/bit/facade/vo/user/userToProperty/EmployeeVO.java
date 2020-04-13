package cn.bit.facade.vo.user.userToProperty;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by decai.liu at  2018/3/31
 */
@Data
public class EmployeeVO implements Serializable{

    /**
     * userToProperty Id
     */
    @NotNull(message = "员工记录ID不能为空", groups = {Modify.class})
    private ObjectId id;

    /**
     * 物业公司ID
     */
    private ObjectId propertyId;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 工号
     */
    @NotBlank(message = "工号不能为空", groups = {Create.class})
    private String employeeId;

    /**
     * 用户ID
     */
    private ObjectId userId;

    /**
     * 员工名称
     */
    @NotBlank(message = "姓名不能为空", groups = {Create.class})
    private String userName;

    /**
     * 员工电话
     */
    @NotBlank(message = "电话不能为空", groups = {Create.class})
    private String phone;

    /**
     * 员工岗位
     */
    @NotBlank(message = "岗位不能为空", groups = {Create.class})
    private String postCode;

    /**
     * 员工性别
     */
    @NotNull(message = "性别不能为空", groups = {Create.class})
    private Integer sex;

    /**
     * 是否正式员工
     */
    @NotNull(message = "员工性质不能为空", groups = {Create.class})
    private Boolean official;

    /**
     * 是否注册
     */
    private Boolean registered;

    public interface Create {}
    public interface Modify {}

}
