package cn.bit.facade.vo.user;

import cn.bit.facade.model.community.Building;

import java.io.Serializable;

public class ProprietorStatistics implements Serializable {
    private Building building;
    private Integer total;

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
