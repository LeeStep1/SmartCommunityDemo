package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "SYS_RESOURCE")
@CompoundIndex(def = "{'type' : 1, 'uriSign' : 1}", background = true)
public class Resource implements Serializable {

    @Id
    private ObjectId id;
    /**
     * 键名
     */
//    @NotBlank(message = "键名不能为空")
    private String key;
    /**
     * 名称
     */
//    @NotBlank(message = "名称不能为空")
    private String name;
    /**
     * 类型（1：接口；2：菜单）
     */
    @NotNull(message = "类型不能为空")
    private Integer type;
    /**
     * URI
     */
    private String uri;
    /**
     * URI签名
     */
    private String uriSign;
    /**
     * 可见度（0：不可见；1：可见；2：有权限时可见）
     */
    private Integer visibility;
    /**
     * 客户端类型集合
     */
    private Set<Integer> clients;
    /**
     * 授权的角色集合
     */
    private Set<String> roles;
    /**
     * 资源组ID
     */
    private ObjectId groupId;
    /**
     *
     * 创建人ID
     */
    private ObjectId creatorId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;

}
