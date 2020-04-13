package cn.bit.facade.model.community;

import cn.bit.facade.vo.community.Points;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据布局实体
 */
@Data
@Document(collection = "DATA_LAYOUT")
public class DataLayout implements Serializable {

    @Id
    private ObjectId id;

    /**
     * 模块key
     */
    private String key;

    /**
     * 模块名称
     */
    private String name;

    /**
     * 社区ID
     */
    @Indexed(background = true)
    private ObjectId communityId;

    /**
     * 屏幕比例类型，预留字段
     */
    private Integer screenRatioType;

    /**
     * 坐标描点
     */
    private Points points;

    /**
     * 附加属性值
     */
    private Integer attachValue;

    /**
     * 数据刷新间隔，单位秒
     */
    private Integer refreshInterval;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 是否展示
     */
    private Boolean displayable;
}
