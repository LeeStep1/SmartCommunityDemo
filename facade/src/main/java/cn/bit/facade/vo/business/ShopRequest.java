package cn.bit.facade.vo.business;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by fxiao
 * on 2018/4/10
 */
@Data
public class ShopRequest implements Serializable {

    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空")
    private ObjectId communityId;

    /**
     * 标签（中餐/西餐/火锅/川菜/等等）
     */
    private String tag;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 搜索范围
     */
    private Double radius;

    private Integer page;

    private Integer size;

}
