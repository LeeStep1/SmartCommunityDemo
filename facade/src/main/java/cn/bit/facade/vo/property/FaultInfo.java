package cn.bit.facade.vo.property;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fxiao
 * on 2018/3/9
 *  故障实体
 */
@Data
public class FaultInfo implements Serializable {

    private ObjectId id;

    /**
     * 故障类型
     * 1：住户；2：公共；
     */
    private Integer faultType;

    /**
     * 故障项目
     * 1：水电燃气；2：房屋结构；3：消防安防；9：其它；
     * 10：电梯；11：门禁；99：其它；
     */
    private Integer faultItem;

    /**
     * 故障状态
     * （0：已取消；1：待接受；2：待分派；3：待检修；4：已完成；-1：已驳回；）
     */
    private Integer faultStatus;

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
     * 故障地址（社区-楼栋-房间）
     */
    private String faultAddress;

    /**
     * 评价状态 （0：未评价；1：已评价）
     */
    private Integer evaluate;

}
