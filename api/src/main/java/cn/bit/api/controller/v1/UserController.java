package cn.bit.api.controller.v1;

import cn.bit.api.support.*;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.api.support.annotation.SendPush;
import cn.bit.api.support.wechat.JsCode2SessionRequest;
import cn.bit.api.support.wechat.JsCode2SessionResponse;
import cn.bit.common.facade.business.service.BusinessFacade;
import cn.bit.common.facade.community.enums.LevelEnum;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.common.facade.enums.AppTypeEnum;
import cn.bit.common.facade.enums.OsEnum;
import cn.bit.common.facade.enums.PlatformEnum;
import cn.bit.common.facade.system.model.OutApp;
import cn.bit.common.facade.system.model.Sign;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.common.facade.user.model.AppUser;
import cn.bit.facade.enums.*;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.community.*;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.communityIoT.MessageParam;
import cn.bit.facade.model.property.Registration;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.model.system.Role;
import cn.bit.facade.model.user.*;
import cn.bit.facade.model.vehicle.Apply;
import cn.bit.facade.service.community.*;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.communityIoT.ProtocolFacade;
import cn.bit.facade.service.property.PropertyFacade;
import cn.bit.facade.service.property.RegistrationFacade;
import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.service.user.*;
import cn.bit.facade.service.vehicle.CarFacade;
import cn.bit.facade.vo.IdsRequestVO;
import cn.bit.facade.vo.community.DistrictRequest;
import cn.bit.facade.vo.community.broadcast.BroadcastSchema;
import cn.bit.facade.vo.community.broadcast.DeviceSchema;
import cn.bit.facade.vo.communityIoT.elevator.FloorVO;
import cn.bit.facade.vo.communityIoT.elevator.KeyNoListElevatorVO;
import cn.bit.facade.vo.communityIoT.elevator.KeyNoListElevatorVOResponse;
import cn.bit.facade.vo.communityIoT.elevator.PropertyCoverElevatorRangeRequest;
import cn.bit.facade.vo.property.Property;
import cn.bit.facade.vo.user.*;
import cn.bit.facade.vo.user.card.CardQueryRequest;
import cn.bit.facade.vo.user.card.CardVO;
import cn.bit.facade.vo.user.card.HouseholdCardVO;
import cn.bit.facade.vo.user.userToProperty.*;
import cn.bit.facade.vo.user.userToRoom.HouseholdPageQuery;
import cn.bit.facade.vo.user.userToRoom.HouseholdVO;
import cn.bit.facade.vo.user.userToRoom.MemberDTO;
import cn.bit.facade.vo.user.userToRoom.UserToRoomVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.IdentityCardUtils;
import cn.bit.framework.utils.httpclient.HttpUtils;
import cn.bit.framework.utils.string.StrUtil;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.framework.utils.validate.IDCardUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.bit.api.support.ErrorCodeEnum.ERR_SYS_PARAM;
import static cn.bit.facade.exception.CommonBizException.*;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_NOT_EXISTS;
import static cn.bit.facade.exception.user.UserBizException.*;
import static cn.bit.framework.exceptions.BizException.OPERATION_FAILURE;

