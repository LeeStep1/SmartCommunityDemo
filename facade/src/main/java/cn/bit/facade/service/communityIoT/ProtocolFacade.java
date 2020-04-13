package cn.bit.facade.service.communityIoT;

import cn.bit.facade.vo.communityIoT.protocol.BanCardVO;
import cn.bit.facade.vo.communityIoT.protocol.IcCardVO;
import cn.bit.facade.vo.communityIoT.protocol.ProtocolVO;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Date;

public interface ProtocolFacade {

    /**
     * 小区管理员协议
     * @param communityId
     * @param os
     * @return
     */
    String encodeProtocol4Property(ObjectId communityId, Integer os);

    /**
     * 临时通行协议
     * @param communityId
     * @param userId
     * @param processTime
     * @param roomId
     * @return
     */
    String encodeProtocol4Visitor(ObjectId communityId, ObjectId userId, Date processTime, ObjectId roomId);


    /**
     * 区域管理员协议
     * @param communityId
     * @param userId
     * @param districtIds
     * @return
     */
    String encodeProtocol4District(ObjectId communityId, ObjectId userId, Collection<ObjectId> districtIds);

    /**
     * 单元管理员协议
     * @param communityId
     * @param userId
     * @return
     */
    String encodeProtocol4Unit(ObjectId communityId, ObjectId userId);

    /**
     * 楼栋管理员协议
     * @param communityId
     * @param buildingId
     * @param userId
     * @return
     */
    String encodeProtocol4Building(ObjectId communityId, ObjectId userId, ObjectId buildingId);

    /**
     * 房屋信息协议
     * @param communityId
     * @param userId
     * @param os
     * @return
     */
    String encodeProtocol4Room(ObjectId communityId, ObjectId userId, Integer os);

    /**
     * 终端写入协议
     * @param protocolVO
     * @return
     */
    String decodeProtocol4Terminal(ProtocolVO protocolVO, ObjectId communityId);

    /**
     * 设备读取mac协议
     * @return
     */
    String encodeProtocol4ReadDevice(ObjectId communityId);

    /**
     * 离线ic卡协议
     * @param communityId
     * @param icCardVO
     * @return
     */
	String encodeProtocol4IC(ObjectId communityId, IcCardVO icCardVO);

    /**
     * 物业禁卡协议
     * @param buildingId
     * @param roomIds
     * @return
     */
    BanCardVO encodeProtocol4BanCard(ObjectId buildingId, Collection<ObjectId> roomIds);

    /**
     * 物业解除禁卡协议
     * @param buildingId
     * @param roomIds
     * @return
     */
    BanCardVO encodeProtocol4LiftBanCard(ObjectId buildingId, Collection<ObjectId> roomIds);

    /**
     * 开启禁用梯控时间协议
     * @param buildingId
     * @param startAt
     * @param endAt
     * @return
     */
    String encodeProtocol4LiftControl(ObjectId buildingId, Date startAt, Date endAt);
}
