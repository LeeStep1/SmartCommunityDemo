package cn.bit.facade.model.business;

import cn.bit.facade.vo.business.BaseInfo;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by fxiao
 * on 2018/4/4
 * 便民服务
 */
@Data
@Document(collection = "BIZ_CONVENIENCE")
@CompoundIndex(def = "{'communityId':1,'dataStatus':1}", background = true, name = "appConvenience")
public class Convenience extends BaseInfo{
    @Id
    @NotNull(message = "便民服务ID不能为空", groups = Update.class)
    private ObjectId id;
    /**
     * 服务名称
     */
    @NotNull(message = "服务名称不能为空", groups = {Add.class})
    @Length(max = 30, message = "服务名称不能超30字")
    private String name;
    /**
     * 图片
     */
    @NotNull(message = "ICON不能为空", groups = {Add.class})
    private String icon;
    /**
     * 服务类型（1：生活服务；2：家政服务）
     */
    @NotNull(message = "分类不能为空", groups = {Add.class})
    private Integer serviceType;
    /**
     * 所属社区
     */
    @NotNull(message = "社区ID不能为空", groups = Search.class)
    private ObjectId communityId;
    /**
     * 所属社区名称
     */
    private String communityName;
    /**
     * 服务方式
     * 1:热线服务,2:在线服务
     */
    private Integer serviceWay;
    /**
     * 热线电话
     */
    private String contact;
    /**
     * 外部链接
     */
    private String url;
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
    /**
     * 排序
     */
    private Integer rank;

    public interface Add{}

    public interface Update{}

    public interface Search{}
}
