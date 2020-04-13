package cn.bit.facade.service.user;

import cn.bit.facade.model.user.*;
import cn.bit.facade.vo.user.CMUserVO;
import cn.bit.facade.vo.user.ClientUserRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by terry on 2018/1/14.
 */
public interface UserFacade {

    /**
     * 新增系统用户
     *
     * @param client
     * @param pushId
     * @param user
     * @param code
     * @return
     * @throws BizException
     */
    UserVO addUser(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId, String pushId,
                   User user, String code) throws BizException;

    /**
     * 校验token
     *
     * @param client
     * @param token
     * @param uid
     * @throws BizException
     */
    void verifyToken(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId,
                     String token, ObjectId uid) throws BizException;

    /**
     * 获取系统用户
     *
     * @param client
     * @param partner
     * @param userId
     * @return
     */
    UserVO getUserById(Integer client, Integer partner, ObjectId userId);

    /**
     * @param token
     * @return
     * @throws BizException
     */
    UserVO getUserByToken(String token) throws BizException;

    /**
     * 系统用户登录
     *
     * @param client
     * @param pushId
     * @param phone
     * @param password
     * @param userDevice
     * @return
     * @throws BizException
     */
    UserVO signIn(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId, String pushId,
                  String phone, String password, UserDevice userDevice) throws BizException;

    /**
     * 系统用户验证码登录
     * @param client
     * @param pushId
     * @param phone
     * @param code
     * @param userDevice
     * @return
     * @throws BizException
     */
    UserVO signInByCode(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId, String pushId,
                        String phone, String code, UserDevice userDevice, String nickName, String avatar, String openId,
                        String unionId) throws BizException;

    /**
     * 系统用户登出
     *
     * @param token
     * @throws BizException
     */
    void signOut(String token) throws BizException;

    /**
     * 重置密码
     *
     * @param client
     * @param partner
     * @param pushId
     * @param phone    手机号
     * @param password
     * @param code 验证码
     * @param userDevice
     * @throws BizException
     */
    void resetPassword(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId, String pushId,
                       String phone, String password, String code, UserDevice userDevice) throws BizException;

    /**
     * 修改密码
     *
     * @param client
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @throws BizException
     */
    void changePassword(Integer client, Integer partner, ObjectId userId, String oldPassword, String newPassword) throws BizException;

    /**
     * 修改手机号
     *
     * @param client
     * @param userId
     * @param phone
     * @param code
     * @throws BizException
     */
    void changePhone(Integer client, Integer partner, ObjectId userId, String phone, String code, String password) throws BizException;

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    User updateUser(Integer client, Integer partner, User user) throws BizException;

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    Map<String, Object> updateUser(User user) throws BizException;

    /**
     * 根据手机号获取用户信息
     * @param phone
     * @return
     */
    User findByPhone(String phone) throws BizException;

    /**
     * 缓存验证码
     *
     * @param client
     * @param codeType
     * @param phone
     * @param code
     */
    void cacheCode(Integer client, Integer partner, String codeType, String phone, String code);

    /**
     * 修改APP附加数据
     *
     * @param client
     * @param userId
     * @param attach
     * @return
     */
    String updateAttach(Integer client, Integer partner, ObjectId userId, String attach) throws BizException;

    /**
     * 获取客户端用户
     *
     * @param clientUserRequest
     * @param page
     * @param size
     * @return
     */
    Page<ClientUser> getClientUsers(ClientUserRequest clientUserRequest, int page, int size);

    /**
     * 获取社区用户
     *
     * @param communityId
     * @param userId
     * @return
     */
    CommunityUser getCommunityUserByCommunityIdAndUserId(ObjectId communityId, ObjectId userId);

    /**
     * 重置物业人员密码
     * @param userId
     * @param password
     * @return
     * @throws BizException
     */
    @Deprecated
    boolean resetClientUserPwd(ObjectId userId, String password) throws BizException;

    /**
     * 重置物业端密码
     * @param userId
     * @param client
     * @param password
     * @return
     * @throws BizException
     */
    @Deprecated
    boolean resetClientUserPwd(ObjectId userId, Integer client, String password) throws BizException;

    ClientUser getClientUserByClientAndPartnerAndUserId(Integer client, Integer partner, ObjectId userId);

    IMUser saveIMToken(Integer client, Integer partner, ObjectId userId, String token);

    /**
     * 新增物业人员后，添加至 community user
     * @param userToProperty
     */
    @Deprecated
    void upsertCommunityUserForUserToProperty(UserToProperty userToProperty);

    /**
     * 注销物业人员时，移除 community user
     * @param userId
     * @param communityId
     * @param postCode
     */
    @Deprecated
    void removeRoleAndClientFromCommunityUser(ObjectId userId, ObjectId communityId, String postCode);

    /**
     * 根据ID获取用户信息
     * @param creatorId
     * @return
     */
    UserVO findById(ObjectId creatorId);

    /**
     * 根据id集合获取user列表
     * @param userIds
     * @return
     */
    List<UserVO> findByIds(Set<ObjectId> userIds);

    /**
     * 根据名字查询id集合
     * @return
     * @param name
     */
    List<UserVO> findByName(String name);

    /**
     * 分页查询社区用户
     * @param cmUserVO
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    Page<CMUserVO> queryWithRoomByCommunity(CMUserVO cmUserVO, ObjectId communityId, Integer page, Integer size);

    /**
     * 根据client，userIds 查询clientUser
     * @param client
     * @param userIds
     * @return
     */
    List<UserVO> listClientUserByClientAndUserIds(Integer client, Integer partner, Set<ObjectId> userIds);

    /**
     * 更新CM_USER的人体信息
     * @param featureCode
     * @param userFeature
     * @param communityId
     * @param userId
     */
    CommunityUser updateCMUserFaceInfo(String featureCode, Integer userFeature, ObjectId communityId,
                                       ObjectId userId);

	void updatePushIdByAppIdAndUserId(String pushId, ObjectId userId, ObjectId appId);

	List<CommunityUser> updateCMUsersFaceInfo(String featureCode, Integer humanFeatureState);

    List<CommunityUser> findCMUserByCommunityIdAndClientAndRoles(ObjectId communityId, Integer client, Collection<String> roles);
}
