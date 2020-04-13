package cn.bit.facade.vo.vehicle;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.Date;

@Data
public class InoutRequest implements Serializable {

    /**
     * 车闸编号
     */
    private String gateNO;

    /**
     * 出入时间
     */
    private Date inOutDate;

    /**
     * 车牌号
     */
    @NotBlank(message = "车牌号不能为空", groups = {CarInOut.class})
    private String carNo;

    private Date enterAtBefore;

    private Date enterAtAfter;

    private Date leaveAtBefore;

    private Date leaveAtAfter;

    public interface CarInOut {}

}
