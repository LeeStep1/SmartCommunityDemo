package cn.bit.facade.data.property;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Complain extends BaseEntity {

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 投诉来源
     * 1：住户；2：员工；
     */
    private Integer source;

    /**
     * 投诉人ID
     */
    private ObjectId userId;

    /**
     * 投诉工单状态 {@link cn.bit.facade.enums.ComplainStatusEnum}
     * （0：待受理，1：待处理，2：已处理，3：已评价，-1：已驳回）
     */
    private Integer status;

    /**
     * 评价等级
     * （1-5个等级）
     */
    private Integer score;

    /**
     * 匿名投诉
     */
    private Boolean anonymity;

    /**
     * 无效工单
     */
    private Boolean invalid;
}
