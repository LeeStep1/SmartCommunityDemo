package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.enums.*;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.CommunityUser;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.communityIoT.MiliConnection;
import cn.bit.facade.service.user.CardFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.community.UpdateDoorElevatorLinkage;
import cn.bit.facade.vo.communityIoT.DeviceRequest;
import cn.bit.facade.vo.communityIoT.DeviceVO;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import cn.bit.facade.vo.communityIoT.door.DoorVo;
import cn.bit.facade.vo.communityIoT.door.freeview.UserFeature;
import cn.bit.facade.vo.user.Device;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.string.StringUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.CommonBizException.UNKNOWN_ERROR;
import static cn.bit.facade.exception.user.CardBizException.CARD_NOT_EXIST;


@RestController
@Slf4j
@RequestMapping(value = "/v1/communityIoT", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DoorController {

    private static final Pattern PATTERN_MAC = Pattern.compile("([A-Fa-f0-9]{2})");

    // 门禁设备
    @Autowired
    private DoorFacade doorFacade;

    // 房屋认证
    @Autowired
    private UserToRoomFacade userToRoomFacade;

    // 物业人员
    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private UserFacade userFacade;

    // 楼栋
    @Autowired
    private BuildingFacade buildingFacade;

    // 社区
    @Autowired
    private CommunityFacade communityFacade;

    // 卡片
    @Autowired
    private CardFacade cardFacade;

    @Autowired
    private MiliConnection miligcConnection;

    // 电梯
    @Autowired
    private ElevatorFacade elevatorFacade;

    /**
     * 添加门禁消息
     *
     * @param door
     * @return
     */
    @PostMapping(name = "新增门禁", path = "/door/add")
    @Authorization
    public ApiResult addDoor(@Validated(Door.AddOwner.class) @RequestBody Door door) {
        door.setCreatorId(SessionUtil.getTokenSubject().getUid());
        door.setCommunityId(SessionUtil.getCommunityId());
        door = doorFacade.addDoor(door);
        communityFacade.addToSetBrandsById(door.getCommunityId(), Collections.singleton("door" + door.getBrandNo()));
        return ApiResult.ok(door);
    }

    /**
     * 编辑门禁消息
     *
     * @param door
     * @return
     */
    @PostMapping(name = "编辑门禁", path = "/door/edit")
    @Authorization
    public ApiResult updateDoor(@Validated(Door.EditDoor.class) @RequestBody Door door) {
        return ApiResult.ok(doorFacade.updateDoor(door));
    }

    /**
     * 删除门禁消息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除门禁", path = "/door/{id}/delete")
    @Authorization
    public ApiResult deleteDoor(@PathVariable ObjectId id) {
        Door door = doorFacade.deleteDoor(id);
        if (door == null) {
            throw UNKNOWN_ERROR;
        }
        Door request = new Door();
        request.setCommunityId(door.getCommunityId());
        request.setBrandNo(door.getBrandNo());
        Page<DoorVo> doorList = doorFacade.getAllDoorsRecord(request, 1, 10);
        if (doorList.getTotal() == 0) {
            communityFacade.pullAllBrandsById(door.getCommunityId(), Collections.singleton("door" + door.getBrandNo()));
        }
        return ApiResult.ok();
    }

    /**
     * 根据id获取门禁消息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "门禁详情", path = "/door/{id}/detail")
    @Authorization
    public ApiResult getDoor(@PathVariable ObjectId id) {
        return ApiResult.ok(doorFacade.getDoorById(id));
    }

    /**
     * 通过社区分页获取门禁列表
     *
     * @param doorRequest
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "社区门禁分页", path = "/door/page")
    @Authorization
    public ApiResult getDoors(@RequestBody DoorRequest doorRequest,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        doorRequest.setCommunityId(SessionUtil.getCommunityId());
        return ApiResult.ok(doorFacade.getBluetoothDoors(doorRequest, page, size));
    }

    /**
     * 通过社区获取门禁列表
     *
     * @param doorRequest
     * @return
     */
    @PostMapping(name = "社区门禁列表", path = "/door/list")
    @Authorization
    public ApiResult getDoors(@RequestBody DoorRequest doorRequest) {
        doorRequest.setCommunityId(SessionUtil.getCommunityId());
        if (doorRequest.getBuildingId().isEmpty()) {
            doorRequest.setBuildingId(
                    new HashSet<>(buildingFacade.getBuildingIdsByCommunityId(doorRequest.getCommunityId())));
        }
        // 蓝牙设备
        doorRequest.setServiceId(Collections.singleton(DoorService.BLUETOOTH.KEY));
        return ApiResult.ok(doorFacade.getServiceDoors(doorRequest));
    }

    /**
     * 通过用户分页获取门禁列表
     *
     * @param doorRequest
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "用户门禁分页", path = "/door/auth/page")
    @Authorization
    public ApiResult getAuthorizedDoors(@RequestBody DoorRequest doorRequest,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        Set<ObjectId> buildingIds = getBuildingIdsByCurrentUser(SessionUtil.getCommunityId());
        if (buildingIds.isEmpty()) {
            // 没有访问门禁的权限
            return ApiResult.ok(Collections.emptyList());
        }
        doorRequest.setBuildingId(buildingIds);
        return ApiResult.ok(doorFacade.getBluetoothDoors(doorRequest, page, size));
    }

    /**
     * 通过用户获取门禁列表
     * 蓝牙接口
     *
     * @param doorRequest
     * @return
     */
    @PostMapping(name = "用户蓝牙门禁列表", path = "/door/auth/list")
    @Authorization(verifyApi = false)
    public ApiResult getAuthorizedDoors(@RequestBody DoorRequest doorRequest) {
        // TODO 区分离线还是在线设备 延后处理
        return getDoorInfoByServiceId(doorRequest, SessionUtil.getCompanyId(), DoorService.BLUETOOTH.KEY);
    }

    /**
     * 通过用户获取门禁列表
     * 远程接口
     *
     * @param doorRequest
     * @return
     */
    @PostMapping(name = "用户远程门禁列表", path = "/door/remote/auth/list")
    @Authorization(verifyApi = false)
    public ApiResult getRemoteDoors(@RequestBody DoorRequest doorRequest) {
        return getDoorInfoByServiceId(doorRequest, SessionUtil.getCompanyId(), DoorService.REMOTE.KEY);
    }

    private ApiResult getDoorInfoByServiceId(DoorRequest doorRequest, ObjectId companyId, int doorService) {
        doorRequest.setServiceId(Collections.singleton(doorService));
        doorRequest.setUserId(SessionUtil.getTokenSubject().getUid());
        doorRequest.setCommunityId(SessionUtil.getCommunityId());
        Set<ObjectId> buildingIds = getBuildingIdsByCurrentUser(doorRequest.getCommunityId());
        if (SessionUtil.getAppSubject().getClient() == ClientType.PROPERTY.value()) {
            UserToProperty entity = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                    SessionUtil.getTokenSubject().getUid(), doorRequest.getCommunityId(), companyId);
            if (entity != null && entity.getBuildingIds().size() > 0) {
                buildingIds.addAll(entity.getBuildingIds());
            }
        }

        if (buildingIds.size() == 0) {
            // 没有访问门禁的权限
            return ApiResult.ok(Collections.emptyList());
        }

        doorRequest.setBuildingId(buildingIds);

        Card toQuery = new Card();
        toQuery.setUserId(SessionUtil.getTokenSubject().getUid());
        toQuery.setCommunityId(doorRequest.getCommunityId());
        // 查询虚拟卡
        toQuery.setKeyType(CertificateType.PHONE_MAC.KEY);
        Card card = cardFacade.getUserCardInCommunity(toQuery);

        if (card == null) {
            log.warn("card not exist...");
            throw CARD_NOT_EXIST;
        }

        doorRequest.setKeyId(card.getKeyId());
        doorRequest.setKeyNo(card.getKeyNo());
        doorRequest.setKeyType(CertificateType.PHONE_MAC.KEY);
        return ApiResult.ok(doorFacade.listDoorInfoByDoorRequest(doorRequest));
    }

    //通过用户获取楼宇ID
    private Set<ObjectId> getBuildingIdsByCurrentUser(ObjectId communityId) {
        return userToRoomFacade.getBuildingsByUserId(communityId, SessionUtil.getTokenSubject().getUid());
    }

    /**
     * 绑定门禁到楼栋或社区
     *
     * @param entity
     * @return
     * @throws BizException
     */
    @PostMapping(name = "绑定门禁", path = "/door/bindDevice")
    @Authorization
    public ApiResult bindDevice(@Validated(Door.BindDoorToCommunity.class) @RequestBody Door entity) throws BizException {
        entity.setCommunityId(SessionUtil.getCommunityId());

        if (entity.getBuildingId() != null) {
            Building building = buildingFacade.findOne(entity.getBuildingId());
            if (building == null) {
                return ApiResult.error(-1, "楼栋不存在");
            }
        }
        Door door = doorFacade.bindDoor(entity);
        return ApiResult.ok(door);
    }


    /**
     * 门禁资料管理列表
     *
     * @param door
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    @PostMapping(name = "门禁资料管理分页", path = "/record/door/list")
    @Authorization
    public ApiResult getCommunityDoorRecordList(@RequestBody Door door,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) throws BizException {
        door.setCommunityId(SessionUtil.getCommunityId());
        Page<DoorVo> doorVos = doorFacade.getAllDoorsRecord(door, page, size);
        if (CollectionUtils.isEmpty(doorVos.getRecords())) {
            return ApiResult.ok(doorVos.getRecords());
        }
        Community community = communityFacade.findOne(door.getCommunityId());
        // 用于保存楼栋名称
        Map<ObjectId, String> buildingMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(doorVos.getRecords().stream().map(DoorVo::getBuildingId).collect(Collectors.toSet()))) {
            List<Building> buildings = buildingFacade.findByIds(doorVos.getRecords().stream().map(DoorVo::getBuildingId).collect(Collectors.toList()));
            buildings.forEach(building -> buildingMap.put(building.getId(), building.getName()));
        }

        doorVos.getRecords().forEach(entity -> {
            entity.setCommunityName(community.getName());
            if (entity.getBuildingId() != null) {
                entity.setBuildingName(buildingMap.get(entity.getBuildingId()));
            }
        });
        return ApiResult.ok(doorVos);
    }

    /*===================================================【door end】========================================================*/

    /*===================================================【remote control start】========================================================*/

    /**
     * 远程开门
     *
     * @param entity
     * @return
     * @throws Exception
     */
    @PostMapping(name = "远程开门", path = "/door/remoteOpenDoor")
    @Authorization
    public ApiResult remoteOpenDoor(@Validated(Door.RemoteOpen.class) @RequestBody Door entity) throws Exception {
        return ApiResult.ok(doorFacade.remoteOpenDoor(entity, SessionUtil.getTokenSubject().getUid().toHexString()));
    }

    @GetMapping(name = "门梯联动查询", path = "/door-elevator-linkage/query")
    @Authorization
    public ApiResult queryDoorElevatorLinkageByCommunityId() {
        return ApiResult.ok(buildingFacade.queryDoorElevatorLinkageInfo(SessionUtil.getCommunityId()));
    }

    @PostMapping(name = "门梯联动信息更新", path = "/door-elevator-linkage/update")
    @Authorization
    public ApiResult updateDoorElevatorLinkage(@RequestBody UpdateDoorElevatorLinkage toUpdate) {
        if (null == toUpdate.getAuths()) {
            return ApiResult.ok();
        }
        toUpdate.setCommunityId(SessionUtil.getCommunityId());
        buildingFacade.updateBuildingLinkage(toUpdate);
        return ApiResult.ok();
    }

    /*===================================================【remote control end】========================================================*/

    /*===================================================【mili device start】============================================*/

    /**
     * 同步米立设备列表到数据库
     *
     * @param communityId
     * @return
     * @throws Exception
     */
    @GetMapping(name = "同步米立设备列表到数据库", path = "/door/{communityId}/updateDeviceList/mili")
    @Authorization
    public ApiResult addSynchronizationDoor(@PathVariable("communityId") ObjectId communityId) throws Exception {
        Community community = communityFacade.findOne(communityId);
        List<Door> doors = new ArrayList<>();
        List<Door> allDoors = doorFacade.getDoorsByCommunityId(communityId);
        Set<Long> deviceIds = allDoors.stream()
                .filter(d -> d.getDeviceId() != null).map(Door::getDeviceId).collect(Collectors.toSet());

        for (DoorService doorService : DoorService.values()) {
            Map<String, Object> result = miligcConnection.APIPost("wuye/community/" +
                    community.getMiliCId() + "/device?service_id=" + doorService.KEY, null);
            Integer errorCode = Integer.parseInt(result.get("errorCode").toString());

            if (errorCode != 1) {
                String msg = result.get("errorMsg").toString();
                return ApiResult.error(-1, msg);
            }

            if (!result.containsKey("body")) {
                continue;
            }

            String body = result.get("body").toString();
            JSONArray jsonArray = JSONArray.parseArray(body);
            List<Device> list = jsonArray.toJavaList(Device.class);

            if (list == null) {
                continue;
            }

            for (Device device : list) {
                if (device == null) {
                    continue;
                }

                // 数据库没有这台设备就添加
                if (deviceIds.contains(device.getId())) {
                    continue;
                }

                Door door = new Door();
                // 蓝牙需要添加PIN
                if (doorService.KEY == DoorService.BLUETOOTH.KEY) {
                    door.setPin(device.getPin());
                    if (!StringUtil.isBlank(device.getSerial_no())) {
                        // mac地址满足有冒号的格式
                        Matcher m = PATTERN_MAC.matcher(device.getSerial_no());
                        String output = m.replaceAll("$1:");
                        String macAddress = output.substring(0, output.length() - 1);
                        door.setMac(macAddress);
                    }
                }
                door.setName(device.getName());
                door.setDeviceId(device.getId());
                door.setDeviceName(device.getName());
                door.setSerialNo(device.getSerial_no());
                door.setCommunityId(community.getId());
                door.setBrandNo(ManufactureType.MILI.KEY);
                door.setBrand(ManufactureType.MILI.VALUE);
                door.setServiceId(Collections.singleton(doorService.KEY));
                door.setDeviceCode(device.getDevice_code());
                door.setOnlineStatus(device.getOnline_status());
                door.setYunDeviceId(device.getYun_device_id());
                door.setCreatorId(SessionUtil.getTokenSubject().getUid());
                door.setCreateAt(new Date());
                door.setDataStatus(DataStatusType.VALID.KEY);
                doors.add(door);
            }
        }

        doorFacade.saveDoors(doors);
        communityFacade.addToSetBrandsById(communityId, Collections.singleton("door" + ManufactureType.MILI.KEY));
        return ApiResult.ok(doors);
    }

    /*=================================================【mili device end】============================================*/

    /*==============================================【freeview device start】=============================================*/

    @PostMapping(name = "添加人脸识别", path = "/door/user-feature/add")
    @Authorization
    public ApiResult addUserFeatureInDoor(@Validated @RequestBody UserFeature userFeature) {
        userFacade.updateCMUserFaceInfo(null, HumanFeatureStatusEnum.WRITING.KEY, SessionUtil.getCommunityId(), userFeature.getUserId());
        // 先删除旧有图片
        try {
            String featureCode = doorFacade.addUserFeatureInDoor(userFeature);
            if (StringUtil.isEmpty(featureCode)) {
                userFacade.updateCMUserFaceInfo(null, HumanFeatureStatusEnum.FAILURE.KEY, SessionUtil.getCommunityId(), userFeature.getUserId());
                //return;
            }
            // 用全视通推送来更新这个写入状态
            userFacade.updateCMUserFaceInfo(featureCode, HumanFeatureStatusEnum.SUCCESS.KEY, SessionUtil.getCommunityId(), userFeature.getUserId());
        } catch (Exception e) {
            log.warn("全视通人脸录入失败", e);
            // 录入失败
            userFacade.updateCMUserFaceInfo(null, HumanFeatureStatusEnum.FAILURE.KEY, SessionUtil.getCommunityId(), userFeature.getUserId());
        }
        return ApiResult.ok();
    }

    @GetMapping(name = "删除人脸识别", path = "/door/user-feature/{userId}/delete")
    @Authorization
    private ApiResult deleteUserFeatureInDoor(@PathVariable("userId") ObjectId userId) {
        CommunityUser cmUser = userFacade.getCommunityUserByCommunityIdAndUserId(SessionUtil.getCommunityId(), userId);

        if (cmUser == null || StringUtils.isEmpty(cmUser.getFaceCode())) {
            return ApiResult.ok();
        }
        // 删除状态由全视通推送
        doorFacade.deleteUserFeatureInDoor(cmUser.getFaceCode());

        return ApiResult.ok();
    }

    @GetMapping(name = "用户人脸列表", path = "/door/user-feature/{userId}/get")
    @Authorization
    public ApiResult getUserFeature(@PathVariable("userId") ObjectId userId) {
        CommunityUser cmUser = userFacade.getCommunityUserByCommunityIdAndUserId(SessionUtil.getCommunityId(), userId);
        if (cmUser == null || StringUtils.isEmpty(cmUser.getFaceCode())) {
            return ApiResult.ok();
        }

        return ApiResult.ok(doorFacade.getUserFeatureInDoor(userId));
    }

    /*==============================================【freeview device end】=============================================*/

    /**
     * 用于查询认证过程所需的门禁设备
     *
     * @param doorRequest
     * @return
     */
    @PostMapping(name = "认证过程所需的门禁设备列表", path = "/door/auth-check")
    public ApiResult getBuildingAndCommunityDoor(@Validated @RequestBody DoorRequest doorRequest) {
        return ApiResult.ok(doorFacade.getBuildingAndCommunityDoor(doorRequest.getBuildingId(), doorRequest.getCommunityId()));
    }

    /**
     * 设备管理平台
     *
     * @return
     */
    @PostMapping(name = "设备管理平台列表", path = "/terminal/device/list")
    public ApiResult listTerminalDevice(@RequestBody DeviceRequest deviceRequest) {
        // 查找门设备
        List<Door> doors = doorFacade.getDoorByTerminalCode(deviceRequest);
        if (CollectionUtils.isEmpty(doors)) {
            return ApiResult.ok(Collections.EMPTY_LIST);
        }
        List<Community> communityList = communityFacade
                .findByIds(doors.stream().map(Door::getCommunityId).collect(Collectors.toSet()));
        Map<ObjectId, String> toMatchCommunityAddress = communityList.stream()
                .collect(Collectors.toMap(Community::getId, Community::getAddress));
        // 门禁设备
        List<DeviceVO> deviceList = doors.stream().map(door -> {
            DeviceVO deviceVO = new DeviceVO();
            deviceVO.setDeviceId(door.getId());
            deviceVO.setDeviceName(door.getName());
            deviceVO.setDeviceType(DeviceType.DOOR.key);
            deviceVO.setManufactureType(ManufactureType.KANGTU_DOOR.KEY);
            deviceVO.setManufactureName(ManufactureType.KANGTU_DOOR.VALUE);
            deviceVO.setOnlineStatus(door.getOnlineStatus());
            deviceVO.setTerminalCode(door.getTerminalCode());
            deviceVO.setTerminalPort(door.getTerminalPort());
            deviceVO.setAddress(toMatchCommunityAddress.get(door.getCommunityId()));
            return deviceVO;
        }).collect(Collectors.toList());

        // 车闸设备暂无
        return ApiResult.ok(deviceList);
    }
}
