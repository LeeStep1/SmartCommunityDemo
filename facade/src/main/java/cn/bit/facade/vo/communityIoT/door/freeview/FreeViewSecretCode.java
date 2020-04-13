package cn.bit.facade.vo.communityIoT.door.freeview;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
public class FreeViewSecretCode implements Serializable {
    private ObjectId roomId;

    private String validStartTime;

    private String validEndTime;

    private Integer maxAvailableTimes;

    private String deviceLocalDirectory;
}
