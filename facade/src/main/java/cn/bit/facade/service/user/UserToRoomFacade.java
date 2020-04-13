package cn.bit.facade.service.user;

import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.vo.user.PrintUserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.*;

public interface UserToRoomFacade {

    /**
     * 添加关联信息
     * @param entity
     * @return
     */
    UserToRoom addOwner(UserToRoom entity) throws BizException;

    /**
     * 更新关联表信息
     * @param entity
     * @return
     * @throws BizException
     */
    boolean editUserToRoom(UserToRoom entity) throws BizException;

    /**
     * 家属或租客申请绑定房间
     * @return
     */
    UserToRoom addAuxiliary(UserToRoom entity) throws BizException;

    /**
     * 判断该房间是否有业主
     * @param roomId
     * @return
     */
    boolean existOwner(ObjectId roomId) throws BizException;

    /**
     * 根据手机号与房间ID判断该用户是否绑定房间
     * 防止重复绑定楼房
     * @param userId
     * @param roomId
     * @return
     */
    boolean existApplication(ObjectId userId, ObjectId roomId) throws BizException;

    /**
     * 根据用户ID与房间ID判断该用户是否已经入住
     * 防止重复绑定楼房
     * @param userId
     * @param roomId
     * @return
     */
    boolean isCheckIn(ObjectId userId, ObjectId roomId) throws BizException;

    /**
     * 查询用户在房间集合的房屋认证
     * @param userId
     * @param roomIds
     * @return
     * @throws BizException
     */
    List<UserToRoom> findByUserIdAndRooms(ObjectId userId, Collection<ObjectId> roomIds) throws BizException;

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    UserToRoom findById(ObjectId id) throws BizException;

    /**
     * 根据房间ID获取业主
     * @param roomId
     * @return
     */
    UserToRoom findOwnerByRoomId(ObjectId roomId) throws BizException;

    /**
     * 物业审核业主
     * 做判断该房间是否有业主
     * 这里也要做预防
     * redis防止并发
     * @param id
     * @param auditorId
     * @return success:添加成功；failed：添加失败；referrer：已有业主；
     */
    public UserToRoom approvalOwner(Integer partner, ObjectId id, ObjectId auditorId, Integer auditStatus) throws BizException;

    /**
     * 删除房间的业主认证
     * @param roomId
     * @param operator
     * @return
     */
    public int relieveOwner(Integer partner, ObjectId roomId, ObjectId operator) throws BizException;

    /**
     * 根据社区ID获取用户信息
     * @param communityId 社区ID
     * @param relationship 用户关系（1：业主；2：家属；3：租客）
     * @param auditStatus 用户与该房间的关系是否审核通过（0：未审核；1：审核通过；-1：驳回；-2：违规）
     * @return
     */
    public Page<UserToRoom> queryByCommunityId(ObjectId communityId, String buildingId, Integer relationship,
                                               Integer auditStatus, String contractPhone,String name, int page, int size);
    /**
     * 分页
     * 根据房间ID获取用户
     * @param roomId
     * @param auditStatus
     * @param userId
     * @param client
     * @return
     */
    public Page<UserToRoom> queryByRoomId(ObjectId roomId, Integer auditStatus, ObjectId userId, Integer client,
                                          Integer page, Integer size);

    /**
     * 根据用户ID获取社区ID列表
     * @param userId
     * @return
     */
    public Set<ObjectId> getCommunityIdsByUserId(ObjectId userId);

    /**
     * 根据用户ID获取楼宇ID列表
     * @param communityId
     * @param userId
     * @return
     */
    public Set<ObjectId> getBuildingsByUserId(ObjectId communityId, ObjectId userId);

    /**
     * 根据用户ID和社区ID获取房间列表(只要审核通过，不分权限)
     * @param entity
     * @return
     */
    public List<UserToRoom> getRoomsByUserId(UserToRoom entity);

    public boolean updatePhoneByUserId(ObjectId userId, String phone);

    public boolean disableAuxiliaryApply(ObjectId id, ObjectId userId, Boolean canApply) throws BizException;

    public UserToRoom auditAuxiliary(Integer partner, ObjectId id, ObjectId userId, Integer auditStatus, boolean Level2Audit) throws BizException;

    /**
     * 解除家属/租客的房间绑定
     * @param id
     * @param operator
     * @param client
     * @return
     * @throws BizException
     */
    public UserToRoom deleteAuxiliary(Integer partner, ObjectId id, ObjectId operator, Integer client) throws BizException;

    /**
     * 关闭申请记录
     * @param id
     * @param userId
     * @param closed
     * @return
     * @throws BizException
     */
    public boolean hiddenUserToRoomApplyById(ObjectId id, ObjectId userId, Boolean closed) throws BizException;

    /**
     * 按楼宇ID获取用户关系列表(业主)（物业）
     * @param buildingId 楼宇ID
     * @param relationship 用户关系（1：业主；2：家属；3：租客）
     * @param auditStatus 用户与该房间的关系是否审核通过（0：未审核；1：审核通过；-1：驳回；-2：违规）
     * @param client
     * @return
     */
    public Page<UserToRoom> queryByBuildingId(ObjectId buildingId, Integer relationship, Integer auditStatus,
                                              Integer client, int page, int size);

    /**
     * 根据社区统计各楼宇有效业主数量
     * @param communityId
     * @return
     */
    public List<Object> proprietorsStatistics(ObjectId communityId) throws BizException;

    /**
     *
     * @param id
     * @return
     */
    public PrintUserVO getContractInfoById(ObjectId id) throws BizException;

