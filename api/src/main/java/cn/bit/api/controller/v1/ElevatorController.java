package cn.bit.api.controller.v1;


import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.ProtocolTypeEnum;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.user.CardFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.community.RoomMainSubDoorQuery;
import cn.bit.facade.vo.community.broadcast.BroadcastSchema;
import cn.bit.facade.vo.community.broadcast.DeviceSchema;
import cn.bit.facade.vo.communityIoT.elevator.*;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;

/**
 * 电梯相关的接口
 */
@RestController
@RequestMapping(value = "/v1/communityIoT/elevator", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ElevatorController {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ElevatorController.class);
    // 电梯
    @Autowired
    private ElevatorFacade elevatorFacade;

    // 卡片
    @Autowired
    private CardFacade cardFacade;

    // 房屋
    @Autowired
    private RoomFacade roomFacade;

    // 房屋认证
    @Autowired
    private UserToRoomFacade userToRoomFacade;

    // 物业人员
    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    // 社区
    @Autowired
    private CommunityFacade communityFacade;

    // 楼栋
    @Autowired
    private BuildingFacade buildingFacade;

    /**
     * 查询电梯列表 根据楼栋和社区查询 (分页)
     *
     * @return
     */
    @PostMapping(name = "电梯分页", path = "/list")
    @Authorization
    public Object getElevators(@RequestBody FindElevatorListRequest request,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        if (request.getBuildingIds() == null || request.getBuildingIds().isEmpty()) {
            List<ObjectId> buildingIds = buildingFacade.getBuildingIdsByCommunityId(SessionUtil.getCommunityId());
            request.setBuildingIds(buildingIds.stream().map(ObjectId::toString).collect(Collectors.toSet()));
        }
        request.setCommunityId(SessionUtil.getCommunityId().toString());
        return elevatorFacade.getElevators(request, page, size);
    }

    /**
     * 获取指定用户电梯关联表信息
     *
     * @param authElevatorRequest
     * @return
     */
    @PostMapping(name = "用户授权电梯列表", path = "/get/auth/list")
    @Authorization(verifyApi = false)
    public ApiResult findAuthMacAddress(@Validated @RequestBody AuthElevatorRequest authElevatorRequest) {
        // 区分在线离线小区，住户端还是物业端
        Community community = communityFacade.findOne(SessionUtil.getCommunityId());
        BroadcastSchema broadcastSchema = community.getBroadcastSchema();
        boolean online = true;
        if (broadcastSchema != null && broadcastSchema.getDeviceProtocols() != null) {
            DeviceSchema deviceSchema = broadcastSchema.getDeviceProtocols().stream()
                    .filter(ds -> ProtocolTypeEnum.OFFLINE.value().equals(ds.getProtocolType()))
                    .findFirst().orElse(null);
            if (deviceSchema != null) {
                online = false;
            }
        }
        if (online) {
            log.info("社区 '{}' 安装的是在线设备，需要获取授权设备列表...", community.getName());
            authElevatorRequest.setCommunityId(SessionUtil.getCommunityId());
            return ApiResult.ok(cardFacade.findUserAuthElevatorList(authElevatorRequest));
        }

        log.info("社区 '{}' 安装的是离线设备，需要获取离线设备列表...", community.getName());
        FindElevatorListRequest request = new FindElevatorListRequest();
        request.setCommunityId(community.getId().toString());
        Integer client = SessionUtil.getAppSubject().getClient();
        // 业主端
        if (client.equals(ClientType.HOUSEHOLD.value())) {
            // 需要找出当前用户所拥有房间的楼栋id集合
            List<UserToRoom> userToRoomList =
                    userToRoomFacade.findByCommunityIdAndUserId(community.getId(), authElevatorRequest.getUserId());
            if (userToRoomList.isEmpty()) {
                return ApiResult.ok();
            }
            request.setBuildingIds(userToRoomList.stream()
                    .map(userToRoom -> userToRoom.getBuildingId().toString()).collect(Collectors.toSet()));
        }
        // 暂定拿200个电梯的数据
        ElevatorPageResult elevatorPageResult = elevatorFacade.getElevators(request, 1, 200);
        if (!elevatorPageResult.isSuccess()) {
            log.info("查询离线电梯失败，{}", elevatorPageResult.getErrorMsg());
            return ApiResult.ok();
        }

        List<KeyNoListElevatorVO> keyNoListElevatorVOList = new ArrayList<>();
        if (elevatorPageResult.getData() != null) {
            elevatorPageResult.getData().getRecords().forEach(elevatorVO -> {
                // 离线社区返回电梯列表清空终端号
                elevatorVO.setDeviceNum(null);
                KeyNoListElevatorVO keyNoListElevatorVO = new KeyNoListElevatorVO();
                BeanUtils.copyProperties(elevatorVO, keyNoListElevatorVO);
                keyNoListElevatorVO.setBuildId(new ObjectId(elevatorVO.getBuildingId()));
                keyNoListElevatorVO.setId(new ObjectId(elevatorVO.getId()));
                keyNoListElevatorVOList.add(keyNoListElevatorVO);
            });
        }
        return ApiResult.ok(keyNoListElevatorVOList);

    }

    /**
     * 查询故障列表 根据电梯id查询 (分页)
     *
     * @return
     */
    @RequestMapping(name = "某电梯故障记录分页", path = "/fault/list", method = {RequestMethod.GET, RequestMethod.POST})
    public Object getFaultList(@Validated @RequestBody ElevatorRequest request,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        return elevatorFacade.findElevatorFaultList(request.getElevatorId(), page, size);
    }

    /**
     * 查询维修记录 根据电梯id查询 (分页)
     *
     * @return
     */
    @PostMapping(name = "某电梯维修记录分页", path = "/repair/list")
    public Object getRepairList(@RequestBody @Validated ElevatorRequest request,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        return elevatorFacade.findElevatorRepairList(request.getElevatorId(), page, size);

    }

    /**
     * 远程召梯，兼容旧版本app
     *
     * @param callElevatorRequest
     * @return
     */
    @PostMapping(name = "远程召梯", path = "/remote-call")
    @Authorization
    public Object remoteCallElevator(@RequestBody CallElevatorRequest callElevatorRequest) {
        Room room = roomFacade.findOne(callElevatorRequest.getRoomId());
        Integer client = SessionUtil.getAppSubject().getClient();
        if (ClientType.HOUSEHOLD.value() == client
                && !userToRoomFacade.isCheckIn(SessionUtil.getTokenSubject().getUid(), callElevatorRequest.getRoomId())) {
            log.info("当前用户没有该楼层的呼梯权限");
            throw AUTHENCATION_FAILD;
        }
        if (ClientType.PROPERTY.value() == client) {
            UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                    SessionUtil.getTokenSubject().getUid(), room.getCommunityId(), SessionUtil.getCompanyId());
            if (userToProperty == null || !userToProperty.getBuildingIds().contains(room.getBuildingId())) {
                log.info("当前物业人员没有该楼层的呼梯权限");
                throw AUTHENCATION_FAILD;
            }
        }
        return elevatorFacade.remoteCallElevator(room, callElevatorRequest);
    }

    /**
     * 业主远程召梯
     *
     * @param callElevatorRequest
     * @return
     */
    @PostMapping(name = "业主提前呼梯", path = "/household/remote")
    @Authorization
    public Object remoteCallForHousehold(@RequestBody CallElevatorRequest callElevatorRequest) {
        if (CollectionUtils.isEmpty(userToRoomFacade.queryListByBuildingId(callElevatorRequest.getBuildingId()))) {
            throw AUTHENCATION_FAILD;
        }
        return elevatorFacade.remoteCallElevator(callElevatorRequest);
    }

    /**
     * 物业远程召梯
     *
     * @param callElevatorRequest
     * @return
     */
    @PostMapping(name = "物业提前呼梯", path = "/property/remote")
    @Authorization
    public Object remoteCall(@RequestBody CallElevatorRequest callElevatorRequest) {
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                SessionUtil.getTokenSubject().getUid(), SessionUtil.getCommunityId(), SessionUtil.getCompanyId());
        if (userToProperty == null || !userToProperty.getBuildingIds().contains(callElevatorRequest.getBuildingId())) {
            throw AUTHENCATION_FAILD;
        }
        return elevatorFacade.remoteCallElevator(callElevatorRequest);
    }

    /**
     * 电梯品牌列表
     *
     * @return
     */
    @RequestMapping(name = "电梯品牌列表", path = "/brand-list", method = {RequestMethod.GET, RequestMethod.POST})
    @Authorization
    public Object elevatorBrandList() {
        return elevatorFacade.findElevatorBrandList();
    }

    /**
     * 电梯控制状态开启
     *
     * @param elevatorVO
     * @return
     */
    @PostMapping(name = "开启电梯控制状态", path = "/control/open")
    @Authorization
    public ApiResult elevatorControlStatusOpen(@RequestBody @Validated(ElevatorVO.ControlStatus.class) ElevatorVO elevatorVO) {
        return ApiResult.ok(elevatorFacade.openElevatorControlStatus(elevatorVO));
    }

    /**
     * 电梯控制状态关闭
     *
     * @param elevatorVO
     * @return
     */
    @PostMapping(name = "关闭电梯控制状态", path = "/control/close")
    @Authorization
    public ApiResult elevatorControlStatusClose(@RequestBody @Validated(ElevatorVO.ControlStatus.class) ElevatorVO elevatorVO) {
        boolean status = elevatorFacade.closeElevatorControlStatus(elevatorVO);
        return ApiResult.ok(status);
    }

    @PostMapping(name = "电梯主副门查询", path = "/main-sub-door-ctrl/query")
    @Authorization
    public ApiResult queryRoomsMainSubDoorControl(@RequestBody RoomMainSubDoorQuery roomMainSubDoorQuery) {
        return ApiResult.ok(roomFacade.findRoomsMainSubDoorControlInfoByIds(roomMainSubDoorQuery.getRoomIds(),
                                                                            SessionUtil.getCommunityId()));
    }

    @PostMapping(name = "主副门设置", path = "/main-sub-door-ctrl/update")
    @Authorization
    public ApiResult updateRoomMainSubDoorControl(@RequestBody @Validated RoomMainSubDoorQuery roomMainSubDoorQuery) {
        roomFacade.updateMainSubDoorById(roomMainSubDoorQuery.getRoomId(),
                                         roomMainSubDoorQuery.getMainDoor(),
                                         roomMainSubDoorQuery.getSubDoor());
        return ApiResult.ok();
    }
}