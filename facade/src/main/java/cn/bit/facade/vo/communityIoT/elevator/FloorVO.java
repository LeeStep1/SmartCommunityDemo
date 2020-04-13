package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class FloorVO implements Serializable {
    /**
     * 楼栋ID
     */
    private String buildId;
    /**
     * 主门楼层
     */
    private Set<String> floors;

    /**
     * 副门楼层
     */
    private Set<String> subFloors;

    public FloorVO(String buildId) {
        this.buildId = buildId;
    }
}
