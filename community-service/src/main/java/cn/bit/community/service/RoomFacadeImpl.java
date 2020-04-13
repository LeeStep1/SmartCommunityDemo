package cn.bit.community.service;

import cn.bit.common.facade.community.constant.CodeConstants;
import cn.bit.common.facade.community.enums.CodeEnum;
import cn.bit.common.facade.community.model.Building;
import cn.bit.common.facade.community.model.Community;
import cn.bit.common.facade.community.model.Floor;
import cn.bit.common.facade.community.query.RoomPageQuery;
import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.common.facade.exception.UnknownException;
import cn.bit.community.dao.RoomRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.*;

@Component("roomFacade")
@Slf4j
public class RoomFacadeImpl implements RoomFacade {

    @Autowired
    private RoomRepository roomRepository;

    @Resource
    private cn.bit.common.facade.community.service.CommunityFacade commonCommunityFacade;

    @Override
    public Room addRoom(Room entity) throws BizException {
        if (entity.getBuildingId() == null) {
            throw BUILDING_ID_NULL;
        }
        if (!StringUtil.isNotNull(entity.getName())) {
            throw NAME_IS_NULL;
        }
        if (!StringUtil.isNotNull(entity.getFloorCode())) {
            throw FLOORCODE_IS_NULL;
        }
        if (entity.getArea() == null || entity.getArea() <= 0) {
            throw AREA_IS_INVALID;
        }

        Building toGet = commonCommunityFacade.getBuildingByBuildingId(entity.getBuildingId());
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw BUILDING_NOT_EXISTS;
        }
        //to check name exist
        Boolean exist = roomRepository.existsByBuildingIdAndNameAndDataStatus(
                entity.getBuildingId(), entity.getName(), DataStatusType.VALID.KEY);
        if (exist) {
            throw NAME_EXIST;
        }
        entity.setCommunityId(toGet.getCommunityId());
        entity.setCreateAt(new Date());
        entity.setUpdateAt(entity.getCreateAt());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return roomRepository.insert(entity);
    }

    @Override
    public Boolean addRooms(List<Room> entity) throws BizException {
        try {
            roomRepository.insertAll(entity);
            return true;
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        return false;
    }

    @Override
    public Room updateRoom(Room entity) throws BizException {
        if (entity.getId() == null) {
            throw ROOMID_NULL;
        }

        Room item = this.checkRoomStatus(entity.getId());
        if (entity.getArea() != null && entity.getArea() <= 0) {
            throw AREA_IS_INVALID;
        }

        if (StringUtil.isNotNull(entity.getName())) {
            //to check name exist
            Room toCheck = roomRepository.findByBuildingIdAndNameAndDataStatus(
                    item.getBuildingId(), item.getName(), DataStatusType.VALID.KEY);
            if (toCheck != null && !toCheck.getId().equals(entity.getId())) {
                throw NAME_EXIST;
            }
        }
        entity.setUpdateAt(new Date());
        entity.setId(null);
        return roomRepository.upsertById(entity, item.getId());
    }

    @Override
    public Room findOne(ObjectId id) throws BizException {
        if (id == null) {
            throw ROOMID_NULL;
        }

        cn.bit.common.facade.community.model.Room room = commonCommunityFacade.getRoomByRoomId(id);
        if (room == null) {
            throw ROOM_NOT_EXISTS;
        }

        Room result = convert(room, Room.class);
        Room toGet = roomRepository.findById(id);
        if (toGet != null) {
            result.setOutId(toGet.getOutId());
            result.setProprietor_id(toGet.getProprietor_id());
            result.setYun_proprietor_id(toGet.getYun_proprietor_id());
        }

        Floor floor = commonCommunityFacade.getFloorByFloorId(room.getFloorId());
        result.setFloorCode(floor.getName());
        result.setFloorNo(floor.getNo() + "");

        return result;
    }

    @Override
    public List<Room> queryList(Room entity) throws BizException {
        List<Room> list = roomRepository.findByNameAndCodeAndBuildingIdAndDataStatusAllIgnoreNullOrderByCreateAtAsc(
                entity.getName(), entity.getCode(), entity.getBuildingId(), DataStatusType.VALID.KEY);
        return list;
    }

    @Override
    public boolean deleteRoom(ObjectId id) throws BizException {
        this.checkRoomStatus(id);
        return roomRepository.remove(id) > 0;
    }

    @Override
    public Page<Room> queryPage(ObjectId buildingId, String name, int page, int size) throws BizException {
        if (!StringUtil.isNotNull(buildingId)) {
            throw BUILDING_ID_NULL;
        }

        RoomPageQuery pageQuery = new RoomPageQuery();
        pageQuery.setBuildingId(buildingId);
        pageQuery.setName(name);
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        cn.bit.common.facade.data.Page<cn.bit.common.facade.community.model.Room> roomPage =
                commonCommunityFacade.listRooms(pageQuery);
        List<Room> rooms = roomRepository.findAllByIdInAndDataStatus(roomPage.getRecords().stream()
                        .map(cn.bit.common.facade.community.model.Room::getId).collect(Collectors.toList()),
                DataStatusEnum.VALID.value());
        Map<ObjectId, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, room -> room));
        rooms = roomPage.getRecords().stream()
                .map(room -> {
                    Room r = convert(room, Room.class);
                    if (roomMap.containsKey(room.getId())) {
                        r.setOutId(roomMap.get(room.getId()).getOutId());
                        r.setProprietor_id(roomMap.get(room.getId()).getProprietor_id());
                        r.setYun_proprietor_id(roomMap.get(room.getId()).getYun_proprietor_id());
                    }
                    return r;
                })
                .collect(Collectors.toList());

        return new Page<>(roomPage.getCurrentPage(), roomPage.getTotal(), size, rooms);
    }

    @Override
    public boolean changeDataStatus(ObjectId id) throws BizException {
        this.checkRoomStatus(id);

        try {
            commonCommunityFacade.removeRoomByRoomId(id);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_ROOM_NOT_EXIST.equals(e.getSubCode())) {
                return false;
            }
        }

        Room toUpdate = new Room();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate = roomRepository.upsertById(toUpdate, id);
        return true;
    }

    private Room checkRoomStatus(ObjectId id) {
        cn.bit.common.facade.community.model.Room room = commonCommunityFacade.getRoomByRoomId(id);
        if (room == null) {
            throw ROOM_NOT_EXISTS;
        }

        Building building = commonCommunityFacade.getBuildingByBuildingId(
                room.getBuildingId());
        if (building.getOpen() != null && building.getOpen()) {
            throw BUILDING_USED;
        }

        return convert(room, Room.class);
    }

    @Override
    public String getRoomLocation(Room room) throws BizException {
        Community community = commonCommunityFacade.getCommunityByCommunityId(room.getCommunityId());
        Building building = commonCommunityFacade.getBuildingByBuildingId(room.getBuildingId());
        if (community != null && building != null) {
            return String.format("%s%s%s", community.getName(), building.getName(), room.getName());
        }
        return room.getName();
    }

    @Override
    public Long countRoomByBuildingId(ObjectId buildingId) throws BizException {
        Building building = commonCommunityFacade.getBuildingByBuildingId(buildingId);
        if (building == null || building.getRoomNum() == null) {
            return 0L;
        }
        return building.getRoomNum().longValue();
    }

    @Override
    public Long countRoom(ObjectId communityId) {
        Community community = commonCommunityFacade.getCommunityByCommunityId(communityId);
        if (community == null || community.getRoomNum() == null) {
            return 0L;
        }
        return community.getRoomNum().longValue();
    }

    @Override
    public Long findRoomsInSameFloor(Collection<ObjectId> roomIds, ObjectId target) {
        cn.bit.common.facade.community.model.Room room = commonCommunityFacade.getRoomByRoomId(target);
        if (room == null) {
            return -1L;
        }

        List<cn.bit.common.facade.community.model.Room> rooms = commonCommunityFacade.listRoomsByFloorId(room.getFloorId());
        List<ObjectId> targets = rooms.stream().map(cn.bit.common.facade.community.model.Room::getId)
                .collect(Collectors.toList());
        return (long) CollectionUtils.intersection(targets, roomIds).size();
    }

    @Override
    public List<Room> findRoomsByIds(Set<ObjectId> roomIds) {
        List<cn.bit.common.facade.community.model.Room> comRooms = commonCommunityFacade.listRoomsByRoomIds(roomIds);
        if (comRooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Room> rooms = roomRepository.findAllByIdInAndDataStatus(roomIds, DataStatusEnum.VALID.value());
        Map<ObjectId, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, room -> room));

        List<Floor> floors = commonCommunityFacade.listFloorsByFloorIds(comRooms.stream()
                .map(cn.bit.common.facade.community.model.Room::getFloorId).collect(Collectors.toSet()));
        Map<ObjectId, Floor> floorMap = floors.stream().collect(Collectors.toMap(Floor::getId, floor -> floor));

        return comRooms
                .stream()
                .map(room -> {
                    Room result = convert(room, Room.class);
                    Floor floor = floorMap.get(room.getFloorId());
                    if (floor != null) {
                        result.setFloorNo(floor.getNo() + "");
                        result.setFloorCode(floor.getName());
                    }

                    Room r = roomMap.get(room.getId());
                    if (r != null) {
                        if (StringUtil.isNotBlank(r.getFloorCode())) {
                            result.setFloorCode(r.getFloorCode());
                        }
                        result.setOutId(r.getOutId());
                        result.setProprietor_id(r.getProprietor_id());
                        result.setYun_proprietor_id(r.getYun_proprietor_id());
                        result.setMainDoor(r.getMainDoor());
                        result.setSubDoor(r.getSubDoor());
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据楼栋获取房间列表
     *
     * @param buildingId
     * @return
     */
    @Override
    public List<Room> queryByBuildingId(ObjectId buildingId) {
        if (buildingId == null) {
            return Collections.emptyList();
        }

        return commonCommunityFacade.listRoomsByBuildingId(buildingId)
                .stream()
                .map(room -> convert(room, Room.class))
                .collect(Collectors.toList());
    }

    @Override
    public Room findByOutId(String outId) {
        Room room = roomRepository.findByOutIdAndDataStatus(outId, DataStatusEnum.VALID.value());
        cn.bit.common.facade.community.model.Room comRoom = commonCommunityFacade.getRoomByRoomId(room.getId());
        Room result = convert(comRoom, Room.class);
        result.setYun_proprietor_id(room.getYun_proprietor_id());
        result.setProprietor_id(room.getProprietor_id());
        result.setOutId(room.getOutId());
        return result;
    }

    @Override
    public void insertAllRooms(List<Room> toAddRooms, ObjectId createId) {
        toAddRooms.forEach(room -> {
            room.setDataStatus(DataStatusType.VALID.KEY);
            room.setUpdateAt(new Date());
            room.setCreateAt(new Date());
            room.setCreateId(createId);
        });
        roomRepository.insertAll(toAddRooms);
    }

    @Override
    public List<Room> queryMiliRooms(List<ObjectId> collect) {
        List<Room> rooms = roomRepository.findByBuildingIdInAndOutIdIsNotNull(collect);
        Map<ObjectId, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, room -> room));
        List<cn.bit.common.facade.community.model.Room> comRooms = commonCommunityFacade.listRoomsByRoomIds(roomMap.keySet());
        return comRooms.stream()
                .map(room -> {
                    Room result = convert(room, Room.class);
                    if (roomMap.containsKey(room.getId())) {
                        result.setYun_proprietor_id(roomMap.get(room.getId()).getYun_proprietor_id());
                        result.setProprietor_id(roomMap.get(room.getId()).getProprietor_id());
                        result.setOutId(roomMap.get(room.getId()).getOutId());
                    }

                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void bindFeesTemplate(ObjectId id, ObjectId feesTemplateId) {
        Room room = new Room();
        room.setId(id);
        room.setFeesTemplateId(feesTemplateId);
        room.setCreateAt(new Date());
        room.setUpdateAt(room.getCreateAt());
        room = roomRepository.upsertWithUnsetIfNullFeesTemplateIdThenSetOnInsertCreateAtById(room, id);
        if (room == null) {
            throw OPERATION_FAILURE;
        }
    }

    /**
     * 校验房间是否存在
     *
     * @param communityId
     * @param roomId
     * @return
     */
    @Override
    public void checkExistByCommunityIdAndRoomId(ObjectId communityId, ObjectId roomId) {
        cn.bit.common.facade.community.model.Room room = commonCommunityFacade.getRoomByRoomId(roomId);
        if (room == null || !communityId.equals(room.getCommunityId())) {
            throw ROOM_NOT_EXISTS;
        }
    }

    private static <S, T> T convert(S source, Class<T> clazz) {
        if (source == null) {
            return null;
        }

        try {
            T target = clazz.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new UnknownException(CodeEnum.UNKNOWN_ERROR, e);
        }
    }

    /**
     * 查询绑定的物业缴费模板Id
     *
     * @param id
     * @return
     */
    @Override
    public ObjectId findTemplateIdById(ObjectId id) {
        Room room = roomRepository.findById(id);
        if (room == null) {
            return null;
        }
        return room.getFeesTemplateId();
    }

    @Override
    public List<Room> findRoomsMainSubDoorControlInfoByIds(Collection<ObjectId> roomIds, ObjectId communityId) {
        List<cn.bit.common.facade.community.model.Room> comRooms = commonCommunityFacade.listRoomsByRoomIds(roomIds);
        if (comRooms.isEmpty()) {
            return Collections.emptyList();
        }

        comRooms.removeIf(r -> !communityId.equals(r.getCommunityId()));

        List<Room> rooms = roomRepository.findAllByIdInAndDataStatus(roomIds, DataStatusEnum.VALID.value());
        Map<ObjectId, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, room -> room));

        return comRooms.stream().map(room -> {
            Room result = convert(room, Room.class);
            Room r = roomMap.get(room.getId());
            if (r != null) {
                // 设置主副门
                result.setMainDoor(r.getMainDoor());
                result.setSubDoor(r.getSubDoor());
            }
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateMainSubDoorById(ObjectId roomId, Boolean mainDoor, Boolean subDoor) {
        Room room = new Room();
        room.setId(roomId);
        room.setMainDoor(mainDoor);
        room.setSubDoor(subDoor);
        room.setDataStatus(DataStatusType.VALID.KEY);
        roomRepository.upsertById(room, roomId);
    }
}

