package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.AppSubject;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.community.model.Floor;
import cn.bit.common.facade.enums.OsEnum;
import cn.bit.common.facade.system.dto.PermissionSelectionDTO;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.constant.RoleConstants;
import cn.bit.facade.enums.*;
import cn.bit.facade.exception.community.CommunityBizException;
import cn.bit.facade.model.community.*;
import cn.bit.facade.model.communityIoT.Camera;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.facade.model.system.Resource;
import cn.bit.facade.model.user.*;
import cn.bit.facade.model.vehicle.Gate;
import cn.bit.facade.service.community.*;
import cn.bit.facade.service.communityIoT.CameraFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.fees.PropertyFeesFacade;
import cn.bit.facade.service.property.FaultFacade;
import cn.bit.facade.service.property.PropertyFacade;
import cn.bit.facade.service.system.ResourceFacade;
import cn.bit.facade.service.user.*;
import cn.bit.facade.service.vehicle.InoutFacade;
import cn.bit.facade.vo.community.*;
import cn.bit.facade.vo.community.zhfreeview.CommunityParams;
import cn.bit.facade.vo.community.zhfreeview.Structure;
import cn.bit.facade.vo.communityIoT.DeviceDataStatisticsVO;
import cn.bit.facade.vo.communityIoT.camera.CameraRequest;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DeviceAuthVO;
import cn.bit.facade.vo.property.Property;
import cn.bit.facade.vo.trade.CommunityTradePlatformVO;
import cn.bit.facade.vo.user.UserCommunityVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.EmployeeRequest;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.StringUtils;
import cn.bit.framework.utils.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.bson.types.ObjectId;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.bit.facade.constant.mq.TagConstant.ADD;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_DOOR_AUTH;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH;
import static cn.bit.facade.exception.community.CommunityBizException.LAYOUT_ID_NULL;
import static cn.bit.facade.exception.user.UserBizException.USER_NOT_EXITS;
import static cn.bit.framework.exceptions.BizException.OPERATION_FAILURE;

