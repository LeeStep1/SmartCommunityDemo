package cn.bit.facade.data.communityIoT;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoorRecord extends BaseEntity {
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 门禁ID
     */
    private ObjectId doorId;
    /**
     * 操作方式(1：蓝牙；2：远程)
     */
    private Integer useStyle;
}
