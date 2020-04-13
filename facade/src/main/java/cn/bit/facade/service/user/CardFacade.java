package cn.bit.facade.service.user;

import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.vo.communityIoT.elevator.AuthElevatorRequest;
import cn.bit.facade.vo.communityIoT.elevator.KeyNoListElevatorVO;
import cn.bit.facade.vo.communityIoT.elevator.KeyNoListElevatorVOResponse;
import cn.bit.facade.vo.statistics.TenantApplicationRequest;
import cn.bit.facade.vo.statistics.TenantApplicationResponse;
import cn.bit.facade.vo.user.card.CardQueryRequest;
import cn.bit.facade.vo.user.card.CardVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSONObject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface CardFacade {

    /**
     * 通过社区ID和用户ID查询一卡通信息
     * @param card
     * @return
     * @throws BizException
     */
    Card getUserCardInCommunity(Card card) throws BizException;

    /**
     * 通过卡号与虚拟卡流水号找到卡信息
     */
    Card findCardByKeyNoAndKeyId(String keyNo, String keyId) throws BizException;

    /**
     * 通过社区ID和用户ID查询一卡通信息(分页)
     * @param entity
     * @return
     * @throws BizException
     */
    Page<Card> findUserCardInCommunityByKeyType(Card entity, Integer page, Integer size) throws BizException;

    /**
     * 删除卡权限并去除卡
     * @param entity
     * @return
     */
    boolean removeAuthAndDeleteCard(Card entity);

    /**
     * 申请一卡通(IC卡, 蓝牙卡, 二维码)
     * @param cardVO
     * @return
     */
    Card applyCardAndElevatorPermission(CardVO cardVO);

    /**
     * 更新一卡通权限
     * @param cardVO
     * @return
     */
    boolean updateCardElevatorPermission(CardVO cardVO);

    /**
     * 删除一卡通权限
     * @param cardVO
     * @return
     */
    boolean deleteCardElevatorPermission(CardVO cardVO);

    /**
     * 查找一卡通是否存在
     * @param keyNo
     * @param keyType
     * @return
     */
    Card findCardByKeyNoAndKeyType(String keyNo, Integer keyType);

    /**
     * 获取授权的电梯设备
     * @return
     */
    KeyNoListElevatorVOResponse getAuthElevatorDevice(Card card);

    /**
     * 组装电梯前端入参成电梯物联所需的参数 (一个房间)
     * @param cardVO
     * @param room
     * @return
     */
    CardVO buildCardVOForElevator(CardVO cardVO, Room room);

    /**
     * 更新卡片门禁信息
     * @param cardVO
     * @return
     */
    boolean updateCardDoorPermission(CardVO cardVO, List<Door> doors);

    /**
     * 删除门禁卡信息
     * @param cardVO
     * @return
     */
    boolean deleteCardDoorPermission(CardVO cardVO, List<Door> doors);

    /**
     * 访客申请统计
     *
     * @param tenantApplicationRequest
     * @return
     */
    TenantApplicationResponse getTenantApplicationStatistics(TenantApplicationRequest tenantApplicationRequest);

    /**
     * 更新卡片房间名称
     * @param cardVO
     * @param roomLocations
     */
    void updateCardRoomNameByRoomLocation(CardVO cardVO, Set<String> roomLocations);

    /**
     * 完成组装电梯物联必要的参数并申请卡片
     * @param cardVO
     * @param rooms
     * @return
     */
    Card applyCardForElevatorPermissionBy(CardVO cardVO, List<Room> rooms);

    /**
     * 完成组装电梯物联必要的参数并申请卡片
     *
     * @param cardVO
     * @param buildingIds
     * @return
     */
    Card applyCardForElevatorPermissionBy(CardVO cardVO, Set<ObjectId> buildingIds, UserToProperty userToProperty, Set<String> roomLocations);

    /**
     * 完成组装电梯物联必要的参数并更新卡片信息
     * @param cardVO
     * @param rooms
     * @return
     */
    boolean updateCardForElevatorPermissionBy(CardVO cardVO, List<Room> rooms);

    /**
     * 完成组装电梯物联必要的参数并删除卡片信息
     * @param cardVO
     * @param rooms
     * @return
     */
    boolean deleteCardForElevatorPermissionBy(CardVO cardVO, List<Room> rooms);

    /**
     * 根据楼栋更新电梯权限
     * @param cardVO
     * @param buildingIds
     * @return
     */
    CardVO updateCardElevatorPermissionFrom(CardVO cardVO, Set<ObjectId> buildingIds);

    List<KeyNoListElevatorVO> findUserAuthElevatorList(AuthElevatorRequest authElevatorRequest) throws BizException;

    /**
     * 申请二维码
     * @param uid
     * @param cardVO
     * @param rooms
     * @return
     */
    Card applyQRCardForElevatorByRooms(ObjectId uid, CardVO cardVO, List<Room> rooms);

    /**
     * 生成虚拟卡
     * @param userId
     * @param communityId
     * @param name
     * @return
     */
    Card applyCardForUser(ObjectId userId, ObjectId communityId, String name);

    /**
     * 住户申请实体卡
     * @param cardVO
     * @return
     */
    CardVO applyHouseholdPhysicalCard(CardVO cardVO);

    /**
     * 物业申请实体卡
     * @param cardVO
     * @return
     */
    CardVO applyPropertyPhysicalCard(CardVO cardVO);

    /**
     * 住户申请二维码
     * @param cardVO
     * @return
     */
    CardVO applyHouseholdQRCard(CardVO cardVO);

    /**
     * 根据卡号获取门禁卡信息
     * @param keyNo
     * @param communityId
     * @return
     */
    Card findByKeyNoAndCmId(String keyNo, ObjectId communityId);

    Card findByKeyNo(String keyNo);

    /**
     * 更改卡片是否已读状态
     * @param id
     * @param isProcessed
     * @return
     */
    Card updateIsProcessedById(ObjectId id, Integer isProcessed);

    JSONObject queryCardInfoFromDTU(CardQueryRequest request);

    Object addCardInfo2DTU(CardQueryRequest request);

    Object deleteCardInfoFromDTU(CardQueryRequest request);

    Boolean existKeyTypeAndKeyNoInCommunity(ObjectId communityId, String keyNo, Integer keyType);

	Card updateCardForProtocol(ObjectId cardId, String protocolKey);

    Page<Card> getUserCardRecord(Card entity, Integer page, Integer size);

    /**
     * 获取社区用户的手机蓝牙卡
     * @param communityId
     * @param userId
     * @param keyType
     * @return
     */
	Card findByCommunityIdAndUserIdAndKeyType(ObjectId communityId, ObjectId userId, int keyType);

    /**
     * 获取用户在社区指定类型的卡集合
     * @param userId
     * @param communityId
     * @param keyType
     * @return
     */
	List<Card> findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(ObjectId userId, ObjectId communityId, List<Integer> keyType);

    Card getByIdAndUserId(ObjectId id, ObjectId userId);

    Card applyQRCardForElevator(CardVO cardVO);

	CardVO applyOffLineICCard(CardVO cardVO);

    /**
     * 专门用于app端的卡片管理
     * @param entity
     * @return
     */
    List<Card> findUserPhysicalCardInCommunity(Card entity);

    Card getById(ObjectId id);

    /**
     * 大屏统计访客数据
     * @param request
     * @return
     */
    TenantApplicationResponse getTenantApplicationStatisticsForScreen(TenantApplicationRequest request);

    /**
     * 根据卡号及类型查询社区下的卡信息
     *
     * @param communityId 社区ID
     * @param keyNo 卡号
     * @param keyType 卡类型
     * @return Card
     */
    List<Card> findByCommunityIdAndKeyNoAndKeyType(ObjectId communityId, String keyNo, Integer keyType);

    /**
     * 移除卡片的房间名称
     *
     * @param card
     */
    void pullAllCardRoomNameByKeyIdAndKeyNo(Card card);

    List<Card> findUsefulCardByHouseholdId(ObjectId householdId);

    List<Card> findUsefulCardByUserIdAndCommunityId(ObjectId userId, ObjectId communityId);
}
