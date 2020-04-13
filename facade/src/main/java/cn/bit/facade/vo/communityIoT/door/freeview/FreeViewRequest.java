package cn.bit.facade.vo.communityIoT.door.freeview;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class FreeViewRequest implements Serializable {

    private String mobile;

    private String devUserName;

    private String userName;

    private String tenantCode;

    private String requestID;

    private String deviceDirectory;

    private Integer cardMediaTypeID;

    private Integer cardTypeID;

    private FreeViewCardRequest card;

    private String cardSerialNumber;

    private Date validStartTime;

    private Date validEndTime;

    /**
     * 最大可用次数
     */
    private Integer maxAvailableTimes;

    /**
     * 二维码
     */
    private String password;

    /**
     * 申请临时密码的设备路径
     */
    private List<String> deviceLocalDirectoryArray;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getValidStartTime() {
        return validStartTime;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getValidEndTime() {
        return validEndTime;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getValidTimeStart() {
        return validStartTime;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getValidTimeEnd() {
        return validEndTime;
    }

    public String getDeviceLocalDirectory() {
        return getDeviceDirectory();
    }

    public FreeViewRequest(String devUserName, String tenantCode, FreeViewCardRequest card) {
        setDevUserName(devUserName);
        setTenantCode(tenantCode);
        setCard(card);
    }
}
