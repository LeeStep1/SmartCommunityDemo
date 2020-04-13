package cn.bit.facade.vo.vehicle;

import cn.bit.facade.model.vehicle.InOut;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InoutVo extends InOut
{
    /**
     * 停留时间
     */
    private Long parkingTime;
}
