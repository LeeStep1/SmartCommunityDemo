package cn.bit.facade.service.user;

import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.vo.statistics.HouseholdRequest;
import cn.bit.facade.vo.statistics.HouseholdResponse;
import cn.bit.facade.vo.user.userToRoom.HouseholdPageQuery;
import cn.bit.facade.vo.user.userToRoom.HouseholdVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface HouseholdFacade {

    /**
     * 根据房间ID获取住户档案列表
     * @param roomId
     * @return
     */
    List<Household> findByRoomId(ObjectId roomId);

    /**
     * 保存住房档案
     * @param householdVO
     */
    List<Household> saveHouseholds(HouseholdVO householdVO);

    /**
     * 分页查询
     * @param query
     * @return
     */
    Page<Household> listHouseholds(HouseholdPageQuery query);

    /**
     * 查看住户档案详细
     * @param id
     * @return
     */
    Household getHouseholdDetail(ObjectId id);

    /**
     * 编辑档案
     * @param household
     * @return
     */
    Household modifyHousehold(Household household);

    /**
     * 获取房间下的业主档案
     * @param roomId
     * @return
     */
    Household findAuthOwnerByRoom(ObjectId roomId);

    /**
     * 根据手机号匹配未激活的业主档案
     * @param phone
     */
    List<Household> listUnactivatedOwnerHouseholdsByPhone(String phone);

    /**
     * 根据 householdIds 批量激活档案
     * @param userId
     * @param householdIds
     */
    Long activatedHouseholdByIds(ObjectId userId, Collection<ObjectId> householdIds);

    /**
     * 查询房屋档案详情
     * @param roomId
     * @return
     */
    HouseholdVO findDetailByRoom(ObjectId roomId);

    /**
     * 批量插入
     * @param households
     * @return
     */
    List<Household> saveHouseholds(List<Household> households);

    /**
     * 注销房屋档案
     * @param roomId
     * @return
     */
    long removeByRoomId(ObjectId roomId);

    /**
     * 根据ID注销住户档案
     * @param householdId
     * @return
     */
    Household removeByHouseholdId(ObjectId householdId);

    /**
     * 根据房间及用户注销档案
     * @param roomId
     * @param userId
     * @return
     */
    Household removeByRoomIdAndUserId(ObjectId roomId, ObjectId userId);

    /**
     * 录入或者激活非业主档案
     * @param household
     * @return
     */
    Household upsertHouseholdForNotOwner(Household household);

    /**
     * 录入或者激活业主档案
     * @param household
     * @return
     */
    Household upsertHouseholdForOwner(Household household);

    /**
     * 根据房屋或者用户查询住户档案列表
     * @param roomId
     * @param userId
     * @return
     */
    List<Household> findByRoomIdOrUserId(ObjectId roomId, ObjectId userId);

    /**
     * 根据房间ID集合查询业主档案列表
     * @param roomIds
     * @return
     */
    List<Household> findOwnerByRoomIds(Collection<ObjectId> roomIds);

    /**
     * 住户统计分析结果
     * @param householdRequest
     * @return
     */
    HouseholdResponse getHouseholdStatistics(HouseholdRequest householdRequest);

    /**
     * 根据住户名称模糊匹配用户ID列表
     * @param communityId
     * @param userName
     * @return
     */
    Set<ObjectId> listHouseholds(ObjectId communityId, String userName);

    /**
     * 查询社区下已经存在住户档案的房间ID集合
     * @param communityId
     * @return
     */
    Set<ObjectId> listRoomIdsByCommunityId(ObjectId communityId);

    /**
     * excel导入档案写入数据库
     * @param households
     * @param sendMsg
     * @return
     */
    List<Household> saveHouseholdsForImporting(List<Household> households, Boolean sendMsg);

    /**
     * 查询用户档案列表
     * @param communityId
     * @param userId
     * @return
     */
    List<Household> findByCommunityIdAndUserId(ObjectId communityId, ObjectId userId);

    /**
     * 更新用户设备权限
     * @param id
     * @param cards
     * @param operate
     */
    void updateDeviceLicense(ObjectId id, List<Card> cards, boolean operate);

    /**
     * 通过房间集合与用户id查住户档案
     * @param roomIds
     * @param userId
     * @return
     */
    List<Household> findByRoomIdsAndUserId(Collection<ObjectId> roomIds, ObjectId userId);
}
