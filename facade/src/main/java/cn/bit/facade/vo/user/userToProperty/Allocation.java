package cn.bit.facade.vo.user.userToProperty;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by decai.liu at  2018/9/11
 */
@Data
public class Allocation implements Serializable{

    /**
     * employee Id
     */
    @NotNull(message = "员工ID不能为空")
    private ObjectId id;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 员工岗位
     */
    @NotBlank(message = "岗位不能为空")
    private String postCode;
}
