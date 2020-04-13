package cn.bit.facade.vo.communityIoT.door;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/14
 **/
@Data
@NoArgsConstructor
public class CommunityDoorVO implements Serializable {

    /**
     * 社区ID
     */
    private String houseId;

    /**
     * 门禁硬件参数
     */
    private Set<DoorDeviceVO> doors;

    public Set<DoorDeviceVO> getDoors() {
        if (doors == null){
            setDoors(new HashSet<>());
        }
        return doors;
    }

    public CommunityDoorVO(String houseId, Set<DoorDeviceVO> doors) {
        this.houseId = houseId;
        this.doors = doors;
    }
}
