package cn.bit.facade.vo.fees;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class OrderRequest implements Serializable {

    @NotNull(message = "账单id不能为空")
    private ObjectId id;

    @NotNull(message = "账单总金额不能为空")
    private Long totalAmount;

    @NotNull(message = "交易平台不能为空")
    private Integer platform;
}
