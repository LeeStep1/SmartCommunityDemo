package cn.bit.facade.model.community;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "CM_PARAMETER")
public class Parameter implements Serializable {

    @Id
    @NotNull(message = "配置项ID不能为空",groups = {Update.class})
    private ObjectId id;

    /**
     * 社区id
     */
    @NotNull(message = "社区ID不能为空",groups = {Add.class})
    private ObjectId communityId;

    /**
     * 配置类型（1：配置账单参数；2：社区动态参数）
     */
    @NotNull(message = "配置类型不能为空", groups = {Add.class})
    private Integer type;

    /**
     * 字段
     */
    @NotBlank(message = "配置项的key不能为空", groups = {Add.class})
    private String key;

    /**
     * 值
     */
//    @NotBlank(message = "配置项的value不能为空", groups = {Add.class})
    private String value;

    /**
     * 配置项的标签名
     */
    @NotBlank(message = "配置项的value不能为空", groups = {Add.class})
    private String name;

    /**
     * 配置项是否必填
     */
    private Boolean isRequired;

    /**
     * 数据类型
     */
    private Integer dataType;

    /**
     * 控件输入规则
     */
    private String inputRule;

    /**
     * 创建人id
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改人id
     */
    private ObjectId modifierId;
    /**
     * 编辑时间
     */
    private Date updateAt;

    /**
     * 数据状态(1:有效；0：失效)
     */
    private Integer dataStatus;

    /**
     * 排序顺序
     */
    private Integer orderNum;

    /**
     * 配置项是否显示
     */
    private Boolean isDisplay;

    public interface Add {}

    public interface Update {}

}