@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class CommunityController {

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private CommunityTradeAccountFacade communityTradeAccountFacade;

    @Autowired
    private FaultFacade faultFacade;

    @Autowired
    private PropertyFeesFacade propertyFeesFacade;

    @Autowired
    private ResourceFacade resourceFacade;

    @Autowired
    private PropertyFacade propertyFacade;

    @Autowired
    private DistrictFacade districtFacade;

    // 用于米立注册设备
    @Autowired
    private DoorFacade doorFacade;

    // 用于米立注册设备
    @Autowired
    private CardFacade cardFacade;

    @Autowired
    private InoutFacade inoutFacade;

    @Autowired
    private CameraFacade cameraFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    @Autowired
    private DefaultMQProducer deviceAuthProducer;

    @Autowired
    private cn.bit.common.facade.community.service.CommunityFacade commonCommunityFacade;

    @Autowired
    private DataLayoutFacade dataLayoutFacade;

    @javax.annotation.Resource
    private SystemFacade systemFacade;

    private static Pattern PATTERN_NUMBER = Pattern.compile("^[-\\+]?[\\d]*$");

    // 快速创建小区
    private static final String getAddFreeViewCommunityUrl = ":21664/api/tenantstructures";

    // =================================================【community begin】=====================================================

    /**
     * 新增社区
     *
     * @param entity
     * @return
     **/
    @PostMapping(name = "新增社区", path = "/community/add")
    @Authorization
    public ApiResult<Community> addCommunity(@RequestBody Community entity) {
        entity.setCreateId(SessionUtil.getTokenSubject().getUid());
        Community community = communityFacade.addCommunity(entity);
        return ApiResult.ok(community);
    }

    /**
     * 修改社区
     *
     * @param entity
     * @return
     **/
    @PostMapping(name = "编辑社区", path = "/community/edit")
    @Authorization
    public ApiResult<Community> updateCommunity(@RequestBody Community entity) {
        entity.setId(SessionUtil.getCommunityId());
        return ApiResult.ok(communityFacade.updateCommunity(entity));
    }

    /**
     * 保存社区实景图片
     *
     * @param entity
     * @return
     **/
    @PostMapping(name = "保存社区实景图片", path = "/community/photos/save")
    @Authorization
    public ApiResult saveCommunity(@RequestBody Community entity) {
        entity.setId(SessionUtil.getCommunityId());
        communityFacade.saveCommunityPhotos(entity);
        return ApiResult.ok();
    }

    /**
     * 修改社区开放状态
     *
     * @param community
     * @return
     */
    @PostMapping(name = "修改社区开放状态", path = "/community/open")
    @Authorization
    public ApiResult openCommunity(@RequestBody Community community) {
        return ApiResult.ok(communityFacade.openCommunity(
                community.getId(), community.getOpen() == null ? true : community.getOpen()));
    }

    /**
     * 删除社区
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "删除社区", path = "/community/{id}/delete")
    @Authorization
    public ApiResult<Community> deleteCommunity(@PathVariable("id") ObjectId communityId) {
        boolean flag = communityFacade.changeDataStatus(communityId);
        if (flag) {
            return ApiResult.ok("删除成功");
        }
        return ApiResult.error(-1, "删除失败");
    }

    /**
     * 获取社区信息
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "社区详情", path = "/community/{communityId}/detail")
    public ApiResult<Community> getCommunity(@PathVariable("communityId") ObjectId communityId) {
        Community community = communityFacade.findOne(communityId);
        try {
            Property property = propertyFacade.findByCommunityId(communityId);
            if (property != null && property.getDataStatus() == DataStatusType.VALID.KEY) {
                community.setPropertyId(property.getId());
                community.setPropertyName(property.getName());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return ApiResult.ok(community);
    }

    /**
     * 社区分页
     *
     * @param name     社区名称
     * @param province 省份
     * @param city     城市
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "社区分页", path = "/community/page")
    @Authorization(verifyApi = false)
    public ApiResult listCommunities(String name, String province, String city,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size) {
        UserVO userVO = SessionUtil.getCurrentUser();
        // 内部人员可以查询所有社区
        Boolean open = Boolean.TRUE.equals(userVO.getInternal()) ? null : true;
        Page<UserCommunityVO> communityPage = communityFacade.listCommunities(SessionUtil.getAppSubject().getPartner(),
                name, province, city, open, page, size);
        Set<ObjectId> communityIds = Collections.EMPTY_SET;
        if (communityPage != null && communityPage.getTotal() > 0 && userVO.getId() != null) {
            communityIds = userToRoomFacade.getCommunityIdsByUserId(userVO.getId());
        }
        for (UserCommunityVO userCommunityVO : communityPage.getRecords()) {
            int num = 0;
            if (communityIds != null) {
                userCommunityVO.setUserId(userVO.getId());
                // 该社区有效房屋的数量
                num = Collections.frequency(communityIds, userCommunityVO.getId());
            }
            userCommunityVO.setRoomsAmount(num);
        }
        return ApiResult.ok(communityPage);
    }

    /**
     * 获取社区列表
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "社区列表", path = "/community/list")
    public ApiResult getList(@RequestBody Community entity) {
        return ApiResult.ok(communityFacade.queryList(SessionUtil.getAppSubject().getPartner(), entity));
    }

    /**
     * 获取社区的键值对
     *
     * @return
     */
    @GetMapping(name = "社区的键值对列表", path = "/community/key-value")
    public ApiResult getCommunityList() {
        return ApiResult.ok(communityFacade.getCommunityList());
    }

    /**
     * 根据物业公司获取社区(分页)
     *
     * @param propertyId
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "根据物业公司获取社区分页列表", path = "/community/{propertyId}/page")
    public ApiResult getList(@PathVariable("propertyId") ObjectId propertyId,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        Page<Community> pages = communityFacade.findCommunitysByPropertyId(propertyId, page, size);
        return ApiResult.ok(pages);
    }

    /**
     * 根据用户ID获取社区列表
     *
     * @param userId
     * @return
     */
    @GetMapping(name = "获取住户拥有的社区列表", path = "/community/{userId}/queryByUserId")
    public ApiResult<Community> getCommunityByUserId(@PathVariable("userId") ObjectId userId) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        ClientUser user = userFacade.getClientUserByClientAndPartnerAndUserId(appSubject.getClient(),
                appSubject.getPartner(), userId);
        if (user == null) {
            throw USER_NOT_EXITS;
        }

        if (CollectionUtils.isEmpty(user.getCommunityIds())) {
            return ApiResult.error(-1, "没有绑定社区");
        }
        List<Community> communities = communityFacade.findByIds(user.getCommunityIds());
        return ApiResult.ok(communities);
    }

    /**
     * 获取已开放的社区列表
     *
     * @return
     */
    @GetMapping(name = "获取已开放的社区键值对列表", path = "/community/list-with-open")
    public ApiResult getListForBusiness() {
        List<CommunityKv> communityList = communityFacade.findAllWithOpen();
        return ApiResult.ok(communityList);
    }

    @GetMapping(name = "获取社区层级分组(目前只支持到楼栋这一层)", path = "/community/{communityId}/building-group")
    public ApiResult getBuildingGroup(@PathVariable ObjectId communityId) {
        return ApiResult.ok(commonCommunityFacade.getBuildingGroupByCommunityId(communityId));
    }

    @GetMapping(name = "获取社区层级关系(默认从社区开始)", path = "/community/hierarchy")
    public ApiResult getElements(@RequestParam ObjectId targetId,
                                 @RequestParam(defaultValue = "1", required = false) Integer level) {
        return ApiResult.ok(commonCommunityFacade.getHierarchy(level, targetId));
    }
    // =================================================【community end】===============================================

    // ================================================【building begin】===============================================

    /**
     * 新增楼栋
     *
     * @param buildingVO
     * @return
     */
    @PostMapping(name = "新增楼栋", path = "/community/building/add")
    @Authorization
    public ApiResult<Building> addBuilding(@RequestBody @Validated(BuildingVO.Add.class) BuildingVO buildingVO) {
        Building entity = new Building();
        BeanUtils.copyProperties(buildingVO, entity);
        entity.setCreateId(SessionUtil.getTokenSubject().getUid());
        Building building = buildingFacade.addBuilding(entity);
        if (building != null) {
            return ApiResult.ok(building);
        }
        return ApiResult.error(-1, "新增失败");
    }

    /**
     * 删除楼宇
     *
     * @param buildingId
     * @return
     */
    @GetMapping(name = "删除楼栋", path = "/community/building/{buildingId}/delete")
    @Authorization
    public ApiResult<Building> deleteBuilding(@PathVariable("buildingId") ObjectId buildingId) {
        boolean flag = buildingFacade.changeDataStatus(buildingId);
        return flag ? ApiResult.ok("删除成功") : ApiResult.error(-1, "删除失败");
    }

    /**
     * 修改楼宇
     *
     * @param buildingVO
     * @return
     */
    @PostMapping(name = "编辑楼栋", path = "/community/building/edit")
    @Authorization
    public ApiResult updateBuilding(@RequestBody @Validated(BuildingVO.Update.class) BuildingVO buildingVO) {
        Building entity = new Building();
        BeanUtils.copyProperties(buildingVO, entity);
        Building item = buildingFacade.updateBuilding(entity);
        return item == null ? ApiResult.error(-1, "修改失败") : ApiResult.ok(item);
    }

    /**
     * 获取楼宇信息
     *
     * @param buildingId
     * @return
     */
    @GetMapping(name = "楼栋详情", path = "/community/building/{buildingId}/detail")
    @Authorization
    public ApiResult<Building> getBuilding(@PathVariable("buildingId") ObjectId buildingId) {
        Building entity = buildingFacade.findOne(buildingId);
        if (entity != null) {
            return ApiResult.ok(entity);
        }
        return ApiResult.error(-1, "没有该楼宇信息");
    }

    /**
     * 楼宇分页(物业App)
     *
     * @param
     * @param page
     * @return
     */
    @GetMapping(name = "app已开放楼栋分页", path = "/community/building/page")
    @Authorization(verifyApi = false)
    public ApiResult queryBuildingPage(String name, String communityId, Integer page, Integer size) {
        if (!StringUtil.isNotBlank(communityId)) {
            throw CommunityBizException.COMMUNITY_ID_NULL;
        }
        Building entity = new Building();
        entity.setOpen(Boolean.TRUE);
        entity.setName(name);
        entity.setCommunityId(new ObjectId(communityId));
        Page<Building> buildingPage = buildingFacade.queryPage(entity, page == null ? 1 : page, size == null ? 10 : size);
        for (Building building : buildingPage.getRecords()) {
            building.setInputRoomNum(roomFacade.countRoomByBuildingId(building.getId()));
        }
        return ApiResult.ok(buildingPage);
    }

    /**
     * web楼宇分页
     *
     * @param
     * @param page
     * @return
     * @since 20190315 社区已抽离公共服务
     */
    @GetMapping(name = "web楼栋分页", path = "/community/building/by-community-id")
    @Authorization
    @Deprecated
    public ApiResult getBuildingPage(String communityId,
                                     String name,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        if (StringUtil.isEmpty(communityId)) {
            return ApiResult.error(-1, "社区ID不能为空");
        }
        Building entity = new Building();
        entity.setOpen(Boolean.TRUE);
        if (SessionUtil.getAppSubject().getOsEnum().value() == OsEnum.WEB.value()) {
            // web后台登录，需要判断是否是社区管理员
            CommunityUser communityUser = userFacade.getCommunityUserByCommunityIdAndUserId(
                    new ObjectId(communityId), SessionUtil.getTokenSubject().getUid());
            if (communityUser != null && communityUser.getRoles().contains(RoleType.CM_ADMIN.name())) {
                entity.setOpen(null);
            }
        }
        entity.setName(name);
        entity.setCommunityId(new ObjectId(communityId));
        Page<Building> buildingPage = buildingFacade.queryPage(entity, page, size);
        return ApiResult.ok(buildingPage);
    }

    /**
     * 获取楼宇列表
     *
     * @param
     * @return
     */
    @GetMapping(name = "楼宇列表", path = "/community/building/list")
    public ApiResult getBuildingListByCommunityIdOrName(String name, String communityId) {
        if (StringUtil.isBlank(communityId) && SessionUtil.getCommunityId() == null) {
            return ApiResult.error(-1, "社区ID不能为空");
        }
        Building entity = new Building();
        entity.setOpen(Boolean.TRUE);
        entity.setName(name);
        entity.setCommunityId(StringUtil.isNotBlank(communityId) ? new ObjectId(communityId) : SessionUtil.getCommunityId());
        List<Building> list = buildingFacade.queryList(entity);
        return ApiResult.ok(list);
    }

    /**
     * 获取楼宇列表,并统计物业账单数量(旧)
     *
     * @param
     * @return
     * @see this.getBuildingList
     * @since 2018-04-12
     */
    @Deprecated
    @GetMapping(name = "楼宇分页(包含物业账单数量)", path = "/community/{id}/building/bill-list")
    @Authorization
    public ApiResult queryBuildingList(@PathVariable("id") ObjectId id, Integer billStatus,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size) {
        Building toGet = new Building();
        toGet.setCommunityId(id);
        toGet.setOpen(Boolean.TRUE);
        Page<Building> pageList = buildingFacade.queryPage(toGet, page, size);
        if (billStatus != null && pageList != null && pageList.getTotal() > 0) {
            for (Building building : pageList.getRecords()) {
                building.setTempNum(propertyFeesFacade.countBillNumByBuildingIdAndBillStatusAndDate(
                        building.getId(), billStatus, null));
            }
        }
        return ApiResult.ok(pageList);
    }

    /**
     * 获取楼宇列表,并统计物业账单数量(新)
     *
     * @param buildingRequest
     * @return
     */
    @PostMapping(name = "楼宇分页(包含物业账单数量)", path = "/community/building/bill-list")
    @Authorization
    public ApiResult getBuildingList(@RequestBody BuildingRequest buildingRequest,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size) {
        Page<Building> pageList = buildingFacade.findPageByCommunityIdAndOpen(
                SessionUtil.getCommunityId(), Boolean.TRUE, page, size);
        if (pageList == null || pageList.getTotal() == 0) {
            log.info("社区({})没有楼栋信息", SessionUtil.getCommunityId());
            return ApiResult.ok();
        }
        Set<ObjectId> buildingIds = new HashSet<>();
        for (Building building : pageList.getRecords()) {
            buildingIds.add(building.getId());
        }
        List<PropertyBill> billList = propertyFeesFacade.findByBuildingIdInAndBillStatusAndDate(
                buildingIds, buildingRequest.getBillStatus(), buildingRequest.getBillDate());
        for (Building building : pageList.getRecords()) {
            for (PropertyBill propertyBill : billList) {
                if (building.getId().equals(propertyBill.getBuildingId())) {
                    building.setTempNum((building.getTempNum() == null ? 0 : building.getTempNum()) + 1);
                }
            }
        }
        return ApiResult.ok(pageList);
    }

    /**
     * 获取楼宇列表,并统计有效住户的数量
     *
     * @return
     */
    @GetMapping(name = "楼宇列表(包含有效住户数量)", path = "/building/household-list")
    @Authorization
    public ApiResult getBuildingListWithHouseholdNum() {
        List<Building> buildingList = buildingFacade.findByCommunityIdAndOpen(SessionUtil.getCommunityId());
        if (buildingList == null || buildingList.size() == 0) {
            log.info("社区({})没有楼栋信息", SessionUtil.getCommunityId());
            return ApiResult.ok();
        }
        Set<ObjectId> buildingIds = new HashSet<>();
        for (Building building : buildingList) {
            buildingIds.add(building.getId());
        }
        List<UserToRoom> userToRoomList = userToRoomFacade.findByBuildingIdsIn(buildingIds);
        for (Building building : buildingList) {
            for (UserToRoom userToRoom : userToRoomList) {
                if (building.getId().equals(userToRoom.getBuildingId())) {
                    building.setTempNum((building.getTempNum() == null ? 0 : building.getTempNum()) + 1);
                }
            }
        }
        return ApiResult.ok(buildingList);
    }

    /**
     * 根据社区ID和用户ID获取楼宇
     *
     * @param communityId
     * @param userId
     * @return
     */
    @GetMapping(name = "获取住户在某社区已认证的楼宇列表", path = "/building/{communityId}/{userId}/queryByUserId")
    @Authorization
    public ApiResult<Building> getBuildingByUserId(@PathVariable("communityId") ObjectId communityId,
                                                   @PathVariable("userId") ObjectId userId) {
        Set<ObjectId> buildingIds = userToRoomFacade.getBuildingsByUserId(communityId, userId);
        if (buildingIds == null || buildingIds.isEmpty()) {
            return ApiResult.ok();
        }
        List<Building> buildings = buildingFacade.findByIds(buildingIds);
        return ApiResult.ok(buildings);
    }

    /**
     * 修改楼宇开放状态
     *
     * @param id
     * @return
     */
    @GetMapping(name = "修改楼宇开放状态", path = "/building/{id}/open")
    @Authorization
    public ApiResult openBuilding(@PathVariable("id") ObjectId id,
                                  @RequestParam(defaultValue = "true") Boolean open) {
        boolean result = buildingFacade.openBuilding(id, open);
        if (!result) {
            throw OPERATION_FAILURE;
        }
        return ApiResult.ok();
    }

    @PostMapping(name = "查找楼层对照表", path = "/building/floor/map")
    @Authorization
    public ApiResult getBuildingFloorMap(@RequestBody Building building) {
        List<Floor> floors = commonCommunityFacade.listFloorsByBuildingId(building.getId());
        List<BuildingFloorVO> floorVOList = floors.stream().map(floor -> {
            BuildingFloorVO buildingFloorVO = new BuildingFloorVO();
            BeanUtils.copyProperties(floor, buildingFloorVO);
            return buildingFloorVO;
        }).collect(Collectors.toList());
        return ApiResult.ok(floorVOList);
    }
    //===================================================【building end】======================================================

    // =================================================【room begin】=====================================================

    /**
     * 新增房间
     *
     * @param roomVO
     * @return
     */
    @PostMapping(name = "新增房间", path = "/community/room/add")
    @Authorization
    public ApiResult<Room> addRoom(@RequestBody @Validated(RoomVO.Add.class) RoomVO roomVO) {
        Room entity = new Room();
        BeanUtils.copyProperties(roomVO, entity);
        entity.setCreateId(SessionUtil.getTokenSubject().getUid());
        Room room = roomFacade.addRoom(entity);
        return room == null ? ApiResult.error(-1, "添加失败") : ApiResult.ok(room);
    }

    /**
     * 【new】
     * 批量新增房间(test)
     *
     * @param entity
     * @return
     * @since 2019-07-04
     */
    @PostMapping(name = "批量新增房间(test)", path = "/community/room/addRooms")
    @Authorization
    @Deprecated
    public ApiResult addRooms(@RequestBody Room entity) {
        ObjectId communityId = entity.getCommunityId();
        ObjectId buildingId = entity.getBuildingId();
        Integer roomNum = entity.getRoomNum();
        ObjectId userId = SessionUtil.getTokenSubject().getUid();
        if (roomNum > 0) {
            List<Room> list = new ArrayList<Room>();
            int floorNo = 1;
            for (int i = entity.getBeginNum(); i <= roomNum; i++) {
                Room item = new Room();
                item.setName((StringUtil.isNotNull(entity.getRoomName()) ? entity.getRoomName() : null)
                        + String.format("%03d", i) + "房");
                item.setCommunityId(communityId);
                item.setBuildingId(buildingId);
                item.setCode((StringUtil.isNotNull(entity.getCodeName()) ? entity.getCodeName() : null)
                        + String.format("%03d", i));
                item.setDataStatus(DataStatusType.VALID.KEY);
                item.setCreateAt(new Date());
                item.setCreateId(userId);
                item.setFloorCode(String.valueOf(i));
                //楼层号，协议用
                item.setFloorNo(floorNo + "");
                //房屋默认面积100m²
                item.setArea(10000);
                item.setRank(i);
                list.add(item);
                floorNo++;
                if (i % 50 == 0) {
                    roomFacade.addRooms(list);
                    list.clear();
                }
            }
            if (!list.isEmpty()) {
                roomFacade.addRooms(list);
            }
        }
        return ApiResult.ok();
    }

    /**
     * 删除房间
     *
     * @param roomId
     * @return
     */
    @Authorization
    @GetMapping(name = "删除房间", path = "/community/room/{roomId}/delete")
    public ApiResult<Building> deleteRoom(@PathVariable("roomId") ObjectId roomId) {
        boolean result = roomFacade.changeDataStatus(roomId);
        return result ? ApiResult.ok("删除成功") : ApiResult.error(-1, "删除失败");
    }

    /**
     * 修改房间
     *
     * @param roomVO
     * @return
     */
    @Authorization
    @PostMapping(name = "编辑房间", path = "/community/room/edit")
    public ApiResult updateRoom(@RequestBody RoomVO roomVO) {
        Room entity = new Room();
        BeanUtils.copyProperties(roomVO, entity);
        Room result = roomFacade.updateRoom(entity);
        if (entity.getId() != null) {
            return ApiResult.ok(result);
        }
        return ApiResult.error(-1, "修改失败");
    }

    /**
     * 获取房间信息
     *
     * @param roomId
     * @return
     */
    @GetMapping(name = "房间详情", path = "/community/room/{roomId}/detail")
    @Authorization
    public ApiResult<Building> getRoom(@PathVariable("roomId") ObjectId roomId) {
        return ApiResult.ok(roomFacade.findOne(roomId));
    }

    /**
     * 房间分页
     *
     * @param name
     * @param buildingId
     * @param page
     * @param size
     * @return
     */
    @GetMapping(name = "房间分页", path = "/community/room/page")
    @Authorization(verifyApi = false)
    public ApiResult queryRoomPage(String name, String buildingId, Integer page, Integer size) {
        Page<Room> pageList = roomFacade.queryPage(new ObjectId(buildingId), name,
                page == null ? 1 : page, size == null ? 10 : size);
        return ApiResult.ok(pageList);
    }

    /*
     * 根据楼栋获取房间列表
     * @param buildingId
     * @return
     */
    @GetMapping(name = "楼栋下的房间列表", path = "/room/{buildingId}/list")
    @Authorization(verifyApi = false)
    public ApiResult getRoomList(@PathVariable("buildingId") ObjectId buildingId) {
        List<Room> list = roomFacade.queryByBuildingId(buildingId);
        return ApiResult.ok(list);
    }

    /*
     * 根据楼栋获取房间列表并校验是否存在档案
     * @param buildingId
     */
    @GetMapping(name = "楼栋下的房间列表(并校验是否存在档案)", path = "/room/{buildingId}/with-households/list")
    @Authorization
    public ApiResult getRoomListWithHouseholdCheck(@PathVariable("buildingId") ObjectId buildingId) {
        List<Room> list = roomFacade.queryByBuildingId(buildingId);
        if (list.isEmpty()) {
            return ApiResult.ok(list);
        }
        List<Household> households = householdFacade.findOwnerByRoomIds(list.stream().map(Room::getId).collect(Collectors.toSet()));
        Map<ObjectId, Boolean> map = new HashMap<>();
        if (!households.isEmpty()) {
            households.forEach(household -> map.put(household.getRoomId(), Boolean.TRUE));
        }
        List<RoomVO> roomVOS = new ArrayList<>();
        list.forEach(room -> {
            RoomVO vo = new RoomVO();
            BeanUtils.copyProperties(room, vo);
            vo.setExistHousehold(map.get(room.getId()));
            roomVOS.add(vo);
        });
        return ApiResult.ok(roomVOS);
    }

    /**
     * 1.18 根据社区ID和用户ID获取房间列表
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "用户在社区下申请的房间认证列表", path = "/room/query-by-user")
    @Authorization(verifyApi = false)
    public ApiResult queryRoomByUserId(@Validated(UserToRoom.GetUserRooms.class) @RequestBody UserToRoom entity) {
        Integer client = SessionUtil.getAppSubject().getClient();
        if (client != ClientType.HOUSEHOLD.value() && entity.getUserId() == null) {
            return ApiResult.error(-1, "物业需要填写用户ID查询房间信息");
        }
        if (client == ClientType.HOUSEHOLD.value()) {
            entity.setClosed(Boolean.FALSE);
            entity.setUserId(SessionUtil.getTokenSubject().getUid());
        }
        List<UserToRoom> list = userToRoomFacade.getRoomsByUserId(entity);
        return ApiResult.ok(list);
    }

    @GetMapping(name = "验证房间是否已被认证", path = "/room/{id}/check-owner")
    @Authorization(verifyApi = false)
    public ApiResult getUserByRoomId(@PathVariable("id") ObjectId roomId) {
        UserToRoom userToRoom = userToRoomFacade.findOwnerByRoomId(roomId);
        if (userToRoom == null) {
            return ApiResult.ok(false);
        }
        return ApiResult.ok(true);
    }

    //===================================================【room end】===================================================

    @GetMapping(name = "社区支付平台列表", path = "/{communityId}/trade-platform")
    @Authorization
    public ApiResult getCommunityTradeAccounts(@PathVariable("communityId") ObjectId communityId) {
        List<CommunityTradeAccount> communityTradeAccounts = communityTradeAccountFacade
                .getCommunityTradeAccountsByCommunityIdAndClient(communityId, SessionUtil.getAppSubject().getClient());
        CommunityTradePlatformVO communityTradePlatformVO = new CommunityTradePlatformVO();
        Set<Integer> platforms = new HashSet<>(communityTradeAccounts.size());
        communityTradePlatformVO.setPlatforms(platforms);
        communityTradeAccounts.forEach(communityTradeAccount -> platforms.add(communityTradeAccount.getPlatform()));
        return ApiResult.ok(communityTradePlatformVO);
    }

    /**
     * 获取待处理事务的数量
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "获取待处理事务的数量", path = "/community/{id}/count")
    @Authorization
    public ApiResult countByCommunityId(@PathVariable("id") ObjectId communityId) {
        Map<String, Long> unReviewedMap = userToRoomFacade.countByCommunityIdAndAuditStatus(
                communityId, AuditStatusType.REVIEWING.getType());
        Map<String, Long> unRepairedMap = faultFacade.countUnRepairedFault(communityId, FaultStatusType.WAITACCEPT.key);
        Map<String, Long> unPayMap = propertyFeesFacade.countOverdueBills(communityId, null);
        Map map = new HashMap();
        map.putAll(unReviewedMap);
        map.putAll(unRepairedMap);
        map.putAll(unPayMap);
        return ApiResult.ok(map);
    }

    /**
     * @return
     * @see CommunityController#getUserFunctions
     * @since 2019-06-10
     */
    @GetMapping(name = "获取社区功能菜单及用户角色权限", path = "/community/config")
    @Authorization(verifyApi = false)
    @Deprecated
    public ApiResult getCommunityConfig() {
        UserVO user = SessionUtil.getCurrentUser();
        Community community = communityFacade.findOne(SessionUtil.getCommunityId());
        List<Resource> menus = resourceFacade.getVisibleResourcesByIdsAndClient(
                community.getMenus(), SessionUtil.getAppSubject().getClient());
        CommunityUser communityUser = userFacade.getCommunityUserByCommunityIdAndUserId(community.getId(), user.getId());

        Set<String> menuKeys = new HashSet<>();
        char validChar;
        boolean hasPermission;
        for (Resource menu : menus) {
            hasPermission = communityUser != null && communityUser.getRoles() != null
                    && !CollectionUtils.intersection(menu.getRoles(), communityUser.getRoles()).isEmpty();
            if (!hasPermission && menu.getVisibility() == VisibilityType.AUTH_VISIBLE.value()) {
                continue;
            }

            validChar = hasPermission ? '1' : '0';
            menuKeys.add(menu.getKey() + validChar);
        }

        CommunityConfig communityConfig = new CommunityConfig();
        communityConfig.setMenus(menuKeys);
        communityConfig.setRoles(communityUser == null
                ? null
                : new HashSet<>(CollectionUtils.intersection(user.getRoles(), communityUser.getRoles())));
        return ApiResult.ok(communityConfig);
    }

    @GetMapping(name = "获取登录用户的功能权限", path = "/user/funcs")
    @Authorization(verifyApi = false)
    public ApiResult getUserFunctions() {
        // 住户端
        if (SessionUtil.getAppSubject().getClient() == ClientType.HOUSEHOLD.value()) {
            PermissionSelectionDTO dto = new PermissionSelectionDTO();
            dto.setClient(SessionUtil.getAppSubject().getClient());
            dto.setRoleIds(Collections.singleton(RoleConstants.ROLE_ID_HOUSEHOLD));
            dto.setTenantIds(Collections.singleton(SessionUtil.getCommunityId()));
            return ApiResult.ok(systemFacade.listPermissions(dto));
        }

        UserVO user = SessionUtil.getCurrentUser();
        Community community = communityFacade.findOne(SessionUtil.getCommunityId());
        CommunityUser communityUser = userFacade.getCommunityUserByCommunityIdAndUserId(community.getId(), user.getId());

        if (communityUser == null || communityUser.getRoles() == null || communityUser.getRoles().isEmpty()) {
            log.warn("社区用户不存在，或者该用户在当前社区没有任何角色");
            return ApiResult.ok();
        }
        Set<ObjectId> roleIds = new HashSet<>(communityUser.getRoles().size());
        for (String role : communityUser.getRoles()) {
            if (role.equals(RoleType.COMPANY_ADMIN.name())) {
                roleIds.add(RoleConstants.ROLE_ID_COMPANY_ADMIN);
                continue;
            }
            try {
                ObjectId roleId = new ObjectId(role);
                roleIds.add(roleId);
            } catch (IllegalArgumentException e) {

            }
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return ApiResult.ok();
        }
        PermissionSelectionDTO dto = new PermissionSelectionDTO();
        dto.setClient(SessionUtil.getAppSubject().getClient());
        dto.setRoleIds(roleIds);

        Set<ObjectId> tenantIds = new HashSet<>(2);
        if (SessionUtil.getCommunityId() != null) {
            tenantIds.add(SessionUtil.getCommunityId());
        }
        if (SessionUtil.getCompanyId() != null) {
            tenantIds.add(SessionUtil.getCompanyId());
        }
        if (tenantIds.isEmpty()) {
            return ApiResult.ok();
        }
        dto.setTenantIds(tenantIds);
        return ApiResult.ok(systemFacade.listPermissions(dto));
    }
    // ==============================================【district start】=================================================

    /**
     * 增加物业区域
     *
     * @param district
     * @return
     */
    @PostMapping(name = "新增职能区域", path = "/district/add")
    @Authorization
    public ApiResult addDistrict(@Validated(District.AddDistrict.class) @RequestBody District district) {
        return ApiResult.ok(districtFacade.addDistrict(district, SessionUtil.getTokenSubject().getUid()));
    }

    /**
     * 删除物业区域
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除职能区域", path = "/district/{id}/delete")
    @Authorization
    public ApiResult deleteDistrict(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(districtFacade.removeDistrict(id));
    }

    /**
     * 查询社区物业区域列表
     *
     * @param district
     * @return
     */
    @PostMapping(name = "职能区域列表", path = "/district/list")
    @Authorization
    public ApiResult listDistrict(@Validated(District.ListDistrict.class) @RequestBody District district) {
        List<District> districts = districtFacade.findAllByCommunityId(district.getCommunityId());
        return ApiResult.ok(districts);
    }

    /**
     * 查询社区物业区域列表（分页）
     *
     * @return
     */
    @PostMapping(name = "职能区域分页", path = "/district/page")
    @Authorization
    public ApiResult queryDistrictPage(@Validated(District.ListDistrict.class) @RequestBody District district,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        return ApiResult.ok(districtFacade.queryPage(district, page, size));
    }

    /**
     * 编辑物业人员职能区域
     *
     * @param district
     * @return
     */
    @PostMapping(name = "编辑职能区域", path = "/district/edit")
    @Authorization
    public ApiResult editDistrict(@Validated(District.EditDistrict.class) @RequestBody District district) {
        District districts = districtFacade.updateDistrict(district);
        return ApiResult.ok(districts);
    }

    @RequestMapping(name = "开放职能区域", path = "/district/{id}/open", method = {RequestMethod.GET, RequestMethod.POST})
    @Authorization
    public ApiResult openDistrict(@PathVariable("id") ObjectId id) {
        District district = districtFacade.openDistrict(id);

        EmployeeRequest request = new EmployeeRequest();
        request.setCommunityId(district.getCommunityId());
        request.setCompanyId(SessionUtil.getCompanyId());
        request.setPartner(SessionUtil.getAppSubject().getPartner());
        request.setRoles(Collections.singleton(RoleConstants.ROLE_STR_COMPANY_ADMIN));
        List<UserToProperty> cmAdminList = userToPropertyFacade.listEmployees(request);
        if (CollectionUtils.isNotEmpty(cmAdminList)) {
            updatePropertyDistrict(district, cmAdminList.get(0));
        }
        return ApiResult.ok(district);
    }

    @PostMapping(name = "查看职能区域可选择的楼栋", path = "/district/available-buildings")
    @Authorization
    public ApiResult getAvailableBuildings(@Validated(DistrictRequest.AvailableDistrictBuilding.class)
                                           @RequestBody DistrictRequest districtRequest) {

        List<DistrictBuildingResponse> building = districtFacade.findAvailableBuilding(districtRequest);

        return ApiResult.ok(building);
    }

    @PostMapping(name = "新增第3方职能区域信息", path = "/district/add/third-part-info")
    @Authorization
    public ApiResult addThirdPartInfo(@Validated(District.AddThirdPartInfo.class) @RequestBody District district) {
        District districts = districtFacade.addThirdPartInfo(district);
        return ApiResult.ok(districts);
    }

    // ===============================================【district end】==================================================

    // ================================================【更新物业人员授权区域】================================================

    private void updatePropertyDistrict(District district, UserToProperty userToProperty) {
        // 将区域楼栋授权给物业人员
        userToProperty.getBuildingIds().addAll(district.getBuildingIds());
        userToProperty.getDistrictIds().add(district.getId());
        userToProperty = userToPropertyFacade.updateDistrictForProperty(userToProperty);

        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setCommunityId(userToProperty.getCommunityId());
        deviceAuthVO.setName(userToProperty.getUserName());
        deviceAuthVO.setPhone(userToProperty.getPhone());
        deviceAuthVO.setCorrelationId(userToProperty.getId());
        deviceAuthVO.setHandleCount(0);
        deviceAuthVO.setUserId(userToProperty.getUserId());
        deviceAuthVO.setSex(userToProperty.getSex());
        deviceAuthVO.setDistrictIds(Collections.singleton(district.getId()));

        Set<BuildingListVO> vos = new HashSet<>();
        for (ObjectId objectId : district.getBuildingIds()) {
            BuildingListVO buildingListVO = new BuildingListVO();
            buildingListVO.setBuildingId(objectId);
            vos.add(buildingListVO);
        }
        deviceAuthVO.setBuildingList(vos);

        Card card = cardFacade.applyCardForUser(userToProperty.getUserId(), userToProperty.getCommunityId(), userToProperty.getUserName());
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setKeyNo(card.getKeyNo());
        deviceAuthVO.setKeyId(card.getKeyId());
        deviceAuthVO.setUserIdentity(ClientType.PROPERTY.value());
        deviceAuthVO.setRelationship(RelationshipType.OWNER.KEY);
        Date startDate = new Date();
        deviceAuthVO.setProcessTime((int) DateUtils.secondsBetween(startDate, DateUtils.addYear(startDate, 50)));
        // 使用次数暂时设定为0
        deviceAuthVO.setUsesTime(0);
        List<DeviceAuthVO> deviceAuthVOS = getPhysicalCardsDeviceAuthVOS(deviceAuthVO);
        for (DeviceAuthVO authVO : deviceAuthVOS) {
            Message doorMessage = new Message(TOPIC_COMMUNITY_IOT_DOOR_AUTH, ADD, JSON.toJSONString(authVO).getBytes());
            Message elevatorMessage = new Message(TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH, ADD, JSON.toJSONString(authVO).getBytes());
            try {
                deviceAuthProducer.send(doorMessage);
                deviceAuthProducer.send(elevatorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("updatePropertyDistrict Exception:", e);
            }
        }
    }

    private List<DeviceAuthVO> getPhysicalCardsDeviceAuthVOS(DeviceAuthVO deviceAuthVO) {
        List<Card> physicalCards = getUserPhysicalCardInCommunity(deviceAuthVO);
        List<DeviceAuthVO> deviceAuthVOS = new ArrayList<>(physicalCards.size() + 1);
        deviceAuthVOS.add(deviceAuthVO);
        deviceAuthVOS.addAll(physicalCards.stream().map(physicalCard -> {
            DeviceAuthVO physicalDeviceAuthVO = new DeviceAuthVO();
            org.springframework.beans.BeanUtils.copyProperties(deviceAuthVO, physicalDeviceAuthVO);
            physicalDeviceAuthVO.setKeyType(physicalCard.getKeyType());
            physicalDeviceAuthVO.setKeyId(physicalCard.getKeyId());
            physicalDeviceAuthVO.setKeyNo(physicalCard.getKeyNo());
            return physicalDeviceAuthVO;
        }).collect(Collectors.toSet()));
        return deviceAuthVOS;
    }

    private List<Card> getUserPhysicalCardInCommunity(DeviceAuthVO deviceAuthVO) {
        return cardFacade.findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(deviceAuthVO.getUserId(),
                deviceAuthVO.getCommunityId(), Arrays.asList(CertificateType.BLUETOOTH_CARD.KEY, CertificateType.IC_CARD.KEY));
    }


    // ============================================【全视通】====================================================

    /**
     * 快速创建社区
     *
     * @param structure
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping(name = "全视通快速创建社区", path = "/community/freeview/quickAdd")
    @Authorization
    public ApiResult createFreeViewCommunity(@Validated @RequestBody Structure structure)
            throws UnsupportedEncodingException {
        CommunityParams params = new CommunityParams();
        params.setStructure(structure);
        String jsonResult = doorFacade.getFreeViewByUrlPOST(getAddFreeViewCommunityUrl, params);
        JSONObject jsonObject = JSON.parseObject(jsonResult);
        Object directory = jsonObject.get("Directory");
        if (directory == null) {
            return ApiResult.error(-1, "全视通快速创建社区失败！");
        }
        // === 创建小区
        Community community = createCommunity(structure, directory);
        if (community == null) {
            return ApiResult.error(-1, "创建社区失败");
        }
        // === 创建楼栋
        // 楼栋数量
        Integer buildingNum = params.getStructure().getBuildingNum();
        // 开始楼数
        Integer buildingStart = params.getStructure().getBuildingNumStart();
        List<Building> buildings = createBuildings(buildingNum, community.getId(), directory, buildingStart, structure);
        // === 创建房间
        createRooms(buildings, structure, community.getId());
        return ApiResult.ok(jsonResult);
    }

    @GetMapping(name = "统计社区所有设备数量", path = "/community/allDevice")
    @Authorization
    public ApiResult getAllKindDevices() {
        ObjectId communityId = SessionUtil.getCommunityId();
        CameraRequest cameraRequest = getCameraRequest(communityId);

        List<Door> doors = doorFacade.getDoorsByCommunityId(communityId);
        List<Camera> cameras = cameraFacade.getCameras(cameraRequest);
        List<Gate> gates = inoutFacade.getAllCarGate(communityId);

        DeviceDataStatisticsVO deviceDataStatisticsVO = new DeviceDataStatisticsVO();
        deviceDataStatisticsVO.setDoors(doors.size());
        deviceDataStatisticsVO.setFaultDoors((int) doors.stream()
                .filter(d -> DoorOnlineStatusType.OFFLINE.key.equals(d.getOnlineStatus())).count());
        deviceDataStatisticsVO.setCameras(cameras.size());
        deviceDataStatisticsVO.setFaultCameras((int) cameras.stream().filter(c -> c.getCameraStatus() != 1).count());
        deviceDataStatisticsVO.setGates(gates.size());
        // 车禁暂不统计错误的门禁
        deviceDataStatisticsVO.setFaultGates(0);

        return ApiResult.ok(deviceDataStatisticsVO);
    }

    private CameraRequest getCameraRequest(ObjectId communityId) {
        CameraRequest cameraRequest = new CameraRequest();
        cameraRequest.setCommunityId(communityId);
        return cameraRequest;
    }

    /**
     * 创建小区
     *
     * @param structure
     * @param directory
     * @return
     */
    private Community createCommunity(Structure structure, Object directory) {
        // 创建小区
        Community community = new Community();
        community.setName(structure.getVillageName());
        // 全视通ID
        Map<String, String> outCommunity = new HashMap<>();
        outCommunity.put(String.valueOf(ManufactureType.FREEVIEW_DOOR.KEY), directory.toString());
        community.setOutId(outCommunity);
        community.setCreateId(SessionUtil.getTokenSubject().getUid());
        community.setCode(directory.toString());
        community = communityFacade.addCommunity(community);
        return community;
    }

    private List<Building> createBuildings(Integer buildingNum, ObjectId communityId,
                                           Object directory, Integer buildingStart, Structure structure) {
        String buildingName = callBuildingName(structure.getBuildingName());
        List<Building> buildings = new ArrayList<>();
        for (int i = 0; i < buildingNum; i++) {
            Building building = new Building();
            building.setId(new ObjectId());
            building.setCommunityId(communityId);
            building.setName(buildingName == null ? Assignment("{}{}", buildingStart, "栋")
                    : Assignment(buildingName, buildingStart));
            // 设备枚举
            building.setCode(directory.toString());
            building.setOutId(new StringBuffer(directory.toString()).append("-").append(buildingStart).append(1).toString());
            building.setOverGround(structure.getOverGround());
            building.setUnderGround(structure.getUnderGround());
            // 房间数量
            building.setRoomNum(structure.getBuildingNum() * structure.getFloorNum() * structure.getRoomNum());

            building.setCreateId(SessionUtil.getTokenSubject().getUid());
            building.setCreateAt(new Date());
            building.setUpdateAt(building.getCreateAt());
            building.setRank(i);
            buildingStart++;
            buildings.add(building);
        }
        buildingFacade.addBuildings(buildings);
        return buildings;
    }

    private void createRooms(List<Building> buildings, Structure structure, ObjectId communityId) {
        List<Room> rooms = new ArrayList<>();
        buildings.forEach(building -> {
            for (int j = 0; j < structure.getFloorNum(); j++) {
                for (int k = 0; k < structure.getRoomNum(); k++) {
                    Room room = new Room();
                    room.setName(new StringBuffer(
                            StringUtils.addZeroForNum(structure.getFloorNumStart() + j, 2))
                            .append(StringUtils.addZeroForNum(structure.getRoomNumStart() + k, 2))
                            .toString());
                    room.setBuildingId(building.getId());
                    room.setCommunityId(communityId);
                    room.setFloorNo(String.valueOf(structure.getFloorNumStart() + j));
                    room.setCode(String.valueOf(structure.getFloorNumStart() + j)
                            + String.valueOf(structure.getRoomNumStart() + k));
                    room.setFloorCode(String.valueOf(structure.getFloorNumStart() + j));
                    room.setArea(structure.getArea());
                    room.setOutId(new StringBuffer(building.getOutId())
                            .append("-").append(structure.getFloorNumStart() + j)
                            .append("-").append(structure.getRoomNumStart() + k).toString());
                    room.setRank(Integer.parseInt(new StringBuffer().append(j).append(k).toString()));

                    room.setCreateId(SessionUtil.getTokenSubject().getUid());
                    room.setCreateAt(new Date());
                    room.setUpdateAt(room.getCreateAt());
                    room.setDataStatus(DataStatusType.VALID.KEY);
                    rooms.add(room);
                }
            }
        });
        // 批量新增
        roomFacade.addRooms(rooms);
    }

    /**
     * 楼栋命名
     *
     * @param buildingName
     * @return
     */
    private static String callBuildingName(String buildingName) {
        if (buildingName == null) {
            return null;
        }
        String[] names = buildingName.split(";");
        StringBuffer buildName_ = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            switch (AttributeType.getByValue(getBackDataType(names[i]))) {
                case LETTER:
                    buildName_.append(names[i]);
                    break;
                case DIGIT:
                    buildName_.append("{}");
                    break;
                case STR:
                    buildName_.append(names[i]);
                    break;
                default:
                    break;
            }
        }
        return buildName_.toString();
    }

    private static String Assignment(String format, Object... argArray) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
        return ft.getMessage();
    }

    private enum AttributeType {
        // 字母、数字、字符串
        LETTER(1), DIGIT(2), STR(3);

        public int value;

        AttributeType(Integer value) {
            this.value = value;
        }

        private static AttributeType getByValue(int value) {
            for (AttributeType attributeType : values()) {
                if (attributeType.value == value) {
                    return attributeType;
                }
            }
            return null;
        }
    }

    /**
     * 判断是否数字、字母
     *
     * @param str
     * @return
     */
    private static int getBackDataType(String str) {
        // 是否数字
        if (PATTERN_NUMBER.matcher(str).matches()) {
            return AttributeType.DIGIT.value;
        }
        //判断字符串是否全为英文字母，是则返回true
        boolean isWord = str.matches("[a-zA-Z]+");
        if (isWord) {
            return AttributeType.LETTER.value;
        }
        return AttributeType.STR.value;
    }

    // ============================================【大屏】==============================================================

    /**
     * 布局列表
     *
     * @param displayable
     * @param screenRatioType
     * @return
     */
    @GetMapping(name = "大屏布局列表", path = "/data/layouts")
    @Authorization
    public ApiResult listLayouts(Boolean displayable, Integer screenRatioType) {
        ObjectId communityId = SessionUtil.getCommunityId();
        DataLayoutQuery query = new DataLayoutQuery();
        query.setCommunityId(communityId);
        query.setDisplayable(displayable);
        query.setScreenRatioType(screenRatioType);
        List<DataLayout> layoutList = dataLayoutFacade.listDataLayouts(query);
        return ApiResult.ok(layoutList);
    }

    /**
     * 保存布局
     *
     * @param dataLayout
     * @return
     */
    @PostMapping(name = "保存布局", path = "/data/layouts/save")
    @Authorization
    public ApiResult saveLayout(@RequestBody DataLayout dataLayout) {
        if (dataLayout.getId() == null) {
            throw LAYOUT_ID_NULL;
        }
        DataLayout layout = dataLayoutFacade.modifyDataLayout(dataLayout);
        return ApiResult.ok(layout);
    }

    /**
     * 批量保存布局
     *
     * @param dataLayouts
     * @return
     */
    @PostMapping(name = "批量保存布局", path = "/data/layouts/batch-save")
    @Authorization
    public ApiResult saveLayouts(@RequestBody List<DataLayout> dataLayouts) {
        if (dataLayouts == null || dataLayouts.isEmpty()) {
            return ApiResult.ok();
        }
        dataLayoutFacade.saveDataLayouts(dataLayouts);
        return ApiResult.ok();
    }

}