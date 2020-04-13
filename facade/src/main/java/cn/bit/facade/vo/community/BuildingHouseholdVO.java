package cn.bit.facade.vo.community;

import cn.bit.facade.model.community.Building;
import lombok.Data;

import java.io.Serializable;

/**
 * 楼宇有效用户
 */
@Data
public class BuildingHouseholdVO implements Serializable {

    /**
     * 有效用户数量
     */
    private Integer total;

    /**
     * 楼栋实体
     */
    private Building buildingEntity;
}
