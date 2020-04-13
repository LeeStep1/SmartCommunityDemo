package cn.bit.facade.vo.mq;

import cn.bit.facade.vo.communityIoT.elevator.FloorVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class KangTuElevatorAuthVO extends ElevatorAuthVO implements Serializable {
    /**
     * 电梯物联需要的参数
     */
    private Set<FloorVO> builds;

    public Set<FloorVO> getBuilds() {
        if(builds == null || builds.isEmpty()){
            builds = new HashSet<>();
        }
        return builds;
    }

}