    /**
     * 根据社区获取对应身份、审核状态的用户数量
     * @param communityId
     * @param auditStatus
     * @return
     */
    public Map<String, Long> countByCommunityIdAndAuditStatus(ObjectId communityId, Integer auditStatus) throws BizException;

    /**
     * 通过id更新关联表
     * @param userToRoom
     * @return
     */
    public boolean updateMiliUIdById(UserToRoom userToRoom, Long newMiliUId);

    /**
     * 统计用户在楼栋有多少套房间
     * @param buildingId
     * @param userId
     * @return
     */
    List<UserToRoom> findByBuildingIdAndUserId(ObjectId buildingId, ObjectId userId);

    String getIdentityCardMetaBirthday(UserToRoom userToRoom);

    /**
     * 根据用户获取该房间的所有审核用户
     * @param roomId
     * @return
     */
    List<UserToRoom> findByRoomId(ObjectId roomId);

    // ============================================[用户统计]==============================================

    /**
     * 获取整个社区的用户
     * @param communityId
     * @return
     */
    public List<UserToRoom> countUserInfo(ObjectId communityId);

    /**
     * 获取上个月的用户数量
     * @param communityId
     * @return
     */
    public int countUserByTime(ObjectId communityId, Date beginDate, Date endDate);

    /**
     * 获取平台下所有有效业主，自动生成账单需要
     * @return
     * @param communityIds
     * @param page
     * @param size
     */
    Page<UserToRoom> findValidProprietorsByCommunityIdIn(Collection<ObjectId> communityIds, int page, int size);

    /**
     * 根据房间ID集合获取业主信息
     * @return
     */
    public List<UserToRoom> getProprietorsByRoomIds(Collection<ObjectId> roomIds);

    /**
     * 分页查询用户认证列表
     * @param userToRoom
     * @param page
     * @param size
     * @return
     */
    Page<UserToRoom> queryPageByCommunityId(UserToRoom userToRoom, Integer page, Integer size);

    /**
     * 物业二级审核非业主的认证
     * @param id
     * @param uid
     * @param auditStatus
     * @return
     */
    UserToRoom auditAuxiliaryByProperty(Integer partner, ObjectId id, ObjectId uid, Integer auditStatus);

    /**
     * 根据房间查询已被注销的用户认证列表
     * @param roomId
     * @return
     */
    List<UserToRoom> findCancelledListByRoomId(ObjectId roomId);

    /**
     * 根据社区ID分页获取待审核的用户认证
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    Page<UserToRoom> queryUnReviewPageByCommunityId(ObjectId communityId, Integer page, Integer size);

    Page<UserToRoom> queryNonProprietorUnReviewPageForRoom(ObjectId roomId, Integer page, Integer size);

    /**
     * 根据社区统计待审核的用户数量
     * @param communityId
     * @return
     */
    Map<String, Long> countUnReviewedProprietorsByCommunityId(ObjectId communityId);

    /**
     * 根据社区统计已审核的用户数量
     * @param communityId
     * @return
     */
    Map<String,Long> countReviewedProprietorsByCommunityId(ObjectId communityId);

    /**
     * 根据楼栋获取已认证用户列表
     * @param buildingId
     * @return
     */
    List<UserToRoom> queryListByBuildingId(ObjectId buildingId);

    /**
     * 根据楼栋ID查询用户已认证列表
     * @param buildingIds
     * @return
     */
    List<UserToRoom> findByBuildingIdsIn(Collection<ObjectId> buildingIds);

    /**
     * 根据房间找已认证通过的住户列表
     * @param roomId
     * @return
     */
    List<UserToRoom> findReviewedListByRoomId(ObjectId roomId);

    /**
     * 补米立授权
     * @param id
     * @since 2018-09-26 14:24:00
     */
    @Deprecated
    void applyUserToMili(ObjectId id);

    /**
     * 获取用户第一个认证的房间
     * @param communityId
     * @param userId
     * @return
     */
	UserToRoom findTop1ByCommunityIdAndUserId(ObjectId communityId, ObjectId userId);

    /**
     * 设置常住房屋
     * @param communityId
     * @param userId
     * @param userToRoomId
     * @return
     */
    UserToRoom editInCommonUse(ObjectId communityId, ObjectId userId, ObjectId userToRoomId);

    /**
     * 查询用户住房
     * @param communityId
     * @param userId
     * @param roomId
     * @return
     */
    UserToRoom findByCommunityIdAndUserIdAndRoomId(ObjectId communityId, ObjectId userId, ObjectId roomId);

    /**
     * 查找常用住房
     * @param communityId
     * @param userId
     * @return
     */
    UserToRoom findInCommonUseByCommunityIdAndUserId(ObjectId communityId, ObjectId userId);

    /**
     * 更新
     * @param toUpdate
     * @param id
     * @return
     */
	UserToRoom updateById(UserToRoom toUpdate, ObjectId id);

    /**
     * 根据communityId及userId获取已拥有的房屋列表
     *
     * @param communityId
     * @param userId
     * @return
     */
	List<UserToRoom> findByCommunityIdAndUserId(ObjectId communityId, ObjectId userId);

    /**
     * 新增已认证的业主记录
     * @param partner
     * @param toAdd
     * @return
     */
    UserToRoom upsertAuthOwnerRecord(Integer partner, UserToRoom toAdd);

    /**
     * 查询房间有效的业主申请记录（审核中/审核通过）
     * @param roomId
     * @return
     */
    List<UserToRoom> getOwnerApplyRecordsByRoomId(ObjectId roomId);

    /**
     * 根据房间、用户查询审核中的业主申请记录
     * @param roomId
     * @param userId
     * @return
     */
    UserToRoom findOwnerReviewingRecordByRoomIdAndUserId(ObjectId roomId, ObjectId userId);

}
