package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * 数据布局查询实体
 */
@Data
public class DataLayoutQuery implements Serializable {

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 屏幕比例类型，预留字段
     */
    private Integer screenRatioType;

    /**
     * 是否展示
     */
    private Boolean displayable;
}
