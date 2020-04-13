package cn.bit.facade.vo.user.card;

import cn.bit.facade.vo.communityIoT.door.CommunityDoorVO;
import cn.bit.facade.vo.communityIoT.elevator.FloorVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/8
 **/
@Data
@NoArgsConstructor
public class CardRequest implements Serializable {
    /**
     * 卡片类型  1:手机MAC； 2:蓝牙卡MAC； 4:IC卡UID； 8:二维码信息号
     */
    private Integer keyType;
    /**
     * 卡号
     */
    private String keyNo;
    /**
     * 卡号
     */
    private String keyId;
    /**
     * 申请的楼层资料
     */
    private Set<FloorVO> builds;
    /**
     * 申请门禁资料
     */
    private Set<CommunityDoorVO> houses;
    /**
     * 有效期
     */
    private Integer processTime;
    /**
     * 有效次数
     */
    private Integer usesTime;
    /**
     * 是否做保留操作
     */
    private Boolean isKeep = false;

    public CardRequest(Integer keyType, String keyNo, String keyId, Set<FloorVO> builds, Set<CommunityDoorVO> houses,
                       Integer processTime, Integer usesTime, Boolean isKeep) {
        this.keyType = keyType;
        this.keyNo = keyNo;
        this.keyId = keyId;
        this.builds = builds;
        this.houses = houses;
        this.processTime = processTime;
        this.usesTime = usesTime;
        this.isKeep = isKeep;
    }

    public CardRequest(Integer keyType, String keyNo, String keyId, Set<FloorVO> builds) {
        this.keyType = keyType;
        this.keyNo = keyNo;
        this.keyId = keyId;
        this.builds = builds;
    }
}
