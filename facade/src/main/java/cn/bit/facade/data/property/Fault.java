package cn.bit.facade.data.property;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Fault extends BaseEntity {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 故障类型
     * 1：住户；2：公共；
     */
    private Integer type;
    /**
     * 维修人ID
     */
    private ObjectId repairId;
    /**
     * 故障状态
     * （0：已取消；1：已提交；2：已受理；3：已指派；4：已完成；-1：已驳回；）
     */
    private Integer status;
    /**
     * 评价等级
     * （1-5个等级）
     */
    private Integer score;
}
