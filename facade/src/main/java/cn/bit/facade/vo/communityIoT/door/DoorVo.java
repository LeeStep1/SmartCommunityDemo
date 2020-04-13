package cn.bit.facade.vo.communityIoT.door;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * 用于门禁资料管理
 **/
@Data
@NoArgsConstructor
public class DoorVo implements Serializable{
    /**
     * 设备ID
     */
    private ObjectId id;
    /**
     * 门禁名称
     */
    private String name;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 社区名称
     */
    @Transient
    private String communityName;
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;
    /**
     * 楼栋名称
     */
    @Transient
    private String buildingName;
    /**
     * 门禁厂商 (1: 米立  2：康途)
     */
    private Integer brandNo;
    /**
     * 厂商名称
     */
    private String brand;
    /**
     * 设备序列号
     */
    private String serialNo;
    /**
     * 设备编码
     */
    private String deviceCode;
    /**
     * 型号
     */
    private String deviceType;
    /**
     * 运行状态
     */
    private Integer onlineStatus;
    /**
     * 门类型(1:社区门，2:楼栋门)
     */
    private Integer doorType;

}