/**
 * Created by terry on 2018/1/14.
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserController {

    private static final String KEY_ALG = "AES";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String UNDEFINED = "undefined";
    // 用户
    @Autowired
    private UserFacade userFacade;
    // 房屋认证
    @Autowired
    private UserToRoomFacade userToRoomFacade;
    // 社区管理
    @Autowired
    private CommunityFacade communityFacade;
    // 房屋管理
    @Autowired
    private RoomFacade roomFacade;
    // 楼栋管理
    @Autowired
    private BuildingFacade buildingFacade;
    // 门禁设备
    @Autowired
    private DoorFacade doorFacade;
    // 物业人员
    @Autowired
    private UserToPropertyFacade userToPropertyFacade;
    // 卡片
    @Autowired
    private CardFacade cardFacade;
    // 车辆信息
    @Autowired
    private CarFacade carFacade;
    // 职能区域
    @Autowired
    private DistrictFacade districtFacade;
    @Autowired
    private ElevatorFacade elevatorFacade;
    @Autowired
    private ParameterFacade parameterFacade;
    @Autowired
    private ProtocolFacade protocolFacade;
    @Resource
    private RegistrationFacade registrationFacade;
    @Autowired
    private PropertyFacade propertyFacade;
    @Autowired
    private HouseholdFacade householdFacade;
    @Resource
    private SystemFacade systemFacade;
    @Autowired
    private BusinessFacade businessFacade;
    @Autowired
    private RestTemplate restTemplate;
    @Resource
    private NeteaseImService imService;
    @Resource
    private cn.bit.common.facade.user.service.UserFacade commonUserFacade;
    @Autowired
    private PushFacade pushFacade;
    @Resource
    private CompanyFacade companyFacade;
    @Value("${send.msg}")
    private Boolean sendMsg;
    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.appid}")
    private String smsAppId;
    @Resource
    private cn.bit.push.facade.dubbo.PushFacade commonPushFacade;

    // =================================================【user start】==================================================

    private static WechatPhoneDataVO decryptWechatData(String sessionKeyStr, String encryptedDataStr, String ivStr)
            throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException,
                   NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        byte[] sessionKeyData = Base64.getDecoder().decode(sessionKeyStr.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataStr.getBytes(StandardCharsets.UTF_8));
        byte[] ivData = Base64.getDecoder().decode(ivStr.getBytes(StandardCharsets.UTF_8));

        SecretKeySpec key = new SecretKeySpec(sessionKeyData, KEY_ALG);
        IvParameterSpec iv = new IvParameterSpec(ivData);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return JSON.parseObject(decryptedData, WechatPhoneDataVO.class);
    }

    /**
     * @param entity
     * @return
     */
    @PostMapping(name = "获取验证码", path = "/getVerificationCode")
    public ApiResult getVerificationCode(@RequestBody @Valid NoteCode entity) throws Exception {
        // 生成验证码
        String code = (String.valueOf(System.currentTimeMillis())).substring(7);
        // 业务类型("1": "注册","2": "登录","3": "修改密码","4": "修改手机号")：
        log.info("手机号：{}，验证码类型:{}，验证码：{}", entity.getPhone(), entity.getBizCode(), code);

        AppSubject appSubject = SessionUtil.getAppSubject();
        // 是否发送短信验证码
        if (sendMsg) {
            String tplId = "";
            switch (Integer.parseInt(entity.getBizCode())) {
                case 1:
                    tplId = SmsTempletType.SMS100003.getKey();
                    break;
                case 2:
                    tplId = SmsTempletType.SMS100005.getKey();
                    break;
                case 3:
                    tplId = SmsTempletType.SMS100004.getKey();
                    break;
                case 4:
                    tplId = SmsTempletType.SMS100006.getKey();
                    break;
                default:
                    throw BIZCODE_INVALID;
            }
            MessageParam.Params params = new MessageParam.Params();
            params.setCode(code);
            String signName = "比亦特";
            if (appSubject.getPartner() != null) {
                Sign sign = systemFacade.getSignByClientAndPartner(appSubject.getClient(), appSubject.getPartner());
                if (sign == null) {
                    throw SIGN_NAME_INVALID;
                }
                signName = sign.getName();
            }
            String response = sendMsg(tplId, entity.getPhone(), signName, params);
            ApiResult item = JSON.toJavaObject(JSON.parseObject(response), ApiResult.class);
            if (item.getErrorCode() == 0) {
                userFacade.cacheCode(appSubject.getClient(),
                                     appSubject.getPartner(),
                                     entity.getBizCode(),
                                     entity.getPhone(),
                                     code);
            }
            return item;
        }

        // 开发环境不发短信
        userFacade.cacheCode(appSubject.getClient(),
                             appSubject.getPartner(),
                             entity.getBizCode(),
                             entity.getPhone(),
                             code);
        return ApiResult.ok(code);
    }

    /**
     * 发送短信
     *
     * @param tplId
     * @param phone
     * @param signName
     * @param params
     * @return
     * @throws Exception
     */
    private String sendMsg(String tplId, String phone, String signName, MessageParam.Params params) throws Exception {
        MessageParam messageParam = new MessageParam();
        messageParam.setTplId(tplId);
        messageParam.setNumber(phone);
        messageParam.setSignName(signName);
        messageParam.setParams(params);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("charset", "utf-8");
        requestHeaders.put("APPID", smsAppId);
        return HttpUtils.doPost(smsUrl, requestHeaders, JSONObject.toJSONString(messageParam));
    }

    /**
     * 用户登录
     *
     * @param request
     * @return
     **/
    @PostMapping(name = "用户登录", path = "/signIn")
    public ApiResult signIn(@RequestBody @Valid SignInRequest request,
                            @RequestHeader(value = "DEVICE-ID", defaultValue = UNDEFINED) String deviceId,
                            @RequestHeader("DEVICE-TYPE") String deviceType,
                            @RequestHeader("OS-VERSION") String osVersion,
                            @RequestHeader(value = "PUSH-ID", required = false) String pushId) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        UserDevice userDevice = buildUserDevice(deviceId, deviceType, appSubject.getOsEnum().value(), osVersion);
        UserVO userVO = userFacade.signIn(appSubject.getClient(),
                                          appSubject.getPartner(),
                                          appSubject.getOsEnum().platform().value(),
                                          appSubject.getOsEnum().appType().value(),
                                          appSubject.getAppId(),
                                          pushId,
                                          request.getPhone(),
                                          request.getPwd(),
                                          userDevice);
        return ApiResult.ok(userVO);
    }

    /**
     * 用户验证码登录
     *
     * @param signInByCode
     * @return
     */
    @PostMapping(name = "用户验证码登录", path = "/signInByCode")
    public ApiResult signInByCode(@RequestBody @Valid SignInByCode signInByCode,
                                  @RequestHeader(value = "DEVICE-ID", defaultValue = UNDEFINED) String deviceId,
                                  @RequestHeader("DEVICE-TYPE") String deviceType,
                                  @RequestHeader("OS-VERSION") String osVersion,
                                  @RequestHeader(value = "PUSH-ID", required = false) String pushId) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        UserDevice userDevice = buildUserDevice(deviceId, deviceType, appSubject.getOsEnum().value(), osVersion);

        String openId = null;
        String unionId = null;
        OsEnum osEnum = SessionUtil.getAppSubject().getOsEnum();
        if (osEnum.appType() == AppTypeEnum.MINI_PROGRAM) {
            if (osEnum.platform() != PlatformEnum.WECHAT) {
                return ApiResult.error(ERR_SYS_PARAM.getErrorCode(), ERR_SYS_PARAM.getErrorDesc());
            }

            OutApp outApp = systemFacade.getOutAppByOutAppId(SessionUtil.getAppSubject().getAccAppId());
            JsCode2SessionRequest request = new JsCode2SessionRequest();
            request.setAppId(outApp.getAppId());
            request.setSecret(outApp.getSecret());
            request.setAuthCode(signInByCode.getAuthCode());
            JsCode2SessionResponse response = wehcatCode2Session(request);
            if (response.getErrCode() != null) {
                return ApiResult.error(ERR_SYS_PARAM.getErrorCode(), response.getErrMsg());
            }

            openId  = response.getOpenId();
            unionId = response.getUnionId();
        }

        UserVO userVO = userFacade.signInByCode(appSubject.getClient(),
                                                appSubject.getPartner(),
                                                appSubject.getOsEnum().platform().value(),
                                                appSubject.getOsEnum().appType().value(),
                                                appSubject.getAppId(),
                                                pushId,
                                                signInByCode.getPhone(),
                                                signInByCode.getCode(),
                                                userDevice,
                                                signInByCode.getNickName(),
                                                signInByCode.getAvatar(),
                                                openId,
                                                unionId);
        if (ClientType.BUSINESS.value() == SessionUtil.getAppSubject().getClient() && userVO.getNewGuy()) {
            businessFacade.registerShopManagerByPhone(userVO.getPhone(), userVO.getId());
        }
        return ApiResult.ok(userVO);
    }

    @PostMapping(name = "小程序获取微信手机信息", path = "/wechat/phone")
    public ApiResult getWechatPhone(@RequestBody WechatPhoneVO wechatPhoneVO) {
        OsEnum osEnum = SessionUtil.getAppSubject().getOsEnum();
        if (osEnum.appType() != AppTypeEnum.MINI_PROGRAM || osEnum.platform() != PlatformEnum.WECHAT) {
            return ApiResult.error(ERR_SYS_PARAM.getErrorCode(), ERR_SYS_PARAM.getErrorDesc());
        }

        OutApp outApp = systemFacade.getOutAppByOutAppId(SessionUtil.getAppSubject().getAccAppId());
        JsCode2SessionRequest request = new JsCode2SessionRequest();
        request.setAppId(outApp.getAppId());
        request.setSecret(outApp.getSecret());
        request.setAuthCode(wechatPhoneVO.getAuthCode());
        JsCode2SessionResponse response = wehcatCode2Session(request);
        if (response.getErrCode() != null) {
            return ApiResult.error(ERR_SYS_PARAM.getErrorCode(), response.getErrMsg());
        }

        try {
            return ApiResult.ok(decryptWechatData(response.getSessionKey(),
                                                  wechatPhoneVO.getEncryptedData(),
                                                  wechatPhoneVO.getIv()));
        } catch (Exception e) {
            return ApiResult.error(ERR_SYS_PARAM.getErrorCode(), ERR_SYS_PARAM.getErrorDesc());
        }
    }

    private JsCode2SessionResponse wehcatCode2Session(JsCode2SessionRequest signInByCode) {
        return restTemplate.getForObject(
                "https://api.weixin.qq.com/sns/jscode2session?appid={appId}&secret={secret}&js_code={jsCode" +
                 "}&grant_type={grantType}",
                JsCode2SessionResponse.class,
                signInByCode.getAppId(),
                signInByCode.getSecret(),
                signInByCode.getAuthCode(),
                signInByCode.getGrantType());
    }

    /**
     * 获取当前用户
     *
     * @return
     **/
    @GetMapping(name = "获取当前用户信息", path = "/curr")
    @Authorization
    public ApiResult getUser() {
        UserVO userVO = SessionUtil.getCurrentUser();
        AppUser appUser = commonUserFacade.getAppUserByAppIdAndUserId(SessionUtil.getAppSubject().getAppId(),
                                                                      userVO.getId());
        userVO.setLastLoginAt(appUser.getLoginAt());
        return ApiResult.ok(userVO);
    }

    /**
     * 1.14.	按手机号码获取用户
     *
     * @param phone
     * @return
     */
    @GetMapping(name = "根据手机号码获取用户信息", path = "/{phone}/getUser")
    public ApiResult<User> findUserByPhone(@PathVariable("phone") String phone) {
        User user = userFacade.findByPhone(phone);
        return ApiResult.ok(user);
    }

    /**
     * 创建用户、注册
     *
     * @return
     **/
    @PostMapping(name = "用户注册", path = "/add")
    public ApiResult addUser(@RequestBody @Validated RegisterVo registerVo,
                             @RequestHeader(value = "DEVICE-ID", defaultValue = UNDEFINED) String deviceId,
                             @RequestHeader("DEVICE-TYPE") String deviceType,
                             @RequestHeader("OS-VERSION") String osVersion,
                             @RequestHeader(value = "PUSH-ID", required = false) String pushId) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        User user = new User();
        user.setPhone(registerVo.getPhone());
        user.setPassword(registerVo.getPassword());
        user.setUserDevice(buildUserDevice(deviceId, deviceType, appSubject.getOsEnum().value(), osVersion));
        // H5端的直接通过身份验证
        if (appSubject.getOsEnum() == OsEnum.WEB) {
            // 已审核
            user.setVerified(VerifiedType.REVIEWED.getKEY());
        } else {
            // 未审核
            user.setVerified(VerifiedType.UNREVIEWED.getKEY());
        }

        if (appSubject.getClient() == ClientType.PROPERTY.value()) {
            // 根据手机号获取物业公司员工信息（取一条用于校验手机号）
            UserToProperty forCheck = userToPropertyFacade.checkEmployee(appSubject.getPartner(),
                                                                         registerVo.getPhone());
            if (forCheck == null) {
                return ApiResult.error(-1, "该号码尚未登记，请联系比亦特");
            }
            log.info("注册物业人员：{}", forCheck);
            if (forCheck.getUserId() != null) {
                ClientUser clientUser = userFacade.getClientUserByClientAndPartnerAndUserId(appSubject.getClient(),
                                                                                            appSubject.getPartner(),
                                                                                            forCheck.getUserId());
                if (clientUser != null) {
                    return ApiResult.error(-1, "您的号码已经注册，请直接登录");
                }
            }

            // 物业app注册，需要校验手机号是否已在社区登记
            List<Registration> registrationList =
             registrationFacade.listRegistrationsByPartnerAndPhone(appSubject.getPartner(),
                                                                                                        registerVo.getPhone());

            // 注册client, cm user
            UserVO userVO = userToPropertyFacade.registerClientUserAndCMUserWithRegistration(appSubject.getPartner(),
                                                                                             appSubject.getOsEnum()
                                                                                                       .platform()
                                                                                                       .value(),
                                                                                             appSubject.getOsEnum()
                                                                                                       .appType()
                                                                                                       .value(),
                                                                                             appSubject.getAppId(),
                                                                                             forCheck,
                                                                                             registerVo.getPassword(),
                                                                                             registerVo.getCode(),
                                                                                             pushId,
                                                                                             user.getUserDevice(),
                                                                                             registrationList);

            return ApiResult.ok(userVO);
        }
        UserVO item = userFacade.addUser(appSubject.getClient(),
                                         appSubject.getPartner(),
                                         appSubject.getOsEnum().platform().value(),
                                         appSubject.getOsEnum().appType().value(),
                                         appSubject.getAppId(),
                                         pushId,
                                         user,
                                         registerVo.getCode());

        CompletableFuture.runAsync(() -> {
            // 注册成功后，匹配有效的业主档案并激活 (异步处理)
            List<Household> list = householdFacade.listUnactivatedOwnerHouseholdsByPhone(item.getPhone());
            Set<ObjectId> householdIds = new HashSet<>();
            for (Household household : list) {
                UserToRoom userToRoom = new UserToRoom();
                BeanUtils.copyProperties(household, userToRoom);
                userToRoom.setId(null);
                userToRoom.setUserId(item.getId());
                userToRoom.setProprietorId(userToRoom.getUserId());
                userToRoom.setName(household.getUserName());
                userToRoom = userToRoomFacade.upsertAuthOwnerRecord(appSubject.getPartner(), userToRoom);
                if (userToRoom != null) {
                    householdIds.add(household.getId());
                }
            }
            if (!householdIds.isEmpty()) {
                Long num = householdFacade.activatedHouseholdByIds(item.getId(), householdIds);
                if (num != null && num > 0) {
                    // 完善用户
                    User toAppend = new User();
                    Household household = list.get(0);
                    toAppend.setName(household.getUserName());
                    toAppend.setIdentityCard(household.getIdentityCard());

                    IdentityCardUtils.IdentityCardMeta meta =
                     IdentityCardUtils.getIdentityCardMeta(toAppend.getIdentityCard());
                    if (meta != null) {
                        toAppend.setBirthday(meta.getBirthday());
                    }
                    toAppend.setSex(household.getSex());
                    toAppend.setId(item.getId());
                    try {
                        userFacade.updateUser(toAppend);
                        item.setName(toAppend.getName());
                        item.setIdentityCard(toAppend.getIdentityCard());
                        item.setBirthday(toAppend.getBirthday());
                    } catch (Exception e) {
                        log.error("注册成功后，填充用户信息异常：", e);
                    }
                    // 更新社区入住人数
                    Community toUpdate = new Community();
                    toUpdate.setHouseholdCnt(num.intValue());
                    toUpdate.setCheckInRoomCnt(toUpdate.getHouseholdCnt());
                    communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate,
                                                                                   household.getCommunityId());
                }
            }
        });
        return ApiResult.ok(item);
    }

    private UserDevice buildUserDevice(String deviceId, String deviceType, Integer os, String osVersion) {
        UserDevice userDevice = new UserDevice();
        userDevice.setDeviceId(deviceId);
        userDevice.setDeviceType(deviceType);
        userDevice.setOs(os);
        userDevice.setOsVersion(osVersion);
        return userDevice;
    }

    /**
     * 1.6.	修改用户信息
     *
     * @param user
     * @return
     **/
    @PostMapping(name = "编辑用户信息", path = "/edit")
    @Authorization
    public ApiResult updateUser(@RequestBody @Validated(User.Update.class) User user) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        user = userFacade.updateUser(appSubject.getClient(), appSubject.getPartner(), user);
        // 不返回更新实体，只返回更新内容
        return ApiResult.ok(user);
    }

    /**
     * 更新推送ID
     *
     * @param pushId
     * @return
     */
    @GetMapping(name = "更新推送ID", path = "/edit/pushId")
    @Authorization(verifyApi = false)
    public ApiResult updatePushId(String pushId) {
        if (StringUtil.isBlank(pushId)) {
            return ApiResult.error(-1, "推送ID不能为空");
        }
        userFacade.updatePushIdByAppIdAndUserId(pushId,
                                                SessionUtil.getTokenSubject().getUid(),
                                                SessionUtil.getAppSubject().getAppId());
        return ApiResult.ok();
    }

    /**
     * 完善实名信息
     *
     * @param user
     * @return
     **/
    @PostMapping(name = "完善实名信息", path = "/real-name-authentication")
    @Authorization
    public ApiResult realNameAuthentication(@RequestBody @Validated(User.RealNameAuthentication.class) User user) {
        UserVO curr = SessionUtil.getCurrentUser();
        if (curr == null || !curr.getId().equals(user.getId())) {
            return ApiResult.error(-1, "只能完善本人信息");
        }
        Map<String, Object> result = userFacade.updateUser(user);
        // 不返回更新实体，只返回更新内容
        return ApiResult.ok(result);
    }

    /**
     * 修改密码
     *
     * @param params
     * @return
     */
    @PostMapping(name = "修改密码", path = "/changePassword")
    @Authorization
    public ApiResult editPassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPass");
        if (StringUtil.isBlank(oldPassword)) {
            return ApiResult.error(-1, "旧密码不能为空");
        }

        String newPassword = params.get("newPass");
        if (StringUtil.isBlank(newPassword)) {
            return ApiResult.error(-1, "新密码不能为空");
        }

        AppSubject appSubject = SessionUtil.getAppSubject();
        userFacade.changePassword(appSubject.getClient(),
                                  appSubject.getPartner(),
                                  SessionUtil.getTokenSubject().getUid(),
                                  oldPassword,
                                  newPassword);
        return ApiResult.ok();
    }

    @PostMapping(name = "修改手机号", path = "/changePhone")
    @Authorization
    public ApiResult editPhone(@RequestBody Map<String, String> params) {
        ObjectId userId = SessionUtil.getTokenSubject().getUid();
        if (userId == null) {
            return ApiResult.error(-1, "非法用户");
        }

        String phone = params.get("newPhone");
        if (StringUtil.isBlank(phone)) {
            return ApiResult.error(-1, "新手机号不能为空");
        }

        String code = params.get("code");
        if (StringUtil.isBlank(code)) {
            return ApiResult.error(-1, "验证码不能为空");
        }

        String password = params.get("password");
        if (StringUtil.isBlank(password)) {
            return ApiResult.error(-1, "密码不能为空");
        }

        AppSubject appSubject = SessionUtil.getAppSubject();
        userFacade.changePhone(appSubject.getClient(), appSubject.getPartner(), userId, phone, code, password);
        return ApiResult.ok();
    }

    @PostMapping(name = "重置登录密码", path = "/resetPassword")
    public ApiResult resetPassword(@RequestBody Map<String, String> params,
                                   @RequestHeader(value = "DEVICE-ID", defaultValue = UNDEFINED) String deviceId,
                                   @RequestHeader("DEVICE-TYPE") String deviceType, @RequestHeader("OS") Integer os,
                                   @RequestHeader("OS-VERSION") String osVersion,
                                   @RequestHeader(value = "PUSH-ID", required = false) String pushId) {
        String phone = params.get("phone");
        if (StringUtil.isBlank(phone)) {
            return ApiResult.error(-1, "手机号不能为空");
        }

        String code = params.get("code");
        if (StringUtil.isBlank(code)) {
            return ApiResult.error(-1, "验证码不能为空");
        }

        String newPass = params.get("newPass");
        if (StringUtil.isBlank(newPass)) {
            return ApiResult.error(-1, "新密码不能为空");
        }

        AppSubject appSubject = SessionUtil.getAppSubject();
        UserDevice userDevice = buildUserDevice(deviceId, deviceType, appSubject.getOsEnum().value(), osVersion);
        userFacade.resetPassword(appSubject.getClient(),
                                 appSubject.getPartner(),
                                 appSubject.getOsEnum().platform().value(),
                                 appSubject.getOsEnum().appType().value(),
                                 appSubject.getAppId(),
                                 pushId,
                                 phone,
                                 newPass,
                                 code,
                                 userDevice);
        return ApiResult.ok();
    }

    @GetMapping(name = "退出登录", path = "/signOut")
    @Authorization(verifyApi = false)
    public ApiResult logout() {
        userFacade.signOut(SessionUtil.getTokenSubject().getToken());
        return ApiResult.ok();
    }

    // =================================================【user end】====================================================

    // =================================================【userToRoom start】============================================

    /**
     * 1.11.	申请房屋认证（业主）
     *
     * @param vo
     * @return
     **/
    @PostMapping(name = "业主申请房间认证", path = "/room/attestation")
    @Authorization(verifyApi = false)
    @SendPush(clientTypes = ClientType.PROPERTY,
              scope = SendPush.Scope.COMMUNITY,
              point = PushPointEnum.APPLY_ROOM_ATTESTATION)
    public ApiResult addUserToRoom(@Validated(UserToRoomVO.AddOwner.class) @RequestBody UserToRoomVO vo) {
        UserVO currentUser = SessionUtil.getCurrentUser();

        if (StringUtil.isBlank(vo.getName()) && StringUtil.isBlank(currentUser.getName())) {
            throw USER_INFO_INCOMPLETE;
        }
        List<Parameter> parameters = parameterFacade.queryByCommunityIdAndTypeForAuth(SessionUtil.getCommunityId(),
                                                                                      ParamConfigType.HOUSEHOLD_AUTH.getKey());

        UserToRoom toAdd = new UserToRoom();
        if (parameters != null && parameters.size() > 0) {
            // 校验业主认证的动态参数
            ApiResult result = verifyUserToRoom(toAdd, parameters, vo.getAuthMap());
            if (result.getErrorCode() == -1) {
                return result;
            }
        }
        toAdd.setName(StringUtil.isBlank(vo.getName()) ? currentUser.getName() : vo.getName());
        if (StringUtil.isBlank(toAdd.getIdentityCard())) {
            toAdd.setIdentityCard(currentUser.getIdentityCard());
        }
        toAdd.setUserId(currentUser.getId());
        toAdd.setRoomId(vo.getRoomId());

        Household household = householdFacade.findAuthOwnerByRoom(vo.getRoomId());
        // 判断该房间是否已有业主
        if (household != null && household.getActivated() != null && household.getActivated()) {
            throw ROOM_BE_AUTHENTICATED;
        }

        // 根据userId查询，防止重复提交申请
        if (userToRoomFacade.existApplication(toAdd.getUserId(), vo.getRoomId())) {
            throw APPLY_EXIST;
        }

        // 获取房间的信息
        Room room = roomFacade.findOne(vo.getRoomId());
        if (room.getArea() == null || room.getArea() <= 0) {
            log.info("该房间面积不合法，默认设置为0，请联系物业管理员。{}-面积{}", room.getName(), room.getArea());
            room.setArea(0);
        }
        Building building = buildingFacade.findOne(room.getBuildingId());
        toAdd.setRoomLocation(building == null ? "" : building.getName() + room.getName());
        toAdd.setRoomName(room.getName());
        toAdd.setArea(room.getArea());
        toAdd.setCommunityId(room.getCommunityId());
        toAdd.setBuildingId(room.getBuildingId());
        // 根据roomId组装locationCode
        Map<Integer, Integer> levelNoMap = communityFacade.listLevelNos(LevelEnum.ROOM.value(), toAdd.getRoomId());

        if (levelNoMap != null && !levelNoMap.isEmpty()) {
            // 社区
            Integer communityNo = levelNoMap.get(LevelEnum.COMMUNITY.value());
            // 区域
            Integer zoneNo = levelNoMap.get(LevelEnum.ZONE.value());
            // 楼栋
            Integer buildingNo = levelNoMap.get(LevelEnum.BUILDING.value());
            // 楼层
            Integer floorNo = levelNoMap.get(LevelEnum.FLOOR.value());
            final String zero = "0";
            final char padChar = '0';
            final String encoding = "utf8";

            StringBuffer levelCode = new StringBuffer();
            levelCode.append(StringUtil.leftPadWithBytes(communityNo == null ? zero : communityNo.toString(),
                                                         5,
                                                         padChar,
                                                         encoding));
            levelCode.append(StringUtil.leftPadWithBytes(zoneNo == null ? zero : zoneNo.toString(),
                                                         2,
                                                         padChar,
                                                         encoding));
            // 单元 00
            levelCode.append(zero).append(zero);
            levelCode.append(StringUtil.leftPadWithBytes(buildingNo == null ? zero : buildingNo.toString(),
                                                         3,
                                                         padChar,
                                                         encoding));
            levelCode.append(StringUtil.leftPadWithBytes(floorNo == null ? zero : floorNo.toString(),
                                                         3,
                                                         padChar,
                                                         encoding));

            toAdd.setLocationCode(levelCode.toString());
        }

        toAdd = userToRoomFacade.addOwner(toAdd);
        return ApiResult.ok(toAdd);
    }

    /**
     * 校验业主认证的动态参数
     *
     * @param userToRoom
     * @param parameters
     * @param authMap
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private ApiResult verifyUserToRoom(UserToRoom userToRoom, List<Parameter> parameters, Map<String, Object> authMap) {
        List<UserToRoom.AuthParam> authParamList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            Object value = authMap == null ? null : authMap.get(parameter.getKey());
            //是否必填
            if (parameter.getIsRequired() != null && parameter.getIsRequired() && value == null) {
                return ApiResult.error(-1, String.format("%s%s", parameter.getName(), "不能为空"));
            }
            UserToRoom.AuthParam authParam = new UserToRoom.AuthParam();
            authParam.setKey(parameter.getKey());
            authParam.setLabel(parameter.getName());
            String inputRule = parameter.getInputRule();
            if (StringUtil.isNotNull(value) && StringUtil.isNotNull(inputRule)) {
                Map<String, String> ruleMap = new HashMap<>();
                // 枚举类型,需要根据key存入value
                if (parameter.getDataType() == 4) {
                    List<Map> mapList = JSONObject.parseArray(inputRule, Map.class);
                    for (Map map : mapList) {
                        ruleMap.putAll(map);
                    }
                    value = ruleMap.get(value);
                    // String 类型需要校验正则
                } else if (parameter.getDataType() == 1) {
                    String valueStr = (String) value;
                    ruleMap = (Map<String, String>) JSONObject.parse(inputRule);
                    String minLen = ruleMap.get("minLength");
                    String maxLen = ruleMap.get("maxLength");
                    String regex = ruleMap.get("regex");
                    String tips = ruleMap.get("tips") == null ? "输入不合法" : ruleMap.get("tips");
                    Integer minLength = null;
                    Integer maxLength = null;
                    if (StringUtil.isNotNull(minLen)) {
                        minLength = Integer.parseInt(minLen);
                    }
                    if (StringUtil.isNotNull(maxLen)) {
                        maxLength = Integer.parseInt(maxLen);
                    }
                    if (minLength != null && maxLength != null &&
                        (valueStr.length() < minLength || valueStr.length() > maxLength)) {
                        return ApiResult.error(-1, String.format("%s%s", parameter.getName(), tips));
                    }
                    if (StringUtil.isNotNull(regex) && !Pattern.matches(regex, valueStr)) {
                        return ApiResult.error(-1, String.format("%s%s", parameter.getName(), tips));
                    }
                }
            }
            authParam.setValue(value);
            authParamList.add(authParam);
        }
        JSONObject jsonObject = new JSONObject(authMap);
        UserToRoom receive = JSON.parseObject(jsonObject.toJSONString(), UserToRoom.class);
        BeanUtils.copyProperties(receive, userToRoom);
        userToRoom.setAuthParamList(authParamList);
        return ApiResult.ok();
    }

    /**
     * 关闭/开放 房屋给家属或者租客申请（业主）
     * 默认关闭
     *
     * @param id
     * @return
     */
    @GetMapping(name = "关闭或开放房间被申请", path = "/room/{id}/disable-apply")
    @Authorization
    public ApiResult disableAuxiliaryApply(@PathVariable("id") ObjectId id, Boolean canApply) {
        boolean result = userToRoomFacade.disableAuxiliaryApply(id, SessionUtil.getTokenSubject().getUid(), canApply);
        if (!result) {
            throw OPERATION_FAILURE;
        }
        return ApiResult.ok();
    }

    /**
     * 1.16.	绑定家属或租客
     *
     * @param userToRoom
     * @return
     */
    @PostMapping(name = "家属/租客申请房间绑定", path = "/member/add")
    @Authorization(verifyApi = false)
    @SendPush(scope = SendPush.Scope.COMMUNITY,
              clientTypes = ClientType.HOUSEHOLD,
              point = PushPointEnum.APPLY_ROOM_BINDING)
    public ApiResult addAuxiliary(@Validated(UserToRoom.AddAuxiliary.class) @RequestBody UserToRoom userToRoom) {
        UserVO userVO = SessionUtil.getCurrentUser();

        if (StringUtil.isBlank(userToRoom.getName()) && StringUtil.isBlank(userVO.getName())) {
            throw USER_INFO_INCOMPLETE;
        }

        if (StringUtil.isNotBlank(userToRoom.getIdentityCard()) && !IDCardUtils.verifi(userToRoom.getIdentityCard())) {
            // 校验身份证格式
            throw IDENTITY_CARD_ILLEGAL;
        }
        userToRoom.setUserId(userVO.getId());
        UserToRoom saved = userToRoomFacade.addAuxiliary(userToRoom);
        if (saved == null) {
            throw OPERATION_FAILURE;
        }
        PushTarget pushTarget = new PushTarget();
        pushTarget.setUserIds(Collections.singleton(saved.getProprietorId()));
        return WrapResult.create(ApiResult.ok(saved), pushTarget);
    }

    /**
     * 解绑家属或租客
     * 业主解绑需要通知物业，物业解绑需要通知业主
     *
     * @param id
     * @return
     */
    @GetMapping(name = "解绑家属或租客的房间绑定", path = "/member/{id}/relieve")
    @Authorization
    @SendPush(scope = SendPush.Scope.COMMUNITY,
              clientTypes = {ClientType.HOUSEHOLD, ClientType.PROPERTY},
              point = PushPointEnum.UNBINDING_ROOM)
    public ApiResult deleteAuxiliary(@PathVariable("id") ObjectId id) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        Integer client = appSubject.getClient();
        UserToRoom userToRoom = userToRoomFacade.deleteAuxiliary(appSubject.getPartner(),
                                                                 id,
                                                                 SessionUtil.getTokenSubject().getUid(),
                                                                 client);
        // 注销档案，如果存在
        householdFacade.removeByRoomIdAndUserId(userToRoom.getRoomId(), userToRoom.getUserId());
        //更新社区人口
        //社区入住人数减 1
        Community toUpdate = new Community();
        toUpdate.setHouseholdCnt(-1);
        toUpdate.setCheckInRoomCnt(0);
        communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, userToRoom.getCommunityId());

        PushTarget pushTarget = new PushTarget();
        if (client == ClientType.PROPERTY.value()) {
            // 物业注销非业主，需要推送给业主
            pushTarget.setUserIds(Collections.singleton(userToRoom.getProprietorId()));
            pushTarget.setClients(Collections.singleton(ClientType.HOUSEHOLD.value()));
        } else {
            // 业主注销家属，需要推送给物业
            PushConfig pushConfig = pushFacade.findPushConfigByCompanyIdAndPointId(SessionUtil.getCompanyId(),
                                                                                   PushPointEnum.UNBINDING_ROOM.name());

            List<CommunityUser> cmUsers =
             userFacade.findCMUserByCommunityIdAndClientAndRoles(userToRoom.getCommunityId(),
                                                                                              ClientType.PROPERTY.value(),
                                                                                              pushConfig.getTargets());
            // 没有找到物业管理员，无需推送
            if (CollectionUtils.isEmpty(cmUsers)) {
                return ApiResult.ok();
            }
            pushTarget.setUserIds(cmUsers.stream().map(CommunityUser::getUserId).collect(Collectors.toSet()));
            pushTarget.setClients(Collections.singleton(ClientType.PROPERTY.value()));
        }

        Map<String, String> map = new HashMap();
        String title = RelationshipType.getValueByKey(userToRoom.getRelationship()) + userToRoom.getName() + "已被解除" +
                       (userToRoom.getRoomLocation() == null ? "" : userToRoom.getRoomLocation()) + "的房屋认证";
        // 推送title
        map.put("title", title);
        // 社区名称
        map.put("communityName", communityFacade.findOne(userToRoom.getCommunityId()).getName());
        return WrapResult.create(ApiResult.ok(), userToRoom, map, pushTarget);
    }

    /**
     * 审核租客/家属绑定（业主）
     * 由租客/家属提交绑定申请，推送至业主审核
     * 若开启2级审核，则业主审核通过后需要推送至物业管理员
     * （0：未审核；1：已审核；-1：驳回；2: 已注销; 4:审核中）
     *
     * @return
     */
    @PostMapping(name = "业主审核家属/租客的房间绑定申请", path = "/member/audit")
    @Authorization
    @SendPush(clientTypes = {ClientType.HOUSEHOLD, ClientType.PROPERTY},
              scope = SendPush.Scope.COMMUNITY,
              point = PushPointEnum.AUDIT_ROOM_BINDING_BY_OWNER)
    public ApiResult auditAuxiliary(@Validated(UserToRoom.AuditAuxiliary.class) @RequestBody UserToRoom entity) {
        UserToRoom item = userToRoomFacade.findById(entity.getId());
        // 是否开启二级审核
        Parameter parameter = parameterFacade.findByTypeAndKeyAndCommunityId(ParamConfigType.HOUSEHOLD_AUTH.getKey(),
                                                                             ParamKeyType.LEVEL2AUDIT.name(),
                                                                             item.getCommunityId());
        if (parameter == null) {
            return ApiResult.error(-1, "该社区缺少二级审核配置信息");
        }
        UserToRoom userToRoom = userToRoomFacade.auditAuxiliary(SessionUtil.getAppSubject().getPartner(),
                                                                entity.getId(),
                                                                SessionUtil.getTokenSubject().getUid(),
                                                                entity.getAuditStatus(),
                                                                Boolean.parseBoolean(parameter.getValue()));
        if (userToRoom == null) {
            return ApiResult.error(-1, "审核错误(记录不存在)");
        }
        Map<String, String> auditMap = new HashMap<>(1);
        PushTarget pushTarget = new PushTarget();
        // 开启二级审核，审核通过不注册IM，因为还要物业审核
        if (Boolean.parseBoolean(parameter.getValue()) &&
            userToRoom.getAuditStatus() == AuditStatusType.REVIEWING.getType()) {
            return ApiResult.ok();
        }
        auditMap.put("auditStatusDesc", AuditStatusType.fromValue(userToRoom.getAuditStatus()));
        // 更新社区入住人数，更新离线协议key，注册IM
        updateOtherInfo4AuditHousehold(userToRoom);
        // 指定这次推送只推用户端
        pushTarget.setClients(Collections.singleton(ClientType.HOUSEHOLD.value()));
        pushTarget.setUserIds(Collections.singleton(userToRoom.getUserId()));
        return WrapResult.create(ApiResult.ok(), userToRoom, pushTarget, auditMap);
    }

    @PostMapping(name = "物业二级审核家属/租客的房间绑定申请", path = "/member/property-audit")
    @Authorization
    @SendPush(clientTypes = ClientType.HOUSEHOLD,
              scope = SendPush.Scope.COMMUNITY,
              point = PushPointEnum.AUDIT_ROOM_BINDING_BY_PROPERTY)
    public ApiResult auditAuxiliaryByProperty(
            @Validated(UserToRoom.AuditAuxiliary.class) @RequestBody UserToRoom entity) {
        UserToRoom userToRoom = userToRoomFacade.auditAuxiliaryByProperty(SessionUtil.getAppSubject().getPartner(),
                                                                          entity.getId(),
                                                                          SessionUtil.getTokenSubject().getUid(),
                                                                          entity.getAuditStatus());
        updateOtherInfo4AuditHousehold(userToRoom);
        Map<String, String> auditMap = new HashMap<>(1);
        auditMap.put("auditStatusDesc", AuditStatusType.fromValue(userToRoom.getAuditStatus()));
        PushTarget pushTarget = new PushTarget();
        pushTarget.setUserIds(Collections.singleton(userToRoom.getUserId()));
        return WrapResult.create(ApiResult.ok(), userToRoom, pushTarget, auditMap);
    }

    /**
     * 1.12.	审核房屋认证（物业）
     *
     * @return
     */
    @PostMapping(name = "审核业主的房间认证申请", path = "/property/audit")
    @Authorization
    @SendPush(scope = SendPush.Scope.COMMUNITY,
              clientTypes = ClientType.HOUSEHOLD,
              point = PushPointEnum.AUDIT_ROOM_ATTESTATION)
    public ApiResult auditOwner(@Validated(UserToRoom.AuditOwner.class) @RequestBody UserToRoom entity) {
        UserToRoom userToRoom = userToRoomFacade.approvalOwner(SessionUtil.getAppSubject().getPartner(),
                                                               entity.getId(),
                                                               SessionUtil.getTokenSubject().getUid(),
                                                               entity.getAuditStatus());
        if (userToRoom == null) {
            throw DATA_INVALID;
        }
        updateOtherInfo4AuditHousehold(userToRoom);
        PushTarget pushTarget = new PushTarget();
        pushTarget.setUserIds(Collections.singleton(userToRoom.getUserId()));
        Map<String, String> auditMap = new HashMap<>(1);
        auditMap.put("auditStatusDesc", AuditStatusType.fromValue(userToRoom.getAuditStatus()));
        return WrapResult.create(ApiResult.ok(), userToRoom, pushTarget, auditMap);
    }

    /**
     * 更新社区入住人数，录入或激活档案，完善实名
     *
     * @param userToRoom
     */
    private void updateOtherInfo4AuditHousehold(UserToRoom userToRoom) {
        if (userToRoom.getAuditStatus() == AuditStatusType.REVIEWED.getType()) {
            appendUserRealInfo(userToRoom);
            // 录入或激活档案
            HouseholdVO householdVO = new HouseholdVO();
            BeanUtils.copyProperties(userToRoom, householdVO);
            householdVO.setUserName(userToRoom.getName());
            // 更新社区人口
            Community toUpdate = new Community();
            toUpdate.setHouseholdCnt(1);
            Household household = new Household();
            BeanUtils.copyProperties(householdVO, household);
            if (RelationshipType.OWNER.KEY.equals(userToRoom.getRelationship())) {
                toUpdate.setCheckInRoomCnt(1);
                householdFacade.upsertHouseholdForOwner(household);
            } else {
                toUpdate.setCheckInRoomCnt(0);
                householdFacade.upsertHouseholdForNotOwner(household);
            }
            communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, userToRoom.getCommunityId());
        }
    }

    /**
     * 更新社区入住人数，校验是否需要实名
     *
     * @param userToRoom
     */
    private void updateHouseholdNumAndCheckUserRealInfo(UserToRoom userToRoom) {
        if (userToRoom.getAuditStatus() == AuditStatusType.REVIEWED.getType()) {
            appendUserRealInfo(userToRoom);
            // 更新社区人口
            Community toUpdate = new Community();
            toUpdate.setHouseholdCnt(1);
            if (RelationshipType.OWNER.KEY.equals(userToRoom.getRelationship())) {
                toUpdate.setCheckInRoomCnt(1);
            } else {
                toUpdate.setCheckInRoomCnt(0);
            }
            communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, userToRoom.getCommunityId());
        }
    }

    /**
     * 检测用户是否需要实名认证
     *
     * @param userToRoom
     */
    private void appendUserRealInfo(UserToRoom userToRoom) {
        UserVO userVO = userFacade.findById(userToRoom.getUserId());
        if (userVO != null && StringUtil.isBlank(userVO.getIdentityCard()) &&
            (StringUtil.isBlank(userVO.getName()) || !StringUtil.isBlank(userToRoom.getIdentityCard()))) {
            User user = new User();
            user.setId(userToRoom.getUserId());
            user.setName(userToRoom.getName());
            user.setIdentityCard(userToRoom.getIdentityCard());
            Map<String, Object> result = userFacade.updateUser(user);
        }
    }

    /**
     * 获取IM的token信息，提供给前端，当业主登录后，被审核了绑定房间，将无法从再一次登录获取token
     * 所以前端打开IM的时候检查有无token，没有则会请求此接口
     *
     * @return
     */
    @GetMapping(name = "获取IM Token", path = "/im/token")
    @Authorization
    public ApiResult imToken() {
        UserVO userVO = SessionUtil.getCurrentUser();
        /**
         * delete by decai.liu at 20190318

         if (CollectionUtils.isEmpty(userVO.getRoles()) || !userVO.getRoles().contains(RoleType.SUPPORTSTAFF.name())
         && !userVO.getRoles().contains(RoleType.HOUSEHOLD.name())) {
         return ApiResult.error(-1, "此角色未注册IM");
         }
         */

        if (CollectionUtils.isEmpty(userVO.getRoles())) {
            throw AUTHENCATION_FAILD;
        }
        IMUser imUser;
        try {
            String token = StrUtil.get32UUID();
            if (userVO.getAccid() == null) {
                imService.registerIM(userVO.getAccid(), token, userVO.getNickName());
            } else {
                imService.updateIM(userVO.getAccid(), token);
            }
            AppSubject appSubject = SessionUtil.getAppSubject();
            imUser = userFacade.saveIMToken(appSubject.getClient(), appSubject.getPartner(), userVO.getId(), token);
        } catch (Exception e) {
            return ApiResult.error(-1, "注册IM失败");
        }

        return ApiResult.ok(imUser);
    }

    /**
     * 注销房屋认证（物业）
     *
     * @param roomId
     * @return
     */
    @GetMapping(name = "注销业主的房间认证", path = "/property/{roomId}/relieve")
    @Authorization
    public ApiResult deleteUserToRoomByRoomId(@PathVariable("roomId") ObjectId roomId) {
        int update = userToRoomFacade.relieveOwner(SessionUtil.getAppSubject().getPartner(),
                                                   roomId,
                                                   SessionUtil.getTokenSubject().getUid());
        // 更新社区人口
        Community toUpdate = new Community();
        toUpdate.setHouseholdCnt(-update);
        toUpdate.setCheckInRoomCnt(-1);
        communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, SessionUtil.getCommunityId());
        // 注销房屋档案
        householdFacade.removeByRoomId(roomId);
        return ApiResult.ok();
    }

    /**
     * 1.15.	按社区获取用户列表（用户关系）（物业）
     *
     * @param communityId
     * @param relationship （1：业主；2：家属；3：租客）
     * @param auditStatus  用户与该房间的关系是否审核通过（0：未审核；1：审核通过；-1：驳回；-2：违规; 2: 已注销; 3: 已解绑;）
     * @return
     * @since 20180518
     */
    @GetMapping(name = "住户房间分页(旧)", path = "/{communityId}/getByCommunityId")
    @Authorization
    @Deprecated
    public ApiResult getUsersByCommunityId(@PathVariable("communityId") ObjectId communityId, Integer relationship,
                                           String buildingId, Integer auditStatus, String contractPhone, String name,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        Page<UserToRoom> users = userToRoomFacade.queryByCommunityId(communityId,
                                                                     buildingId,
                                                                     relationship,
                                                                     auditStatus,
                                                                     contractPhone,
                                                                     name,
                                                                     page,
                                                                     size);
        return ApiResult.ok(users);
    }

    @PostMapping(name = "某社区已认证住户分页", path = "/with-room/list")
    @Authorization
    public ApiResult getUserListByCommunityId(@RequestBody CMUserVO cmUserVO,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return ApiResult.ok(userFacade.queryWithRoomByCommunity(cmUserVO, SessionUtil.getCommunityId(), page, size));
    }

    @PostMapping(name = "用户认证申请分页", path = "/user-to-room/list")
    @Authorization
    public ApiResult getUserToRoomListByCommunityId(@RequestBody UserToRoom userToRoom,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        userToRoom.setCommunityId(SessionUtil.getCommunityId());
        Page<UserToRoom> users = userToRoomFacade.queryPageByCommunityId(userToRoom, page, size);
        return ApiResult.ok(users);
    }

    @GetMapping(name = "待审核的用户认证申请分页", path = "/user-to-room/un-review/list")
    @Authorization
    public ApiResult getUnReviewList(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<UserToRoom> users = userToRoomFacade.queryUnReviewPageByCommunityId(SessionUtil.getCommunityId(),
                                                                                 page,
                                                                                 size);
        return ApiResult.ok(users);
    }

    @PostMapping(name = "待审核的非业主认证申请分页", path = "/non-proprietor/un-review/page")
    @Authorization
    public ApiResult getNonProprietorUnReviewPage(@RequestBody UserToRoom userToRoom,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        Page<UserToRoom> userToRoomPage = userToRoomFacade.queryNonProprietorUnReviewPageForRoom(userToRoom.getRoomId(),
                                                                                                 page,
                                                                                                 size);
        return ApiResult.ok(userToRoomPage);
    }

    /**
     * 分页
     * 1.19.	根据房间获取用户列表（业主/物业）（0：未审核；1：审核通过；-1：驳回；-2：违规; 2: 已注销; 3: 已解绑;4:审核中）
     *
     * @param roomId
     * @param auditStatus
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "某房间下的住户认证申请分页", path = "/{roomId}/getByRoomId")
    @Authorization
    public ApiResult<UserToRoom> getUserByRoomId(@PathVariable("roomId") ObjectId roomId, Integer auditStatus,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<UserToRoom> pageList = userToRoomFacade.queryByRoomId(roomId,
                                                                   auditStatus,
                                                                   SessionUtil.getTokenSubject().getUid(),
                                                                   SessionUtil.getAppSubject().getClient(),
                                                                   page,
                                                                   size);
        return ApiResult.ok(pageList);
    }

    /**
     * 根据房间获取用户列表（H5）
     *
     * @param roomId
     * @return
     */
    @GetMapping(name = "某房间已认证住户列表", path = "/{roomId}/getByRoomId/list")
    @Authorization
    public ApiResult<List<UserToRoom>> getUserByRoomId(@PathVariable("roomId") ObjectId roomId) {
        List<UserToRoom> userToRoomList = userToRoomFacade.findReviewedListByRoomId(roomId);
        return ApiResult.ok(userToRoomList);
    }

    /**
     * 隐藏当前的申请记录
     *
     * @param id
     * @param closed
     * @return
     */
    @GetMapping(name = "隐藏被拒绝/被注销的申请记录", path = "/{id}/closedApplyById")
    @Authorization(verifyApi = false)
    public ApiResult<UserToRoom> hiddenUserToRoomApplyById(@PathVariable("id") ObjectId id, Boolean closed) {
        if (closed == null) {
            closed = true;
        }
        boolean flag = userToRoomFacade.hiddenUserToRoomApplyById(id, SessionUtil.getTokenSubject().getUid(), closed);
        if (flag) {
            return ApiResult.ok();
        }
        throw OPERATION_FAILURE;
    }

    /**
     * APP端添加上次记录
     *
     * @param params
     * @return
     */
    @PostMapping(name = "记录用户登录痕迹", path = "/editAttach")
    @Authorization(verifyApi = false)
    public ApiResult editAttach(@RequestBody Map<String, String> params) {
        ObjectId userId = SessionUtil.getTokenSubject().getUid();
        String attach = params.get("attach");
        AppSubject appSubject = SessionUtil.getAppSubject();
        userFacade.updateAttach(appSubject.getClient(), appSubject.getPartner(), userId, attach);
        return ApiResult.ok(attach);
    }


    /**
     * 获取业主/租客合同信息(物业-H5)
     *
     * @param id
     * @return
     */
    @GetMapping(name = "获取业主/租客合同信息", path = "/{id}/contract-info")
    @Authorization
    public ApiResult getContractInfoById(@PathVariable("id") ObjectId id) {
        PrintUserVO printUserVO = userToRoomFacade.getContractInfoById(id);
        UserToRoom userToRoom = printUserVO.getUserToRoom();
        if (userToRoom != null) {
            List<Apply> carList = carFacade.findCarByUserIdAndCommunityIdAndAuditStatus(userToRoom.getUserId(),
                                                                                        userToRoom.getCommunityId(),
                                                                                        VerifiedType.REVIEWED.getKEY());
            printUserVO.setCarList(carList);
            return ApiResult.ok(printUserVO);
        }
        return ApiResult.error(-1, "数据异常");
    }

    /**
     * @param buildingId
     * @param relationship （1：业主；2：家属；3：租客）
     * @param auditStatus  用户与该房间的关系是否审核通过（0：未审核；1：审核通过；-1：驳回；2: 已注销; 3: 已解绑;4：审核中）
     * @return
     * @since 20180511
     * 按楼宇ID获取用户关系列表(业主)（物业）
     */
    @GetMapping(name = "某楼栋住户认证申请分页(旧)", path = "/{buildingId}/by-building-id")
    @Authorization
    @Deprecated
    public ApiResult getUsersByBuildingId(@PathVariable("buildingId") ObjectId buildingId,
                                          @RequestParam(defaultValue = "1") Integer relationship,
                                          @RequestParam(defaultValue = "1") Integer auditStatus,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        Page<UserToRoom> users = userToRoomFacade.queryByBuildingId(buildingId,
                                                                    relationship,
                                                                    auditStatus,
                                                                    SessionUtil.getAppSubject().getClient(),
                                                                    page,
                                                                    size);
        return ApiResult.ok(users);
    }

    /**
     * 按楼宇ID获取用户已认证列表
     *
     * @param buildingId
     * @return
     */
    @GetMapping(name = "某楼栋已认证住户列表", path = "/{buildingId}/user-to-room/list")
    @Authorization
    public ApiResult getUserToRoomListByBuildingId(@PathVariable("buildingId") ObjectId buildingId) {
        List<UserToRoom> userToRoomList = userToRoomFacade.queryListByBuildingId(buildingId);
        return ApiResult.ok(userToRoomList);
    }

    /**
     * @param communityId
     * @return
     * @since 20180514
     * 根据社区统计各楼宇有效用户数量
     */
    @Deprecated
    @GetMapping(name = "统计各楼宇有效业主数量(旧)", path = "/{communityId}/proprietors-statistics")
    @Authorization
    public ApiResult proprietorsStatistics(@PathVariable("communityId") ObjectId communityId) {
        List<Object> list = userToRoomFacade.proprietorsStatistics(SessionUtil.getCommunityId());
        return ApiResult.ok(list);
    }

    /**
     * 根据id获取详情
     *
     * @param id
     * @return
     */
    @GetMapping(name = "认证申请记录详情", path = "/{id}/userToRoom-detail")
    @Authorization
    public ApiResult queryDetailById(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(userToRoomFacade.findById(id));
    }

    /**
     * 物业帮业主申请米立的设备
     *
     * @param id
     * @return
     * @since 2018-09-26 14:24:00
     */
    @GetMapping(name = "业主申请米立设备(旧)", path = "/apply/{id}/miliDevice")
    @Authorization
    @Deprecated
    public ApiResult applyUserMiliDevice(@PathVariable("id") ObjectId id) {
        userToRoomFacade.applyUserToMili(id);
        return ApiResult.ok();
    }

    /**
     * 设置常住房屋，返回新的房屋协议key
     *
     * @param userToRoomId
     * @return
     */
    @GetMapping(name = "设置常住房屋(返回新的房屋协议)", path = "/room/edit/{userToRoomId}/in-common-use")
    @Authorization
    public ApiResult editInCommonUse(@PathVariable("userToRoomId") ObjectId userToRoomId,
                                     @RequestHeader("OS") Integer os) {
        UserToRoom toUpdate = userToRoomFacade.editInCommonUse(SessionUtil.getCommunityId(),
                                                               SessionUtil.getTokenSubject().getUid(),
                                                               userToRoomId);
        if (toUpdate != null) {
            // 生成协议
            String protocolKey = protocolFacade.encodeProtocol4Room(toUpdate.getCommunityId(),
                                                                    toUpdate.getUserId(),
                                                                    os);
            Card card = cardFacade.findByCommunityIdAndUserIdAndKeyType(toUpdate.getCommunityId(),
                                                                        toUpdate.getUserId(),
                                                                        CertificateType.PHONE_MAC.KEY);
            card.setProtocolKey(protocolKey);
            return ApiResult.ok(card);
        }
        throw OPERATION_FAILURE;
    }
    // =================================================【userToRoom end】==============================================

    // ================================================【household start】==============================================

    /**
     * @param roomId
     * @return
     */
    @GetMapping(name = "检测房间是否存在有效的申请", path = "/households/{roomId}/check")
    @Authorization
    public ApiResult checkApplyRecord(@PathVariable ObjectId roomId) {
        List<Household> households = householdFacade.findOwnerByRoomIds(Collections.singleton(roomId));
        if (!households.isEmpty()) {
            log.info("房屋已存在档案");
            return ApiResult.ok(-1);
        }
        List<UserToRoom> list = userToRoomFacade.getOwnerApplyRecordsByRoomId(roomId);
        long reviewing = list.stream().filter(u -> AuditStatusType.REVIEWING.getType() == u.getAuditStatus()).count();
        return ApiResult.ok(reviewing);
    }

    /**
     * @param householdVO
     * @return
     */
    @PostMapping(name = "物业录入住户档案", path = "/households/save")
    @Authorization
    public ApiResult saveHouseholds(@Validated @RequestBody HouseholdVO householdVO) throws Exception {
        if (!IDCardUtils.verifi(householdVO.getIdentityCard())) {
            // 校验身份证格式
            return ApiResult.error(-1, "身份证号码输入不合法");
        }
        Room room = roomFacade.findOne(householdVO.getRoomId());
        householdVO.setRoomName(room.getName());
        // 暂时不考虑分区
        Building building = buildingFacade.findOne(room.getBuildingId());
        householdVO.setRoomLocation(building == null ? "" : building.getName() + room.getName());
        householdVO.setCommunityId(SessionUtil.getCommunityId());
        householdVO.setBuildingId(room.getBuildingId());
        List<Household> list = householdFacade.saveHouseholds(householdVO);
        // 发送短信邀请业主
        if (sendMsg) {
            Community community = communityFacade.findOne(householdVO.getCommunityId());
            Property property = propertyFacade.findByCommunityId(householdVO.getCommunityId());
            MessageParam.Params params = new MessageParam.Params();
            params.setPhone(householdVO.getPhone());
            params.setCompany(property.getName());
            params.setCommunity(community.getName());
            params.setHouse(householdVO.getRoomLocation());
            // 给住户端发送短信，所以用住户端的签名
            Sign sign = systemFacade.getSignByClientAndPartner(ClientType.HOUSEHOLD.value(),
                                                               SessionUtil.getAppSubject().getPartner());
            if (sign == null) {
                throw SIGN_NAME_INVALID;
            }
            if (StringUtil.isBlank(sign.getAppName())) {
                throw APP_NAME_INVALID;
            }
            params.setAppName(sign.getAppName());
            sendMsg(SmsTempletType.SMS100018.getKey(), householdVO.getPhone(), sign.getName(), params);
        }
        // 需要校验业主是否已经注册
        Household owner = list.stream()
                              .filter(h -> RelationshipType.OWNER.KEY.equals(h.getRelationship()))
                              .findFirst()
                              .get();
        User user = userFacade.findByPhone(owner.getPhone());
        if (user == null) {
            return ApiResult.ok();
        }

        AppSubject appSubject = SessionUtil.getAppSubject();

        ObjectId userId = user.getId();
        ClientUser clientUser = userFacade.getClientUserByClientAndPartnerAndUserId(ClientType.HOUSEHOLD.value(),
                                                                                    appSubject.getPartner(),
                                                                                    userId);
        if (clientUser == null) {
            return ApiResult.ok();
        }

        UserToRoom toGet = userToRoomFacade.findOwnerReviewingRecordByRoomIdAndUserId(owner.getRoomId(), userId);
        UserToRoom userToRoom = new UserToRoom();
        // 当前操作的管理员录入
        userToRoom.setAuditorId(SessionUtil.getTokenSubject().getUid());
        if (toGet != null) {
            userToRoom.setId(toGet.getId());
        } else {
            BeanUtils.copyProperties(owner, userToRoom);
            userToRoom.setId(null);
            userToRoom.setUserId(userId);
            userToRoom.setProprietorId(userId);
            userToRoom.setName(owner.getUserName());
        }
        userToRoom = userToRoomFacade.upsertAuthOwnerRecord(appSubject.getPartner(), userToRoom);
        if (userToRoom != null) {
            Household toUpdate = new Household();
            toUpdate.setId(owner.getId());
            toUpdate.setUserId(userId);
            toUpdate.setActivated(true);
            householdFacade.modifyHousehold(toUpdate);
            updateHouseholdNumAndCheckUserRealInfo(userToRoom);
        }
        return ApiResult.ok();
    }

    /**
     * @param household
     * @return
     */
    @PostMapping(name = "编辑住户档案", path = "/households/{householdId}/modify")
    @Authorization
    public ApiResult modifyHousehold(@PathVariable("householdId") ObjectId householdId,
                                     @RequestBody Household household) {
        household.setId(householdId);
        if (household != null && StringUtil.isNotNull(household.getIdentityCard()) &&
            !IDCardUtils.verifi(household.getIdentityCard())) {
            // 校验身份证格式
            return ApiResult.error(-1, "身份证号码输入不合法");
        }
        household.setActivated(null);
        householdFacade.modifyHousehold(household);
        return ApiResult.ok();
    }

    /**
     * @return
     */
    @GetMapping(name = "住户档案分页", path = "/households")
    @Authorization
    public ApiResult listHouseholds(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) ObjectId buildingId,
                                    @RequestParam(required = false) ObjectId roomId, String userName, String phone,
                                    String roomName, Integer relationship, Boolean activated) {
        HouseholdPageQuery query = new HouseholdPageQuery();
        query.setCommunityId(SessionUtil.getCommunityId());
        query.setPage(page);
        query.setSize(size);
        query.setBuildingId(buildingId);
        query.setRoomId(roomId);
        query.setUserName(userName);
        query.setPhone(phone);
        query.setRoomName(roomName);
        query.setRelationship(relationship);
        query.setActivated(activated);
        return ApiResult.ok(householdFacade.listHouseholds(query));
    }

    /**
     * @param householdId
     * @return
     */
    @GetMapping(name = "住户档案详细", path = "/households/{householdId}")
    @Authorization
    public ApiResult getHouseholdDetail(@PathVariable("householdId") ObjectId householdId) {
        return ApiResult.ok(householdFacade.getHouseholdDetail(householdId));
    }

    /**
     * @param roomId
     * @return
     */
    @GetMapping(name = "某房间住户档案详细", path = "/households/room/{roomId}")
    @Authorization
    public ApiResult listHouseholdsByRoomId(@PathVariable("roomId") ObjectId roomId) {
        Room room = null;
        try {
            room = roomFacade.findOne(roomId);
        } catch (BizException e) {
            log.error(roomId + "_查看房屋档案详细(获取房间面积)：" + e.getMsg());
        }
        HouseholdVO householdVO = householdFacade.findDetailByRoom(roomId);
        if (room != null) {
            householdVO.setRoomArea(room.getArea());
        }
        return ApiResult.ok(householdVO);
    }

    /**
     * @param roomId
     * @return
     */
    @GetMapping(name = "查看房间的业主档案", path = "/households/owner/{roomId}")
    @Authorization
    public ApiResult getOwnerHouseholdByRoomId(@PathVariable("roomId") ObjectId roomId) {
        return ApiResult.ok(householdFacade.findAuthOwnerByRoom(roomId));
    }

    /**
     * @param roomId
     * @return
     */
    @PostMapping(name = "在某房间新增家属档案", path = "/households/{roomId}/member/add")
    @Authorization
    public ApiResult addHouseholdMember(@PathVariable("roomId") ObjectId roomId,
                                        @RequestBody @Validated MemberDTO memberDTO) {
        List<Household> owners = householdFacade.findOwnerByRoomIds(Collections.singleton(roomId));
        if (owners.isEmpty()) {
            throw HOUSEHOLD_NOT_EXIST;
        }
        Household owner = owners.get(0);
        ObjectId communityId = SessionUtil.getCommunityId();
        Household household = new Household();
        BeanUtils.copyProperties(memberDTO, household);
        household.setCommunityId(communityId);
        household.setBuildingId(owner.getBuildingId());
        household.setRoomId(roomId);
        household.setRoomName(owner.getRoomName());
        household.setRoomLocation(owner.getRoomLocation());
        household.setActivated(false);
        return ApiResult.ok(householdFacade.saveHouseholds(Collections.singletonList(household)));
    }

    /**
     * @param roomId
     * @return
     */
    @PostMapping(name = "删除某房间住户档案", path = "/households/room/{roomId}/remove")
    @Authorization
    public ApiResult removeHouseholdByRoomId(@PathVariable("roomId") ObjectId roomId) {
        if (SessionUtil.getAppSubject().getClient() != ClientType.PROPERTY.value()) {
            throw AUTHENCATION_FAILD;
        }
        householdFacade.removeByRoomId(roomId);
        // 修改申请记录状态，并移除权限
        int update = userToRoomFacade.relieveOwner(SessionUtil.getAppSubject().getPartner(),
                                                   roomId,
                                                   SessionUtil.getTokenSubject().getUid());
        // 更新社区人口
        Community toUpdate = new Community();
        toUpdate.setHouseholdCnt(-update);
        toUpdate.setCheckInRoomCnt(-1);
        communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, SessionUtil.getCommunityId());
        return ApiResult.ok();
    }

    /**
     * @param householdId
     * @return
     */
    @PostMapping(name = "删除住户档案", path = "/households/member/{householdId}/remove")
    @Authorization
    @SendPush(scope = SendPush.Scope.COMMUNITY,
              clientTypes = {ClientType.HOUSEHOLD},
              point = PushPointEnum.UNBINDING_ROOM)
    public ApiResult removeHouseholdByHouseholdId(@PathVariable("householdId") ObjectId householdId) {
        Household household = householdFacade.removeByHouseholdId(householdId);
        // 未激活，则无需修改申请记录状态
        if (household == null || household.getActivated() == null || !household.getActivated()) {
            return ApiResult.ok();
        }

        AppSubject appSubject = SessionUtil.getAppSubject();

        // 修改申请记录状态，并移除权限
        if (RelationshipType.OWNER.KEY.equals(household.getRelationship())) {
            // 业主身份
            int update = userToRoomFacade.relieveOwner(appSubject.getPartner(),
                                                       household.getRoomId(),
                                                       SessionUtil.getTokenSubject().getUid());
            // 更新社区人口
            Community toUpdate = new Community();
            toUpdate.setHouseholdCnt(-update);
            toUpdate.setCheckInRoomCnt(-1);
            communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, SessionUtil.getCommunityId());
        } else {
            UserToRoom toGet = userToRoomFacade.findByCommunityIdAndUserIdAndRoomId(household.getCommunityId(),
                                                                                    household.getUserId(),
                                                                                    household.getRoomId());
            if (toGet != null) {
                UserToRoom toDelete = userToRoomFacade.deleteAuxiliary(appSubject.getPartner(),
                                                                       toGet.getId(),
                                                                       SessionUtil.getTokenSubject().getUid(),
                                                                       appSubject.getClient());
                // 更新社区人口
                Community toUpdate = new Community();
                toUpdate.setHouseholdCnt(-1);
                communityFacade.updateWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, toDelete.getCommunityId());
                Map<String, String> map = new HashMap();
                String title =
                        RelationshipType.getValueByKey(toDelete.getRelationship()) + toDelete.getName() + "已被解除" +
                        (toDelete.getRoomLocation() == null ? "" : toDelete.getRoomLocation()) + "的房屋认证";
                // 推送title
                map.put("title", title);
                // 社区名称
                map.put("communityName", communityFacade.findOne(toDelete.getCommunityId()).getName());
                PushTarget pushTarget = new PushTarget();
                pushTarget.setUserIds(Collections.singleton(toDelete.getProprietorId()));
                return WrapResult.create(ApiResult.ok(), toDelete, map, pushTarget);
            }
        }
        return ApiResult.ok();
    }

    /**
     * @return
     */
    @GetMapping(name = "住户档案列表", path = "/households/list")
    @Authorization
    public ApiResult listHouseholds(String roomId, String userId) {
        if (StringUtil.isBlank(roomId) && StringUtil.isBlank(userId)) {
            log.info("roomId and userId all null, return null !!!");
            return ApiResult.ok();
        }
        ObjectId rid = null;
        ObjectId uid = null;
        try {
            if (StringUtil.isNotBlank(roomId)) {
                rid = new ObjectId(roomId);
            }
            if (StringUtil.isNotBlank(userId)) {
                uid = new ObjectId(userId);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ApiResult.ok();
        }
        return ApiResult.ok(householdFacade.findByRoomIdOrUserId(rid, uid));
    }

    // =================================================【household end】===============================================

    // =================================================【userToProperty start】========================================

    /**
     * 查询物业人员详细
     */
    @GetMapping(name = "物业员工详情", path = "/property/{id}/detail")
    @Authorization
    public ApiResult getPropertyUser(@PathVariable ObjectId id) {
        UserToProperty userToProperty = userToPropertyFacade.findByIdAndCommunityId(id, SessionUtil.getCommunityId());
        if (userToProperty != null) {
            UserToPropertyResponse response = new UserToPropertyResponse();
            BeanUtils.copyProperties(userToProperty, response);
            return ApiResult.ok(response);
        }
        throw PROPERTY_NOT_EXIST;
    }

    /**
     * 创建社区物业人员
     *
     * @see UserController#createPropertyEmployee(EmployeeVO)
     * 拆分创建社区管理员及物业员工
     * @since 20190318
     */
    @PostMapping(name = "创建物业员工", path = "/property/create-user")
    @Authorization
    @Deprecated
    public ApiResult createPropertyUser(@RequestBody @Validated(EmployeeVO.Create.class) EmployeeVO employeeVO)
            throws Exception {
        employeeVO.setCommunityId(SessionUtil.getCommunityId());
        // 通过功能配置，代码层去除角色控制
        /*this.checkCreateUserPermission(SessionUtil.getAppSubject().getClient(),
                SessionUtil.getTokenSubject().getUid(), employeeVO.getCommunityId(), employeeVO.getPostCode());*/
        Community community = communityFacade.findOne(employeeVO.getCommunityId());
        if (community == null || community.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw COMMUNITY_NOT_EXISTS;
        }
        ObjectId companyId = SessionUtil.getCompanyId();
        if (companyId == null) {
            Property property = propertyFacade.findByCommunityId(community.getId());
            companyId = property.getId();
        }
        employeeVO.setPropertyId(companyId);
        User toGet = userFacade.findByPhone(employeeVO.getPhone());
        employeeVO.setUserId(toGet == null ? null : toGet.getId());
        boolean registered = toGet != null;
        AppSubject appSubject = SessionUtil.getAppSubject();
        if (registered) {
            ClientUser clientUser = userFacade.getClientUserByClientAndPartnerAndUserId(appSubject.getClient(),
                                                                                        appSubject.getPartner(),
                                                                                        toGet.getId());
            registered = clientUser != null;
        }
        employeeVO.setRegistered(registered);
        // 新增物业员工
        UserToProperty userToProperty = userToPropertyFacade.addEmployee(employeeVO,
                                                                         SessionUtil.getTokenSubject().getUid());
        if (userToProperty == null) {
            throw OPERATION_FAILURE;
        }

        if (registered) {
            // 注册 client-user, cm-user
            userToProperty.setUserId(toGet.getId());
            userToPropertyFacade.upsertClientUserAndCMUserForCreatePropertyUser(appSubject.getPartner(),
                                                                                userToProperty);
        } else {
            // 登记员工信息
            Registration registration = new Registration();
            registration.setPartner(appSubject.getPartner());
            registration.setCommunityId(employeeVO.getCommunityId());
            registration.setPhone(employeeVO.getPhone());
            registration.setEmployeeId(userToProperty.getId());
            registration.setRoles(Collections.singleton(employeeVO.getPostCode()));
            registrationFacade.addRegistration(registration);
        }
        // 发送短信邀请
        if (sendMsg) {
            MessageParam.Params params = new MessageParam.Params();
            params.setPhone(userToProperty.getPhone());
            params.setCompany(userToProperty.getPropertyName());
            params.setCommunity(community.getName());
            // 给住户端发送短信，所以用住户端的签名
            Sign sign = systemFacade.getSignByClientAndPartner(appSubject.getClient(), appSubject.getPartner());
            if (sign == null) {
                throw SIGN_NAME_INVALID;
            }
            if (StringUtil.isBlank(sign.getAppName())) {
                throw APP_NAME_INVALID;
            }
            params.setAppName(sign.getAppName());
            sendMsg(SmsTempletType.SMS100016.getKey(), userToProperty.getPhone(), sign.getName(), params);
        }
        UserToPropertyResponse response = new UserToPropertyResponse();
        BeanUtils.copyProperties(userToProperty, response);
        return ApiResult.ok(response);
    }

    /**
     * 创建社区管理员
     *
     * @see UserController#createPropertyEmployee(EmployeeVO)
     * @since 2019/05/27
     */
    @PostMapping(name = "创建社区管理员", path = "/property/create-cm-admin")
    @Authorization
    @Deprecated
    public ApiResult createCMAdmin(@RequestBody @Validated(EmployeeVO.Create.class) EmployeeVO employeeVO)
            throws Exception {
        return createEmployee(employeeVO);
    }

    /**
     * 创建物业员工
     */
    @PostMapping(name = "创建物业员工", path = "/property/create-employee")
    @Authorization
    public ApiResult createPropertyEmployee(@RequestBody @Validated(EmployeeVO.Create.class) EmployeeVO employeeVO)
            throws Exception {
        return createEmployee(employeeVO);
    }

    private ApiResult createEmployee(EmployeeVO employeeVO) throws Exception {
        employeeVO.setCommunityId(SessionUtil.getCommunityId());
        Community community = communityFacade.findOne(employeeVO.getCommunityId());
        if (community == null || community.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw COMMUNITY_NOT_EXISTS;
        }
        ObjectId companyId = SessionUtil.getCompanyId();
        if (companyId == null) {
            Property property = propertyFacade.findByCommunityId(community.getId());
            companyId = property.getId();
        }
        employeeVO.setPropertyId(companyId);
        User toGet = userFacade.findByPhone(employeeVO.getPhone());
        employeeVO.setUserId(toGet == null ? null : toGet.getId());
        boolean registered = toGet != null;
        AppSubject appSubject = SessionUtil.getAppSubject();
        if (registered) {
            ClientUser clientUser = userFacade.getClientUserByClientAndPartnerAndUserId(appSubject.getClient(),
                                                                                        appSubject.getPartner(),
                                                                                        toGet.getId());
            registered = clientUser != null;
        }
        employeeVO.setRegistered(registered);
        // 新增物业员工
        UserToProperty userToProperty = userToPropertyFacade.addEmployee(employeeVO,
                                                                         SessionUtil.getTokenSubject().getUid());

        if (userToProperty == null) {
            throw OPERATION_FAILURE;
        }

        if (registered) {
            // 注册 client-user, cm-user
            userToProperty.setUserId(toGet.getId());
            userToPropertyFacade.upsertClientUserAndCMUserForCreatePropertyUser(appSubject.getPartner(),
                                                                                userToProperty);
        } else {
            // 登记员工信息
            Registration registration = new Registration();
            registration.setPartner(appSubject.getPartner());
            registration.setCommunityId(employeeVO.getCommunityId());
            registration.setPhone(employeeVO.getPhone());
            registration.setEmployeeId(userToProperty.getId());
            registration.setRoles(Collections.singleton(employeeVO.getPostCode()));
            registrationFacade.addRegistration(registration);
        }
        // 发送短信邀请
        if (sendMsg) {
            MessageParam.Params params = new MessageParam.Params();
            params.setPhone(userToProperty.getPhone());
            params.setCompany(userToProperty.getPropertyName());
            params.setCommunity(community.getName());
            // 给住户端发送短信，所以用住户端的签名
            Sign sign = systemFacade.getSignByClientAndPartner(appSubject.getClient(), appSubject.getPartner());
            if (sign == null) {
                throw SIGN_NAME_INVALID;
            }
            if (StringUtil.isBlank(sign.getAppName())) {
                throw APP_NAME_INVALID;
            }
            params.setAppName(sign.getAppName());
            sendMsg(SmsTempletType.SMS100016.getKey(), userToProperty.getPhone(), sign.getName(), params);
        }
        UserToPropertyResponse response = new UserToPropertyResponse();
        BeanUtils.copyProperties(userToProperty, response);
        return ApiResult.ok(response);
    }

    /**
     * 社区分派物业人员，从企业人员列表拉取
     */
    @PostMapping(name = "社区从企业员工分派物业员工", path = "/property/allocate-employee")
    @Authorization
    public ApiResult allocationEmployee(@RequestBody @Validated Allocation allocation) throws Exception {
        allocation.setCommunityId(SessionUtil.getCommunityId());
        AppSubject appSubject = SessionUtil.getAppSubject();
        Community community = communityFacade.findOne(allocation.getCommunityId());
        if (community == null || community.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw COMMUNITY_NOT_EXISTS;
        }
        // 分派物业人员（更新企业员工的角色）
        UserToProperty userToProperty = userToPropertyFacade.allocationEmployee(allocation);
        if (userToProperty == null) {
            throw OPERATION_FAILURE;
        }
        if (userToProperty.getUserId() != null) {
            // 更新 client-user, cm-user
            userToPropertyFacade.upsertClientUserAndCMUserForAllocation(appSubject.getPartner(), userToProperty);
        } else {
            // 社区登记物业人员
            Registration registration = new Registration();
            registration.setPartner(appSubject.getPartner());
            registration.setCommunityId(userToProperty.getCommunityId());
            registration.setEmployeeId(userToProperty.getId());
            registration.setPhone(userToProperty.getPhone());
            registration.setRoles(userToProperty.getPostCode());
            registrationFacade.addRegistration(registration);
        }
        // 发送短信邀请
        if (sendMsg) {
            MessageParam.Params params = new MessageParam.Params();
            params.setPhone(userToProperty.getPhone());
            params.setCompany(userToProperty.getPropertyName());
            params.setCommunity(community.getName());
            Sign sign = systemFacade.getSignByClientAndPartner(appSubject.getClient(), appSubject.getPartner());
            if (sign == null) {
                throw SIGN_NAME_INVALID;
            }
            if (StringUtil.isBlank(sign.getAppName())) {
                throw APP_NAME_INVALID;
            }
            params.setAppName(sign.getAppName());
            sendMsg(SmsTempletType.SMS100016.getKey(), userToProperty.getPhone(), sign.getName(), params);
        }
        UserToPropertyResponse response = new UserToPropertyResponse();
        BeanUtils.copyProperties(userToProperty, response);
        return ApiResult.ok(response);
    }

    /**
     * 修改社区物业人员
     */
    @PostMapping(name = "编辑物业员工", path = "/property/update-user")
    @Authorization
    public ApiResult updatePropertyUser(@RequestBody @Validated(EmployeeVO.Modify.class) EmployeeVO employeeVO) {
        employeeVO.setCommunityId(SessionUtil.getCommunityId());
        UserToProperty userToProperty = userToPropertyFacade.modifyEmployee(employeeVO);
        UserToPropertyResponse response = new UserToPropertyResponse();
        BeanUtils.copyProperties(userToProperty, response);
        return ApiResult.ok(response);
    }

    /**
     * 根据岗位查找物业人员
     *
     * @param communityId
     * @param postCode
     * @return
     */
    @GetMapping(name = "社区员工列表", path = "/property/{communityId}/user-list")
    @Authorization
    public ApiResult getPropertyByPostCode(@PathVariable("communityId") ObjectId communityId, String postCode) {
        EmployeeRequest request = new EmployeeRequest();
        request.setCommunityId(communityId);
        request.setCompanyId(SessionUtil.getCompanyId());
        request.setPartner(SessionUtil.getAppSubject().getPartner());
        if (StringUtil.isNotBlank(postCode)) {
            request.setRoles(Collections.singleton(postCode));
        }
        List<UserToProperty> userToPropertyList = userToPropertyFacade.listEmployees(request);
        return ApiResult.ok(userToPropertyList);
    }

    /**
     * 根据岗位分页查找物业人员
     *
     * @param employeeVO
     * @return
     */
    @PostMapping(name = "某岗位员工分页", path = "/property/employee-page")
    @Authorization
    public ApiResult employeePage(@RequestBody EmployeeVO employeeVO, @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        employeeVO.setCommunityId(SessionUtil.getCommunityId());
        employeeVO.setPropertyId(SessionUtil.getCompanyId());
        Page<UserToProperty> pageList = userToPropertyFacade.findPageByCommunityIdAndUserToProperty(employeeVO,
                                                                                                    SessionUtil.getAppSubject()
                                                                                                               .getPartner(),
                                                                                                    page,
                                                                                                    size);
        return ApiResult.ok(pageList);
    }

    /**
     * 根据多岗位查找物业人员列表
     *
     * @param idsRequestVO
     * @return
     */
    @PostMapping(name = "查询多岗位员工列表", path = "/property/employees-list")
    @Authorization
    public ApiResult listEmployeesByRoles(@RequestBody IdsRequestVO idsRequestVO) {
        if (idsRequestVO == null || CollectionUtils.isEmpty(idsRequestVO.getIds())) {
            return ApiResult.ok();
        }
        List<UserToProperty> employees = userToPropertyFacade.listEmployeesByCommunityIdAndCompanyIdAndRoles(SessionUtil
                                                                                                                     .getCommunityId(),
                                                                                                             SessionUtil
                                                                                                                     .getCompanyId(),
                                                                                                             idsRequestVO
                                                                                                                     .getIds());
        return ApiResult.ok(employees);
    }

    /**
     * 查询所有岗位
     *
     * @param communityId
     * @return
     * @see CompanyController#listRoles(String, Integer, Integer)
     * @since 2019/05/24
     */
    @GetMapping(name = "某社区岗位列表", path = "/property/{communityId}/post-list")
    @Authorization
    @Deprecated
    public ApiResult getPostListByCommunityId(@PathVariable("communityId") ObjectId communityId) {
        List<Role> lists = new ArrayList<>();

        for (RoleType roleType : RoleType.values()) {
            // 企业管理员，住户不返回前端物业人员岗位展示
            if (roleType.key() == 5 || roleType.key() == 7) {
                continue;
            }
            Role role = new Role();
            role.setKey(roleType.name());
            role.setName(roleType.value());
            lists.add(role);
        }
        return ApiResult.ok(lists);
    }

    /**
     * 注销物业人员
     *
     * @param id
     * @return
     */
    @GetMapping(name = "注销物业员工", path = "/property/{id}/delete")
    @Authorization
    public ApiResult deleteEmployee(@PathVariable("id") ObjectId id) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        boolean result = userToPropertyFacade.deleteEmployee(appSubject.getPartner(), id, SessionUtil.getCommunityId());
        if (!result) {
            return ApiResult.error(-1, "注销物业人员失败");
        }
        return ApiResult.ok();
    }

    /**
     * 获取物业员工档案
     *
     * @return
     */
    @GetMapping(name = "物业员工档案详情", path = "/property/detail")
    @Authorization
    public ApiResult getUserToPropertyDetail() {
        UserToProperty userToProperty =
         userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(SessionUtil.getTokenSubject()
                                                                                                               .getUid(),
                                                                                                    SessionUtil.getCommunityId(),
                                                                                                    SessionUtil.getCompanyId());
        if (userToProperty == null) {
            ApiResult.error(-1, "员工档案不存在");
        }
        UserToPropertyResponse response = new UserToPropertyResponse();
        BeanUtils.copyProperties(userToProperty, response);
        return ApiResult.ok(response);
    }

    /**
     * 获取企业员工列表
     *
     * @return
     */
    @GetMapping(name = "物业公司对应企业中已注册员工分页", path = "/property/employees")
    @Authorization
    public ApiResult getEmployees(@RequestParam(defaultValue = "10") Integer size,
                                  @RequestParam(defaultValue = "1") Integer direction,
                                  @RequestParam(required = false) ObjectId id, String keyword) {
        List<UserToProperty> userToPropertyList = userToPropertyFacade.incrementEmployees(SessionUtil.getCommunityId(),
                                                                                          keyword,
                                                                                          id,
                                                                                          direction,
                                                                                          size);
        return ApiResult.ok(userToPropertyList);
    }

    // =================================================【userToProperty end】==========================================

    // =================================================【card start】==================================================

    /**
     * 用于兼容旧版APP 申请实体卡 二维码
     *
     * @param cardVO
     * @return
     */
    @PostMapping(name = "申请蓝牙卡、IC卡、二维码", path = "/card/add")
    @Authorization
    public ApiResult applyCard(@Validated(CardVO.AddOwner.class) @RequestBody CardVO cardVO) {
        Integer client = SessionUtil.getAppSubject().getClient();
        if (cardVO.getKeyType() == CertificateType.PHONE_MAC.KEY) {
            return ApiResult.error(-1, "虚拟卡只能由后台生成。");
        }

        if (cardVO.getKeyType() == CertificateType.IC_CARD.KEY ||
            cardVO.getKeyType() == CertificateType.BLUETOOTH_CARD.KEY) {
            // 实体卡卡号不能为空
            if (StringUtil.isBlank(cardVO.getKeyNo())) {
                return ApiResult.error(-1, "卡号不能为空");
            }
            // 实体卡都给50年
            Date startDate = new Date();
            int processTime = (int) DateUtils.secondsBetween(startDate, DateUtils.addYear(startDate, 50));
            cardVO.setProcessTime(processTime);
        }

        Set<String> roomLocations = new HashSet<>();
        Set<ObjectId> buildingIds = new HashSet<>();
        Card card = null;

        // 住户申请实体卡
        if (client == ClientType.HOUSEHOLD.value() && (cardVO.getKeyType() == CertificateType.IC_CARD.KEY ||
                                                       cardVO.getKeyType() == CertificateType.BLUETOOTH_CARD.KEY)) {
            Set<ObjectId> roomIds = new HashSet<>();
            UserToRoom qo = new UserToRoom();
            qo.setUserId(SessionUtil.getTokenSubject().getUid());
            qo.setCommunityId(cardVO.getCommunityId());
            List<UserToRoom> userToRooms = userToRoomFacade.getRoomsByUserId(qo);
            userToRooms = userToRooms.stream()
                                     .filter(u -> u.getAuditStatus() == AuditStatusType.REVIEWED.getType())
                                     .collect(Collectors.toList());
            userToRooms.forEach(u -> roomIds.add(u.getRoomId()));
            List<Room> rooms = roomFacade.findRoomsByIds(roomIds);
            buildingIds = rooms.stream().map(Room::getBuildingId).collect(Collectors.toSet());
            userToRooms.forEach(u -> roomLocations.add(u.getRoomLocation()));
            cardVO.setRoomName(roomLocations);
            cardVO.setName(SessionUtil.getCurrentUser().getName());

            cardVO.setUserId(SessionUtil.getTokenSubject().getUid());
            log.info("apply elevator permission for card...");
            card = cardFacade.applyCardForElevatorPermissionBy(cardVO, rooms);
        }

        // 住户申请二维码
        if (client == ClientType.HOUSEHOLD.value() && cardVO.getKeyType() == CertificateType.QR_CODE.KEY) {
            List<Room> rooms = roomFacade.findRoomsByIds(cardVO.getRooms());

            buildingIds = rooms.stream().map(Room::getBuildingId).collect(Collectors.toSet());
            card        = cardFacade.applyQRCardForElevatorByRooms(SessionUtil.getTokenSubject().getUid(),
                                                                   cardVO,
                                                                   rooms);
        }

        // 物业申请实体卡
        if (client == ClientType.PROPERTY.value()) {
            UserToProperty userToProperty = userToPropertyFacade.findByIdAndCommunityId(cardVO.getUserToPropertyId(),
                                                                                        SessionUtil.getCommunityId());
            if (userToProperty == null) {
                throw PROPERTY_NOT_EXIST;
            }
            buildingIds = userToProperty.getBuildingIds();
            if (buildingIds.size() == 0) {
                return ApiResult.error(-1, "物业人员需要先授权职能区域");
            }

            List<Building> buildings = buildingFacade.findByIds(buildingIds);
            buildings.forEach(b -> roomLocations.add(b.getName()));

            card = cardFacade.applyCardForElevatorPermissionBy(cardVO, buildingIds, userToProperty, roomLocations);
        }

        // 申请卡片成功后申请门禁设备
        Set<ObjectId> finalBuildingIds = buildingIds;
        Card finalCard = card;
        CompletableFuture.runAsync(() -> {
            if (finalCard != null) {
                cardVO.setKeyId(finalCard.getKeyId());
                cardVO.setKeyNo(finalCard.getKeyNo());
                log.info("apply door permission for card...");
                updateDoorPermission(cardVO, finalBuildingIds, finalCard.getCommunityId());
            }
        });
        return ApiResult.ok(card);
    }

    /**
     * 物业给住户发放实体(新)
     *
     * @param vo
     * @return
     */
    @PostMapping(name = "物业发放实体卡", path = "/card/household/allocate")
    @Authorization
    public ApiResult allocateHouseholdPhysicalCard(@Validated @RequestBody HouseholdCardVO vo) {
        if (vo.getHouseholdId() == null && StringUtil.isBlank(vo.getUserName())) {
            throw HOUSEHOLD_MISSING_PARAM;
        }
        CardVO cardVO = new CardVO();
        BeanUtils.copyProperties(vo, cardVO);
        cardVO.setCommunityId(SessionUtil.getCommunityId());
        if (vo.getHouseholdId() != null) {
            Household toGet = householdFacade.getHouseholdDetail(vo.getHouseholdId());
            if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
                throw HOUSEHOLD_NOT_EXIST;
            }
            cardVO.setUserId(toGet.getUserId());
            cardVO.setName(toGet.getUserName());
            cardVO.setPhone(toGet.getPhone());
            // 验证设备许可
            cardVO.setRooms(new HashSet<>(this.checkDeviceLicense(cardVO.getUserId(),
                                                                  Collections.singleton(vo.getRoomId()))));
        } else {
            cardVO.setRooms(Collections.singleton(vo.getRoomId()));
            cardVO.setName(vo.getUserName());
        }
        return householdPhysicalCardApply(cardVO);
    }

    /**
     * 住户申请实体卡（旧）
     *
     * @param cardVO
     * @return
     * @since 2018-12-08 by decai.liu
     */
    @Deprecated
    @PostMapping(name = "住户申请蓝牙卡、IC卡(旧)", path = "/card/household-apply")
    @Authorization
    public ApiResult applyHouseholdPhysicalCard(@Validated(CardVO.HouseholdApply.class) @RequestBody CardVO cardVO) {
        cardVO.setCommunityId(SessionUtil.getCommunityId());
        cardVO.setRooms(new HashSet<>(this.checkDeviceLicense(cardVO.getUserId(), cardVO.getRooms())));
        return householdPhysicalCardApply(cardVO);
    }

    /**
     * 住户申请实体卡
     *
     * @param cardVO
     * @return
     */
    private ApiResult householdPhysicalCardApply(CardVO cardVO) {
        if (CollectionUtils.isNotEmpty(cardVO.getRooms())) {
            try {
                Room room = roomFacade.findOne(cardVO.getRooms().iterator().next());
                Building building = buildingFacade.findOne(room.getBuildingId());
                cardVO.setBuildingIds(Collections.singleton(room.getBuildingId()));
                cardVO.setRoomName(Collections.singleton(building.getName() + room.getName()));
            } catch (Exception e) {
                log.error("获取楼栋房屋详情异常：", e);
            }
        }
        // 离线
        if (!checkCommunityOnline(cardVO.getCommunityId())) {
            cardFacade.applyOffLineICCard(cardVO);
            return ApiResult.ok();
        }
        cardVO = cardFacade.applyHouseholdPhysicalCard(cardVO);

        buildFloorInCardVO(cardVO);

        List<Door> doors = doorFacade.getBuildingAndCommunityDoor(cardVO.getBuildingIds(), cardVO.getCommunityId());

        CardVO finalCardVO = cardVO;
        CompletableFuture.runAsync(() -> cardFacade.updateCardElevatorPermission(finalCardVO));
        if (doors == null || doors.size() == 0) {
            return ApiResult.ok();
        }
        CompletableFuture.runAsync(() -> cardFacade.updateCardDoorPermission(finalCardVO, doors));
        CompletableFuture.runAsync(() -> doorFacade.applyFreeViewUserCard(finalCardVO,
                                                                          doors,
                                                                          ClientType.HOUSEHOLD.value()));
        return ApiResult.ok();
    }

    /**
     * 物业人员申请实体卡
     *
     * @param cardVO
     * @return
     */
    @PostMapping(name = "物业人员申请蓝牙卡、IC卡", path = "/card/property-apply")
    @Authorization
    public ApiResult applyPropertyPhysicalCard(@Validated(CardVO.PropertyApply.class) @RequestBody CardVO cardVO) {
        cardVO.setCommunityId(SessionUtil.getCommunityId());
        if (CollectionUtils.isNotEmpty(cardVO.getRooms())) {
            try {
                Room room = roomFacade.findOne(cardVO.getRooms().iterator().next());
                Building building = buildingFacade.findOne(room.getBuildingId());
                cardVO.setBuildingIds(Collections.singleton(room.getBuildingId()));
                cardVO.setRoomName(Collections.singleton(building.getName() + room.getName()));
            } catch (Exception e) {
                log.error("获取楼栋房屋详情异常：", e);
            }
        }
        // 离线
        if (!checkCommunityOnline(cardVO.getCommunityId())) {
            cardFacade.applyOffLineICCard(cardVO);
            return ApiResult.ok();
        }

        cardVO = cardFacade.applyPropertyPhysicalCard(cardVO);

        List<Door> doors = doorFacade.getBuildingAndCommunityDoor(cardVO.getBuildingIds(), cardVO.getCommunityId());

        CardVO finalCardVO = cardVO;
        CompletableFuture.runAsync(() -> cardFacade.updateCardElevatorPermission(finalCardVO));

        if (CollectionUtils.isEmpty(doors)) {
            return ApiResult.ok();
        }

        CompletableFuture.runAsync(() -> cardFacade.updateCardDoorPermission(finalCardVO, doors));

        CompletableFuture.runAsync(() -> doorFacade.applyFreeViewUserCard(finalCardVO,
                                                                          doors,
                                                                          ClientType.PROPERTY.value()));

        return ApiResult.ok();
    }

    /**
     * 验证社区配置是否在线
     *
     * @param communityId
     * @return
     */
    private boolean checkCommunityOnline(ObjectId communityId) {
        Community community = communityFacade.findOne(communityId);
        BroadcastSchema broadcastSchema = community.getBroadcastSchema();
        boolean online = true;
        if (broadcastSchema != null && broadcastSchema.getDeviceProtocols() != null) {
            DeviceSchema deviceSchema = broadcastSchema.getDeviceProtocols()
                                                       .stream()
                                                       .filter(ds -> ProtocolTypeEnum.OFFLINE.value()
                                                                                             .equals(ds.getProtocolType()))
                                                       .findFirst()
                                                       .orElse(null);
            if (deviceSchema != null) {
                online = false;
            }
        }
        return online;
    }

    /**
     * 住户申请二维码
     *
     * @param cardVO
     * @return
     */
    @PostMapping(name = "住户申请访客通行", path = "/card/QRCode-apply")
    @Authorization
    public ApiResult applyHouseholdQRCard(@Validated(CardVO.QRCodeApply.class) @RequestBody CardVO cardVO) {
        UserVO userVO = SessionUtil.getCurrentUser();
        cardVO.setUserId(userVO.getId());
        cardVO.setName(userVO.getName());
        cardVO.setKeyType(CertificateType.QR_CODE.KEY);
        cardVO.setCommunityId(SessionUtil.getCommunityId());
        ObjectId roomId = cardVO.getRooms().iterator().next();

        // 验证是否有设备许可
        this.checkDeviceLicense(cardVO.getUserId(), Collections.singletonList(roomId));
        cardVO = cardFacade.applyHouseholdQRCard(cardVO);

        if (cardVO == null) {
            return ApiResult.error(-1, "二维码正在申请中，请稍候");
        }
        // 离线
        if (!checkCommunityOnline(cardVO.getCommunityId())) {
            // 生成二维码离线协议key
            String protocolKey = protocolFacade.encodeProtocol4Visitor(cardVO.getCommunityId(),
                                                                       cardVO.getUserId(),
                                                                       cardVO.getExpireAt(),
                                                                       roomId);
            if (StringUtil.isNotNull(protocolKey)) {
                Card toUpdate = cardFacade.updateCardForProtocol(cardVO.getCardId(), protocolKey);
                return ApiResult.ok(toUpdate);
            }
            return ApiResult.error(-1, "二维码正在申请中，请稍候");
        }

        buildFloorInCardVO(cardVO);

        List<Door> doors = doorFacade.getBuildingAndCommunityDoor(cardVO.getBuildingIds(), cardVO.getCommunityId());

        // 组装前端所需的返回参数
        Card card = new Card();
        card.setId(cardVO.getCardId());
        card.setName(cardVO.getName());
        card.setKeyId(cardVO.getKeyId());
        card.setProcessTime(cardVO.getExpireAt());
        card.setKeyNo(cardVO.getKeyNo());
        card.setKeyType(CertificateType.QR_CODE.KEY);
        card.setUseTimes(0);
        card.setUserId(cardVO.getUserId());
        card.setRoomName(cardVO.getRoomName());
        card.setValidState(CardStatusType.VALID.KEY);
        card.setCommunityId(cardVO.getCommunityId());
        card.setStartDate(new Date());
        card.setEndDate(card.getProcessTime());
        card.setDataStatus(DataStatusType.VALID.KEY);

        Card newCard = cardFacade.applyQRCardForElevator(cardVO);
        cardVO.setKeyNo(newCard.getKeyNo());
        CardVO finalCardVO = cardVO;
        if (CollectionUtils.isEmpty(doors)) {
            return ApiResult.ok(card);
        }

        long kangtuDoorNum = doors.stream()
                                  .filter(door -> door.getBrandNo() == ManufactureType.KANGTU_DOOR.KEY)
                                  .count();
        if (kangtuDoorNum > 0) {
            CompletableFuture.runAsync(() -> cardFacade.updateCardDoorPermission(finalCardVO, doors));
        }

        List<Door> freeViewDoors = doors.stream()
                                        .filter(door -> door.getBrandNo() == ManufactureType.FREEVIEW_DOOR.KEY)
                                        .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(freeViewDoors)) {
            CompletableFuture.runAsync(() -> doorFacade.applyFreeViewSecretCode(finalCardVO, freeViewDoors));
        }

        return ApiResult.ok(card);
    }

    private CardVO buildFloorInCardVO(CardVO cardVO) {
        List<Room> roomList = roomFacade.findRoomsByIds(cardVO.getUserToRooms()
                                                              .stream()
                                                              .map(UserToRoom::getRoomId)
                                                              .collect(Collectors.toSet()));

        // 之所以需要重复校验是因为离线卡申请只有一间房，在线写卡是多房间写卡
        this.checkDeviceLicense(cardVO.getUserId(), roomList.stream().map(Room::getId).collect(Collectors.toList()));

        Map<ObjectId, Room> map = roomList.stream().collect(Collectors.toMap(Room::getId, r -> r));
        // 一栋楼对应一个FloorVO
        Map<ObjectId, FloorVO> floorVOMap = new HashMap<>();

        Set<FloorVO> floorVOS = new HashSet<>(roomList.size());
        cardVO.setBuilds(floorVOS);

        // 根据房屋认证组装参数
        for (UserToRoom u : cardVO.getUserToRooms()) {
            if (floorVOMap.get(u.getBuildingId()) == null) {
                FloorVO floorVO = new FloorVO(u.getBuildingId().toString());
                floorVO.setFloors(new HashSet<>());
                floorVO.setSubFloors(new HashSet<>());
                floorVOMap.put(u.getBuildingId(), floorVO);
                floorVOS.add(floorVOMap.get(u.getBuildingId()));
            }

            // 如果该房间没有主副门设置
            // 主副门都授权
            if (map.get(u.getRoomId()).getMainDoor() == null && map.get(u.getRoomId()).getSubDoor() == null) {
                floorVOMap.get(u.getBuildingId()).getFloors().add(map.get(u.getRoomId()).getFloorCode());
                floorVOMap.get(u.getBuildingId()).getSubFloors().add(map.get(u.getRoomId()).getFloorCode());
                continue;
            }

            // 授权主门
            if (map.get(u.getRoomId()).getMainDoor() != null && map.get(u.getRoomId()).getMainDoor()) {
                floorVOMap.get(u.getBuildingId()).getFloors().add(map.get(u.getRoomId()).getFloorCode());
            }

            // 授权副门
            if (map.get(u.getRoomId()).getSubDoor() != null && map.get(u.getRoomId()).getSubDoor()) {
                floorVOMap.get(u.getBuildingId()).getSubFloors().add(map.get(u.getRoomId()).getFloorCode());
            }
        }

        return cardVO;
    }

    /**
     * 社区凭证根据楼层授权设备
     *
     * @param cardVO
     * @return
     */
    @PostMapping(name = "为卡片生成楼层凭证(增加电梯和门禁授权)", path = "/card/floor/add")
    @Authorization
    public ApiResult applyCardFloor(@RequestBody CardVO cardVO) {
        cardVO.setUserId(SessionUtil.getTokenSubject().getUid());
        List<UserToRoom> userToRooms = null;
        Set<String> roomLocations = new HashSet<>();

        if (cardVO.getKeyType() == CertificateType.BLUETOOTH_CARD.KEY ||
            cardVO.getKeyType() == CertificateType.IC_CARD.KEY) {
            // 验证房屋认证
            userToRooms = userToRoomFacade.findByUserIdAndRooms(SessionUtil.getTokenSubject().getUid(),
                                                                cardVO.getRooms());

            if (CollectionUtils.isEmpty(userToRooms)) {
                return ApiResult.error(-1, "缺少当前房屋的房屋认证，请联系管理员。");
            }
        }

        List<Room> rooms = roomFacade.findRoomsByIds(cardVO.getRooms());
        Room one = rooms.get(0);
        cardVO.setCommunityId(one.getCommunityId());
        // 汇总需要门禁的楼栋和有效期
        Set<ObjectId> buildingIds = new HashSet<>();
        rooms.forEach(r -> buildingIds.add(r.getBuildingId()));
        // 更新卡片信息
        boolean flag = cardFacade.updateCardForElevatorPermissionBy(cardVO, rooms);

        CompletableFuture.runAsync(() -> {
            log.info("update door permission...");
            updateDoorPermission(cardVO, buildingIds, one.getCommunityId());
        });

        List<UserToRoom> finalUserToRooms = userToRooms;
        CompletableFuture.runAsync(() -> {
            if (CollectionUtils.isNotEmpty(finalUserToRooms)) {
                // 拿到本次申请的房间的roomLocation来查有没有该房间
                finalUserToRooms.forEach(u -> roomLocations.add(u.getRoomLocation()));
                cardFacade.updateCardRoomNameByRoomLocation(cardVO, roomLocations);
            }
        });

        return flag ? ApiResult.ok() : ApiResult.error(1, "编辑凭证权限失败");
    }

    /**
     * 社区凭证添加楼栋的授权
     *
     * @param cardVO
     * @return
     */
    @PostMapping(name = "为卡片生成楼栋凭证(增加电梯和门禁授权)", path = "/card/building/add")
    @Authorization
    public ApiResult applyCardBuilding(
            @Validated(CardVO.UpdatePermissionWithBuilding.class) @RequestBody CardVO cardVO) {
        cardVO.setUserId(SessionUtil.getTokenSubject().getUid());
        cardVO.setCommunityId(SessionUtil.getCommunityId());
        Set<ObjectId> buildingIds = cardVO.getBuildingIds();
        CardVO finalCardVo = cardVO;
        Set<String> roomLocations = new HashSet<>();

        cardVO = cardFacade.updateCardElevatorPermissionFrom(cardVO, cardVO.getBuildingIds());


        CompletableFuture.runAsync(() -> {
            log.info("update door permission...");
            updateDoorPermission(finalCardVo, finalCardVo.getBuildingIds(), finalCardVo.getCommunityId());
        });

        CompletableFuture.runAsync(() -> {
            List<Building> buildings = buildingFacade.findByIds(buildingIds);
            // 拿到本次申请的房间的roomLocation来查有没有该房间
            buildings.forEach(b -> roomLocations.add(b.getName()));
            cardFacade.updateCardRoomNameByRoomLocation(finalCardVo, roomLocations);
        });

        return cardVO != null ? ApiResult.ok() : ApiResult.error(1, "编辑凭证权限失败");
    }

    /**
     * 删除凭证指定房屋相关设备权限(业主)
     *
     * @param cardVO
     * @return
     */
    @PostMapping(name = "删除卡片设备权限", path = "/card/floor/delete")
    @Authorization
    public ApiResult deleteCardFloor(@Validated(CardVO.DeleteFloor.class) @RequestBody CardVO cardVO) {
        Set<ObjectId> buildings = new HashSet<>();
        List<Room> rooms = roomFacade.findRoomsByIds(cardVO.getRooms());
        cardVO.setCommunityId(rooms.get(0).getCommunityId());

        // 汇总需要门禁的楼栋和有效期
        Set<ObjectId> buildingIds = new HashSet<>();
        rooms.forEach(r -> buildingIds.add(r.getBuildingId()));

        // 更新卡片信息
        cardFacade.deleteCardForElevatorPermissionBy(cardVO, rooms);

        // 删除已经没有电梯权限所在楼栋的门禁设备
        Card card = new Card();
        card.setKeyType(cardVO.getKeyType());
        card.setKeyId(cardVO.getKeyId());

        this.deleteDoorWithoutPermission(cardVO, buildings, card, rooms.get(0).getCommunityId());

        // 如果需要删除权限的是实体卡 需要减少card的roomName中的地址列表
        if (cardVO.getKeyType() == CertificateType.BLUETOOTH_CARD.KEY ||
            cardVO.getKeyType() == CertificateType.IC_CARD.KEY) {
            // 验证房屋认证
            List<UserToRoom> userToRooms = userToRoomFacade.findByUserIdAndRooms(SessionUtil.getTokenSubject().getUid(),
                                                                                 cardVO.getRooms());
            if (Objects.isNull(userToRooms) || userToRooms.size() == 0) {
                return ApiResult.error(-1, "缺少当前房屋的房屋认证，请联系管理员。");
            }

            // 拿到本次申请的房间的roomLocation来查有没有该房间
            Set<String> roomLocations = new HashSet<>();
            userToRooms.forEach(u -> roomLocations.add(u.getRoomLocation()));
            card.setRoomName(roomLocations);
            card.setKeyNo(cardVO.getKeyNo());
            cardFacade.pullAllCardRoomNameByKeyIdAndKeyNo(card);
        }

        return ApiResult.ok();
    }

    /**
     * 凭证删除门禁
     */
    @PostMapping(name = "删除卡片门禁权限", path = "/card/door/delete")
    @Authorization
    public ApiResult deleteCardDoor(@Validated(CardVO.DeleteDoors.class) @RequestBody CardVO cardVO) {
        List<Door> doors = doorFacade.getDoorsInIds(cardVO.getDoors());
        boolean flag = cardFacade.deleteCardDoorPermission(cardVO, doors);
        return ApiResult.ok(flag);
    }

    /**
     * 物业打开用户设备权限
     */
    @PostMapping(name = "用户设备权限设为可用", path = "/device/license/{householdId}/enable")
    @Authorization
    public ApiResult openCardPermission(@PathVariable ObjectId householdId) {
        List<Card> cards = cardFacade.findUsefulCardByHouseholdId(householdId);

        householdFacade.updateDeviceLicense(householdId, cards, true);

        return ApiResult.ok();
    }

    /**
     * 物业关闭用户设备权限
     */
    @PostMapping(name = "用户设备权限设为不可用", path = "/device/license/{householdId}/disable")
    @Authorization
    public ApiResult cancelCardPermission(@PathVariable ObjectId householdId) {
        List<Card> cards = cardFacade.findUsefulCardByHouseholdId(householdId);

        householdFacade.updateDeviceLicense(householdId, cards, false);

        return ApiResult.ok();
    }

    /**
     * 通过社区和用户获取凭证信息（app）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "某用户在某社区的卡片分页", path = "/card/get/list")
    @Authorization(verifyApi = false)
    public ApiResult findUserCard(@Validated(Card.GetCardInfo.class) @RequestBody Card entity,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size, @RequestHeader("OS") Integer os) {
        entity.setCommunityId(SessionUtil.getCommunityId());
        Page<Card> cardPage = cardFacade.findUserCardInCommunityByKeyType(entity, page, size);

        // 离线协议 20180816 decai.liu
        cardPage.getRecords()
                .stream()
                .filter(card -> card.getKeyType() == CertificateType.PHONE_MAC.KEY)
                .forEach(card -> {
                    if (card != null) {
                        Integer client = SessionUtil.getAppSubject().getClient();
                        String key = null;
                        if (client == ClientType.HOUSEHOLD.value()) {
                            key = protocolFacade.encodeProtocol4Room(card.getCommunityId(), card.getUserId(), os);
                        } else if (client == ClientType.PROPERTY.value()) {
                            key = protocolFacade.encodeProtocol4Property(card.getCommunityId(), os);
                        }
                        card.setProtocolKey(key);
                    }
                });

        return ApiResult.ok(cardPage);
    }

    /**
     * 卡片管理（app）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "实体卡列表", path = "/card/physical/list")
    @Authorization
    public ApiResult findUserPhysicalCard(@RequestBody Card entity) {
        entity.setCommunityId(SessionUtil.getCommunityId());
        entity.setUserId(SessionUtil.getTokenSubject().getUid());
        List<Card> cardList = cardFacade.findUserPhysicalCardInCommunity(entity);
        return ApiResult.ok(cardList);
    }

    /**
     * 通过社区和用户获取凭证信息（H5）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "某社区卡片分页(去除虚拟卡)", path = "/card/record/page")
    @Authorization
    public ApiResult findCardRecord(@Validated(Card.GetCardInfo.class) @RequestBody Card entity,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        entity.setCommunityId(SessionUtil.getCommunityId());
        Page<Card> cardPage = cardFacade.getUserCardRecord(entity, page, size);
        return ApiResult.ok(cardPage);
    }

    /**
     * 通过凭证查询设备列表
     *
     * @param card
     * @return
     */
    @PostMapping(name = "据卡片类型和凭证ID号来查询卡片所拥有的设备列表", path = "/card/type/get/device/list")
    @Authorization
    public Object findCardAuthDeviceList(@Validated(Card.GetCertificateDevice.class) @RequestBody Card card) {
        List<Object> authDeviceList = new ArrayList<>();
        authDeviceList = doorFacade.getAllAuthListInDoors(card, authDeviceList);

        // 查询卡电梯设备
        List<KeyNoListElevatorVO> devices;
        devices = cardFacade.getAuthElevatorDevice(card).getData();
        if (CollectionUtils.isNotEmpty(devices)) {
            authDeviceList.addAll(devices);
        }

        return ApiResult.ok(authDeviceList);
    }

    /**
     * 查询卡片设备权限详情
     *
     * @param card
     * @return
     */
    @PostMapping(name = "某卡片设备权限详情(可使用的楼层与门)", path = "/card/type/get/device/detail")
    @Authorization(verifyApi = false)
    public Object findCardAuthDeviceDetail(@Validated(Card.GetCertificateDevice.class) @RequestBody Card card) {
        return doorFacade.getCardPermissionDetail(card);
    }

    /**
     * 查询卡片设备权限详情
     *
     * @param id
     * @return
     */
    @GetMapping(name = "某卡片详情", path = "/card/{id}/detail")
    @Authorization
    public ApiResult findCardById(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(cardFacade.getByIdAndUserId(id, SessionUtil.getTokenSubject().getUid()));
    }

    @PostMapping(name = "卡片设备权限详情(可使用的楼层与门)", path = "/card/elevator/view")
    @Authorization(verifyApi = false)
    public Object isCardAuth(@Validated(Card.GetCertificateDevice.class) @RequestBody Card card) {
        KeyNoListElevatorVOResponse response = cardFacade.getAuthElevatorDevice(card);
        if (!response.getSuccess()) {
            log.info("用户ID -> {} : 获取电梯列表失败", SessionUtil.getTokenSubject().getUid());
            return ApiResult.ok();
        }
        return response;
    }

    /**
     * 查询用户在当前社区是否具有指定类型的卡
     *
     * @param card
     * @return
     */
    @PostMapping(name = "查询用户在当前社区是否具有指定类型的卡", path = "/card/user-keyType/query")
    @Authorization
    public ApiResult findUserKeyTypeInCommunity(@Validated(Card.QueryKeyType.class) @RequestBody Card card) {
        return ApiResult.ok(cardFacade.existKeyTypeAndKeyNoInCommunity(SessionUtil.getCommunityId(),
                                                                       card.getKeyNo(),
                                                                       card.getKeyType()));
    }

    @PostMapping(name = "卡片删除权限", path = "/card/auth/remove")
    @Authorization
    public ApiResult deleteCardAuth(@RequestBody Card card) {
        Card toDelete = cardFacade.getById(card.getId());
        if (toDelete == null) {
            return ApiResult.error(-1, "一卡通不存在");
        }
        // 删除全视通实体卡权限
        deleteFreeViewAuth(toDelete);

        return ApiResult.ok(cardFacade.removeAuthAndDeleteCard(toDelete));
    }

    private void deleteFreeViewAuth(Card toDelete) {
        doorFacade.deleteFreeViewUserCard(toDelete.getKeyNo());

        if (null != toDelete.getRoomId() && null == toDelete.getUserId()) {
            Room room = roomFacade.findOne(toDelete.getRoomId());

            doorFacade.deleteFreeViewCardWithoutUserInfo(room, toDelete.getKeyNo());
        }
    }


    @PostMapping(name = "查询设备终端信息", path = "/card/dtu-info/query")
    public Object cardQueryFromIoT(@Valid @RequestBody CardQueryRequest signInByCode) {
        return cardFacade.queryCardInfoFromDTU(signInByCode);
    }

    @PostMapping(name = "新增设备终端信息", path = "/card/dtu-info/add")
    public Object addCardFromIoT(@Valid @RequestBody CardQueryRequest signInByCode) {
        return cardFacade.addCardInfo2DTU(signInByCode);
    }

    @PostMapping(name = "删除设备终端信息", path = "/card/dtu-info/delete")
    public Object deleteCardFromIoT(@Valid @RequestBody CardQueryRequest signInByCode) {
        return cardFacade.deleteCardInfoFromDTU(signInByCode);
    }

    /**
     * 根据卡号查询卡片
     *
     * @param keyNo
     * @return
     */
    @GetMapping(name = "根据卡号及卡类型查询卡片", path = "/card/{keyNo}")
    @Authorization
    public ApiResult findCardByKeyNoAndKeyType(@PathVariable("keyNo") String keyNo,
                                               @RequestParam("keyType") Integer keyType) {
        List<Card> cards = cardFacade.findByCommunityIdAndKeyNoAndKeyType(SessionUtil.getCommunityId(), keyNo, keyType);
        return ApiResult.ok(cards);
    }

    /**
     * 兼容旧接口
     * 更新电梯权限后更新门禁设备权限
     *
     * @param cardVO
     * @param buildingIds
     * @param communityId
     */
    private void updateDoorPermission(CardVO cardVO, Set<ObjectId> buildingIds, ObjectId communityId) {
        List<Door> doors = doorFacade.getBuildingAndCommunityDoor(buildingIds, communityId);
        doors.removeIf(door -> door.getBrandNo() == ManufactureType.MILI.KEY);

        if (cardVO.getKeyType() != CertificateType.PHONE_MAC.KEY) {
            // 去除远程开门等实体卡和二维码用不了的设备
            doors.removeIf(door -> !door.getServiceId().contains(DoorService.BLUETOOTH.KEY));
        }

        if (doors.size() > 0) {
            log.info("connect to IoT...");
            cardFacade.updateCardDoorPermission(cardVO, doors);
        }
    }

    private List<ObjectId> checkDeviceLicense(ObjectId userId, Collection<ObjectId> roomIds) {
        if (userId == null) {
            return new ArrayList<>(roomIds);
        }
        List<Household> licenses = householdFacade.findByRoomIdsAndUserId(roomIds, userId);

        if (licenses.size() < 1) {
            log.error("缺少房间住户认证");
            throw ROOMS_AUTH_NOT_EXIST;
        }

        if (licenses.size() == 1 && !licenses.get(0).getDeviceLicense()) {
            log.error("设备许可被取消");
            throw DEVICE_LISENCE_CANCEL;
        }

        return licenses.stream()
                       .filter(Household::getDeviceLicense)
                       .map(Household::getRoomId)
                       .collect(Collectors.toList());
    }

    // =================================================【card end】====================================================

    // ==============================================【district start】=================================================

    /**
     * 保留指定房屋相关设备权限(物业)
     * 推送-透传
     *
     * @param districtRequest
     * @return
     */
    @PostMapping(name = "编辑物业人员职能区域", path = "/property/edit-district")
    //    @SendPush(clientTypes = ClientType.PROPERTY, scope = SendPush.Scope.COMMUNITY)
    @Authorization
    public ApiResult propertyEditDistrict(@RequestBody DistrictRequest districtRequest) {
        UserToProperty userToProperty = userToPropertyFacade.findByIdAndCommunityId(districtRequest.getUserToPropertyId(),
                                                                                    SessionUtil.getCommunityId());
        if (userToProperty == null) {
            throw PROPERTY_NOT_EXIST;
        }
        Set<ObjectId> delDistrictIds = userToProperty.getDistrictIds();
        List<District> districts = districtFacade.findInIds(districtRequest.getDistrictIds());
        Set<ObjectId> updateDistrictSet = new HashSet<>();
        Set<ObjectId> updateBuildingIdSet = new HashSet<>();
        for (District district : districts) {
            updateDistrictSet.add(district.getId());
            if (!district.getOpen()) {
                return ApiResult.error(-1, "物业区域尚未开放。");
            }
            updateBuildingIdSet.addAll(district.getBuildingIds());
        }

        userToProperty.setDistrictIds(updateDistrictSet);
        userToProperty.setBuildingIds(updateBuildingIdSet);
        userToPropertyFacade.updateUserToPropertyDistrictRange(userToProperty, delDistrictIds);

        return ApiResult.ok();
    }

    @PostMapping(name = "修改物业人员的电梯使用权限", path = "/property/device-auth/edit")
    @Authorization
    public ApiResult editPropertyDeviceAuth(@RequestBody PropertyCoverElevatorRangeRequest request) {
        List<Card> cards = cardFacade.findUsefulCardByUserIdAndCommunityId(request.getUserId(),
                                                                           SessionUtil.getCommunityId());
        // 如果该员工暂时没有卡，需要补一张
        if (CollectionUtils.isEmpty(cards)) {
            cn.bit.common.facade.user.model.User commonUserInfo = commonUserFacade.getUserByUserId(request.getUserId());
            Card newCard = cardFacade.applyCardForUser(request.getUserId(),
                                                       SessionUtil.getCommunityId(),
                                                       commonUserInfo.getName());
            cards.add(newCard);
        }
        log.info("需要编辑的物业卡 : {}", cards);
        for (Card toUpdate : cards) {
            elevatorFacade.coverAuthByDeviceNumAndCard(request.getTerminalCode(), toUpdate);
        }
        return ApiResult.ok();
    }

    private void deleteDoorWithoutPermission(CardVO cardVO, Set<ObjectId> buildings, Card card, ObjectId communityId) {
        List<KeyNoListElevatorVO> authElevatorDevice = cardFacade.getAuthElevatorDevice(card).getData();
        Set<ObjectId> buildingSet = authElevatorDevice.stream()
                                                      .map(KeyNoListElevatorVO::getBuildId)
                                                      .collect(Collectors.toSet());

        // 删除楼栋门
        for (ObjectId building : buildings) {
            if (!buildingSet.contains(building)) {
                // 如果授权列表里面不存在该楼栋则删除该楼栋的门禁列表
                List<Door> doors = doorFacade.getDoorByCommunityIdAndBuildingId(communityId, building);
                cardFacade.deleteCardDoorPermission(cardVO, doors);
            }
        }
    }
    // ===============================================【district end】==================================================

    /**
     * 获取用户信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping(name = "用户认证详情", path = "/proprietor/{userToRoomId}/audit-detail")
    @Authorization
    public ApiResult findProprietorAuditDetail(@PathVariable("userToRoomId") ObjectId id) throws Exception {
        UserToRoom userToRoom = userToRoomFacade.findById(id);
        UserToRoomVO userToRoomVO = new UserToRoomVO();
        BeanUtils.copyProperties(userToRoom, userToRoomVO);
        return ApiResult.ok(userToRoomVO);
    }
}