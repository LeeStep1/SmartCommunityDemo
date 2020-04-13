package cn.bit.facade.vo.communityIoT;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaoxi.lao
 * @Description :
 * @Date ： 2018/12/10 16:54
 */
@Data
public class DeviceDataStatisticsVO implements Serializable {
    private Integer doors;

    private Integer faultDoors;

    private Integer cameras;

    private Integer faultCameras;

    private Integer gates;

    private Integer faultGates;
}
