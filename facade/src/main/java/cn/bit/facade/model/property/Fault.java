package cn.bit.facade.model.property;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by fxiao
 * on 2018/3/7
 * 故障表
 */
@Data
@Document(collection = "PROP_FAULT")
public class Fault implements Serializable {

    @Id
    @NotNull(message = "故障表ID不能为空", groups = {Audit.class, Assign.class})
    private ObjectId id;
    /**
     * 故障编号
     */
    @Indexed(background = true)
    private String no;
    /**
     * 社区ID
     */
    @Indexed(background = true)
    private ObjectId communityId;

    /**
     * 楼宇ID
     */
    private ObjectId buildingId;

    /**
     * 故障类型
     * 1：住户；2：公共；
     */
    @NotNull(message = "故障类型不能为空", groups = Add.class)
    private Integer faultType;

    /**
     * 故障项目
     * 1：水电燃气；2：房屋结构；3：消防安防；9：用户其他
     * 10：电梯；11：门禁；99：其它；
     */
    @NotNull(message = "故障项目不能为空", groups = Add.class)
    private Integer faultItem;

    /**
     * 故障描述
     */
    @NotBlank(message = "故障描述不能为空", groups = Add.class)
    private String faultContent;

    /**
     * 故障图片
     */
    @Size(max = 5, message = "故障图片不能超过五张")
    private List<String> faultAccessory;

    /**
     * 故障状态
     * （-1：已驳回；0：已取消；1：已提交；2：已受理；3：已指派；4：已完成；）
     */
    @NotNull(message = "故障处理结果不能为空", groups = {Audit.class})
    private Integer faultStatus;

    /**
     * 4：已完成
     * 完成时间
     */
    private Date finishTime;

    /**
     * 设备ID
     */
    private ObjectId deviceId;

    // ==================== 申请人 ====================

    /**
     * 申请人ID
     */
    @Indexed
    private ObjectId userId;

    /**
     * 申请人名称
     */
    private String userName;

    /**
     * 申请时间
     */
    private Date playTime;

    /**
     * 身份
     * 1：住户；2：物业
     */
    private Integer identity;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 房间ID
     */
    private ObjectId roomId;

    /**
     * 故障地址（社区-楼栋-房间）
     */
    private String faultAddress;

    // ====================== 受理人 ==================
    /**
     * 受理人ID
     */
    private ObjectId acceptId;

    /**
     * 受理人名称
     */
    private String acceptName;

    /**
     * 受理时间
     */
    private Date acceptTime;

    // =================评论===============
    /**
     * 评价状态 （0：未评价；1：已评价）
     */
    private Integer evaluate;

    /**
     * 评价等级
     * （1-5个等级）
     */
    private Integer evaluationGrade;

    /**
     * 评价
     */
    private String evaluation;

    /**
     * 评价文件（图片）
     */
    private List<String> evaluationAccessory;

    /**
     * 评论时间
     */
    private Date evaluationTime;

    // ====================驳回====================

    /**
     * 驳回ID
     */
    private ObjectId rejectId;

    /**
     * 驳回人名称
     */
    private String rejectName;

    /**
     * 驳回时间
     */
    private Date rejectTime;

    /**
     * 驳回理由
     */
    private String rejectReason;

    // ==================维修人===================

    /**
     * 维修人ID
     */
    @NotNull(message = "请选择维修人员", groups = Assign.class)
    private ObjectId repairId;

    /**
     * 维修人名称
     */
    private String repairName;

    /**
     * 维修人联系方式
     */
    private String repairContact;

    /**
     * 维修人类型
     * 1：社区维修人；2：私人维修人
     */
    private Integer repairType;

    /**
     * 是否产生费用状态（预留字段）
     * 0：没产生费用；1：产生费用；
     */
    private Integer payStatus;

    /**
     * 电梯故障单
     * 是否已发送到电梯互联网
     * (0或null：未发送；1：已发送)
     */
    private Integer sendToElevator;

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
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;

    /**
     * 申报开始时间
     */
    @Transient
    private Date playTimeBegin;

    /**
     * 申报最后时间
     */
    @Transient
    private Date playTimeEnd;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 新增
     */
    public interface Add {
    }

    /**
     * 审核
     */
    public interface Audit {
    }


    /**
     * 分派维修人
     */
    public interface Assign {
    }

}
