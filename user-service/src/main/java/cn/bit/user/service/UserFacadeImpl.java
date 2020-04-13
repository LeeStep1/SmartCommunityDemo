package cn.bit.user.service;

import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.common.facade.exception.InvalidAuthorizationException;
import cn.bit.common.facade.exception.InvalidParameterException;
import cn.bit.common.facade.user.constant.CodeConstants;
import cn.bit.common.facade.user.dto.*;
import cn.bit.common.facade.user.query.ClientUserPageQuery;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.HumanFeatureStatusEnum;
import cn.bit.facade.enums.RoleType;
import cn.bit.facade.model.user.*;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.user.CMUserVO;
import cn.bit.facade.vo.user.ClientUserRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.user.dao.CommunityUserRepository;
import cn.bit.user.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.user.UserBizException.*;

/**
 * Created by terry on 2018/1/14.
 */
@Service("userFacade")
@Slf4j
public class UserFacadeImpl implements UserFacade {

    @Resource
    private cn.bit.common.facade.user.service.UserFacade commonUserFacade;

    @Resource
    private CommunityUserRepository communityUserRepository;

    @Override
    public UserVO addUser(Integer client, Integer partner, Integer platform,
                          Integer appType, ObjectId appId, String pushId,
                          User user, String code) throws BizException {
        if (client == null) {
            throw CLIENT_NULL;
        }

        if (user == null || StringUtil.isBlank(user.getPhone())) {
            throw PHONE_NULL;
        }

        RegistrationDTO registrationDTO = UserUtils.convert(user, RegistrationDTO.class);
        registrationDTO.setClient(client);
        registrationDTO.setPartner(partner);
        registrationDTO.setPlatform(platform);
        registrationDTO.setAppType(appType);
        registrationDTO.setAppId(appId);
        registrationDTO.setCode(code);
        registrationDTO.setPushId(pushId);
        registrationDTO.setNickName(StringUtil.desensitize(user.getPhone(), 3, 2));
        registrationDTO.setAvatar(user.getHeadImg());
        try {
            UserDTO userDTO = commonUserFacade.register(registrationDTO);
            user = UserUtils.convert(userDTO, User.class);
            user.setHeadImg(userDTO.getAvatar());
            UserVO userVO = new UserVO(user, userDTO.getToken());
            userVO.setNewGuy(userDTO.getNewGuy());
            return userVO;
        } catch (InvalidParameterException | cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_INVALID_CODE:
                    throw CODE_NOT_CORRECT;
                case CodeConstants.CODE_PHONE_REGISTERED:
                    throw PHONE_REGISTERED;
                case CodeConstants.CODE_INVALID_IDENTITY_CARD:
                    throw IDENTITY_CARD_ILLEGAL;
                default:
                    throw e;
            }
        }
    }

    @Override
    public void verifyToken(Integer client, Integer partner, Integer platform,
                            Integer appType, ObjectId appId,
                            String token, ObjectId uid) throws BizException {
        VerificationDTO verificationDTO = new VerificationDTO();
        verificationDTO.setClient(client);
        verificationDTO.setPartner(partner);
        verificationDTO.setPlatform(platform);
        verificationDTO.setAppType(appType);
        verificationDTO.setAppId(appId);
        verificationDTO.setToken(token);
        verificationDTO.setUserId(uid);
        try {
            commonUserFacade.verifyToken(verificationDTO);
        } catch (InvalidAuthorizationException e) {
            throw TOKEN_INVALID;
        }
    }

    @Override
    public UserVO getUserById(Integer client, Integer partner, ObjectId userId) {
        if (client == null) {
            throw CLIENT_NULL;
        }

        if (userId == null) {
            throw USER_ID_NULL;
        }

        try {
            ClientAndPartnerAndUserIdDTO dto = new ClientAndPartnerAndUserIdDTO();
            dto.setClient(client);
            dto.setPartner(partner);
            dto.setUserId(userId);
            cn.bit.common.facade.user.model.User user = commonUserFacade.getUserByClientAndPartnerAndUserId(dto);
            User u = UserUtils.convert(user, User.class);
            u.setHeadImg(user.getAvatar());
            u.setBirthday(DateUtils.getShortDateStr(user.getBirthday()));
            UserVO local = new UserVO(u, null);
            local.setAccid(user.getAccId());
            local.setImToken(user.getImToken());
            return local;
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                default:
                    throw e;
            }
        }
    }

    @Override
    public UserVO getUserByToken(String token) throws BizException {
        try {
            cn.bit.common.facade.user.model.User user = commonUserFacade.getUserByToken(token);
            User local = UserUtils.convert(user, User.class);
            local.setHeadImg(user.getAvatar());
            local.setBirthday(DateUtils.getShortDateStr(user.getBirthday()));
            return new UserVO(local, null);
        } catch (InvalidAuthorizationException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_INVALID_AUTH_TOKEN:
                    throw TOKEN_INVALID;
                default:
                    throw e;
            }
        }
    }

    @Override
    public UserVO signIn(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId,
                         String pushId, String phone, String password, UserDevice userDevice) throws BizException {
        return signIn(client, partner, platform, appType, appId, pushId, phone, password,
                null, userDevice, null, null, null, null,
                signInInfoDTO -> commonUserFacade.signIn(signInInfoDTO));
    }

    @Override
    public UserVO signInByCode(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId,
                               String pushId, String phone, String code, UserDevice userDevice, String nickName,
                               String avatar, String openId, String unionId) throws BizException {
        return signIn(client, partner, platform, appType, appId, pushId, phone, null,
                code, userDevice, nickName, avatar, openId, unionId,
                signInInfoDTO -> commonUserFacade.signInByCode(signInInfoDTO));
    }

    private void removeIMDataIfNecessary(UserVO userVO) {
        if (CollectionUtils.isEmpty(userVO.getRoles())) {
            userVO.setAccid(null);
            userVO.setImToken(null);
        }
    }

    @Override
    public IMUser saveIMToken(Integer client, Integer partner, ObjectId userId, String token) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setClient(client);
        profileDTO.setPartner(partner);
        profileDTO.setUserId(userId);
        profileDTO.setImToken(token);
        cn.bit.common.facade.user.model.ClientUser clientUser =
                commonUserFacade.saveProfileByClientAndPartnerAndUserId(profileDTO);
        IMUser imUser = new IMUser();
        imUser.setId(clientUser.getId());
        imUser.setToken(clientUser.getImToken());
        return imUser;
    }

    @Override
    public void signOut(String token) throws BizException {
        try {
            commonUserFacade.signOut(token);
        } catch (cn.bit.common.facade.exception.BizException | InvalidAuthorizationException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_USER_NOT_EXIST:
                case CodeConstants.CODE_INVALID_AUTH_TOKEN:
                    throw TOKEN_INVALID;
                default:
                    throw e;
            }
        }
    }

    @Override
    public void resetPassword(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId,
                              String pushId, String phone, String password, String code, UserDevice userDevice)
            throws BizException {

        if (client == null) {
            throw CLIENT_NULL;
        }

        if (StringUtil.isBlank(phone)) {
            throw PHONE_NULL;
        }

        // 物业端
        if (client == ClientType.PROPERTY.value()) {
            cn.bit.common.facade.user.model.User user = commonUserFacade.getUserByPhone(phone);
            if (user == null) {
                throw USER_NOT_EXITS;
            }
            ClientAndPartnerAndUserIdDTO dto = new ClientAndPartnerAndUserIdDTO();
            dto.setClient(client);
            dto.setPartner(partner);
            dto.setUserId(user.getId());
            cn.bit.common.facade.user.model.ClientUser clientUser =
                    commonUserFacade.getClientUserByClientAndPartnerAndUserId(dto);
            if (clientUser == null || clientUser.getRoles() == null || clientUser.getRoles().isEmpty()) {
                throw USER_NOT_EXITS;
            }
        }
        SignInInfoDTO signInInfoDTO = new SignInInfoDTO();
        signInInfoDTO.setClient(client);
        signInInfoDTO.setPartner(partner);
        signInInfoDTO.setPlatform(platform);
        signInInfoDTO.setAppType(appType);
        signInInfoDTO.setAppId(appId);
        signInInfoDTO.setLoginName(phone);
        signInInfoDTO.setPassword(password);
        signInInfoDTO.setCode(code);
        signInInfoDTO.setUserDevice(UserUtils.convert(userDevice, cn.bit.common.facade.user.model.UserDevice.class));
        signInInfoDTO.setPushId(pushId);

        try {
            commonUserFacade.resetPassword(signInInfoDTO);
        } catch (InvalidParameterException | cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_DAILY_RESET_PASSWORD_FAILURE_LIMIT_EXCEEDED:
                    throw REACH_RESET_PASSWORD_FAIL_TIMES_PRE_DAY;
                case CodeConstants.CODE_INVALID_CODE:
                    throw CODE_NOT_CORRECT;
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                default:
                    throw e;
            }
        }
    }

    @Override
    public void changePassword(Integer client, Integer partner, ObjectId userId, String oldPassword, String newPassword)
            throws BizException {

        if (client == null) {
            throw CLIENT_NULL;
        }

        if (userId == null) {
            throw USER_ID_NULL;
        }

        if (StringUtil.isBlank(newPassword)) {
            throw NEW_PASSWORD_NULL;
        }

        PrivacyModificationDTO privacyModificationDTO = new PrivacyModificationDTO();
        privacyModificationDTO.setClient(client);
        privacyModificationDTO.setPartner(partner);
        privacyModificationDTO.setUserId(userId);
        privacyModificationDTO.setPassword(oldPassword);
        privacyModificationDTO.setValue(newPassword);
        try {
            commonUserFacade.changePassword(privacyModificationDTO);
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_DAILY_CHANGE_PASSWORD_FAILURE_LIMIT_EXCEEDED:
                    throw REACH_CHANGE_PASSWORD_FAIL_TIMES_PRE_DAY;
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                case CodeConstants.CODE_INVALID_PASSWORD:
                    throw PASSWORD_NOT_CORRECT;
                default:
                    throw e;
            }
        }
    }

    @Override
    public void changePhone(Integer client, Integer partner, ObjectId userId, String phone, String code, String password)
            throws BizException {

        if (client == null) {
            throw CLIENT_NULL;
        }

        if (StringUtil.isBlank(phone)) {
            throw PHONE_NULL;
        }

        if (userId == null) {
            throw USER_ID_NULL;
        }

        PrivacyModificationDTO privacyModificationDTO = new PrivacyModificationDTO();
        privacyModificationDTO.setClient(client);
        privacyModificationDTO.setPartner(partner);
        privacyModificationDTO.setUserId(userId);
        privacyModificationDTO.setPassword(password);
        privacyModificationDTO.setValue(phone);
        privacyModificationDTO.setCode(code);
        try {
            commonUserFacade.changePhone(privacyModificationDTO);
        } catch (InvalidParameterException | cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_DAILY_CHANGE_PHONE_FAILURE_LIMIT_EXCEEDED:
                    throw REACH_CHANGE_PHONE_FAIL_TIMES_PRE_DAY;
                case CodeConstants.CODE_INVALID_CODE:
                    throw CODE_NOT_CORRECT;
                case CodeConstants.CODE_PHONE_REGISTERED:
                    throw PHONE_REGISTERED;
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                case CodeConstants.CODE_INVALID_PASSWORD:
                    throw PASSWORD_NOT_CORRECT;
                default:
                    throw e;
            }
        }
    }

    @Override
    public Map<String, Object> updateUser(User user) throws BizException {
        if (user == null || user.getId() == null) {
            throw USER_ID_NULL;
        }

        PerfectionDTO perfectionDTO = new PerfectionDTO();
        perfectionDTO.setUserId(user.getId());
        perfectionDTO.setName(user.getName());
        perfectionDTO.setIdentityCard(user.getIdentityCard());
        perfectionDTO.setBirthday(DateUtils.getDateByStr(user.getBirthday()));
        perfectionDTO.setSex(user.getSex());
        try {
            cn.bit.common.facade.user.model.User userRemote = commonUserFacade.improveUserByUserId(perfectionDTO);
            // 返回前端更新(实名信息)
            Map<String, Object> result = new HashMap<>();
            result.put("name", userRemote.getName());
            result.put("identityCard", userRemote.getIdentityCard());
            result.put("sex", userRemote.getSex());
            result.put("birthday", DateUtils.getShortDateStr(userRemote.getBirthday()));

            return result;
        } catch (cn.bit.common.facade.exception.BizException e) {
            log.error("cn.bit.common.facade.exception.BizException code:", e.getSubCode());
            switch (e.getSubCode()) {
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                default:
                    throw e;
            }
        } catch (InvalidParameterException e) {
            log.error("InvalidParameterException code:", e.getSubCode());
            switch (e.getSubCode()) {
                case CodeConstants.CODE_INVALID_IDENTITY_CARD:
                    throw IDENTITY_CARD_ILLEGAL;
                case CodeConstants.CODE_INVALID_BIRTHDAY:
                    throw INVALID_BIRTHDAY;
                default:
                    throw e;
            }
        }
    }

    @Override
    public User updateUser(Integer client, Integer partner, User user) throws BizException {
        if (client == null) {
            throw CLIENT_NULL;
        }

        if (user == null || user.getId() == null) {
            throw USER_ID_NULL;
        }


        PerfectionDTO perfectionDTO = new PerfectionDTO();
        perfectionDTO.setUserId(user.getId());
        perfectionDTO.setName(user.getName());
        perfectionDTO.setAvatar(user.getHeadImg());
        perfectionDTO.setIdentityCard(user.getIdentityCard());
        perfectionDTO.setBirthday(DateUtils.getDateByStr(user.getBirthday()));
        perfectionDTO.setSex(user.getSex());
        try {
            cn.bit.common.facade.user.model.User userRemote = commonUserFacade.improveUserByUserId(perfectionDTO);

            ProfileDTO profileDTO = new ProfileDTO();
            profileDTO.setClient(client);
            profileDTO.setPartner(partner);
            profileDTO.setUserId(user.getId());
            profileDTO.setNickName(user.getNickName());

            cn.bit.common.facade.user.model.ClientUser clientUser =
                    commonUserFacade.saveProfileByClientAndPartnerAndUserId(profileDTO);

            user = UserUtils.convert(userRemote, User.class);
            user.setLoginName(user.getPhone());
            user.setHeadImg(userRemote.getAvatar());
            user.setAttach(clientUser.getAttach());
            user.setNickName(clientUser.getNickName());

            return user;
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                default:
                    throw e;
            }
        } catch (InvalidParameterException e) {
            log.error("InvalidParameterException code:", e.getSubCode());
            switch (e.getSubCode()) {
                case CodeConstants.CODE_INVALID_IDENTITY_CARD:
                    throw IDENTITY_CARD_ILLEGAL;
                case CodeConstants.CODE_INVALID_BIRTHDAY:
                    throw INVALID_BIRTHDAY;
                default:
                    throw e;
            }
        }
    }

    @Override
    public User findByPhone(String phone) throws BizException {
        if (StringUtil.isEmpty(phone)) {
            throw PHONE_NULL;
        }

        cn.bit.common.facade.user.model.User userRemote = commonUserFacade.getUserByPhone(phone);
        if (userRemote == null) {
            return null;
        }
        User user = UserUtils.convert(userRemote, User.class);
        user.setHeadImg(userRemote.getAvatar());
        return user;
    }

    @Override
    public void cacheCode(Integer client, Integer partner, String codeType, String phone, String code) {
        if (client == null) {
            throw CLIENT_NULL;
        }

        CheckCodeDTO checkCodeDTO = new CheckCodeDTO();
        checkCodeDTO.setClient(client);
        checkCodeDTO.setPartner(partner);
        checkCodeDTO.setPhone(phone);
        checkCodeDTO.setCode(code);
        checkCodeDTO.setType(Integer.valueOf(codeType));
        checkCodeDTO.setTtl(600L);
        commonUserFacade.recordCheckCode(checkCodeDTO);
    }

    @Override
    public String updateAttach(Integer client, Integer partner, ObjectId userId, String attach) throws BizException {
        if (client == null) {
            throw CLIENT_NULL;
        }

        if (userId == null) {
            throw USER_ID_NULL;
        }

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setClient(client);
        profileDTO.setPartner(partner);
        profileDTO.setUserId(userId);
        profileDTO.setAttach(attach);

        try {
            cn.bit.common.facade.user.model.ClientUser clientUser =
                    commonUserFacade.saveProfileByClientAndPartnerAndUserId(profileDTO);
            return clientUser.getAttach();
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                default:
                    throw e;
            }
        }
    }

    @Override
    public Page<ClientUser> getClientUsers(ClientUserRequest clientUserRequest, int page, int size) {
        ClientUserPageQuery clientUserPageQuery = UserUtils.convert(clientUserRequest, ClientUserPageQuery.class);
        clientUserPageQuery.setPage(page);
        clientUserPageQuery.setSize(size);
        clientUserPageQuery.setUserIds(clientUserRequest.getUserId());
        if (clientUserRequest.getCommunityId() != null) {
            clientUserPageQuery.setTags(Collections.singleton(clientUserRequest.getCommunityId()));
        }

        cn.bit.common.facade.data.Page<cn.bit.common.facade.user.model.ClientUser> cltUserPage =
                commonUserFacade.listClientUsers(clientUserPageQuery);

        Page<ClientUser> result = new Page<>(cltUserPage.getCurrentPage(), cltUserPage.getTotal(),
                clientUserPageQuery.getSize(), null);

        List<ClientUser> clientUsers = cltUserPage.getRecords()
                .stream()
                .map(clientUser -> {
                    ClientUser cltUser = UserUtils.convert(clientUser, ClientUser.class);
                    if (clientUser.getTags() != null && !clientUser.getTags().isEmpty()) {
                        cltUser.setCommunityIds(
                                clientUser.getTags().stream()
                                        .filter(tag -> tag instanceof ObjectId)
                                        .map(tag -> (ObjectId) tag).collect(Collectors.toSet()));
                    }
                    return cltUser;
                })
                .collect(Collectors.toList());

        result.setRecords(clientUsers);
        return result;
    }

    @Override
    public CommunityUser getCommunityUserByCommunityIdAndUserId(ObjectId communityId, ObjectId userId) {
        return communityUserRepository.findByCommunityIdAndUserIdAndDataStatus(
                communityId, userId, DataStatusType.VALID.KEY);
    }

    @Override
    public boolean resetClientUserPwd(ObjectId userId, String password) throws BizException {
        return this.resetClientUserPwd(userId, ClientType.PROPERTY.value(), password);
    }

    @Override
    public boolean resetClientUserPwd(ObjectId userId, Integer client, String password) throws BizException {
        SingleModificationDTO singleModificationDTO = new SingleModificationDTO();
        singleModificationDTO.setClient(client);
        singleModificationDTO.setUserId(userId);
        singleModificationDTO.setValue(password);
        try {
            commonUserFacade.resetPassword(singleModificationDTO);
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_USER_NOT_EXIST:
                    throw PROPERTY_NOT_EXIST;
                default:
                    throw e;
            }
        }

        return true;
    }

    @Override
    public ClientUser getClientUserByClientAndPartnerAndUserId(Integer client, Integer partner, ObjectId userId) {
        ClientAndPartnerAndUserIdDTO dto = new ClientAndPartnerAndUserIdDTO();
        dto.setClient(client);
        dto.setPartner(partner);
        dto.setUserId(userId);
        cn.bit.common.facade.user.model.ClientUser clientUser =
                commonUserFacade.getClientUserByClientAndPartnerAndUserId(dto);
        ClientUser cltUser = UserUtils.convert(clientUser, ClientUser.class);
        if (clientUser != null && clientUser.getTags() != null && !clientUser.getTags().isEmpty()) {
            cltUser.setCommunityIds(clientUser.getTags().stream().filter(tag -> tag instanceof ObjectId)
                    .map(tag -> (ObjectId) tag).collect(Collectors.toSet()));
        }
        return cltUser;
    }

    /**
     * 新增物业人员后，添加至 community user
     *
     * @param userToProperty
     */
    @Override
    public void upsertCommunityUserForUserToProperty(UserToProperty userToProperty) {
        CommunityUser communityUser = new CommunityUser();
        Set<Integer> clients = new HashSet<>();
        clients.add(ClientType.PROPERTY.value());
        Set<String> postCodes = userToProperty.getPostCode();
        communityUser.setClients(clients);
        communityUser.setUserId(userToProperty.getUserId());
        communityUser.setCommunityId(userToProperty.getCommunityId());
        communityUser.setRoles(postCodes);
        communityUser.setCreateAt(userToProperty.getCreateAt());
        communityUser.setUpdateAt(communityUser.getCreateAt());
        communityUser.setDataStatus(DataStatusType.VALID.KEY);
        try {
            communityUserRepository.upsertWithAddToSetClientsAndRolesByCommunityIdAndUserIdAndDataStatus(
                    communityUser, userToProperty.getCommunityId(), userToProperty.getUserId(), DataStatusType.VALID.KEY);
        } catch (Exception e) {
            log.error("upsertCommunityUserForUserToProperty error:", e);
            throw e;
        }
    }

    /**
     * 注销物业人员时，移除 community user
     *
     * @param userId
     * @param communityId
     * @param postCode
     */
    @Override
    public void removeRoleAndClientFromCommunityUser(ObjectId userId, ObjectId communityId, String postCode) {
        CommunityUser communityUser = new CommunityUser();
        communityUser.setRoles(Collections.singleton(postCode));
        Set<Integer> clients = new HashSet<>();
        clients.add(ClientType.PROPERTY.value());
        communityUser.setClients(clients);
        // 移除物业的授权
        communityUser.setBuildingIds(null);
        communityUser.setDistrictIds(null);
        communityUser.setMiliUIds(null);
        communityUserRepository.pullAllByCommunityIdAndUserId(communityUser, communityId, userId);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param creatorId
     * @return
     */
    @Override
    public UserVO findById(ObjectId creatorId) {
        if (creatorId == null) {
            return null;
        }

        cn.bit.common.facade.user.model.User userRemote = commonUserFacade.getUserByUserId(creatorId);
        User user = UserUtils.convert(userRemote, User.class);
        user.setHeadImg(userRemote.getAvatar());
        return new UserVO(user, null);
    }

    /**
     * 根据id集合获取user列表
     *
     * @param userIds
     * @return
     */
    @Override
    public List<UserVO> findByIds(Set<ObjectId> userIds) {
        if (userIds == null || userIds.size() == 0) {
            return Collections.emptyList();
        }

        List<cn.bit.common.facade.user.model.User> userList = commonUserFacade.listUsersByUserIds(userIds);
        return userList.stream()
                .map(user -> {
                    User local = UserUtils.convert(user, User.class);
                    local.setHeadImg(user.getAvatar());
                    return new UserVO(local, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据名字查询id集合
     *
     * @param name
     * @return
     */
    @Override
    public List<UserVO> findByName(String name) {
        if (!StringUtil.isNotNull(name)) {
            return Collections.emptyList();
        }

        List<cn.bit.common.facade.user.model.User> userList = commonUserFacade.listUsersByUserName(name);
        return userList.stream()
                .map(user -> {
                    User local = UserUtils.convert(user, User.class);
                    local.setHeadImg(user.getAvatar());
                    return new UserVO(local, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * 分页查询社区用户
     *
     * @param cmUserVO
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<CMUserVO> queryWithRoomByCommunity(CMUserVO cmUserVO, ObjectId communityId, Integer page, Integer size) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }

        boolean filtered = false;
        List<cn.bit.common.facade.user.model.User> users = Collections.emptyList();
        if (StringUtil.isNotBlank(cmUserVO.getPhone())) {
            cn.bit.common.facade.user.model.User user = commonUserFacade.getUserByPhone(cmUserVO.getPhone());
            if (user != null
                    && (StringUtil.isBlank(cmUserVO.getName()) || user.getName().contains(cmUserVO.getName()))) {
                users = Collections.singletonList(user);
            }

            filtered = true;
        } else if (StringUtil.isNotBlank(cmUserVO.getName())) {
            // 需要支持模糊查询
            users = commonUserFacade.listUsersByUserName(cmUserVO.getName());

            filtered = true;
        }

        if (filtered && users.isEmpty()) {
            return new Page<>();
        }
        Set<Integer> featureSet = null;
        if (cmUserVO.getFaceStatus() != null) {
            featureSet = new HashSet<>();
            featureSet.add(cmUserVO.getFaceStatus());
            if (cmUserVO.getFaceStatus().equals(HumanFeatureStatusEnum.EMPTY.KEY)) {
                featureSet.add(null);
            }
        }

        Set<ObjectId> userIds = users.isEmpty() ? null
                : users.stream().map(cn.bit.common.facade.user.model.User::getId).collect(Collectors.toSet());
        Pageable pageable = new PageRequest(page - 1, size);
        org.springframework.data.domain.Page<CommunityUser> cmUserPage =
                communityUserRepository.findByCommunityIdAndUserIdInAndRolesInAndFaceStatusInAndDataStatusAllIgnoreNullOrderByUpdateAtDesc(
                        communityId, userIds, Collections.singleton(RoleType.HOUSEHOLD.name()), featureSet,
                        DataStatusType.VALID.KEY, pageable);

        if (cmUserPage.getNumberOfElements() == 0) {
            return new Page<>();
        }

        userIds = cmUserPage.getContent().stream().map(CommunityUser::getUserId).collect(Collectors.toSet());
        users = commonUserFacade.listUsersByUserIds(userIds);
        Map<ObjectId, cn.bit.common.facade.user.model.User> userMap = users.stream().collect(Collectors.toMap(
                cn.bit.common.facade.user.model.User::getId, user -> user));
        List<CMUserVO> cmUserVOs = cmUserPage.getContent()
                .stream()
                .map(communityUser -> {
                    cn.bit.common.facade.user.model.User user = userMap.get(communityUser.getUserId());
                    if (user == null) {
                        return null;
                    }

                    CMUserVO vo = UserUtils.convert(user, CMUserVO.class);
                    vo.setBirthday(DateUtils.getShortDateStr(user.getBirthday()));
                    vo.setFaceStatus(communityUser.getFaceStatus());
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<>(cmUserPage.getNumber(), cmUserPage.getTotalElements(), cmUserPage.getSize(), cmUserVOs);
    }

    /**
     * 根据client，userIds 查询clientUser
     *
     * @param client
     * @param userIds
     * @return
     */
    @Override
    public List<UserVO> listClientUserByClientAndUserIds(Integer client, Integer partner, Set<ObjectId> userIds) {
        if (userIds == null || userIds.size() == 0) {
            return Collections.emptyList();
        }
        ClientAndPartnerAndUserIdsDTO dto = new ClientAndPartnerAndUserIdsDTO();
        dto.setClient(client);
        dto.setPartner(partner);
        dto.setUserIds(userIds);
        List<cn.bit.common.facade.user.model.User> userList = commonUserFacade.listUsersByClientAndPartnerAndUserIds(dto);

        return userList.stream().map(user -> {
            User local = UserUtils.convert(user, User.class);
            local.setHeadImg(user.getAvatar());
            return new UserVO(local, null);
        }).collect(Collectors.toList());
    }

    @Override
    public CommunityUser updateCMUserFaceInfo(String featureCode, Integer userFeature, ObjectId
            communityId, ObjectId userId) {
        CommunityUser communityUser = new CommunityUser();
        communityUser.setFaceCode(featureCode);
        communityUser.setFaceStatus(userFeature);
        communityUser.setUpdateAt(new Date());
        return communityUserRepository.updateByUserIdAndCommunityIdAndDataStatus(
                communityUser, userId, communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public void updatePushIdByAppIdAndUserId(String pushId, ObjectId userId, ObjectId appId) {
        ModifyPushIdDTO modifyPushIdDTO = new ModifyPushIdDTO();
        modifyPushIdDTO.setAppId(appId);
        modifyPushIdDTO.setUserId(userId);
        modifyPushIdDTO.setPushId(pushId);

        commonUserFacade.modifyPushIdByAppIdAndUserId(modifyPushIdDTO);
    }

    @Override
    public List<CommunityUser> updateCMUsersFaceInfo(String featureCode, Integer humanFeatureState) {
        CommunityUser communityUser = communityUserRepository.findByFaceCodeAndDataStatus(featureCode, DataStatusType.VALID.KEY);

        CommunityUser toUpdate = new CommunityUser();
        toUpdate.setUserId(communityUser.getUserId());
        toUpdate.setUpdateAt(new Date());
        toUpdate.setFaceStatus(humanFeatureState == 1 ? HumanFeatureStatusEnum.SUCCESS.KEY : HumanFeatureStatusEnum.DELETE.KEY);

        return communityUserRepository.updateByUserIdAndFaceCodeAndDataStatus(
                toUpdate, communityUser.getUserId(), featureCode, DataStatusType.VALID.KEY);
    }

    @Override
    public List<CommunityUser> findCMUserByCommunityIdAndClientAndRoles(ObjectId communityId,
                                                                        Integer client, Collection<String> roles) {
        return communityUserRepository.findByCommunityIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                communityId, Collections.singleton(client), roles, DataStatusEnum.VALID.value());
    }

    private UserVO signIn(Integer client, Integer partner, Integer platform, Integer appType, ObjectId appId,
                          String pushId, String phone, String password, String code, UserDevice userDevice,
                          String nickName, String avatar, String openId, String unionId,
                          Function<SignInInfoDTO, UserDTO> callback) {
        ClientType clientType = ClientType.fromValue(client);
        if (clientType == null) {
            throw CLIENT_NULL;
        }

        if (StringUtil.isBlank(phone)) {
            throw PHONE_NULL;
        }

        SignInInfoDTO signInInfoDTO = new SignInInfoDTO();
        signInInfoDTO.setClient(client);
        signInInfoDTO.setPartner(partner);
        signInInfoDTO.setPlatform(platform);
        signInInfoDTO.setAppType(appType);
        signInInfoDTO.setAppId(appId);
        signInInfoDTO.setLoginName(phone);
        signInInfoDTO.setPassword(password);
        signInInfoDTO.setNickName(nickName);
        signInInfoDTO.setAvatar(avatar);
        signInInfoDTO.setOpenId(openId);
        signInInfoDTO.setUnionId(unionId);
        signInInfoDTO.setCode(code);
        signInInfoDTO.setUserDevice(UserUtils.convert(userDevice, cn.bit.common.facade.user.model.UserDevice.class));
        signInInfoDTO.setPushId(pushId);
        if (clientType == ClientType.BUSINESS) {
            signInInfoDTO.setAutoRegister(true);
        }
        UserDTO userDTO;
        try {
            userDTO = callback.apply(signInInfoDTO);

            User user = UserUtils.convert(userDTO, User.class);
            user.setHeadImg(userDTO.getAvatar());
            user.setBirthday(DateUtils.getShortDateStr(userDTO.getBirthday()));

            UserVO userVO = new UserVO(user, userDTO.getToken());
            userVO.setNewGuy(userDTO.getNewGuy());
            removeIMDataIfNecessary(userVO);
            return userVO;
        } catch (InvalidAuthorizationException | InvalidParameterException | cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_DAILY_SIGN_IN_FAILURE_LIMIT_EXCEEDED:
                    throw REACH_SIGN_IN_FAIL_TIMES_PRE_DAY;
                case CodeConstants.CODE_DAILY_SIGN_IN_BY_CODE_FAILURE_LIMIT_EXCEEDED:
                    throw REACH_SIGN_IN_BY_CODE_FAIL_TIMES_PRE_DAY;
                case CodeConstants.CODE_USER_NOT_EXIST:
                case CodeConstants.CODE_CLIENT_NOT_REGISTER:
                    throw USER_NOT_EXITS;
                case CodeConstants.CODE_SIGN_IN_FAILURE:
                    throw SIGN_IN_FAILURE;
                case CodeConstants.CODE_INVALID_CODE:
                    throw CODE_NOT_CORRECT;
                default:
                    throw e;
            }
        }
    }
}