package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.enums.*;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.model.statistics.Statistics;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.DoorRecordFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.communityIoT.ElevatorRecordFacade;
import cn.bit.facade.service.fees.FeesFacade;
import cn.bit.facade.service.fees.PropertyFeesFacade;
import cn.bit.facade.service.property.AlarmFacade;
import cn.bit.facade.service.property.FaultFacade;
import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.service.statistics.StatisticsFacade;
import cn.bit.facade.service.user.*;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorPageResult;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorVO;
import cn.bit.facade.vo.communityIoT.elevator.FindElevatorListRequest;
import cn.bit.facade.vo.statistics.*;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v1/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsFacade statisticsFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private PropertyFeesFacade propertyFeesFacade;

    @Autowired
    private AlarmFacade alarmFacade;

    @Autowired
    private FaultFacade faultFacade;

    @Autowired
    private CardFacade cardFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private DoorRecordFacade doorRecordFacade;

    @Autowired
    private DoorFacade doorFacade;

    @Autowired
    private ElevatorRecordFacade elevatorRecordFacade;

    @Autowired
    private ElevatorFacade elevatorFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    @Autowired
    private FeesFacade feesFacade;

    @Autowired
    private PushFacade pushFacade;

    private static List<Region> householdRegions = defaultHouseholdRegions();

    /**
     * @return
     * @since 20180511
     * 获取首页的统计数据,获取待处理事务的数量
     **/
    @GetMapping(name = "统计待处理事务", path = "/manager/{communityId}/todo")
    @Authorization
    @Deprecated
    public ApiResult getHomeStatistics(@PathVariable ObjectId communityId) {
        communityId = SessionUtil.getCommunityId();
        /*
        1、用户认证：待审核用户数量unReviewedNum 已认证用户数量reviewedNum
        2、物业缴费：待缴费数量unPaymentNum, 已缴费数量paymentNum, 未通知数量unPublishNum
        3、警报消息：待受理数量unCheckedNum 待排查数量receivedNum
        4、故障报修：待处理数量unRepairedNum
        */
        Map<String, Long> map = new HashMap<>();

        Map<String, Long> unReviewedMap = userToRoomFacade.countByCommunityIdAndAuditStatus(communityId, AuditStatusType.REVIEWING.getType());
        map.putAll(unReviewedMap);

        Map<String, Long> reviewedMap = userToRoomFacade.countByCommunityIdAndAuditStatus(communityId, AuditStatusType.REVIEWED.getType());
        map.put("reviewedNum", reviewedMap.get("unReviewedNum"));

        Map<String, Long> unPayMap = feesFacade.countBills(communityId);
        map.putAll(unPayMap);

        Map<String, Long> unCheckedMap = alarmFacade.findReceiveAlarmNum(communityId, ReceiveStatusType.UNCHECKED.key);
        map.putAll(unCheckedMap);

        Map<String, Long> receivedMap = alarmFacade.findReceiveAlarmNum(communityId, ReceiveStatusType.RECEIVED.key);
        map.put("receivedNum", receivedMap.get("unCheckedNum"));

        Map<String, Long> unRepairedMap = faultFacade.countUnRepairedFault(communityId, FaultStatusType.WAITACCEPT.key);
        map.putAll(unRepairedMap);

        return ApiResult.ok(map);
    }

    /**
     * 获取首页的统计数据,获取待处理事务的数量
     *
     * @return
     **/
    @GetMapping(name = "统计待处理事务", path = "/manager/todo")
    @Authorization
    public ApiResult getHomeStatistics() {
        ObjectId communityId = SessionUtil.getCommunityId();
        /*
        1、用户认证：待审核用户数量unReviewedNum 已认证用户数量reviewedNum
        2、物业缴费：待缴费数量unPaymentNum, 已缴费数量paymentNum, 未通知数量unPublishNum
        3、警报消息：待受理数量unCheckedNum 待排查数量receivedNum
        4、故障报修：待处理数量unRepairedNum
        */
        Map<String, Long> map = new HashMap<>();

        Map<String, Long> unReviewedMap = userToRoomFacade.countUnReviewedProprietorsByCommunityId(communityId);
        map.putAll(unReviewedMap);

        Map<String, Long> reviewedMap = userToRoomFacade.countReviewedProprietorsByCommunityId(communityId);
        map.putAll(reviewedMap);

        Map<String, Long> unPayMap = feesFacade.countBills(communityId);
        map.putAll(unPayMap);

        Map<String, Long> unCheckedMap = alarmFacade.findReceiveAlarmNum(communityId, ReceiveStatusType.UNCHECKED.key);
        map.putAll(unCheckedMap);

        Map<String, Long> receivedMap = alarmFacade.findReceiveAlarmNum(communityId, ReceiveStatusType.RECEIVED.key);
        map.put("receivedNum", receivedMap.get("unCheckedNum"));

        Map<String, Long> unRepairedMap = faultFacade.countUnRepairedFault(communityId, FaultStatusType.WAITACCEPT.key);
        map.putAll(unRepairedMap);

        return ApiResult.ok(map);
    }

    /**
     * 故障统计
     *
     * @return
     **/
    @GetMapping(name = "故障统计", path = "/{communityId}/fault")
    @Authorization
    public ApiResult getFaultStatistics(@PathVariable("communityId") ObjectId communityId) {
        Statistics target = new Statistics();
        target.setCommunityId(communityId);
        target.setStatisticsType(StatisticsType.FAULT.key);
        Statistics statistics = statisticsFacade.findOne(target);
        return ApiResult.ok(statistics);
    }

    /**
     * 房间统计
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "房间入住统计", path = "/{communityId}/room")
    public ApiResult getRoomStatistics(@PathVariable("communityId") ObjectId communityId) {
        Community community = communityFacade.findOne(communityId);
        Integer roomTotal = community.getRoomNum();
        Integer checkInRoomCnt = community.getCheckInRoomCnt();
        RoomResponse roomResponse = new RoomResponse();
        roomResponse.setTotal(Long.valueOf(roomTotal == null ? 0 : roomTotal));
        roomResponse.setCheckInCount(Long.valueOf(checkInRoomCnt == null ? 0 : checkInRoomCnt));
        return ApiResult.ok(roomResponse);
    }

    /**
     * 住户统计
     *
     * @return
     **/
    @GetMapping(name = "住户统计", path = "/{communityId}/household")
    @Authorization
    public ApiResult getHouseholdStatistics(@PathVariable("communityId") ObjectId communityId) {
        HouseholdRequest householdRequest = new HouseholdRequest();
        householdRequest.setCommunityId(communityId);
        householdRequest.setAgeRegions(householdRegions);
        HouseholdResponse householdResponse = householdFacade.getHouseholdStatistics(householdRequest);
        return ApiResult.ok(householdResponse);
    }

    private static List<Region> defaultHouseholdRegions() {
        List<Region> regions = new LinkedList<>();

        Region region = new Region();
        region.setName("18岁以下");
        region.setTo(18);
        regions.add(region);

        region = new Region();
        region.setName("18-40岁");
        region.setFrom(18);
        region.setTo(40);
        regions.add(region);

        region = new Region();
        region.setName("40-65岁");
        region.setFrom(40);
        region.setTo(65);
        regions.add(region);

        region = new Region();
        region.setName("65岁以上");
        region.setFrom(65);
        regions.add(region);

        return regions;
    }

    /**
     * 营收统计
     *
     * @return
     **/
    @GetMapping(name = "营收统计", path = "/{communityId}/revenue")
    @Authorization
    public ApiResult getRevenueStatistics(@PathVariable("communityId") ObjectId communityId) {
        Statistics target = new Statistics();
        target.setCommunityId(communityId);
        target.setStatisticsType(StatisticsType.REVENUE.key);
        Statistics statistics = statisticsFacade.findOne(target);
        return ApiResult.ok(statistics);
    }

    /**
     * 设备统计
     *
     * @return/{communityId}/count
     **/
    @GetMapping(name = "设备统计", path = "/{communityId}/device")
    @Authorization
    public ApiResult getDeviceStatistics(@PathVariable("communityId") ObjectId communityId) {
        Statistics target = new Statistics();
        target.setCommunityId(communityId);
        target.setStatisticsType(StatisticsType.DEVICE.key);
        Statistics statistics = statisticsFacade.findOne(target);
        return ApiResult.ok(statistics);
    }

    @PostMapping(name = "访客申请统计", path = "/tenant/application")
    @Authorization
    public ApiResult getTenantApplicationStatistics(@Validated @RequestBody TenantApplicationRequest tenantApplicationRequest) {
        return ApiResult.ok(cardFacade.getTenantApplicationStatistics(tenantApplicationRequest));
    }

    @PostMapping(name = "访客申请统计(大屏)", path = "/tenant/application/screen")
    @Authorization
    public ApiResult getTenantApplicationStatisticsForScreen(@RequestBody TenantApplicationRequest request) {
        request.setCommunityId(SessionUtil.getCommunityId());
        return ApiResult.ok(cardFacade.getTenantApplicationStatisticsForScreen(request));
    }

    @PostMapping(name = "警报统计", path = "/alarm")
    @Authorization
    public ApiResult getAlarmStatistics(@Validated @RequestBody StatisticsRequest statisticsRequest) {
        statisticsRequest.setCommunityId(SessionUtil.getCommunityId());
        AlarmResponse alarmResponse = alarmFacade.getAlarmStatistics(statisticsRequest);
        Set<ObjectId> securityUserIds = new HashSet<>(alarmResponse.getSecuritySections().size());
        alarmResponse.getSecuritySections().forEach(section -> securityUserIds.add(new ObjectId(section.getName())));
        PushConfig pushConfig = pushFacade.findPushConfigByCompanyIdAndPointId(
                SessionUtil.getCompanyId(), PushPointEnum.ALARM.name());
        StatisticsVO statisticsVO = new StatisticsVO();
        statisticsVO.setCommunityId(statisticsRequest.getCommunityId());
        statisticsVO.setCompanyId(pushConfig.getCompanyId());
        statisticsVO.setRoles(pushConfig.getTargets());
        statisticsVO.setUserIds(securityUserIds);
        List<UserToProperty> securities = userToPropertyFacade.findByStatisticsVO(statisticsVO);

        Map<String, UserToProperty> securityMap = new HashMap<>(securities.size());
        Set<ObjectId> userIds = new HashSet<>(securities.size());
        securities.forEach(security -> {
            securityMap.put(security.getUserId().toString(), security);
            userIds.add(security.getUserId());
        });

        List<UserVO> userVOs = userFacade.findByIds(userIds);
        Map<String, UserVO> userMap = new HashMap<>(userIds.size());
        userVOs.forEach(userVO -> userMap.put(userVO.getId().toString(), userVO));

        alarmResponse.getSecuritySections().forEach(section -> {
            UserVO userVO = userMap.get(section.getName());
            if (userVO != null) {
                section.setIcon(userVO.getHeadImg());
            }

            UserToProperty security = securityMap.remove(section.getName());
            if (security != null) {
                section.setName(security.getUserName());
            }
        });
        // 没有处理过警报的目前在职的保安，也要返回
        securityMap.forEach((id, security) -> {
            if (security.getDataStatus() == DataStatusType.INVALID.KEY) {
                return;
            }

            Section section = new Section();
            section.setName(security.getUserName());
            section.setCount(0L);
            section.setProportion("0.00%");

            UserVO userVO = userMap.get(security.getUserId().toString());
            if (userVO != null) {
                section.setIcon(userVO.getHeadImg());
            }

            alarmResponse.getSecuritySections().add(section);
        });

        return ApiResult.ok(alarmResponse);
    }

    @PostMapping(name = "故障统计", path = "/fault")
    @Authorization
    public ApiResult getFaultStatistics(@Validated @RequestBody StatisticsRequest statisticsRequest) {
        statisticsRequest.setCommunityId(SessionUtil.getCommunityId());
        FaultResponse faultResponse = faultFacade.getFaultStatistics(statisticsRequest);
        Set<ObjectId> repairUserIds = new HashSet<>(faultResponse.getRepairSections().size());
        faultResponse.getRepairSections().forEach(section -> repairUserIds.add(new ObjectId(section.getName())));

        PushConfig pushConfig = pushFacade.findPushConfigByCompanyIdAndPointId(
                SessionUtil.getCompanyId(), PushPointEnum.FAULT_ALLOCATED.name());
        StatisticsVO statisticsVO = new StatisticsVO();
        statisticsVO.setCommunityId(statisticsRequest.getCommunityId());
        statisticsVO.setCompanyId(pushConfig.getCompanyId());
        statisticsVO.setRoles(pushConfig.getTargets());
        statisticsVO.setUserIds(repairUserIds);

        List<UserToProperty> servicemans = userToPropertyFacade.findByStatisticsVO(statisticsVO);

        Map<String, UserToProperty> servicemanMap = new HashMap<>(servicemans.size());
        Set<ObjectId> userIds = new HashSet<>(servicemans.size());
        servicemans.forEach(serviceman -> {
            if (serviceman != null && serviceman.getUserId() != null) {
                servicemanMap.put(serviceman.getUserId().toString(), serviceman);
                userIds.add(serviceman.getUserId());
            }
        });

        List<UserVO> userVOs = userFacade.findByIds(userIds);
        Map<String, UserVO> userMap = new HashMap<>(userIds.size());
        userVOs.forEach(userVO -> userMap.put(userVO.getId().toString(), userVO));

        faultResponse.getRepairSections().forEach(section -> {
            UserVO userVO = userMap.get(section.getName());
            if (userVO != null) {
                section.setIcon(userVO.getHeadImg());
            }

            UserToProperty serviceman = servicemanMap.remove(section.getName());
            if (serviceman != null) {
                section.setName(serviceman.getUserName());
            }
        });
        // 没有处理过故障的目前在职的维修工，也要返回
        servicemanMap.forEach((id, serviceman) -> {
            if (serviceman.getDataStatus() == DataStatusType.INVALID.KEY) {
                return;
            }

            FaultResponse.Section section = new FaultResponse.Section();
            section.setName(serviceman.getUserName());
            section.setCount(0L);
            section.setProportion("0.00%");

            UserVO userVO = userMap.get(serviceman.getUserId().toString());
            if (userVO != null) {
                section.setIcon(userVO.getHeadImg());
            }

            faultResponse.getRepairSections().add(section);
        });

        return ApiResult.ok(faultResponse);
    }

    @PostMapping(name = "故障统计(大屏)", path = "/fault/screen")
    @Authorization
    public ApiResult getFaultStatisticsForBigScreen(@Validated @RequestBody StatisticsRequest statisticsRequest) {
        FaultResponse faultResponse = faultFacade.getFaultStatisticsForBigScreen(statisticsRequest);
        return ApiResult.ok(faultResponse);
    }

    @PostMapping(name = "物业账单统计", path = "/property-bill/summary")
    @Authorization
    public ApiResult getPropertyBillSummary(@Validated @RequestBody PropertyBillSummaryRequest request) {
        request.setCommunityId(SessionUtil.getCommunityId());
        return ApiResult.ok(feesFacade.getBillSummary(request));
    }

    /**
     * @param communityId
     * @return
     * @since 20191106 新的账单没有超期这个状态
     */
    @GetMapping(name = "超期账单统计", path = "/{communityId}/property-bill/expire")
    @Authorization
    @Deprecated
    public ApiResult getExpirePropertyBillStatistics(@PathVariable("communityId") ObjectId communityId) {
        ExpirePropertyBillResponse response = propertyFeesFacade.getExpirePropertyBillStatistics(communityId);
        List<UserToRoom> proprietors = userToRoomFacade.getProprietorsByRoomIds(
                response.getProprietorSections().stream()
                        .map(section -> new ObjectId(section.getName()))
                        .collect(Collectors.toList()));

        Map<String, UserToRoom> proprietorMap = new HashMap<>(proprietors.size());
        Set<ObjectId> userIds = new HashSet<>(proprietors.size());
        proprietors.forEach(proprietor -> {
            String roomId = proprietor.getRoomId().toString();
            proprietorMap.put(roomId, proprietor);
            userIds.add(proprietor.getProprietorId());
        });

        List<UserVO> userVOs = userFacade.findByIds(userIds);
        Map<ObjectId, UserVO> userMap = userVOs.stream().collect(Collectors.toMap(UserVO::getId, proprietor -> proprietor));
        List<ExpirePropertyBillResponse.Section> validSections = new ArrayList<>(response.getProprietorSections().size());
        response.getProprietorSections().forEach(section -> {
            UserToRoom proprietor = proprietorMap.get(section.getName());
            if (proprietor == null) {
                return;
            }

            UserVO userVO = userMap.get(proprietor.getProprietorId());
            if (userVO == null) {
                return;
            }

            section.setName(userVO.getName());
            section.setIcon(userVO.getHeadImg());
            section.setPhone(userVO.getPhone());
            section.setRoomName(proprietor.getRoomLocation());
            validSections.add(section);
        });
        response.setProprietorSections(validSections);
        return ApiResult.ok(response);
    }

    @GetMapping(name = "门禁数量统计", path = "/{communityId}/door/summary")
    @Authorization
    public ApiResult getDoorSummary(@PathVariable("communityId") ObjectId communityId) {
        Long total = doorFacade.countDoorByCommunityId(communityId);
        Long faultCount = doorFacade.countFaultedDoorByCommunityId(communityId);
        DoorSummartyResponse response = new DoorSummartyResponse();
        response.setTotal(total);
        response.setFaultCount(faultCount);
        return ApiResult.ok(response);
    }

    @PostMapping(name = "门禁使用记录统计", path = "/door/record")
    @Authorization
    public ApiResult getDoorRecordStatistics(@Validated @RequestBody DoorRecordRequest recordRequest) {
        DoorRecordResponse response = doorRecordFacade.getDoorRecordStatistics(recordRequest);
        if (response.getDoorSections().isEmpty()) {
            return ApiResult.ok(response);
        }

        List<Door> doors = doorFacade.getDoorsInIds(response.getDoorSections().stream()
                .map(section -> new ObjectId(section.getName())).collect(Collectors.toSet()));

//        List<Building> buildings = buildingFacade.findByIds(doors.stream()
//                .map(Door::getBuildingId).collect(Collectors.toList()));

        Map<String, Door> doorMap = doors.stream().collect(Collectors.toMap(door -> door.getId().toString(),
                door -> door));

/*        Map<ObjectId, String> buildingMap = buildings.stream().collect(Collectors.toMap(Building::getId,
                Building::getName));

        response.getDoorSections().forEach(section -> {
            Door door = doorMap.get(section.getName());
            if (door == null) {
                return;
            }

            String buildingName = buildingMap.get(door.getBuildingId());
            if (StringUtil.isNotBlank(buildingName)) {
                section.setName(buildingName + door.getName());
            } else {
                section.setName(door.getName());
            }
        });*/

        Iterator<Section> iterator = response.getDoorSections().iterator();
        while (iterator.hasNext()) {
            Section section = iterator.next();
            Door door = doorMap.get(section.getName());
            if (door == null) {
                iterator.remove();
                continue;
            }
            section.setName(door.getName());
        }

        return ApiResult.ok(response);
    }

    @GetMapping(name = "电梯数量统计", path = "/{communityId}/elevator/summary")
    @Authorization
    public ApiResult getElevatorSummary(@PathVariable("communityId") ObjectId communityId) {
        return ApiResult.ok(elevatorFacade.summaryElevators(communityId));
    }

    @PostMapping(name = "电梯使用记录统计", path = "/elevator/record")
    @Authorization
    public ApiResult getElevatorRecordStatistics(@Validated @RequestBody ElevatorRecordRequest recordRequest) {
        ElevatorRecordResponse response = elevatorRecordFacade.getElevatorRecordStatistics(recordRequest);

        FindElevatorListRequest elevatorRequest = new FindElevatorListRequest();
        elevatorRequest.setCommunityId(recordRequest.getCommunityId().toString());
        elevatorRequest.setMacAddress(response.getElevatorSections().stream()
                .map(section -> section.getName()).collect(Collectors.toSet()));
        elevatorRequest.setDataStatus(new HashSet<>(Arrays.asList(DataStatusType.VALID.KEY,
                DataStatusType.INVALID.KEY)));
        try {
            ElevatorPageResult result = elevatorFacade.getElevators(elevatorRequest, null, null);
            List<ElevatorVO> elevators = result.getData().getRecords();

            List<Building> buildings = buildingFacade.findByIds(elevators.stream()
                    .map(elevator -> new ObjectId(elevator.getId()))
                    .collect(Collectors.toList()));

            Map<String, ElevatorVO> elevatorMap = elevators.stream()
                    .collect(Collectors.toMap(elevator -> elevator.getMacAddress(),
                            elevator -> elevator, (k, v) -> v));

 /*           Map<String, String> buildingMap = buildings.stream()
                    .collect(Collectors.toMap(building -> building.getId().toString(), Building::getName));

            response.getElevatorSections().forEach(section -> {
                ElevatorVO elevator = elevatorMap.get(section.getName());
                if (elevator == null) {
                    return;
                }

                String buildingName = buildingMap.get(elevator.getBuildingId());
                if (StringUtil.isNotBlank(buildingName)) {
                    section.setName(buildingName + elevator.getName());
                } else {
                    section.setName(elevator.getName());
                }
            });*/

            Iterator<Section> iterator = response.getElevatorSections().iterator();
            while (iterator.hasNext()) {
                Section section = iterator.next();
                ElevatorVO elevator = elevatorMap.get(section.getName());
                if (elevator == null) {
                    iterator.remove();
                    continue;
                }
                section.setName(elevator.getName());
            }

        } catch (Exception e) {
            log.warn("fail to get elevators from ElevatorIoT", e);
        }

        return ApiResult.ok(response);
    }

}
