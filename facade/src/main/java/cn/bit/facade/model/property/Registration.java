package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "PROP_REGISTRATION")
public class Registration implements Serializable {

    /**
     * 员工登记ID
     */
    @Id
    private ObjectId id;

    /**
     * 应用组代码
     */
    private Integer partner;

    /**
     * 员工ID
     */
    private ObjectId employeeId;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 角色/岗位
     */
    private Set<String> roles;

    /**
     * 手机号
     */
    private String phone;

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

}
