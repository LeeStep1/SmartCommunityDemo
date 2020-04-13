package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * 商家类型
 */
@Data
@Document(collection = "BIZ_SHOP_TYPE")
public class ShopType extends BaseInfo{
    @Id
    private ObjectId id;
    /**
     * 类型名称
     */
    @NotNull(message = "类型不能为空")
    private String name;
    /**
     * 类型码
     */
    @NotNull(message = "类型码不能为空")
    private Integer code;
    /**
     * 标签（中餐/西餐/火锅/川菜/等等）
     */
    private Set<String> tag;
    /**
     * 创建人ID
     */
    private ObjectId createId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 数据状态
     */
    private Integer dataStatus;
}
