package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomResponse implements Serializable {
    /**
     * 总数
     */
    private Long total;
    /**
     * 入住房间数（认证房间数）
     */
    private Long checkInCount;
}
