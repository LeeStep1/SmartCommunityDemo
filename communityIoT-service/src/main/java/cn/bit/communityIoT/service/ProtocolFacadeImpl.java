package cn.bit.communityIoT.service;

import cn.bit.common.facade.community.enums.LevelEnum;
import cn.bit.common.facade.community.model.Building;
import cn.bit.common.facade.community.model.Room;
import cn.bit.communityIoT.support.protocol.DeviceProtocol;
import cn.bit.communityIoT.support.protocol.data.DeviceProtocolFactory;
import cn.bit.communityIoT.support.protocol.data.Message4B55;
import cn.bit.communityIoT.support.protocol.data.MsgHouse;
import cn.bit.communityIoT.support.protocol.impl.Protocol4B55;
import cn.bit.facade.enums.TimeUnitEnum;
import cn.bit.facade.enums.protocol.MsgTypeEnum;
import cn.bit.facade.enums.protocol.ProtocolTypeEnum;
import cn.bit.facade.enums.protocol.ProtocolVersionEnum;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.District;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.DistrictFacade;
import cn.bit.facade.service.communityIoT.ProtocolFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.community.broadcast.BroadcastSchema;
import cn.bit.facade.vo.community.broadcast.DeviceSchema;
import cn.bit.facade.vo.communityIoT.protocol.BanCardVO;
import cn.bit.facade.vo.communityIoT.protocol.IcCardVO;
import cn.bit.facade.vo.communityIoT.protocol.ProtocolVO;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.BUILDING_ID_NULL;
import static cn.bit.facade.exception.community.CommunityBizException.BUILDING_NOT_EXISTS;
import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.*;

/**
 * @author decai.liu
 * @desc 广播协议实现类
 * @date 2018-07-11 17:29
 */
@Service("protocolFacade")
@Slf4j
public class ProtocolFacadeImpl implements ProtocolFacade {

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Resource
    private cn.bit.common.facade.community.service.CommunityFacade commonCommunityFacade;

    // 职能区域
    @Autowired
    private DistrictFacade districtFacade;

    /**
     * {@link ProtocolVersionEnum}
     * 对应枚举的phrase
     */
    private static final String PROTOCOL = "Protocol4B55";

    private static final String ZERO = "0";
    private static final char PAD_CHAR = '0';
    private static final String ENCODING = "utf8";

    private static final int EIGHT = 8;

    /**
     * 小区管理员协议（广播）
     *
     * @param communityId
     * @param os
     * @return
     */
    @Override
    public String encodeProtocol4Property(ObjectId communityId, Integer os) {
        Community community = communityFacade.findOne(communityId);
        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCID(community.getNo());
        message4B55.setMsgCMD(9);
        message4B55.setMsgSys(os);
        log.info("start 4B55 encodeProtocol4Property !!!");
        return protocol.encode(message4B55);
    }

