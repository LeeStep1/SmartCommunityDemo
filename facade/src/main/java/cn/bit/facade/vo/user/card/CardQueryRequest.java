package cn.bit.facade.vo.user.card;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 申请电梯凭证请求
 **/
@Data
public class CardQueryRequest implements Serializable {
    /**
     * 凭证类型
     */
    @NotNull(message = "卡类型不能为空")
    @JSONField(name = "KeyType")
    private Integer keyType;
    /**
     * 暂定是用户手机蓝牙MAC地址
     */
    @NotEmpty(message = "卡号不能为空")
    @JSONField(name = "KeyNO")
    private String keyNo;
    /**
     * 终端编号
     */
    @NotEmpty(message = "终端编号不能为空")
    @JSONField(name = "TerminalCode")
    private String terminalCode;
    /**
     * 终端端口号
     */
    @NotNull(message = "终端端口号不能为空")
    @JSONField(name = "TerminalPort")
    private Integer terminalPort;
    /**
     * 使用次数
     */
    @JSONField(name = "UseTimes")
    private Integer useTimes;
    /**
     * 有效开始时间
     */
    @JSONField(name = "StartDate")
    private Long startDate;
    /**
     * 有效结束时间
     */
    @JSONField(name = "EndDate")
    private Long endDate;
    /**
     * 无：0；1表示有效期控制；2表示时段控制；4表示星期控制；8表示直达召梯控制
     */
    @JSONField(name = "ControlType")
    private Integer controlType;
    /**
     * 楼层集合
     */
    @JSONField(name = "Floors")
    private String floors;
    /**
     * 操作类型
     */
    @JSONField(name = "ProcessType")
    private Integer processType;
}
