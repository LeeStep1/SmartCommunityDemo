package cn.bit.facade.vo.communityIoT.door;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
@Data
public class DoorRecordRequest implements Serializable {

    @NotNull(message = "社区id不能为空")
    private ObjectId communityId;

    private String communityName;

    private String userName;

    private Integer userStatus;

    private String deviceId;

    private ObjectId doorId;

    private Integer useType;

    private Integer useStyle;

    private String keyNo;

    private String result;

    private String userCommand;

    private Date startDate;

    private Date endDate;
}
