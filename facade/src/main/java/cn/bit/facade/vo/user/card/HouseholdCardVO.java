package cn.bit.facade.vo.user.card;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class HouseholdCardVO implements Serializable {
    private String userName;

    private String phone;

    /**
     * {@link cn.bit.facade.enums.CertificateType}
     */
    @NotNull(message = "卡片类型不能为空")
    private Integer keyType;

    @NotBlank(message = "卡号不能为空")
    private String keyNo;

    @NotNull(message = "房间ID不能为空")
    private ObjectId roomId;

    private ObjectId householdId;

    /**
     * 有效期时长
     */
    private Integer processTime;

    private Date expireAt;

    /**
     * 有效期时长的时间度量单位
     * {@link cn.bit.facade.enums.TimeUnitEnum}
     */
    private Integer timeUnit;
}
