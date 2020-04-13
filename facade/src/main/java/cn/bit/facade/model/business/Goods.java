package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by fxiao
 * on 2018/4/4
 * 商品
 */
@Data
@Document(collection = "BIZ_GOODS")
public class Goods extends BaseInfo {
    @Id
    @NotNull(message = "商品ID不能为空", groups = Update.class)
    private ObjectId id;
    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空", groups = {Add.class, Update.class})
    private String name;
    /**
     * 图片
     */
    private String picture;
    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空", groups = {Add.class, Update.class})
    @Indexed(background = true)
    private ObjectId shopId;
    /**
     * 商家名称
     */
    private String shopsName;
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

    public interface Add{}

    public interface Update{}
}
