package cn.bit.facade.vo.vehicle;

import cn.bit.facade.model.vehicle.InOut;
import lombok.Data;

import java.io.Serializable;

@Data
public class CarInOutVO extends InOut implements Serializable {
    private String inGateName;

    private String outGateName;
}
