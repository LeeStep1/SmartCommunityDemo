package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.AppSubject;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.system.dto.ClientAndPartnerAndOsDTO;
import cn.bit.common.facade.system.model.AppVersion;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.UserStatus;
import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.facade.model.system.App;
import cn.bit.facade.model.system.Feedback;
import cn.bit.facade.model.system.Slide;
import cn.bit.facade.model.system.Version;
import cn.bit.facade.service.communityIoT.DoorRecordFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.communityIoT.ElevatorRecordFacade;
import cn.bit.facade.service.system.AppFacade;
import cn.bit.facade.service.system.FeedbackFacade;
import cn.bit.facade.service.system.SlideFacade;
import cn.bit.facade.service.system.VersionFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.communityIoT.door.DoorRecordRequest;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorPageResult;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorRecordRequest;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorVO;
import cn.bit.facade.vo.communityIoT.elevator.FindElevatorListRequest;
import cn.bit.facade.vo.system.FeedbackVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/v1/sys", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SystemController {

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private DoorRecordFacade doorRecordFacade;

    @Autowired
    private ElevatorRecordFacade elevatorRecordFacade;

    @Autowired
    private SlideFacade slideFacade;

    @Autowired
    private FeedbackFacade feedbackFacade;

    @Autowired
    private VersionFacade versionFacade;

    @Autowired
    private AppFacade appFacade;

    @Autowired
    private ElevatorFacade elevatorFacade;

    @Resource
    private SystemFacade systemFacade;

    /*===============================================  DoorRecord  =================================================*/

    /**
     * 保存门禁使用记录
     * @param entity
     * @return
     * @since 2018-08-09 前端不使用该接口
     */
    @PostMapping(name = "保存门禁使用记录(旧)", path = "/door-record/add")
    @Authorization
    @Deprecated
    public ApiResult<DoorRecord> addDoorRecord(@RequestBody @Valid DoorRecord entity) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        Integer client = appSubject.getClient();
        if (client == ClientType.HOUSEHOLD.value()) {
            entity.setUserStatus(UserStatus.RESIDENT.key);
        }
        if (client == ClientType.PROPERTY.value()) {
            entity.setUserStatus(UserStatus.PROPERTY.key);
        }
        UserVO vo = userFacade.getUserById(client, appSubject.getPartner(), entity.getUserId());
        if (vo != null){
            entity.setUserName(vo.getName());
            entity.setPhone(vo.getPhone());
            entity.setHeadImg(vo.getHeadImg());
        }
        entity.setCreatorId(SessionUtil.getTokenSubject().getUid());
        entity = doorRecordFacade.addDoorRecord(entity);
        return ApiResult.ok(entity);
    }

    /**
     * 删除门禁使用记录
     * @param id
     * @return
     */
    @GetMapping(name = "删除门禁使用记录", path = "/door-record/{id}/delete")
    @Authorization
    public ApiResult deleteDoorRecord(@PathVariable ObjectId id) {
        DoorRecord entity = doorRecordFacade.deleteDoorRecordById(id);
        if (entity == null) {
            ApiResult.error(-1,"删除失败");
        }
        return ApiResult.ok();
    }
    /**
     * 根据id查询门禁使用记录
     * @param id
     * @return
     */
    @GetMapping(name = "门禁使用记录详情", path = "/door-record/{id}/detail")
    @Authorization
    public ApiResult<DoorRecord> getDoorRecord(@PathVariable ObjectId id) {
        DoorRecord entity = doorRecordFacade.findById(id);
        if (entity == null) {
            return ApiResult.error(-1,"查询设备使用记录失败");
        }
        return ApiResult.ok(entity);
    }

    /**
     * 分页查询门禁使用记录
     * @param doorRecordRequest
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "门禁使用记录分页", path = "/door-record/page")
    @Authorization
    public ApiResult queryDoorRecordPage(@RequestBody @Valid DoorRecordRequest doorRecordRequest,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        Page<DoorRecord> list = doorRecordFacade.getDoorRecords(doorRecordRequest, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 批量添加门禁使用记录
     * @param map
     * @return
     */
    @PostMapping(name = "批量新增门禁使用记录", path = "door-record/batch")
    @Authorization
    public ApiResult batchAddDoorRecord(@RequestBody @Valid Map<String, List<DoorRecord>> map){
        List<DoorRecord> entities = map.get("records");
        if (entities == null || entities.size()== 0) {
            return ApiResult.ok();
        }
        List<DoorRecord> removeDoorRecords = new ArrayList<>();
        for (DoorRecord entity : entities) {
            if (containsIgnoreDoorRecord(entity)) {
                removeDoorRecords.add(entity);
                continue;
            }
            Integer client = Objects.equals(UserStatus.RESIDENT.key, entity.getUserStatus())
                    ? ClientType.HOUSEHOLD.value() : ClientType.PROPERTY.value();
            UserVO vo = userFacade.getUserById(client, SessionUtil.getAppSubject().getPartner(), entity.getUserId());
            if(vo != null){
                entity.setUserName(vo.getName());
                entity.setPhone(vo.getPhone());
                entity.setHeadImg(vo.getHeadImg());
            }
            entity.setCreateAt(new Date());
            entity.setDataStatus(DataStatusType.VALID.KEY);
            entity.setCreatorId(SessionUtil.getTokenSubject().getUid());
        }
        log.info("无效的门禁使用记录：{}", removeDoorRecords);
        entities.removeAll(removeDoorRecords);
        doorRecordFacade.batchAddDoorRecord(entities);
        return ApiResult.ok(entities);
    }

    /**
     * 统计当天开门次数
     * @param request
     * @return
     */
    @PostMapping(name = "统计当天开门次数", path = "/door-record/count")
    @Authorization
    public ApiResult countDoorRecord(@RequestBody DoorRecordRequest request) {
        Long num = doorRecordFacade.countByCommunityIdAndDate(
                SessionUtil.getCommunityId(), request.getStartDate(), request.getEndDate());
        return ApiResult.ok(num);
    }

    private boolean containsIgnoreDoorRecord(DoorRecord entity) {
        return Objects.isNull(entity.getUserStatus())
                || StringUtil.isEmpty(entity.getKeyNo())
                || Objects.isNull(entity.getResultCode())
                // doorId和mac地址若同时为空就放到可忽略的门禁列表中
                || Objects.isNull(entity.getDoorId()) && StringUtil.isBlank(entity.getMacAddress())
                // 如果mac地址不为空需要校验正则
                || (StringUtil.isNotBlank(entity.getMacAddress()) && !Pattern.matches("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}", entity.getMacAddress()))
                // 开门失败的记录不保存
                || entity.getResultCode() != 1;
    }
    /*===============================================  ElevatorRecord  ===============================================*/

    /**
     * 保存梯禁使用记录
     * @param entity
     * @return
     * @since 2018-08-09 前端不使用
     */
    @PostMapping(name = "保存梯禁使用记录(旧)", path = "/elevator-record/add")
    @Authorization
    @Deprecated
    public ApiResult<ElevatorRecord> addElevatorRecord(@RequestBody @Valid ElevatorRecord entity) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        Integer client = appSubject.getClient();
        if (client == ClientType.HOUSEHOLD.value()) {
            entity.setUserStatus(UserStatus.RESIDENT.key);
        }
        if (client == ClientType.PROPERTY.value()) {
            entity.setUserStatus(UserStatus.PROPERTY.key);
        }
        UserVO vo = userFacade.getUserById(client, appSubject.getPartner(), entity.getUserId());
        if(vo != null){
            entity.setUserName(vo.getName());
            entity.setPhone(vo.getPhone());
            entity.setHeadImg(vo.getHeadImg());
        }
        entity.setCreatorId(SessionUtil.getTokenSubject().getUid());
        entity = elevatorRecordFacade.addElevatorRecord(entity);
        return ApiResult.ok(entity);
    }

    /**
     * 删除梯禁使用记录
     * @param id
     * @return
     */
    @GetMapping(name = "删除梯禁使用记录", path = "/elevator-record/{id}/delete")
    @Authorization
    public ApiResult deleteElevatorRecord(@PathVariable ObjectId id) {
        ElevatorRecord entity = elevatorRecordFacade.deleteElevatorRecordById(id);
        if (entity == null) {
            ApiResult.error(-1,"删除失败");
        }
        return ApiResult.ok();
    }

    /**
     * 根据id查询梯禁使用记录
     * @param id
     * @return
     */
    @GetMapping(name = "梯禁使用记录详情", path = "/elevator-record/{id}/detail")
    @Authorization
    public ApiResult<ElevatorRecord> getElevatorRecord(@PathVariable ObjectId id) {
        ElevatorRecord entity = elevatorRecordFacade.findById(id);
        if (entity == null) {
            return ApiResult.error(-1,"查询设备使用记录失败");
        }
        return ApiResult.ok(entity);
    }

    /**
     * 分页查询梯禁使用记录
     * @param elevatorRecordRequest
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "梯禁使用记录分页", path = "/elevator-record/page")
    @Authorization
    public ApiResult queryElevatorRecordPage(@RequestBody @Valid ElevatorRecordRequest elevatorRecordRequest,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        Page<ElevatorRecord> list = elevatorRecordFacade.getElevatorRecords(elevatorRecordRequest, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 批量添加梯禁使用记录
     * @param map
     * @return
     */
    @PostMapping(name = "批量新增梯禁使用记录", path = "elevator-record/batch")
    @Authorization
    public ApiResult batchAddElevatorRecord(@RequestBody @Valid Map<String, List<ElevatorRecord>> map){
        List<ElevatorRecord> entities = map.get("records");
        if (entities == null || entities.size()== 0) {
            return ApiResult.ok();
        }

        List<ElevatorVO> elevators = obtainElevatorsFromIoT(entities);
        List<ElevatorRecord> removeElevatorRecords = new ArrayList<>();
        for (ElevatorRecord entity : entities) {
            // 如果没有上传用户身份或者mac地址不正确就忽略这条使用记录
            if (containsIgnoreElevatorRecord(entity)) {
                removeElevatorRecords.add(entity);
                continue;
            }
            Integer client = Objects.equals(UserStatus.RESIDENT.key, entity.getUserStatus())
                    ? ClientType.HOUSEHOLD.value() : ClientType.PROPERTY.value();

            UserVO vo = userFacade.getUserById(client, SessionUtil.getAppSubject().getPartner(), entity.getUserId());
            if (vo != null) {
                entity.setUserName(vo.getName());
                entity.setPhone(vo.getPhone());
                entity.setHeadImg(vo.getHeadImg());
            }
	        entity.setCreateAt(new Date());
	        entity.setDataStatus(DataStatusType.VALID.KEY);
	        entity.setCreatorId(SessionUtil.getTokenSubject().getUid());
            completeElevatorRecordInfo(entities, elevators, entity);
        }
        log.info("无效的电梯使用记录：{}", removeElevatorRecords);
        entities.removeAll(removeElevatorRecords);
        elevatorRecordFacade.batchAddElevatorRecord(entities);
        // 返回正确的使用记录
        return ApiResult.ok(entities);
    }

    private boolean containsIgnoreElevatorRecord(ElevatorRecord entity) {
        return entity.getUserStatus() == null
                || StringUtil.isBlank(entity.getMacAddress())
                || !Pattern.matches("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}", entity.getMacAddress())
                || entity.getResultCode() != 1;
    }

    private List<ElevatorVO> obtainElevatorsFromIoT(List<ElevatorRecord> entities) {
        List<ElevatorVO> elevators = Collections.emptyList();
        FindElevatorListRequest elevatorRequest = new FindElevatorListRequest();
        elevatorRequest.setCommunityId(SessionUtil.getCommunityId().toHexString());
        elevatorRequest.setDataStatus(Collections.singleton(DataStatusType.VALID.KEY));
        elevatorRequest.setMacAddress(entities.stream().filter(entity -> entity.getDeviceId() == null)
                .map(ElevatorRecord::getMacAddress).collect(Collectors.toSet()));
        if (!CollectionUtils.isEmpty(elevatorRequest.getMacAddress())) {
            ElevatorPageResult result = elevatorFacade.getElevators(elevatorRequest, null, null);
            elevators = result.getData().getRecords();
        }
        return elevators;
    }

    private void completeElevatorRecordInfo(List<ElevatorRecord> entities, List<ElevatorVO> elevators, ElevatorRecord entity) {
        if (entity.getDeviceId() == null || entity.getMacAddress() == null) {
            for (ElevatorVO elevator : elevators) {
                if (elevator.getMacAddress().equals(entity.getMacAddress())) {
                    entity.setDeviceId(elevator.getId());
                    entity.setDeviceName(elevator.getName());
                    break;
                }
                if (elevator.getId().equals(entity.getDeviceId())) {
                    entity.setDeviceName(elevator.getName());
                    entity.setMacAddress(elevator.getMacAddress());
                    break;
                }
            }
            if (entity.getDeviceId() == null) {
                entities.remove(entity);
            }
        }
    }

    /*==================================================   BizSlide  ===================================================*/

    /**
     * 新增轮播图
     * @param slide
     * @return
     */
    @PostMapping(name = "新增轮播图", path = "/slide/add")
    @Authorization
    public ApiResult<Slide> addSlide(@Validated(Slide.AddSlide.class) @RequestBody Slide slide){
        ObjectId uid = SessionUtil.getTokenSubject().getUid();
        if (uid != null) {
            slide.setCreatorId(uid);
        }
        return ApiResult.ok(slideFacade.addSlide(slide));
    }

    /**
     * 删除轮播图
     * @param id
     * @return
     */
    @GetMapping(name = "删除轮播图", path = "/slide/{id}/delete")
    @Authorization
    public ApiResult deleteSlide(@PathVariable("id") ObjectId id){
        if (id == null) {
            return ApiResult.error(-1, "轮播图id不能为空");
        }
        Slide slide = slideFacade.deleteSlideById(id);
        if (slide == null) {
            return ApiResult.error(-1, "删除失败,不存在该轮播图信息");
        }
        return ApiResult.ok();
    }

    /**
     * 修改轮播图
     * @param slide
     * @return
     */
    @PostMapping(name = "编辑轮播图", path = "/slide/edit")
    @Authorization
    public ApiResult updateSlide(@RequestBody @Validated(Slide.UpdateSlide.class) Slide slide){
        Slide entity = slideFacade.updateSlide(slide);
        if (entity == null) {
            ApiResult.error(-1,"修改轮播图失败");
        }
        return ApiResult.ok(entity);
    }

    /**
     * 根据id发布轮播图
     * @param id
     * @return
     */
    @GetMapping(name = "发布轮播图", path = "/slide/{id}/publish")
    @Authorization
    public ApiResult publishSlide(@PathVariable ObjectId id){
        if (id == null) {
            return ApiResult.error(-1, "轮播图id不能为空");
        }
        Slide slide = slideFacade.publishSlideById(id);
        if (slide == null) {
            return ApiResult.error(-1, "发布轮播图失败");
        }
        return ApiResult.ok();
    }

    /**
     * 根据id撤回轮播图
     * @param id
     * @return
     */
    @GetMapping(name = "撤回轮播图", path = "/slide/{id}/retract")
    @Authorization
    public ApiResult retractSlide(@PathVariable ObjectId id){
        if (id == null) {
            return ApiResult.error(-1, "轮播图id不能为空");
        }
        Slide slide = slideFacade.retractSlideById(id);
        if (slide == null) {
            return ApiResult.error(-1, "撤回轮播图失败");
        }
        return ApiResult.ok();
    }


    /**
     * 根据id获取轮播图
     * @param id
     * @return
     */
    @GetMapping(name = "轮播图详情", path = "/slide/{id}/detail")
    public ApiResult getSlide(@PathVariable("id") ObjectId id) {
        Slide slide = slideFacade.getSlideById(id);
        if (slide == null) {
            return ApiResult.error(-1, "没有该轮播图信息");
        }
        return ApiResult.ok(slide);
    }

    /**
     * 分页查询已发布轮播图列表
     * @param slide
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "已发布轮播图分页", path = "/slide/page")
    @Authorization
    public ApiResult queryPublishedSlidePage(@RequestBody Slide slide,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size){
        Page<Slide> list = slideFacade.getAllSlidesPage(slide, page, size);
        return ApiResult.ok(list);
    }
    /**
     * 查询已发布轮播图列表(不分页)
     * @param slide
     * @return
     */
    @PostMapping(name = "已发布轮播图列表", path = "/slide/list")
    @Authorization
    public ApiResult queryPublishedSlideList(@RequestBody Slide slide){
        List<Slide> list = slideFacade.getPublishedSlidesList(slide);
        return ApiResult.ok(list);
    }
    /*==================================================   Version   ================================================*/
    /**
     * 新增APP版本信息
     * @param version
     * @return
     */
    @PostMapping(name = "新增APP版本", path = "/version/add")
    @Authorization
    public ApiResult<Version> addVersion(@Validated @RequestBody Version version){
        UserVO vo = SessionUtil.getCurrentUser();
        if (vo!=null) {
            version.setCreatorId(vo.getId());
        }
        version = versionFacade.addVersion(version);
        return ApiResult.ok(version);
    }

    /**
     * 删除APP版本信息
     * @param id
     * @return
     */
    @GetMapping(name = "删除APP版本", path = "/version/{id}/delete")
    @Authorization
    public ApiResult deleteVersion(@PathVariable("id") ObjectId id){
        Version version = versionFacade.deleteVersionById(id);
        if (version == null) {
            return ApiResult.error(-1, "删除失败");
        }
        return ApiResult.ok();
    }

    /**
     * 修改APP版本信息
     * @param version
     * @return
     */
    @PostMapping(name = "编辑APP版本", path = "/version/edit")
    @Authorization
    public ApiResult updateVersion(@RequestBody @Validated Version version){
        if (version.getId() == null) {
            return ApiResult.error(-1, "版本id不能为空");
        }
        return ApiResult.ok(versionFacade.updateVersion(version));
    }

    /**
     * 根据id发布版本
     * @param id
     * @return
     */
    @GetMapping(name = "发布APP版本", path = "/version/{id}/publish")
    @Authorization
    public ApiResult publishVersion(@PathVariable ObjectId id){
        if (id == null) {
            return ApiResult.error(-1, "版本id不能为空");
        }
        Version version = versionFacade.publishVersionById(id);
        if (version == null) {
            return ApiResult.error(-1, "发布版本失败");
        }
        return ApiResult.ok(version);
    }


    /**
     * 根据id获取版本信息
     * @param id
     * @return
     */
    @GetMapping(name = "APP版本详情", path = "/version/{id}/detail")
    public ApiResult getVersion(@PathVariable("id") ObjectId id) {
        Version version = versionFacade.getVersionById(id);
        if (version == null) {
            return ApiResult.error(-1, "没有该版本信息");
        }
        return ApiResult.ok(version);
    }

    /**
     * APP版本信息分页
     * @param appId
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "APP版本分页", path = "/{appId}/version/page")
    @Authorization
    public ApiResult queryVersionPage(@PathVariable ObjectId appId,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size){
        if (appId == null) {
            return ApiResult.error(-1, "appId不能为空");
        }
        Page<Version> list = versionFacade.getVersionsByAppId(appId, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 获取版本信息列表
     * @param appId
     * @return
     */
    @GetMapping(name = "某APP版本列表", path = "/{appId}/version/list")
    @Authorization
    public ApiResult getVersionList(@PathVariable ObjectId appId){
        if (appId == null) {
            return ApiResult.error(-1, "appId不能为空");
        }
        List<Version> list = versionFacade.getAllVersionsByAppId(appId);
        return ApiResult.ok(list);
    }

    /**
     * 移动端获取最新的发布版本
     * @param appId
     * @param sequence
     * @return
     */
    @GetMapping(name = "移动端获取某APP最新版本", path = "/{appId}/version/{sequence}/new")
    public ApiResult getNewVersion(@PathVariable ObjectId appId, @PathVariable String sequence) {
        if (appId == null) {
            return ApiResult.error(-1, "appId不能为空");
        }
        if (StringUtil.isBlank(sequence)) {
            return ApiResult.error(-1, "当前版本号不能为空");
        }

        Version version = null;
        AppVersion appVersion = systemFacade.getNewAppVersionByAppIdAndSequence(appId, sequence);
        if (appVersion != null) {
            version = new Version();
            BeanUtils.copyProperties(appVersion, version);
            version.setPublished(true);
            version.setPublishAt(appVersion.getCreateAt());
            version.setCreatorId(appVersion.getCreator());
        }
        return ApiResult.ok(version);
    }

    @GetMapping(name = "根据客户端代码及操作系统获取最新的应用版本信息", path = "/{client}/{partner}/{os}/version/new")
    public ApiResult getNewVersion(@PathVariable Integer client, @PathVariable Integer partner, @PathVariable Integer os) {
        ClientAndPartnerAndOsDTO dto = new ClientAndPartnerAndOsDTO();
        dto.setClient(client);
        dto.setPartner(partner);
        dto.setOs(os);
        return ApiResult.ok(systemFacade.getNewAppVersionByClientAndPartnerAndOs(dto));
    }

    /*===============================================   Feedback   =======================================================*/
    @GetMapping(name = "根据客户端代码获取场景列表", path = "/use-cases")
    @Authorization
    public ApiResult listUseCases() {
        AppSubject appSubject = SessionUtil.getAppSubject();
        return ApiResult.ok(systemFacade.listUseCasesByClientAndPartner(appSubject.getClient(), appSubject.getPartner()));
    }

    /**
     * 新增反馈信息
     * @param feedback
     * @return
     */
    @PostMapping(name = "新增反馈信息", path = "/feedback/add")
    @Authorization
    public ApiResult addFeedback(@Validated @RequestBody Feedback feedback){
        UserVO vo = SessionUtil.getCurrentUser();
        if (vo != null) {
            feedback.setCreatorId(vo.getId());
            feedback.setUserName(vo.getName());
            feedback.setPhone(vo.getPhone());
        }
        Feedback entity = feedbackFacade.addFeedback(feedback);
        if (entity == null) {
            return ApiResult.error(-1, "添加失败");
        }
        return ApiResult.ok(entity);
    }

    @PostMapping(name = "保存反馈信息", path = "/feedback/save")
    @Authorization
    public ApiResult saveFeedback(@RequestBody FeedbackVO feedbackVO,
                                  @RequestHeader("DEVICE-TYPE") String device,
                                  @RequestHeader("OS-VERSION") String osVersion) {
        UserVO userVO = SessionUtil.getCurrentUser();
        cn.bit.common.facade.system.model.Feedback feedback = new cn.bit.common.facade.system.model.Feedback();
        BeanUtils.copyProperties(feedbackVO, feedback);
        AppSubject appSubject = SessionUtil.getAppSubject();
        feedback.setClient(appSubject.getClient());
        feedback.setPartner(appSubject.getPartner());
        feedback.setDevice(device);
        feedback.setOs(appSubject.getOsEnum().phrase() + " " + osVersion);
        feedback.setCreator(userVO.getId());
        feedback.setPhone(userVO.getPhone());
        feedback = systemFacade.createFeedback(feedback);
        return ApiResult.ok(feedback);
    }

    /**
     * 删除反馈信息
     * @param id
     * @return
     */
    @GetMapping(name = "删除反馈信息", path = "/feedback/{id}/delete")
    @Authorization
    public ApiResult deleteFeedback(@PathVariable("id") ObjectId id){
        Feedback feedback = feedbackFacade.deleteFeedbackById(id);
        if (feedback == null) {
            return ApiResult.error(-1,"删除失败");
        }
        return ApiResult.ok();
    }

    /**
     * 修改反馈信息
     * @param feedback
     * @return
     */
    @PostMapping(name = "编辑反馈信息", path = "/feedback/edit")
    @Authorization
    public ApiResult updateFeedback(@Validated @RequestBody Feedback feedback){
        if (feedback.getId() == null) {
            return ApiResult.error(-1, "反馈id不能为空");
        }
        return ApiResult.ok(feedbackFacade.updateFeedback(feedback));
    }

    /**
     * 根据id获取信息
     * @param id
     * @return
     */
    @GetMapping(name = "反馈信息详情", path = "/feedback/{id}/detail")
    @Authorization
    public ApiResult getFeedback(@PathVariable("id") ObjectId id) {
        Feedback feedback = feedbackFacade.getFeedbackById(id);
        if (feedback != null) {
            return ApiResult.ok(feedback);
        }
        return ApiResult.error(-1, "没有该反馈者信息");
    }

    /**
     * 反馈信息分页
     * @param appId
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "某APP反馈信息分页", path = "/{appId}/feedback/page")
    @Authorization
    public ApiResult queryFeedbackPage(@PathVariable ObjectId appId,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size){
        if (appId == null) {
            return ApiResult.error(-1, "appId不能为空");
        }
        Page<Feedback> list = feedbackFacade.getFeedbacksByAppId(appId, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 获取反馈信息列表
     * @param appId
     * @return
     */
    @GetMapping(name = "某APP反馈信息列表", path = "/{appId}/feedback/list")
    @Authorization
    public ApiResult getFeedbackList(@PathVariable ObjectId appId){
        if (appId == null) {
            return ApiResult.error(-1, "appId不能为空");
        }
        List<Feedback> list = feedbackFacade.getFeedbacksByAppId(appId);
        return ApiResult.ok(list);
    }

    /*===============================================   App   =======================================================*/

    /**
     * 新增APP信息
     * @param app
     * @return
     */
    @PostMapping(name = "新增APP信息", path = "/app/add")
    @Authorization
    public ApiResult addApp(@Validated @RequestBody App app){
        ObjectId userId = SessionUtil.getTokenSubject().getUid();
        if (userId!=null) {
            app.setCreatorId(userId);
        }
        App entity = appFacade.addApp(app);
        if (entity == null) {
            return ApiResult.error(-1, "添加失败");
        }
        return ApiResult.ok(entity);
    }

    /**
     * 删除APP信息
     * @param id
     * @return
     */
    @GetMapping(name = "删除APP信息", path = "/app/{id}/delete")
    @Authorization
    public ApiResult deleteApp(@PathVariable("id") ObjectId id){
        App app = appFacade.deleteAppById(id);
        if (app == null) {
            return ApiResult.error(-1,"删除失败");
        }
        return ApiResult.ok();
    }

    /**
     * 修改APP信息
     * @param app
     * @return
     */
    @PostMapping(name = "编辑APP信息", path = "/app/edit")
    @Authorization
    public ApiResult updateApp(@Validated @RequestBody App app){
        if (app.getId() == null) {
            return ApiResult.error(-1, "应用id不能为空");
        }
        App entity = appFacade.updateApp(app);
        if (entity == null) {
            return ApiResult.error(-1, "没有该应用信息");
        }
        return ApiResult.ok(entity);
    }

    /**
     * 根据id获取APP信息
     * @param id
     * @return
     */
    @GetMapping(name = "APP信息详情", path = "/app/{id}/detail")
    @Authorization
    public ApiResult getApp(@PathVariable("id") ObjectId id) {
        App app = appFacade.getAppById(id);
        if (app == null || app.getDataStatus() == DataStatusType.INVALID.KEY) {
            return ApiResult.error(-1, "没有该应用信息");
        }
        return ApiResult.ok(app);
    }

    /**
     * APP信息分页
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "APP信息分页", path = "/app/page")
    @Authorization
    public ApiResult queryAppPage(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size){
        Page<App> list = appFacade.getApps(page, size);
        return ApiResult.ok(list);
    }
}