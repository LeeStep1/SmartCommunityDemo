package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Dell
 * @date 2018/6/27 13:31
 */
@Data
public class ElevatorFaultVO implements Serializable {
    /**
     * 故障ID
     */
    @NotNull(message = "故障ID不能为空")
    private ObjectId faultId;
    /**
     * 故障状态
     * （-1：已驳回；0：已取消；1：已提交；2：已受理；3：已指派；4：已完成；）
     */
    @NotNull(message = "故障状态不能为空")
    private Integer faultStatus;
    /**
     * 用户ID
     */
    private ObjectId repairId;
    /**
     * 用户名称
     */
    private String repairName;
    /**
     * 手机号
     */
    private String repairPhone;

}
