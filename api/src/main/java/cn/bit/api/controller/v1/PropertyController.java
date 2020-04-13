package cn.bit.api.controller.v1;

import cn.bit.api.support.*;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.api.support.annotation.InHand;
import cn.bit.api.support.annotation.SendPush;
import cn.bit.facade.enums.*;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.Parameter;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.property.*;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.model.user.CommunityUser;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.ParameterFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.property.*;
import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.vo.RemarkVO;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorFaultVO;
import cn.bit.facade.vo.property.*;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.massmessaging.facade.dubbo.MassMessagingFacade;
import cn.bit.massmessaging.facade.dubbo.dto.SendMessagesDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.property.PropertyBizException.FAULT_IS_NULL;
import static cn.bit.framework.exceptions.BizException.OPERATION_FAILURE;

@RestController
@RequestMapping(value = "/v1/property", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class PropertyController {

    @Autowired
    private ComplainFacade complainFacade;

    @Autowired
    private ReleasePassFacade releasePassFacade;

    @Autowired
    private AlarmFacade alarmFacade;

    @Autowired
    private NoticeFacade noticeFacade;

    @Autowired
    private PropertyFacade propertyFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private FaultFacade faultFacade;

    @Autowired
    private DoorFacade doorFacade;

    @Autowired
    private ParameterFacade parameterFacade;

    @Autowired
    private GtaskzsFacade gtaskzsFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    @Autowired
    private NoticeTemplateFacade templateFacade;

    @Resource
    private MassMessagingFacade massMessagingFacade;

    @Autowired
    private PushFacade pushFacade;

    @Autowired
    private UserFacade userFacade;

    //==================================【Complain begin】=============================================

    /**
     * 新增投诉
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "新增投诉", path = "/complain/add")
    @Authorization
    public ApiResult<Complain> addComplain(@RequestBody @Validated Complain entity) {
        entity.setCommunityId(SessionUtil.getCommunityId());
        UserVO vo = SessionUtil.getCurrentUser();
        if (vo != null) {
            entity.setUserId(vo.getId());
            entity.setUserName(vo.getName());
            entity.setPhone(vo.getPhone());
        }
        Integer client = SessionUtil.getAppSubject().getClient();
        if (client == ClientType.HOUSEHOLD.value()) {
            entity.setMessageSource(UserStatus.RESIDENT.key);
            List<Household> households =
                    householdFacade.findByCommunityIdAndUserId(entity.getCommunityId(), entity.getUserId());
            if (!households.isEmpty()) {
                entity.setRoomInfo(households.stream().map(Household::getRoomLocation).collect(Collectors.toList()));
            }
        }
        if (client == ClientType.PROPERTY.value()) {
            entity.setMessageSource(UserStatus.PROPERTY.key);
        }
        Parameter complainParam = parameterFacade.findByTypeAndKeyAndCommunityId(
                ParamConfigType.COMPLAIN.getKey(), ParamKeyType.COMPLAIN_AUTO_PENDING.name(), entity.getCommunityId());
        if (complainParam == null || complainParam.getValue().equalsIgnoreCase("true")) {
            entity.setStatus(ComplainStatusEnum.PENDING.value);
        } else {
            entity.setStatus(ComplainStatusEnum.TO_ACCEPT.value);
        }
        entity = complainFacade.addComplain(entity);
        return ApiResult.ok(entity);
    }

    /**
     * 获取投诉详细
     *
     * @param id
     * @return
     */
    @GetMapping(name = "投诉详情", path = "/complain/{id}/detail")
    @Authorization
    public ApiResult getComplainDetail(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(complainFacade.getComplainById(id));
    }

    /**
     * 删除工单(H5)
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除投诉工单", path = "/complain/{id}/delete")
    @Authorization
    public ApiResult deleteComplain(@PathVariable("id") ObjectId id) {
        complainFacade.deleteComplainById(id);
        return ApiResult.ok();
    }

    /**
     * 隐藏工单
     *
     * @param id
     * @return
     */
    @PostMapping(name = "隐藏投诉工单", path = "/complain/{id}/hidden")
    @Authorization
    public ApiResult hiddenComplain(@PathVariable("id") ObjectId id) {
        complainFacade.hiddenComplainById(id, SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok();
    }

    /**
     * 处理投诉
     *
     * @param complain
     * @return
     */
    @PostMapping(name = "处理投诉工单", path = "/complain/process")
    @Authorization
    public ApiResult processComplain(@RequestBody Complain complain) {
        complain.setStatus(ComplainStatusEnum.PROCESSED.value);
        complainFacade.processComplain(complain);
        return ApiResult.ok();
    }

    /**
     * 评价处理结果
     *
     * @param complain
     * @return
     */
    @PostMapping(name = "评价投诉处理结果", path = "/complain/evaluate")
    @Authorization
    public ApiResult evaluateComplain(@RequestBody Complain complain) {
        complain.setStatus(ComplainStatusEnum.EVALUATED.value);
        complainFacade.evaluateComplain(complain);
        return ApiResult.ok();
    }

    /**
     * 分页查询
     *
     * @param request
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "投诉工单分页", path = "/complain/page")
    @Authorization
    public ApiResult getComplainPage(@RequestBody ComplainRequest request,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        request.setCommunityId(SessionUtil.getCommunityId());
        AppSubject appSubject = SessionUtil.getAppSubject();
        if (appSubject.getClient() == ClientType.HOUSEHOLD.value()) {
            request.setUserId(SessionUtil.getTokenSubject().getUid());
//            request.setMessageSource(UserStatus.RESIDENT.key);
            request.setHidden(Boolean.TRUE);
        }

//        if (appSubject.getClient() == ClientType.PROPERTY.value()
//                && appSubject.getOsEnum().value() != OsEnum.WEB.value()) {
//            request.setUserId(SessionUtil.getTokenSubject().getUid());
//            request.setMessageSource(UserStatus.PROPERTY.key);
//            request.setHidden(Boolean.TRUE);
//        }
        Page<Complain> pages = complainFacade.getComplainPage(request, page, size);
        return ApiResult.ok(pages);
    }

    /**
     * 大屏查询投诉报事
     *
     * @param size
     * @return
     */
    @GetMapping(name = "投诉工单列表(大屏)", path = "/complain/page/screen")
    @Authorization
    public ApiResult getComplainPage(@RequestParam(defaultValue = "10") Integer size) {
        List<Complain> complains = complainFacade.listComplainsForScreen(SessionUtil.getCommunityId(), size);
        return ApiResult.ok(complains);
    }

    //==================================【Complain end】=============================================

    //==================================【property begin】=============================================

    /**
     * 根据id查询物业详情
     *
     * @param id
     * @return
     */
    @GetMapping(name = "物业公司详情", path = "/{id}/detail")
    public ApiResult<Property> getProperty(@PathVariable("id") ObjectId id) {
        Property property = propertyFacade.findOne(id);
        if (property == null) {
            return ApiResult.error(-1, "查询物业公司详细信息失败");
        }
        return ApiResult.ok(property);
    }

    /**
     * 根据社区id查询物业信息
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "查询某社区的物业公司详情", path = "/{communityId}/detail-by-community")
    public ApiResult<Property> getPropertyCommunityId(@PathVariable("communityId") ObjectId communityId) {
        Property property = propertyFacade.findByCommunityId(communityId);
        return ApiResult.ok(property);
    }
    //==================================【property end】===============================================

    //==================================【ReleasePass begin】===============================================

    /**
     * 添加放行条
     *
     * @param releasePass
     * @return
     */
    @PostMapping(name = "新增放行条", path = "/rpass/add")
    @Authorization
    public ApiResult<String> addReleasePass(@RequestBody @Validated(ReleasePass.Add.class) ReleasePass releasePass) {
        UserVO vo = SessionUtil.getCurrentUser();
        if (vo != null) {
            releasePass.setUserId(vo.getId());
            releasePass.setUserName(vo.getName());
            releasePass.setPhone(vo.getPhone());
            releasePass.setCreatorId(vo.getId());
        }
        ReleasePass entity = releasePassFacade.addReleasePass(releasePass);
        return ApiResult.ok(entity);
    }

    /**
     * 删除放行条
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除放行条", path = "/rpass/{id}/delete")
    @Authorization
    public ApiResult deleteReleasePass(@PathVariable("id") ObjectId id) {
        try {
            releasePassFacade.changeStatus(id);
            return ApiResult.ok();
        } catch (Exception e) {
            return ApiResult.error(-1, "更改状态失败");
        }
    }

    /**
     * 根据id查询放行条数据
     *
     * @param id
     * @return
     */
    @GetMapping(name = "放行条详情", path = "/rpass/{id}/detail")
    @Authorization
    public ApiResult getReleasePassById(@PathVariable("id") ObjectId id) {
        ReleasePass releasePass = releasePassFacade.getReleasePassByIdAndCommunityId(
                id, SessionUtil.getCommunityId());
        return ApiResult.ok(releasePass);
    }

    /**
     * 列表
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "某社区放行条列表", path = "/rpass/{communityId}/list")
    @Authorization
    public ApiResult getReleasePassList(@PathVariable ObjectId communityId) {
        return ApiResult.ok(releasePassFacade.getReleasePassList(communityId));
    }

    /**
     * 分页查询
     *
     * @param request
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "放行条分页", path = "/rpass/page")
    @Authorization
    public ApiResult getReleasePassPage(@RequestBody @Validated ReleasePassRequest request,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        if (SessionUtil.getAppSubject().getClient() == ClientType.HOUSEHOLD.value()) {
            request.setUserId(request.getUserId() == null ? SessionUtil.getTokenSubject().getUid() : request.getUserId());
        }
        request.setCommunityId(request.getCommunityId() == null ? SessionUtil.getCommunityId() : request.getCommunityId());
        return ApiResult.ok(releasePassFacade.getReleasePassPage(request, page, size));
    }

    /**
     * 确认放行条
     *
     * @param id
     * @return
     */
    @GetMapping(name = "确认放行条", path = "/rpass/{id}/check")
    @Authorization
    public ApiResult checkReleasePass(@PathVariable ObjectId id) {
        UserVO vo = SessionUtil.getCurrentUser();
        ReleasePass releasePass = new ReleasePass();
        releasePass.setId(id);
        if (vo != null) {
            releasePass.setVerifierId(vo.getId());
            releasePass.setVerifierName(vo.getName());
        }
        ReleasePass entity = releasePassFacade.checkReleasePass(releasePass);
        if (entity == null) {
            return ApiResult.error(-1, "确认放行条失败");
        }
        if (entity.getReleaseStatus() == -1) {
            return ApiResult.error(-1, "放行条已过期，请从新生成");
        }
        return ApiResult.ok(entity);
    }

    //==================================【ReleasePass end】=================================================

    //==================================【Alarm begin】=================================================

    /**
     * 报警
     *
     * @param alarm
     * @return
     */
    @PostMapping(name = "用户报警", path = "/alarm/add")
    @Authorization
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.PROPERTY,
            point = PushPointEnum.ALARM
    )
    public ApiResult<Alarm> addAlarmRecord(@Validated(Alarm.AddAlarm.class) @RequestBody Alarm alarm) {
        UserVO currentUser = SessionUtil.getCurrentUser();
        // 根据roomId查询
        Room room = roomFacade.findOne(alarm.getRoomId());
        Building building = buildingFacade.findOne(room.getBuildingId());
        Community community = communityFacade.findOne(room.getCommunityId());
        alarm.setCallerId(currentUser.getId());
        alarm.setCallerPhoneNum(currentUser.getPhone());
        alarm.setCallerName(currentUser.getName());
        alarm.setRoomName(room.getName());
        alarm.setBuildingId(building.getId());
        alarm.setBuildingName(building.getName());
        alarm.setCommunityId(community.getId());
        alarm.setCommunityName(community.getName());
        alarm = alarmFacade.addRecord(alarm);
        return ApiResult.ok(alarm);
    }

    /**
     * 用户查看报警数据
     * 物业 使用buildingId或receiveStatus或报警人名字查询
     * 业主 查看自己的报警信息
     *
     * @return
     */
    @RequestMapping(name = "报警工单分页", path = "/alarm/getAlarm", method = {RequestMethod.POST})
    @Authorization
    public ApiResult getAlarm(@RequestBody Alarm alarm,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "5") Integer size) {
        Integer client = SessionUtil.getAppSubject().getClient();
        alarm.setCommunityId(SessionUtil.getCommunityId());
        Page<Alarm> record;
        if (ClientType.PROPERTY.value() == client) {
            record = alarmFacade.getAlarmRecord(alarm, page, size);
        } else {
            record = alarmFacade.getProprietorAlarm(SessionUtil.getTokenSubject().getUid(), page, size);
        }
        return ApiResult.ok(record);
    }

    /**
     * 保安查看待接警数量
     * 保安按照communityId查找当前社区的待接警数量
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "某社区待接警工单列表", path = "/alarm/{communityId}/unchecked-count")
    @Authorization
    public ApiResult getAlarmNumForReceive(@PathVariable ObjectId communityId) {
        return ApiResult.ok(alarmFacade.findReceiveAlarmNum(communityId, ReceiveStatusType.UNCHECKED.key));
    }

    /**
     * 物业端接警
     *
     * @param alarm
     * @return
     */
    @PostMapping(name = "保安接警", path = "/alarm/receiveAlarm")
    @Authorization
    public ApiResult receiveAlarm(@Validated(Alarm.ReceiveAlarm.class) @RequestBody Alarm alarm) {
        UserVO currentUser = SessionUtil.getCurrentUser();
        alarm.setReceiverId(currentUser.getId());
        alarm.setReceiverName(currentUser.getName());
        alarm.setReceiverPhoneNum(currentUser.getPhone());
        return ApiResult.ok(alarmFacade.receiveAlarm(alarm));
    }

    /**
     * 物业添加排查记录
     * 排查时间需要作为参数传递
     *
     * @param alarm
     * @return
     */
    @PostMapping(name = "提交警报排查报告", path = "/alarm/troubleShoot")
    @Authorization
    public ApiResult troubleShoot(@Validated(Alarm.TroubleShoot.class) @RequestBody Alarm alarm) {
        return ApiResult.ok(alarmFacade.troubleShoot(alarm));
    }

    //==================================【Alarm end】=================================================

    //==================================【Notice begin】=================================================

    /**
     * 新增公告
     *
     * @param notice
     * @return
     */
    @PostMapping(name = "新增公告", path = "/notice/add")
    /*@SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD, ClientType.PROPERTY},
            point = PushPointEnum.NOTICE)*/
    @Authorization
    public ApiResult addNotice(@RequestBody @Validated(Notice.Add.class) Notice notice) {
        notice.setCommunityId(SessionUtil.getCommunityId());
        UserVO vo = SessionUtil.getCurrentUser();
        if (vo != null) {
            notice.setEditorId(vo.getId());
            notice.setEditorName(vo.getName());
        }
        Notice entity = noticeFacade.addNotice(notice);
        if (entity == null) {
            return ApiResult.ok();
        }
        PushConfig pushConfig =
                pushFacade.findPushConfigByCompanyIdAndPointId(SessionUtil.getCompanyId(), PushPointEnum.NOTICE.name());
        List<CommunityUser> cmUsers = userFacade.findCMUserByCommunityIdAndClientAndRoles(
                notice.getCommunityId(), ClientType.HOUSEHOLD.value(), Collections.singleton(RoleType.HOUSEHOLD.name()));

        SendMessagesDTO sendMessagesDTO = new SendMessagesDTO();
        sendMessagesDTO.setTenantId(entity.getCommunityId());
        sendMessagesDTO.setSenderUserId(SessionUtil.getCurrentUser().getId());
        sendMessagesDTO.setSenderUsername(SessionUtil.getCurrentUser().getPhone());
        sendMessagesDTO.setSenderNickname(SessionUtil.getCurrentUser().getNickName());
        sendMessagesDTO.setMessageType(PushPointEnum.NOTICE.value());
        sendMessagesDTO.setMessageTitle(entity.getTitle());
        sendMessagesDTO.setMessageAlert(entity.getBody());
        sendMessagesDTO.setTargetPartners(Collections.singleton(SessionUtil.getAppSubject().getPartner()));
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("action", 100101);
        dataMap.put("communityId", entity.getCommunityId());
        dataMap.put("notice_id", entity.getId());
        dataMap.put("thumbnail", entity.getThumbnailUrl());
        dataMap.put("url", entity.getUrl());
        dataMap.put("type", entity.getNoticeType());
        dataMap.put("publish_time", entity.getPublishAt());
        dataMap.put("editor_name", entity.getEditorName());
        sendMessagesDTO.setMessageData(JSONObject.toJSONString(dataMap));
        if (pushConfig != null) {
            sendMessagesDTO.setTargetClients(Collections.singleton(ClientType.PROPERTY.value()));
            sendMessagesDTO.setTargetRoles(pushConfig.getTargets());
            massMessagingFacade.sendMessages(sendMessagesDTO);
        }
        if (CollectionUtils.isNotEmpty(cmUsers)) {
            sendMessagesDTO.setTargetClients(Collections.singleton(ClientType.HOUSEHOLD.value()));
            sendMessagesDTO.setTargetRoles(null);
            sendMessagesDTO.setTargetUserIds(cmUsers.stream().map(cmUser -> cmUser.getUserId().toString()).collect(Collectors.toSet()));
            massMessagingFacade.sendMessages(sendMessagesDTO);
        }
        return ApiResult.ok(entity);
    }

    /**
     * 删除公告（逻辑删除)
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除公告", path = "/notice/{id}/delete")
    @Authorization
    public ApiResult deleteNotice(@PathVariable("id") ObjectId id) {
        Notice notice = noticeFacade.deleteNoticeById(id);
        if (notice == null) {
            return ApiResult.error(-1, "删除失败");
        }
        return ApiResult.ok();
    }

    /**
     * （废弃）
     * 修改公告
     *
     * @param notice
     * @return
     * @since 20180419
     * @since {20180419}
     */
    @Deprecated
    @PostMapping(name = "编辑公告(旧)", path = "/notice/edit")
    @Authorization
    public ApiResult updateNotice(@RequestBody @Validated(Notice.Update.class) Notice notice) {
        UserVO vo = SessionUtil.getCurrentUser();
        if (vo != null) {
            notice.setEditorId(vo.getId());
            notice.setEditorName(vo.getName());
        }
        return ApiResult.ok(noticeFacade.updateNotice(notice));
    }

    /**
     * 根据id获取公告
     *
     * @param id
     * @return
     */
    @GetMapping(name = "公告详情", path = "/notice/{id}/detail")
    public ApiResult getNotice(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(noticeFacade.getNoticeById(id));
    }

    /**
     * （废弃）
     * 发布公告
     *
     * @param id
     * @return
     * @since 20180419
     */
    @Deprecated
    @GetMapping(name = "发布公告", path = "/notice/{id}/publish")
    @Authorization
    public ApiResult publishNotice(@PathVariable("id") ObjectId id) {
        Notice notice = noticeFacade.publishNoticeById(id);
        if (notice == null || notice.getDataStatus() == 0) {
            return ApiResult.error(-1, "公告已失效");
        }
        return ApiResult.ok(notice);
    }

    /**
     * 分页获取公告
     *
     * @param entity
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "公告分页", path = "/notice/page")
    public ApiResult queryNoticePage(@RequestBody @Validated NoticeRequest entity,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<Notice> pages = noticeFacade.getNoticePage(entity, page, size);
        return ApiResult.ok(pages);
    }
    //==================================【Notice end】=================================================

    //==================================【Template start】=================================================
    @PostMapping(name = "新增公告模板", path = "/templates/add")
    @Authorization
    public ApiResult addNoticeTemplate(@RequestBody @Validated(NoticeTemplate.Add.class) NoticeTemplate template) {
        return ApiResult.ok(templateFacade.addNoticeTemplate(template));
    }

    @GetMapping(name = "公告模板详情", path = "/templates/{id}/detail")
    public ApiResult getNoticeTemplateDetail(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(templateFacade.findNoticeTemplateById(id));
    }

    @GetMapping(name = "公告模板分页", path = "/templates/page")
    public ApiResult listNoticeTemplates(String name, String title,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        NoticeTemplatePageQuery query = new NoticeTemplatePageQuery();
        query.setCommunityId(SessionUtil.getCommunityId());
        query.setName(name);
        query.setTitle(title);
        query.setPage(page);
        query.setSize(size);
        return ApiResult.ok(templateFacade.listNoticeTemplates(query));
    }

    @PostMapping(name = "编辑公告模板", path = "/templates/modify")
    @Authorization
    public ApiResult modifyNoticeTemplate(@RequestBody @Validated(NoticeTemplate.Modify.class) NoticeTemplate template) {
        return ApiResult.ok(templateFacade.modifyNoticeTemplate(template));
    }

    @PostMapping(name = "删除公告模板", path = "/templates/{id}/delete")
    @Authorization
    public ApiResult deleteNoticeTemplate(@PathVariable("id") ObjectId id) {
        templateFacade.deleteNoticeTemplateById(id);
        return ApiResult.ok();
    }

    @GetMapping(name = "某社区公告模板列表", path = "/templates/list")
    @Authorization(verifyApi = false)
    public ApiResult listNoticeTemplates() {
        return ApiResult.ok(templateFacade.listNoticeTemplates(SessionUtil.getCommunityId()));
    }

    //==================================【Template end】=================================================


    //==================================【故障管理 begin】=================================================

    /**
     * 新增故障信息（物业端、APP端）
     *
     * @param entity
     * @return
     * @since 20190319 拆分物业端及app端
     */
    @PostMapping(name = "新增故障", path = "/fault/addFault")
    @Authorization
    @Deprecated
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.valid)
    public ApiResult<Fault> addFault(@Validated(Fault.Add.class) @RequestBody Fault entity) {
        // 回填用户信息
        BackUser(entity, SessionUtil.getAppSubject().getClient(), SessionUtil.getCommunityId(), SessionUtil.getCompanyId());
        // 回填故障地址
        BackfillFault(entity);
        return ApiResult.ok(faultFacade.addFault(entity));
    }

    /**
     * 新增故障信息（物业端）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "物业人员新增故障", path = "/fault/property-add")
    @Authorization
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.valid)
    public ApiResult<Fault> addFaultForProperty(@Validated(Fault.Add.class) @RequestBody Fault entity) {
        ObjectId communityId = SessionUtil.getCommunityId();
        // 回填用户信息
        UserVO userVO = SessionUtil.getCurrentUser();
        // 故障状态默认【待受理】
        entity.setFaultStatus(FaultStatusType.WAITACCEPT.key);
        // 初始提交默认session的名称
        entity.setUserName(userVO.getNickName());
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                userVO.getId(), communityId, SessionUtil.getCompanyId());
        // 显示物业人员在公司的名字
        if (userToProperty != null) {
            entity.setUserName(userToProperty.getUserName());
        }
        entity.setCommunityId(communityId);
        // 未评价
        entity.setEvaluate(EvaluateType.NOEVALUATION.key);
        entity.setIdentity(UserStatus.PROPERTY.key);
        entity.setContact(userVO.getPhone());
        entity.setUserId(userVO.getId());
        entity.setCreateId(userVO.getId());
        // 回填故障地址
        BackfillFault(entity);
        return ApiResult.ok(faultFacade.addFault(entity));
    }

    /**
     * 新增故障信息（APP端）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "住户新增故障", path = "/fault/household-add")
    @Authorization
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.valid)
    public ApiResult<Fault> addFaultForHousehold(@Validated(Fault.Add.class) @RequestBody Fault entity) {
        UserVO userVO = SessionUtil.getCurrentUser();
        // 回填用户信息
        entity.setCommunityId(SessionUtil.getCommunityId());
        // 故障状态默认【待受理】
        entity.setFaultStatus(FaultStatusType.WAITACCEPT.key);
        // 未评价
        entity.setEvaluate(EvaluateType.NOEVALUATION.key);
        entity.setUserName(userVO.getName());
        entity.setIdentity(UserStatus.RESIDENT.key);
        entity.setContact(userVO.getPhone());
        entity.setUserId(userVO.getId());
        entity.setCreateId(userVO.getId());
        // 回填故障地址
        BackfillFault(entity);
        return ApiResult.ok(faultFacade.addFault(entity));
    }

    /**
     * 废弃接口
     *
     * @param entity
     * @return
     * @date 2018-08-02
     * 修改（APP端）
     */
    @PostMapping(name = "编辑故障(旧)", path = "/fault/editFault")
    @Authorization
    public ApiResult updateFault(@Valid @RequestBody Fault entity) {
        // 限制图片数量
        if (entity.getFaultAccessory() != null && entity.getFaultAccessory().size() > 5) {
            return ApiResult.error(-1, "上传的图片不能超过五张");
        }
        entity = faultFacade.updateFault(entity, SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(entity);
    }

    /**
     * 受理故障单（物业端、APP端）
     *
     * @param entity
     * @return （-1：已驳回；0：已取消；2：已受理；4：已完成；）
     * @since 2019-04-08 拆分4个接口（取消申报，受理故障，驳回故障申请，检修故障）
     */
    @PostMapping(name = "处理故障（旧）", path = "/fault/editFaultStatus")
    @Authorization
    @Deprecated
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.unvalid)
    public ApiResult auditFaultStatus(@Validated(Fault.Audit.class) @RequestBody Fault entity) throws Exception {
        // 获取当前操作人
        UserVO user = SessionUtil.getCurrentUser();
        entity = faultFacade.auditFault(entity, user);
        return ApiResult.ok(entity);
    }

    /**
     * 受理故障
     *
     * @param faultId
     * @return
     * @throws Exception
     */
    @PostMapping(name = "受理故障", path = "/fault/{id}/handle")
    @Authorization
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.unvalid)
    public ApiResult handleFault(@PathVariable("id") ObjectId faultId) {
        // 获取当前操作人
        UserVO user = SessionUtil.getCurrentUser();
        Fault toHandle = new Fault();
        toHandle.setId(faultId);
        toHandle.setFaultStatus(FaultStatusType.WAITALOCATION.key);
        toHandle = faultFacade.auditFault(toHandle, user);
        return ApiResult.ok(toHandle);
    }

    /**
     * 驳回故障申请
     *
     * @param faultId
     * @param remarkVO
     * @return
     * @throws Exception
     */
    @PostMapping(name = "驳回故障申请", path = "/fault/{id}/reject")
    @Authorization
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.unvalid)
    public ApiResult rejectFault(@PathVariable("id") ObjectId faultId, @RequestBody RemarkVO remarkVO) {
        // 获取当前操作人
        UserVO user = SessionUtil.getCurrentUser();
        Fault toReject = new Fault();
        toReject.setId(faultId);
        toReject.setFaultStatus(FaultStatusType.REJECT.key);
        toReject.setRejectReason(remarkVO.getRemark());
        toReject = faultFacade.auditFault(toReject, user);
        return ApiResult.ok(toReject);
    }

    /**
     * 取消故障申报
     *
     * @param faultId
     * @return
     * @throws Exception
     */
    @PostMapping(name = "取消故障申报", path = "/fault/{id}/cancel")
    @Authorization
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.unvalid)
    public ApiResult cancelFault(@PathVariable("id") ObjectId faultId) {
        // 获取当前操作人
        UserVO user = SessionUtil.getCurrentUser();
        Fault toCancel = new Fault();
        toCancel.setId(faultId);
        toCancel.setFaultStatus(FaultStatusType.CANCEL.key);
        toCancel = faultFacade.auditFault(toCancel, user);
        return ApiResult.ok(toCancel);
    }

    /**
     * 检修故障
     *
     * @param faultId
     * @return
     * @throws Exception
     */
    @PostMapping(name = "检修故障", path = "/fault/{id}/recondition")
    @Authorization
    @InHand(taskType = InHand.TaskType.fault, dataStatus = InHand.DataStatus.unvalid)
    public ApiResult reconditionFault(@PathVariable("id") ObjectId faultId) {
        // 获取当前操作人
        UserVO user = SessionUtil.getCurrentUser();
        Fault toRcondition = new Fault();
        toRcondition.setId(faultId);
        toRcondition.setFaultStatus(FaultStatusType.FINISH.key);
        toRcondition = faultFacade.auditFault(toRcondition, user);
        return ApiResult.ok(toRcondition);
    }

    /**
     * 分配维修人员（物业端）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "故障分配维修人员", path = "/fault/allocation")
    @Authorization
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.PROPERTY,
            point = PushPointEnum.FAULT_ALLOCATED
    )
    public ApiResult<Fault> allocation(@Validated(Fault.Assign.class) @RequestBody Fault entity) {
        // 获取故障单信息
        Fault item = faultFacade.findOne(entity.getId());
        if (item == null) {
            throw FAULT_IS_NULL;
        }
        // 已经受理了无需重新受理分派
        if (FaultStatusType.WAITALOCATION.key != item.getFaultStatus()) {
            return ApiResult.error(-1, "此故障单已被分配维修人");
        }
        /**
         * 根据维修人员ID获取维修人员信息
         */
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                entity.getRepairId(), item.getCommunityId(), SessionUtil.getCompanyId());
        if (userToProperty == null) {
            return ApiResult.error(-1, "维修人员不存在");
        }
        item.setRepairId(entity.getRepairId());
        // 维修人名称
        item.setRepairName(userToProperty.getUserName());
        // 1：社区维修工；2：私人维修工 （维修人ID为空，表示私人）
        item.setRepairType(FaultRepairerType.PROPERTY.getType());
        // 维修人联系方式
        item.setRepairContact(userToProperty.getPhone());
        // 修改状态-【待检修】
        item.setFaultStatus(FaultStatusType.WAITRECONDTION.key);
        entity = faultFacade.editFault(item);
        if (entity == null || entity.getDataStatus() == DataStatusType.INVALID.KEY
                || FaultStatusType.WAITRECONDTION.key != entity.getFaultStatus()) {
            return ApiResult.error(-1, "故障单据已失效");
        }

        if (entity.getRepairId() == null) {
            return ApiResult.ok(entity);
        }

        PushTarget target = new PushTarget();
        Map<String, String> map = new HashMap();
        // 故障类型
        map.put("faultTypeDesc", FaultType.getValueByKey(entity.getFaultType()));
        // 故障项目
        map.put("faultItemDesc", FaultItemType.getValueByKey(entity.getFaultItem()));
        target.setUserIds(Collections.singletonList(entity.getRepairId()));
        return WrapResult.create(ApiResult.ok(entity), map, target);
    }

    /**
     * 维修工:获取指派给自己的待检修故障数量
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "获取当前维修工待检修故障数量", path = "fault/{communityId}/unrepaired-count")
    @Authorization
    public ApiResult queryFaultCountByCommunityId(@PathVariable ObjectId communityId) {
        return ApiResult.ok(
                faultFacade.queryFaultCountByCommunityIdAndRepairId(
                        communityId, SessionUtil.getTokenSubject().getUid(), FaultStatusType.WAITRECONDTION.key));
    }

    /**
     * 故障分页（物业端、app端）
     *
     * @param entity
     * @param page
     * @param size
     * @return
     * @since 20190321 拆分3个接口（物业查询所有故障工单、用户提交的故障工单、维修工的故障工单）
     */
    @Deprecated
    @RequestMapping(name = "故障分页", path = "/fault/queryFaultPage", method = {RequestMethod.POST})
    @Authorization
    public ApiResult queryFaultPage(@RequestBody Fault entity,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        entity.setCommunityId(SessionUtil.getCommunityId());
        // 用户端
        if (SessionUtil.getAppSubject().getClient() == ClientType.HOUSEHOLD.value()) {
            entity.setUserId(SessionUtil.getTokenSubject().getUid());
        }
        return ApiResult.ok(faultFacade.queryFaultPage(entity, SessionUtil.getAppSubject().getClient(), page, size));
    }

    /**
     * 故障工单分页
     *
     * @param faultStatus 工单状态
     * @param userName    用户名称
     * @param startAt     开始时间
     * @param endAt       结束时间
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "故障工单分页", path = "/faults")
    @Authorization
    public ApiResult listFaults(Integer faultStatus, String userName, Date startAt, Date endAt,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        FaultPageQuery query = new FaultPageQuery();
        query.setCommunityId(SessionUtil.getCommunityId());
        query.setUserName(userName);
        query.setFaultStatus(faultStatus);
        query.setStartAt(startAt);
        query.setEndAt(endAt);
        query.setPage(page);
        query.setSize(size);
        return ApiResult.ok(faultFacade.listFaults(query));
    }

    /**
     * 故障工单分页
     *
     * @param faultStatus 工单状态
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "维修工故障工单分页", path = "/maintainer-faults")
    @Authorization
    public ApiResult listMaintainerFaults(Integer faultStatus,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        FaultPageQuery query = new FaultPageQuery();
        query.setCommunityId(SessionUtil.getCommunityId());
        query.setRepairId(SessionUtil.getTokenSubject().getUid());
        query.setFaultStatus(faultStatus);
        query.setPage(page);
        query.setSize(size);
        return ApiResult.ok(faultFacade.listFaults(query));
    }

    /**
     * 获取当前用户故障工单分页
     *
     * @param faultStatus 工单状态
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "当前用户故障工单分页", path = "/user-faults")
    @Authorization
    public ApiResult listFaultsWithoutHidden(Integer faultStatus,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        FaultPageQuery query = new FaultPageQuery();
        query.setCommunityId(SessionUtil.getCommunityId());
        query.setFaultStatus(faultStatus);
        query.setUserId(SessionUtil.getTokenSubject().getUid());
        query.setHidden(Boolean.TRUE);
        query.setPage(page);
        query.setSize(size);
        return ApiResult.ok(faultFacade.listFaults(query));
    }

    /**
     * 获取故障列表
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "故障列表", path = "/fault/getFaultList")
    @Authorization
    public ApiResult getFaultList(@RequestBody Fault entity) {
        if (SessionUtil.getAppSubject().getClient() == ClientType.HOUSEHOLD.value()) {
            entity.setUserId(SessionUtil.getTokenSubject().getUid());
        }
        entity.setCommunityId(SessionUtil.getCommunityId());
        return ApiResult.ok(faultFacade.getFaultList(entity));
    }

    /**
     * 住户评价
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "评价故障处理结果", path = "/fault/comment")
    @Authorization
    public ApiResult faultComment(@RequestBody Fault entity) {
        Fault toComment = new Fault();
        toComment.setId(entity.getId());
        toComment.setEvaluationGrade(entity.getEvaluationGrade());
        toComment.setEvaluation(entity.getEvaluation());
        toComment = faultFacade.faultComment(toComment, SessionUtil.getTokenSubject().getUid());
        if (toComment == null) {
            throw OPERATION_FAILURE;
        }
        return ApiResult.ok(toComment);
    }

    /**
     * 查询详细信息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "故障详情", path = "/fault/{id}/detail")
    @Authorization
    public ApiResult getFaultById(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(faultFacade.findOne(id));
    }

    /**
     * 隐藏故障单
     *
     * @param id
     * @return
     */
    @GetMapping(name = "隐藏故障工单", path = "/fault/{id}/delete")
    @Authorization
    public ApiResult hiddenFault(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(faultFacade.hiddenById(id));
    }

    /**
     * 获取待办列表
     *
     * @param gtaskzs
     * @return
     */
    @PostMapping(name = "待办事项分页", path = "/gtasks/queryGtasksPage")
    @Authorization
    public ApiResult queryGtasksPage(@RequestBody Gtaskzs gtaskzs,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<Gtaskzs> pages = gtaskzsFacade.queryPage(gtaskzs, page, size);
        return ApiResult.ok(pages);
    }

    /**
     * 废弃
     *
     * @param params
     * @return
     * @date 2018-08-02
     * 电梯互联网回调电梯故障信息
     */
    @PostMapping(name = "电梯互联网回调电梯故障信息(旧)", path = "/fault/elevator/call-back")
    public ApiResult<ElevatorFaultVO> elevatorCallBack(@Validated @RequestBody ElevatorFaultVO params) {
        Fault fault = faultFacade.findOne(params.getFaultId());
        if (fault == null) {
            throw FAULT_IS_NULL;
        }
        // 已分配维修人员
        switch (FaultStatusType.getByValue(params.getFaultStatus())) {
            case WAITRECONDTION:
                // 回调分配人员信息
                if (fault.getFaultStatus() == FaultStatusType.WAITALOCATION.key) {
                    fault.setRepairId(params.getRepairId());
                    fault.setRepairName(params.getRepairName());
                    fault.setRepairContact(params.getRepairPhone());
                    fault.setFaultStatus(FaultStatusType.WAITRECONDTION.key);
                    faultFacade.editFault(fault);
                } else {
                    return ApiResult.error(-1, "故障单状态已改变");
                }
                break;
            case FINISH:
                fault.setFaultStatus(FaultStatusType.FINISH.key);
                fault.setFinishTime(new Date());
                faultFacade.editFault(fault);
                break;
            default:
                break;
        }
        return ApiResult.ok();
    }

    //==================================【故障管理 end】=================================================

    //==================================【parameter start】=============================================================

    /**
     * 获取物业公司配置参数的keyType集合
     *
     * @return
     */
    @GetMapping(name = "物业公司配置项键值对", path = "/parameter/key-types")
    public ApiResult getPropertyParameterKeyType() {
        Map map = new HashMap();
        map.put("parameterKeys", ParamKeyType.getParamKeys());
        return ApiResult.ok(map);
    }

    /**
     * 新增物业公司配置参数
     *
     * @param parameter
     * @return
     */
    @PostMapping(name = "新增配置项", path = "/parameter/add")
    @Authorization
    public ApiResult addPropertyParameter(@RequestBody @Validated(Parameter.Add.class) Parameter parameter) {
        parameter.setCreatorId(SessionUtil.getTokenSubject().getUid());
        parameter = parameterFacade.addParameter(parameter);
        return parameter == null ? ApiResult.error(-1, "新增失败") : ApiResult.ok(parameter);
    }

    /**
     * 修改社区配置参数
     *
     * @param parameter
     * @return
     */
    @PostMapping(name = "编辑配置项", path = "/parameter/{id}/edit")
    @Authorization
    public ApiResult updatePropertyParameter(@PathVariable("id") ObjectId id,
                                             @RequestBody Parameter parameter) {
        parameter.setId(id);
        parameter.setModifierId(SessionUtil.getTokenSubject().getUid());
        parameter = parameterFacade.updateParameter(parameter);
        return parameter == null ? ApiResult.error(-1, "修改失败") : ApiResult.ok(parameter);
    }

    /**
     * 批量修改社区配置参数
     *
     * @param parameters
     * @return
     */
    @PostMapping(name = "批量编辑配置项", path = "/parameter/multi-edit")
    @Authorization
    public ApiResult updateMultiPropertyParameter(@Validated(Parameter.Update.class)
                                                  @RequestBody List<Parameter> parameters) {
        BizException exception = null;
        for (Parameter parameter : parameters) {
            parameter.setModifierId(SessionUtil.getTokenSubject().getUid());
            try {
                parameterFacade.updateParameter(parameter);
            } catch (BizException e) {
                exception = e;
                log.error("BizException:" + e.getCode() + " , " + e.getMsg() + ", id = " + parameter.getId());
                log.error("updateMultiPropertyParameter BizException:", e);
            }
        }
        return exception == null ? ApiResult.ok() : ApiResult.error(exception.getCode(), exception.getMsg());
    }

    /**
     * 删除物业公司配置参数
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除配置项", path = "/parameter/{id}/delete")
    @Authorization
    public ApiResult deletePropertyParameter(@PathVariable("id") ObjectId id) {
        boolean result = parameterFacade.deleteParameter(id, SessionUtil.getTokenSubject().getUid());
        return result ? ApiResult.ok() : ApiResult.error(-1, "删除失败");
    }

    /**
     * 分页查询
     *
     * @param parameter
     * @param page
     * @param size
     * @return
     * @since 2018-05-09
     */
    @Deprecated
    @PostMapping(name = "配置项分页(旧)", path = "/parameter/page")
    @Authorization
    public ApiResult queryPagePropertyParameter(@RequestBody Parameter parameter,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Parameter> list = parameterFacade.queryPage(parameter, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 分页查询物业账单配置(物业管理员)
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "物业账单配置项分页", path = "/parameter/fees-page")
    @Authorization
    public ApiResult queryPageFeesParameter(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        Page<Parameter> list = parameterFacade.queryPageByCommunityIdAndType(
                SessionUtil.getCommunityId(), ParamConfigType.BILL.getKey(), page, size);
        return ApiResult.ok(list);
    }

    /**
     * 分页查询社区动态配置(物业管理员)
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "社区动态配置项分页", path = "/parameter/moment-page")
    @Authorization
    public ApiResult queryPageMomentParameter(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Page<Parameter> list = parameterFacade.queryPageByCommunityIdAndType(
                SessionUtil.getCommunityId(), ParamConfigType.MOMENT.getKey(), page, size);
        return ApiResult.ok(list);
    }

    /**
     * 分页查询社区配置（系统后台）
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "系统后台配置项分页", path = "/parameter/household-auth/{communityId}/page")
    @Authorization
    public ApiResult queryPageCommunityParameter(@PathVariable("communityId") ObjectId communityId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<Parameter> list = parameterFacade.queryPageByCommunityIdAndType(communityId,
                ParamConfigType.HOUSEHOLD_AUTH.getKey(), page, size);
        return ApiResult.ok(list);
    }

    /**
     * 获取用户认证配置
     *
     * @return
     */
    @GetMapping(name = "用户认证配置项分页", path = "/parameter/household-auth/{communityId}/list")
    @Authorization(verifyApi = false)
    public ApiResult queryCommunityParameter(@PathVariable("communityId") ObjectId communityId) {
        List<Parameter> list = parameterFacade.queryByCommunityIdAndTypeForAuth(
                communityId, ParamConfigType.HOUSEHOLD_AUTH.getKey());
        return ApiResult.ok(list);
    }

    //==================================【parameter end】===============================================================

    /**
     * 回填用户信息
     *
     * @param entity
     * @param companyId
     * @return
     * @since 20190319
     */
    @Deprecated
    private Fault BackUser(Fault entity, Integer client, ObjectId communityId, ObjectId companyId) {
        UserVO userVO = SessionUtil.getCurrentUser();
        // 审核状态默认【待受理】
        entity.setFaultStatus(FaultStatusType.WAITACCEPT.key);
        // 初始提交默认session的名称
        entity.setUserName(userVO.getName());
        // 物业端
        if (ClientType.HOUSEHOLD.value() != client) {
            entity.setUserName(userVO.getNickName());
            UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                    userVO.getId(), communityId, companyId);
            // 显示物业人员在公司的名字
            if (userToProperty != null) {
                entity.setUserName(userToProperty.getUserName());
                if ((userToProperty.getPostCode().contains(RoleType.COMPANY_ADMIN.name())
                        || userToProperty.getPostCode().contains(RoleType.CM_ADMIN.name())
                        || userToProperty.getPostCode().contains(RoleType.MANAGER.name()))
                        && entity.getFaultItem().equals(FaultItemType.ELEVATOR.key())) {
                    // 判断是否是管理人员，如果是则完成状态
                    entity.setFaultStatus(client.equals(ClientType.PROPERTY.value())
                            ? FaultStatusType.FINISH.key : FaultStatusType.WAITACCEPT.key);
                }
            }
        }
        entity.setCommunityId(communityId);
        // 未评价
        entity.setEvaluate(EvaluateType.NOEVALUATION.key);
        // 身份 1：住户；2：物业(物业管理人员，表单直接完成)
        entity.setIdentity(client == ClientType.HOUSEHOLD.value() ? UserStatus.RESIDENT.key : UserStatus.PROPERTY.key);
        entity.setContact(userVO.getPhone());
        entity.setUserId(userVO.getId());
        entity.setCreateId(userVO.getId());
        return entity;
    }

    /**
     * 回填故障地址
     *
     * @param entity
     */
    private void BackfillFault(Fault entity) {
        // 故障地址为空，根据id获取地址
        if (StringUtil.isEmpty(entity.getFaultAddress())) {
            if (entity.getDeviceId() != null && entity.getFaultItem().equals(FaultItemType.DOORCONTROL.key())) {
                Door door = doorFacade.getDoorById(entity.getDeviceId());
                Building building = buildingFacade.findOne(door.getBuildingId());
                entity.setFaultAddress(building == null ? door.getName() : building.getName());
            } else if (entity.getRoomId() != null) {
                Room room = roomFacade.findOne(entity.getRoomId());
                Building building = buildingFacade.findOne(room.getBuildingId());
                entity.setFaultAddress(String.format("%s%s", building.getName(), room.getName()));
                entity.setCommunityId(entity.getCommunityId() == null ? room.getCommunityId() : entity.getCommunityId());
            } else if (entity.getBuildingId() != null) {
                Building building = buildingFacade.findOne(entity.getBuildingId());
                entity.setFaultAddress(building == null ? null : building.getName());
                entity.setCommunityId(entity.getCommunityId() == null ? building.getCommunityId() : entity.getCommunityId());
            } else if (entity.getBuildingId() == null && entity.getDeviceId() == null
                    && entity.getFaultItem() == FaultItemType.OTHER.key()) {
                entity.setFaultAddress("公共区域");
            }
        }
    }
}
