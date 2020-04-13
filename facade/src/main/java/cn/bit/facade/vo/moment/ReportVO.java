package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 举报请求参数
 */
@Data
public class ReportVO implements Serializable {

    private ObjectId communityId;

    @NotNull(message = "言论的ID不能为空", groups = {Add.class, Search.class})
    private ObjectId speechId;

    @NotNull(message = "言论类型不能为空", groups = {Add.class, Search.class})
    private Integer type;

    /**
     * 举报原因
     */
    @NotBlank(message = "原因不能为空", groups = {Add.class})
    private String reason;

    public interface Add{}
    public interface Search{}
}
