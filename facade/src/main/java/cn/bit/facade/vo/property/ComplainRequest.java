package cn.bit.facade.vo.property;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
public class ComplainRequest implements Serializable {

    private ObjectId communityId;

    /**
     * 投诉人ID
     */
    private ObjectId userId;

    /**
     * 投诉人名称
     */
    private String userName;

    /**
     * 投诉人手机
     */
    private String phone;

    /**
     * 信息来源（住户/员工，由请求头里的CLIENT做出判断。住户：1，物业：2）
     */
    private Integer messageSource;

    /**
     * 投诉开始时间
     */
    private Date startAt;

    /**
     * 投诉截至时间
     */
    private Date endAt;

    /**
     * 投诉记录处理状态
     */
    private Set<Integer> status;

    /**
     * 是否隐藏工单
     */
    private Boolean hidden;

    /**
     * 无效工单
     */
    private Boolean invalid;

}
