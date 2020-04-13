package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DoorRecordResponse implements Serializable {
    /**
     * 门禁分块集合
     */
    private List<Section> doorSections;
}
