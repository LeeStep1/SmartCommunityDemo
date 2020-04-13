package cn.bit.facade.service.community;

import cn.bit.facade.model.community.Room;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RoomFacade {

    /**
     * 新增楼房
     * @param entity
     * @return
     */
    Room addRoom(Room entity) throws BizException;

    /**
     * 批量添加房间
     * @param entity
     * @return
     * @throws BizException
     */
    Boolean addRooms(List<Room> entity) throws BizException;

    /**
     * 更新楼房
     * @param entity
     * @return
     */
    Room updateRoom(Room entity) throws BizException;

    /**
     * 查询
     * @param id
     * @return
     */
    Room findOne(ObjectId id) throws BizException;

    /**
     * 获取楼房列表
     * @param entity
     * @return
     */
    List<Room> queryList(Room entity) throws BizException;

    /**
     * 删除信息
     * @param id
     */
    boolean deleteRoom(ObjectId id) throws BizException;

    /**
     * 分页
     * @param buildingId
     * @param name
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    Page<Room> queryPage(ObjectId buildingId, String name, int page, int size) throws BizException;

    /**
     * 修改数据状态
     * @param id
     * @return
     */
    boolean changeDataStatus(ObjectId id) throws BizException;

    /**
     * 获取房间的全名/位置
     * @param room
     * @return
     * @throws BizException
     */
    String getRoomLocation(Room room) throws BizException;

    /**
     * 根据楼宇ID 获取房间数量
     * @param buildingId
     * @return
     */
    public Long countRoomByBuildingId(ObjectId buildingId) throws BizException;

    /**
     * 根据社区获取房间数量
     * @param communityId
     * @return
     */
    public Long countRoom(ObjectId communityId);

    Long findRoomsInSameFloor(Collection<ObjectId> roomIds, ObjectId target);

    List<Room> findRoomsByIds(Set<ObjectId> roomIds);

    /**
     * 根据楼栋获取房间列表
     * @param buildingId
     * @return
     */
    List<Room> queryByBuildingId(ObjectId buildingId);

    /**
     * 根据第三方ID获取房间信息
     * @param outId
     * @return
     */
    Room findByOutId(String outId);

    void insertAllRooms(List<Room> toAddRooms, ObjectId createId);

    List<Room> queryMiliRooms(List<ObjectId> collect);

    void bindFeesTemplate(ObjectId id, ObjectId feesTemplateId);

    /**
     * 校验房间是否存在
     *
     * @param communityId
     * @param roomId
     * @return
     */
    void checkExistByCommunityIdAndRoomId(ObjectId communityId, ObjectId roomId);

    /**
     * 查询绑定的物业缴费模板Id
     * @param id
     * @return
     */
    ObjectId findTemplateIdById(ObjectId id);

    /**
     * 查询房间电梯主副门权限
     * @param roomIds
     * @param communityId
     * @return
     */
    List<Room> findRoomsMainSubDoorControlInfoByIds(Collection<ObjectId> roomIds, ObjectId communityId);

    /**
     * 更新房间主副门设置
     * @param roomId
     * @param mainDoor
     * @param subDoor
     */
    void updateMainSubDoorById(ObjectId roomId, Boolean mainDoor, Boolean subDoor);
}
