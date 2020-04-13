package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class ElevatorRecordRequest implements Serializable {

    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    private String communityName;

    private String macAddress;

    private String userName;

    private Integer userStatus;

    private String deviceId;

    private Integer useType;

    private Integer useStyle;

    private String keyNo;

    private String result;

    private String userCommand;

    private Date startDate;

    private Date endDate;

    private String phone;
}
