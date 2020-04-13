package cn.bit.communityIoT.support.protocol.data;

import lombok.Data;

/**
 * @author decai.liu
 * @desc 设备协议
 * @date 2018-07-05 15:26
 */

@Data
public class MsgHouse {
    /**
     * 区域id
     */
    private int districtId;

    /**
     * 单元id
     */
    private int unitId;

    /**
     * 楼栋id
     */
    private int buildingId;

    /**
     * 楼层
     */
    private int floorId;

    /**
     * 房间id
     */
    private int roomId;
}
