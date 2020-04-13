package cn.bit.facade.vo.vehicle;

import cn.bit.facade.enums.VerifiedType;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class VehicleAuditVo implements Serializable
{
    /**
     * 需要审核的车牌记录ID
     */
    @NotNull(message = "车牌记录ID为空")
    private ObjectId carId;

    /**
     * 审核状态: 0：未审核，  1：审核通过，  2：审核拒绝
     */
    private int verifyCode = VerifiedType.UNREVIEWED.getKEY();
}
