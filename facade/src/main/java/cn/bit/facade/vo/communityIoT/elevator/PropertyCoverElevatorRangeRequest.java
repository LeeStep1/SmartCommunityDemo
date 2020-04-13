package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

/**
 * @Description :
 * @Date ： 2019/10/30 11:41
 */
@Data
public class PropertyCoverElevatorRangeRequest implements Serializable {
    private List<String> terminalCode;

    private ObjectId userId;
}
