package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

@Data
public class BuildingParams implements Serializable {
    /**
     * 社区ID
     */
    private Integer StructureID;
    /**
     * 结构ID
     */
    private String ParentDirectory;
    /**
     * 社区结构
     */
    private String Directory;
    /**
     * 楼栋ID
     */
    private String BuildingName;

    private Integer Attribute;

}
