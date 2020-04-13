package cn.bit.facade.vo.communityIoT.door.freeview;

import cn.bit.framework.utils.DateUtils;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class FreeViewCardRequest implements Serializable {
    /**
     * 门禁卡序列号
     */
    private String cardSerialNumber;

    /**
     * 卡介质类型
     */
    private Integer cardMediaTypeID;

    /**
     * 卡介质类型
     */
    private Integer cardType;

    private Date validStartTime;

    private Date validEndTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getValidStartTime() {
        return validStartTime;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getValidEndTime() {
        return validEndTime;
    }

    public void buildCardRequestForApplyAuth(String cardSerialNumber, Integer cardMediaTypeID, Integer cardType) {
        Date start = new Date();
        setValidStartTime(start);
        setValidEndTime(DateUtils.addYear(start, 50));
        setCardType(cardType);
        setCardMediaTypeID(cardMediaTypeID);
        setCardSerialNumber(cardSerialNumber);
    }
}
