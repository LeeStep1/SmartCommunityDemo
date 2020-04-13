package cn.bit.facade.vo.task;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class GenerateRequest implements Serializable {

    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    @NotBlank(message = "角色/岗位不能为空")
    private String postCode;

    @NotNull(message = "类型不能为空")
    private Integer classType;

    @NotNull(message = "开始时间不能为空")
    private Date startDate;

    @NotNull(message = "结束时间不能为空")
    private Date endDate;
}
