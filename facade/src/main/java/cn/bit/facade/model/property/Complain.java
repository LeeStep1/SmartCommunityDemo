package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "PROP_COMPLAIN")
public class Complain implements Serializable {

    @Id
    private ObjectId id;

    /**
     * 投诉人id
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
     * 住房信息
     */
    private List<String> roomInfo;

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 信息来源（住户/员工，由请求头里的CLIENT做出判断。住户：1，物业：2）
     */
    private Integer messageSource;

    /**
     * 投诉内容
     */
    @NotBlank(message = "投诉内容不能为空")
    @Length(max = 200, message = "投诉内容不能超过200字符")
    private String content;

    /**
     * 图片，最大支持3张
     */
    @Size(max = 3, message = "图片不能超过3张")
    private List<String> photos;

    /**
     * 匿名投诉
     */
    private Boolean anonymity;

    /**
     * 投诉工单状态
     */
    private Integer status;

    /**
     * 处理结果
     */
    @Length(max = 1000, message = "处理结果不能超过1000字符")
    private String result;

    /**
     * 处理时间
     */
    private Date processAt;

    /**
     * 对处理结果的评价
     */
    private Integer evaluation;

    /**
     * 评价内容
     */
    @Length(max = 200, message = "评价内容不能超过200字符")
    private String evaluationContent;

    /**
     * 隐藏工单
     */
    private Boolean hidden;

    /**
     * 无效工单
     */
    private Boolean invalid;

    /**
     * 创建时间（投诉时间）
     */
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;
}
