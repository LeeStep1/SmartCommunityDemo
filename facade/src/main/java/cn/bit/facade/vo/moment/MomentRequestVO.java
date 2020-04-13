package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * 动态信息请求参数
 */
@Data
public class MomentRequestVO implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 发布者名称
     */
    private String creatorName;

    /**
     * 发布者ID集合
     */
    private Collection<ObjectId> creatorId;

    /**
     * 发布开始时间
     */
    private Date createStart;

    /**
     * 发布结束时间
     */
    private Date createEnd;
}
