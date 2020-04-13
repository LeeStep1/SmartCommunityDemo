package cn.bit.facade.vo.communityIoT;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author xiaoxi.lao
 * @Description :
 * @Date ： 2018/12/20 10:21
 */
@Data
public class DeviceRequest implements Serializable {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 终端号
     */
    private String terminalCode;
}