    /**
     * 临时通行协议（访客）
     *
     * @param communityId
     * @param userId
     * @param processTime
     * @return
     */
    @Override
    public String encodeProtocol4Visitor(ObjectId communityId, ObjectId userId, Date processTime, ObjectId roomId) {
        // 根据房屋去获取层级关系
        Map<Integer, Integer> levelNoMap = null;
        try {
            levelNoMap = communityFacade.listLevelNos(LevelEnum.ROOM.value(), roomId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        if (levelNoMap == null) {
            log.error("没有获取到对应的层级结构");
            return null;
        }
        // 社区
        Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
        // 区域
        Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
        // 楼栋
        Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
        // 楼层
        Integer floorNo = levelNoMap.get(LevelEnum.FLOOR.value());
        if (communityNo == null) {
            log.error("社区没有对应的编号");
            return null;
        }
        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCID(communityNo);
        message4B55.setMsgCMD(0);
        // 临时通行过期时间
        message4B55.setMsgTime(processTime);
        MsgHouse msgHouse = new MsgHouse();
        msgHouse.setDistrictId(zoneNo == null ? 0 : zoneNo);
        msgHouse.setUnitId(0);
        msgHouse.setBuildingId(buildingNo == null ? 0 : buildingNo);
        msgHouse.setFloorId(floorNo == null ? 0 : floorNo);
        message4B55.setMsgHouses(Collections.singletonList(msgHouse));
        log.info("start 4B55 encodeProtocol4Visitor !!!");
        return protocol.encode(message4B55);
    }

    /**
     * 房屋信息协议
     *
     * @param communityId
     * @param userId
     * @param os          操作系统（1：IOS, 2：Android, 3：h5, 4:IPad, 5:AndroidPad）
     * @return
     */
    @Override
    public String encodeProtocol4Room(ObjectId communityId, ObjectId userId, Integer os) {
        UserToRoom userToRoom = userToRoomFacade.findInCommonUseByCommunityIdAndUserId(communityId, userId);
        if (userToRoom == null) {
            userToRoom = userToRoomFacade.findTop1ByCommunityIdAndUserId(communityId, userId);
        }
        if (userToRoom == null) {
            log.error("用户在此社区没有任何房屋认证");
            return null;
        }

        String locationCode = userToRoom.getLocationCode();
        // 旧数据，则要根据楼栋去获取层级关系
        if (!StringUtil.isNotNull(locationCode)) {
            //根据房屋去获取层级关系
            Map<Integer, Integer> levelNoMap = null;
            try {
                levelNoMap = communityFacade.listLevelNos(LevelEnum.ROOM.value(), userToRoom.getRoomId());
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
            if (levelNoMap == null) {
                log.error("没有获取到对应的层级结构");
                return null;
            }
            // 社区
            Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
            // 区域
            Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
            // 楼栋
            Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
            // 楼层
            Integer floorNo = levelNoMap.get(LevelEnum.FLOOR.value());

            if (communityNo == null) {
                log.error("社区没有对应的编号");
                return null;
            }

            StringBuffer levelCode = new StringBuffer();
            levelCode.append(StringUtil.leftPadWithBytes(communityNo.toString(), 5, PAD_CHAR, ENCODING));
            levelCode.append(StringUtil.leftPadWithBytes(zoneNo == null ? ZERO : zoneNo.toString(), 2, PAD_CHAR, ENCODING));
            // 单元 00
            levelCode.append(ZERO).append(ZERO);
            levelCode.append(StringUtil.leftPadWithBytes(buildingNo == null ? ZERO : buildingNo.toString(), 3, PAD_CHAR, ENCODING));
            levelCode.append(StringUtil.leftPadWithBytes(floorNo == null ? ZERO : floorNo.toString(), 3, PAD_CHAR, ENCODING));
            locationCode = levelCode.toString();
            // 写入locationCode及设置常住房屋
            UserToRoom toUpdate = new UserToRoom();
            if (userToRoom.getInCommonUse() == null || !userToRoom.getInCommonUse()) {
                toUpdate.setInCommonUse(Boolean.TRUE);
            }
            toUpdate.setLocationCode(locationCode);
            toUpdate.setUpdateAt(new Date());
            userToRoomFacade.updateById(toUpdate, userToRoom.getId());
        }
        if (locationCode.length() != 15) {
            log.error("房屋所在位置编号错误：{}", locationCode);
            return null;
        }
        log.info("房屋所在位置编号：{}", locationCode);

        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCID(Integer.parseInt(locationCode.substring(0, 5)));//小区
        message4B55.setMsgCMD(4);//协议类型
        message4B55.setMsgSys(os);
        MsgHouse msgHouse = new MsgHouse();
        msgHouse.setDistrictId(Integer.parseInt(locationCode.substring(5, 7)));//区域
        msgHouse.setUnitId(Integer.parseInt(locationCode.substring(7, 9)));//单元
        msgHouse.setBuildingId(Integer.parseInt(locationCode.substring(9, 12)));//楼栋
        msgHouse.setFloorId(Integer.parseInt(locationCode.substring(12, 15)));//楼层
        message4B55.setMsgHouses(Collections.singletonList(msgHouse));
        log.info("start 4B55 encodeProtocol4Visitor !!!");
        return protocol.encode(message4B55);
    }

    /**
     * 终端ID写入协议
     *
     * @param protocolVO
     * @return
     */
    @Override
    public String decodeProtocol4Terminal(ProtocolVO protocolVO, ObjectId communityId) {
        Integer level = protocolVO.getLevel();
        ObjectId target = protocolVO.getTarget();
        // 根据level target 获取层级结构
        Map<Integer, Integer> levelNoMap = communityFacade.listLevelNos(level, target);
        Integer msgCMD = 6;
        Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
        Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
        Integer unitNo = 0;
        Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
        if (communityNo == null) {
            log.error("社区没有对应的编号");
            return null;
        }
        // 社区
        Integer msgType = MsgTypeEnum.COMMUNITY.value();
        if (buildingNo != null) {
            // 楼栋
            msgType = MsgTypeEnum.BUILDING.value();
        } else if (zoneNo != null) {
            // 区域
            msgType = MsgTypeEnum.ZONE.value();
        }

        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCMD(msgCMD);
        message4B55.setMsgType(msgType);
        message4B55.setMsgCID(communityNo);
        MsgHouse msgHouse = new MsgHouse();
        msgHouse.setDistrictId(zoneNo == null ? 0 : zoneNo);
        // 暂时没有单元 set 0
        msgHouse.setUnitId(unitNo);
        msgHouse.setBuildingId(buildingNo == null ? 0 : buildingNo);
        message4B55.setMsgHouses(Collections.singletonList(msgHouse));
        log.info("start 4B55 decodeProtocol4Terminal !!!");
        return protocol.encode(message4B55);
    }

    /**
     * 设备读取mac协议
     *
     * @return
     */
    @Override
    public String encodeProtocol4ReadDevice(ObjectId communityId) {
        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCMD(5);
        log.info("start 4B55 encodeProtocol4ReadDevice");
        return protocol.encode(message4B55);
    }

    /**
     * 区域管理员协议
     *
     * @param communityId
     * @param userId
     * @param districtIds
     * @return
     */
    @Override
    public String encodeProtocol4District(ObjectId communityId, ObjectId userId, Collection<ObjectId> districtIds) {
        if (districtIds == null || districtIds.isEmpty()) {
            log.error("区域ID为空");
            return null;
        }
        Community community = communityFacade.findOne(communityId);
        Integer msgCID = community.getNo();
        if (msgCID == null) {
            log.error("社区没有对应的编号");
            return null;
        }
        Integer msgCMD = 1;

        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;

        List<District> districtList = districtFacade.findInIds(districtIds);
        if (districtList.isEmpty()) {
            log.error("districtFacade.findInIds 没有找到对应的区域信息");
            return null;
        }

        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCMD(msgCMD);
        message4B55.setMsgCID(msgCID);

        if (districtList.size() > 8) {
            // 默认取前8个区域
            districtList = districtList.subList(0, 8);
        }
        List<MsgHouse> msgHouseList = new ArrayList<>();
        districtList.stream().forEach(district -> {
            MsgHouse msgHouse = new MsgHouse();
            msgHouse.setDistrictId(district.getNo() == null ? 0 : district.getNo());
            msgHouseList.add(msgHouse);
        });
        message4B55.setMsgHouses(msgHouseList);
        return protocol.encode(message4B55);
    }

    /**
     * 单元管理员协议
     *
     * @param communityId
     * @param userId
     * @return
     */
    @Override
    public String encodeProtocol4Unit(ObjectId communityId, ObjectId userId) {
        //TODO 待实现
        return null;
    }

    /**
     * 楼栋管理员协议
     *
     * @param communityId
     * @param userId
     * @return
     */
    @Override
    public String encodeProtocol4Building(ObjectId communityId, ObjectId userId, ObjectId buildingId) {
        //TODO 待实现
        return null;
    }

    /**
     * 离线ic卡协议
     *
     * @param communityId
     * @param icCardVO
     * @return
     */
    @Override
    public String encodeProtocol4IC(ObjectId communityId, IcCardVO icCardVO) {
        Integer userType = icCardVO.getUserType();
        ObjectId targetId = icCardVO.getTargetId();
        Date expireAt = icCardVO.getExpireAt();
        Integer processTime = icCardVO.getProcessTime();
        Integer timeUnit = icCardVO.getTimeUnit() == null ? TimeUnitEnum.SECOND.value() : icCardVO.getTimeUnit();
        if (TimeUnitEnum.fromValue(timeUnit) == null) {
            throw TIME_UNIT_INVALID;
        }
        if (userType == 1 && targetId == null) {
            throw TARGET_ID_IS_NULL;
        }

        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(communityId);
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", communityId);
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;

        // 根据房屋去获取层级关系
        Map<Integer, Integer> levelNoMap = null;
        Integer msgCMD = null;
        try {
            // 住户
            if (userType == 1) {
                msgCMD = 10;
                levelNoMap = communityFacade.listLevelNos(LevelEnum.ROOM.value(), targetId);
                // 物业
            } else if (userType == 2) {
                msgCMD = 11;
                levelNoMap = communityFacade.listLevelNos(LevelEnum.COMMUNITY.value(), communityId);
            } else {
                log.info("IC卡用户类型不正确");
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        if (levelNoMap == null) {
            log.error("没有获取到对应的层级结构");
            return null;
        }
        // 社区
        Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
        // 区域
        Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
        // 单元
        Integer unitNo = 0;
        // 楼栋
        Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
        // 楼层
        Integer floorNo = levelNoMap.get(LevelEnum.FLOOR.value());
        // 房号
        Integer roomNo = levelNoMap.get(LevelEnum.ROOM.value());

        if (communityNo == null) {
            log.error("社区没有对应的编号");
            return null;
        }
        if (userType == 1 && floorNo == null) {
            log.error("房间没有对应的楼层号");
            return null;
        }

        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCMD(msgCMD);
        // IC卡用户类型
        message4B55.setMsgType(userType);
        message4B55.setMsgCID(communityNo);
        if (expireAt == null && processTime == null) {
            // 有效期默认50年
            expireAt = DateUtils.addYear(new Date(), 50);
        } else {
            if (expireAt == null) {
                expireAt = getExpireAt(processTime, timeUnit);
            }
        }
        message4B55.setMsgTime(DateUtils.getEndTime(expireAt));
        MsgHouse msgHouse = new MsgHouse();
        msgHouse.setDistrictId(zoneNo == null ? 0 : zoneNo);
        // 暂时没有单元 set 0
        msgHouse.setUnitId(unitNo);
        msgHouse.setBuildingId(buildingNo == null ? 0 : buildingNo);
        msgHouse.setFloorId(floorNo == null ? 0 : floorNo);
        msgHouse.setRoomId(roomNo == null ? 0 : roomNo);
        message4B55.setMsgHouses(Collections.singletonList(msgHouse));
        log.info("start 4B55 encodeProtocol4IC：{}", message4B55);
        return protocol.encode(message4B55);
    }

    @Override
    public BanCardVO encodeProtocol4BanCard(ObjectId buildingId, Collection<ObjectId> roomIds) {
        return encodeProtocol4BanCardByMsgCMD(buildingId, roomIds, 80);
    }

    @Override
    public BanCardVO encodeProtocol4LiftBanCard(ObjectId buildingId, Collection<ObjectId> roomIds) {
        return encodeProtocol4BanCardByMsgCMD(buildingId, roomIds, 81);
    }

    @Override
    public String encodeProtocol4LiftControl(ObjectId buildingId, Date startAt, Date endAt) {
        Building building = commonCommunityFacade.getBuildingByBuildingId(buildingId);
        if (building == null) {
            log.info("{}-楼栋不存在", buildingId);
            throw BUILDING_NOT_EXISTS;
        }

        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(building.getCommunityId());
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", building.getCommunityId());
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        // 根据楼栋去获取层级关系
        Map<Integer, Integer> levelNoMap = communityFacade.listLevelNos(LevelEnum.BUILDING.value(), buildingId);
        if (levelNoMap == null) {
            log.error("没有获取到对应的层级结构");
            return null;
        }
        // 社区
        Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
        // 区域
        Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
        // 单元
        Integer unitNo = 0;
        // 楼栋
        Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCMD(128);
        message4B55.setMsgCID(communityNo);
        MsgHouse msgHouse = new MsgHouse();
        msgHouse.setDistrictId(zoneNo == null ? 0 : zoneNo);
        // 暂时没有单元 set 0
        msgHouse.setUnitId(unitNo);
        msgHouse.setBuildingId(buildingNo == null ? 0 : buildingNo);
        message4B55.setMsgHouses(Collections.singletonList(msgHouse));
        message4B55.setMsgTime(startAt);
        message4B55.setMsgTimeTo(endAt);
        log.info("start 4B55 encodeProtocol4LiftControl !!!");
        return protocol.encode(message4B55);
    }

    /**
     * 离线禁卡、解除禁卡协议
     * @param buildingId
     * @param roomIds
     * @param msgCMD
     * @return
     */
    private BanCardVO encodeProtocol4BanCardByMsgCMD(ObjectId buildingId, Collection<ObjectId> roomIds, Integer msgCMD) {
        if (buildingId == null) {
            throw BUILDING_ID_NULL;
        }
        if (roomIds == null || roomIds.isEmpty()) {
            throw ROOMS_EMPTY;
        }
        Building building = commonCommunityFacade.getBuildingByBuildingId(buildingId);
        if (building == null) {
            log.info("{}-楼栋不存在", buildingId);
            throw BUILDING_NOT_EXISTS;
        }
        Community community = communityFacade.findOne(building.getCommunityId());
        List<Room> rooms = commonCommunityFacade.listRoomsByBuildingId(buildingId);
        if (!rooms.stream().map(Room::getId).collect(Collectors.toSet()).containsAll(roomIds)) {
            throw ROOMS_NOT_IN_THE_SAME_BUILDING;
        }

        DeviceProtocol deviceProtocol = getDeviceProtocolByCommunityId(building.getCommunityId());
        if (deviceProtocol == null) {
            log.info("{}-小区不存在4B55离线协议", building.getCommunityId());
            return null;
        }
        Protocol4B55 protocol = (Protocol4B55) deviceProtocol;
        // 根据楼栋去获取层级关系
        Map<Integer, Integer> levelNoMap = communityFacade.listLevelNos(LevelEnum.BUILDING.value(), buildingId);
        if (levelNoMap == null) {
            log.error("没有获取到对应的层级结构");
            return null;
        }
        // 社区
        Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
        // 区域
        Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
        // 单元
        Integer unitNo = 0;
        // 楼栋
        Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
        Message4B55 message4B55 = new Message4B55();
        message4B55.setMsgCMD(msgCMD);
        message4B55.setMsgCID(communityNo);
        MsgHouse msgHouse = new MsgHouse();
        msgHouse.setDistrictId(zoneNo == null ? 0 : zoneNo);
        // 暂时没有单元 set 0
        msgHouse.setUnitId(unitNo);
        msgHouse.setBuildingId(buildingNo == null ? 0 : buildingNo);
        message4B55.setMsgHouses(Collections.singletonList(msgHouse));
        log.info("start 4B55 encodeProtocol4BanCard !!!");
        String block1 = protocol.encode(message4B55);
        int num = roomIds.size();
        List<String> roomNo16List = new ArrayList<>(num);
        List<BanCardVO.IdName> roomLocationList = new ArrayList<>(num);
        String buildingLocation = community.getName() + building.getName();
        BanCardVO banCardVO = new BanCardVO();
        for (Room room : rooms) {
            if (roomIds.contains(room.getId())) {
                // 根据楼栋去获取层级关系
                Map<Integer, Integer> roomLevelMap = communityFacade.listLevelNos(LevelEnum.ROOM.value(), room.getId());
                Integer floorNo = roomLevelMap.get(LevelEnum.FLOOR.value()) == null ? 0 : roomLevelMap.get(LevelEnum.FLOOR.value());
                Integer roomNo = roomLevelMap.get(LevelEnum.ROOM.value()) == null ? 0 : roomLevelMap.get(LevelEnum.ROOM.value());
                String floorNoStr = StringUtil.leftPadWithBytes(floorNo.toString(), 2, PAD_CHAR, ENCODING);
                String roomNoStr = StringUtil.leftPadWithBytes(roomNo.toString(), 2, PAD_CHAR, ENCODING);
                // 将楼层no及房间no转成2个byte的10进制字符串作为房间号
                roomNo16List.add(floorNoStr + roomNoStr);
                BanCardVO.IdName idName = banCardVO.new IdName();
                idName.setId(room.getId());
                idName.setName(buildingLocation + room.getName());
                roomLocationList.add(idName);
            }
        }

        StringBuffer key = new StringBuffer(block1);
        do {
            key.append(",");
            List<String> temp = null;
            if (roomNo16List.size() > EIGHT) {
                temp = new ArrayList<>(roomNo16List.subList(0, EIGHT));
                roomNo16List.removeAll(temp);
            } else {
                temp = new ArrayList<>(roomNo16List);
                roomNo16List.clear();
            }
            StringBuffer block = new StringBuffer();
            for (String roomNo : temp) {
                block.append(roomNo);
            }
            key.append(StringUtil.rightPadWithBytes(block.toString(), 32, PAD_CHAR, ENCODING));
        } while (roomNo16List.size() > 0);

        banCardVO.setBuildingId(buildingId);
        banCardVO.setKey(key.toString());
        banCardVO.setRoomLocations(roomLocationList);
        return banCardVO;
    }

    /**
     * 根据小区获取4B55离线协议实体
     * @param communityId
     * @return
     */
    private DeviceProtocol getDeviceProtocolByCommunityId(ObjectId communityId) {
        Community community = communityFacade.findOne(communityId);
        BroadcastSchema broadcastSchema = community.getBroadcastSchema();
        if (broadcastSchema == null || Objects.isNull(broadcastSchema.getDeviceProtocols())
                || broadcastSchema.getDeviceProtocols().isEmpty()) {
            log.error("社区没有配置相关离线设备协议");
            return null;
        }
        log.info("此社区存在{}个正常设备协议，deviceProtocolList:{}", broadcastSchema.getDeviceProtocols().size(),
                broadcastSchema.getDeviceProtocols());

        List<DeviceSchema> deviceProtocolList = broadcastSchema.getDeviceProtocols().stream()
                .filter(deviceSchema -> ProtocolTypeEnum.OFFLINE.value().equals(deviceSchema.getProtocolType())
                        && ProtocolVersionEnum.fromValue(deviceSchema.getProtocol()) != null
                        && ProtocolVersionEnum.fromValue(deviceSchema.getProtocol()).phrase().equals(PROTOCOL))
                .collect(Collectors.toList());
        if (deviceProtocolList.isEmpty() || deviceProtocolList.get(0) == null) {
            log.error("社区没有配置离线设备协议（4B55）");
            return null;
        }
        return DeviceProtocolFactory.getInstance(PROTOCOL);
    }

    /**
     * 根据有效时长及度量单位计算过期时间
     *
     * @param processTime
     * @param timeUnit    {@link TimeUnitEnum}
     * @return
     */
    private Date getExpireAt(int processTime, int timeUnit) {

        if (TimeUnitEnum.MILLISECOND.value().equals(timeUnit)) {
            return DateUtils.addMillisecond(new Date(), processTime);
        }
        if (TimeUnitEnum.SECOND.value().equals(timeUnit)) {
            return DateUtils.addSecond(new Date(), processTime);
        }
        if (TimeUnitEnum.MINUTE.value().equals(timeUnit)) {
            return DateUtils.addMinute(new Date(), processTime);
        }
        if (TimeUnitEnum.HOUR.value().equals(timeUnit)) {
            return DateUtils.addHour(new Date(), processTime);
        }
        if (TimeUnitEnum.DAY.value().equals(timeUnit)) {
            return DateUtils.addDay(new Date(), processTime);
        }
        if (TimeUnitEnum.MONTH.value().equals(timeUnit)) {
            return DateUtils.addMonth(new Date(), processTime);
        }
        if (TimeUnitEnum.YEAR.value().equals(timeUnit)) {
            return DateUtils.addYear(new Date(), processTime);
        }
        return null;
    }
}
