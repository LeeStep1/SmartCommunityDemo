package cn.bit.facade.vo.property;

import cn.bit.common.facade.query.PageQuery;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

@Data
public class FaultPageQuery extends PageQuery implements Serializable {

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 用户ID
     */
    private ObjectId userId;

    /**
     * 维修工ID
     */
    private ObjectId repairId;

    /**
     * 不包含隐藏数据
     */
    private Boolean hidden;

    /**
     * 状态
     */
    private Integer faultStatus;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 开始时间
     */
    private Date startAt;

    /**
     * 结束时间
     */
    private Date endAt;
}
