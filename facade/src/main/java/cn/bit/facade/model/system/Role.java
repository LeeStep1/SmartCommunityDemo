package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Terry on 2018/2/4 0004.
 */
@Document(collection = "SYS_ROLE")
@Data
public class Role implements Serializable {
    /**
     * 角色key,主键
     */
    @Id
    private String key;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String descr;

    /**
     * 权限key集合
     */
    private Set<String> permissions;
}
