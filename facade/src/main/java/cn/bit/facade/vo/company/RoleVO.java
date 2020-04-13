package cn.bit.facade.vo.company;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class RoleVO implements Serializable {

    /**
     * id
     */
    private ObjectId id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String desc;

    /**
     * 所属企业类型
     */
    private Integer type;

    /**
     * 二级分类
     */
    private Integer subtype;

    /**
     * 租户ID
     */
    private ObjectId tenantId;

    /**
     * 保护功能码
     */
    private List<String> funcs;

    /**
     * 推送功能节点
     */
    private Set<String> pushPoints;

    /**
     * 数据状态
     */
    private Integer dataStatus;
}
