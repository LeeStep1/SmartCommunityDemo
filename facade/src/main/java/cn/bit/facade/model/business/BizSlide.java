package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by fxiao
 * on 2018/4/2
 * 轮播图
 */
@Data
@Document(collection = "BIZ_SLIDE")
@CompoundIndex(def = "{'communityId':1,'published':1,'rank':1,'dataStatus':1}", background = true, name = "appSlide")
public class BizSlide extends BaseInfo {
    @Id
    @NotNull(message = "轮播图ID不能为空", groups = Update.class)
    private ObjectId id;
    /**
     * 标题
     */
    private String title;
    /**
     * 图片
     */
    @NotNull(message = "图片不能为空", groups = {Add.class, Update.class})
    private String photo;
    /**
     * 跳转类型（1：本地商店；2：外来连接）
     */
    @NotNull(message = "跳转类型不能为空", groups = {Add.class, Update.class})
    private Integer gotoType;
    /**
     * 外来连接
     */
    private String href;
    /**
     * 店铺ID
     */
    private ObjectId shopId;
    /**
     * 商家名称
     */
    private String shopName;
    /**
     * 发布状态(0:未发布；1：已发布)
     */
    private Integer published;
    /**
     * 发布时间
     */
    private Date publishAt;
    /**
     * 社区ID
     */
    @NotNull(message = "社区ID不能为空", groups = {Add.class, Update.class, Search.class})
    private ObjectId communityId;
    /**
     * 社区名称
     */
    private String communityName;
    /**
     * 排序
     */
    private Integer rank;
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

    public interface Search{}
}
