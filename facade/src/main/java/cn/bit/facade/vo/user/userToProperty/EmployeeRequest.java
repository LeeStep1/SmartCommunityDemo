package cn.bit.facade.vo.user.userToProperty;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by decai.liu at 2019/12/06
 */
@Data
public class EmployeeRequest implements Serializable {

    /**
     * 物业公司ID
     */
    private ObjectId companyId;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 角色集合
     */
    private Set<String> roles;

    /**
     * 合作伙伴
     */
    private Integer partner;
}
