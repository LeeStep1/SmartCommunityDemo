package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.enums.CommunityTypeEnum;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.ManufactureType;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.MiliConnection;
import cn.bit.facade.vo.user.*;
import cn.bit.facade.vo.user.mili.MiliBuilding;
import cn.bit.facade.vo.user.mili.MiliCommunity;
import cn.bit.facade.vo.user.mili.MiliRoom;
import cn.bit.facade.vo.user.mili.ServiceVO;
import cn.bit.framework.utils.string.StringUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MiliDeviceController {

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private MiliConnection miligcConnection;

    /**
     * done
     * 获取小区
     *
     * @return
     */
    @GetMapping(name = "米立关联的小区列表", path = "/wuye/community/{company}/relevance")
    public ApiResult getWyCommunityList(@PathVariable("company") String company) throws Exception {
        Map<String, Object> apiGet = new HashMap<String, Object>();
        switch (company) {
            case "mili":
                apiGet = miligcConnection.APIGet("wuye/community");
                break;
            default:
                break;
        }
        Integer errorCode = Integer.parseInt(apiGet.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = apiGet.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = apiGet.get("body");

        List<MiliCommunity> list = JSON.parseArray(body.toString(), MiliCommunity.class);
/*        if (list != null && list.size() > 0) {
            for (MiliCommunity entity : list) {
                Community item = new Community();
                item.setMiliCId(entity.getId());
                item.setName(entity.getName());
                item.setYun_community_id(entity.getYun_community_id());
                item.setDataStatus(DataStatusType.VALID.KEY);
                // item.setDataStatus(entity.getState());
                communityFacade.addCommunity(item);
            }
        }*/
        return ApiResult.ok(apiGet);
    }

    // =======================[楼宇]===============================

    /**
     * 关联楼宇
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "米立关联小区的楼栋列表", path = "/wuye/building/{communityId}/relevance")
    public ApiResult getWyBuildingList(@PathVariable("communityId") String communityId, String miliCommunityId)
            throws Exception {
        Map<String, Object> apiGet = new HashMap<String, Object>();
        if (StringUtil.isNotNull(miliCommunityId)) {
            apiGet = miligcConnection.APIGet("wuye/community/" + miliCommunityId + "/building");

            Integer errorCode = Integer.parseInt(apiGet.get("errorCode").toString());
            if (errorCode != 1) {
                String msg = apiGet.get("errorMsg").toString();
                return ApiResult.error(-1, msg);
            }
            Object body = apiGet.get("body");

            List<MiliBuilding> list = JSON.parseArray(body.toString(), MiliBuilding.class);
            /*for (MiliBuilding entity : list) {
                Building item = new Building();
                item.setMiliBId(entity.getId());
                item.setCommunityId(new ObjectId(communityId));
                item.setCode(entity.getCode());
                item.setName(entity.getName());
                item.setDataStatus(DataStatusType.VALID.KEY);
                buildingFacade.addBuilding(item);
            }*/
        } return ApiResult.ok(apiGet);
    }

    // =======================[房号]===============================

    /**
     * 关联房间
     *
     * @param communityId
     * @param buildingId
     * @return
     */
    @GetMapping(name = "米立关联小区楼栋的房间列表", path = "/wuye/room/{communityId}/{buildingId}/relevance")
    public ApiResult getWyRoomList(@PathVariable("communityId") String communityId,
                                   @PathVariable("buildingId") String buildingId, String miliCommunityId,
                                   String miliBuildingId) throws Exception {
        Map<String, Object> apiGet = miligcConnection
                .APIGet("wuye/community/" + miliCommunityId + "/building/" + miliBuildingId + "/room");

        Integer errorCode = Integer.parseInt(apiGet.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = apiGet.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = apiGet.get("body");

        List<MiliRoom> list = JSON.parseArray(body.toString(), MiliRoom.class);
        /*for (MiliRoom entity : list) {
            Room item = new Room();
            item.setOutId(entity.getId());
            item.setName(entity.getName());
            item.setBuildingId(new ObjectId(buildingId));
            item.setCommunityId(new ObjectId(communityId));
            item.setYun_proprietor_id(entity.getYun_community_id());
            item.setDataStatus(DataStatusType.VALID.KEY);
            roomFacade.addRoom(item);
        }*/
        return ApiResult.ok(apiGet);
    }

    /**
     * 获取小区服务
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "获取米立小区服务", path = "/wuye/community/{communityId}/service")
    public ApiResult queryService(@PathVariable("communityId") String communityId) throws IOException {
        Map<String, Object> result = miligcConnection.APIGet("wuye/community/" + communityId);
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("body");

        ServiceVO serviceVO = JSON.toJavaObject(JSON.parseObject(body.toString()), ServiceVO.class);
        return ApiResult.ok(serviceVO);
    }


    // ========================[业主]==============================

    /**
     * 新增业主信息
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "米立新增业主信息", path = "/wuye/proprietor/add")
    public ApiResult proprietorAdd(@Validated @RequestBody Proprietor entity) throws Exception {

        Map<String, Object> apiresult = miligcConnection.APIPost("wuye/proprietor/add", entity);
        Integer errorCode = Integer.parseInt(apiresult.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = apiresult.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = apiresult.get("body");

        try {
            Proprietor item = JSON.parseObject(JSON.toJSONString(body), Proprietor.class);
            return ApiResult.ok(item);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return ApiResult.error(-1, "系统错误");
    }


    /**
     * 删除业主信息
     *
     * @param proprietorId
     * @return
     */
    @GetMapping(name = "米立删除业主信息", path = "/wuye/proprietor/{proprietorId}/delete")
    public ApiResult wyProprietorDelete(@PathVariable("proprietorId") String proprietorId) throws IOException {
        Map<String, Object> apiGet = miligcConnection.APIGet("wuye/proprietor/" + proprietorId + "/delete");
        Integer errorCode = Integer.parseInt(apiGet.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = apiGet.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        return ApiResult.ok(apiGet.get("errorMsg"));
    }


    /**
     * 小区业主列表查询
     *
     * @param communityId
     * @return
     * @throws IOException
     */
    @GetMapping(name = "米立小区业主列表", path = "/wuye/community/{communityId}/proprietor")
    public ApiResult queryProprietor(@PathVariable("communityId") String communityId,
                                     @RequestParam(defaultValue = "1") Integer page) throws IOException {
        Map<String, Object> result = miligcConnection
                .APIGet("wuye/community/" + communityId + "/proprietor?page=" + page);
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("body");

        List<Proprietors> list = JSON.parseArray(body.toString(), Proprietors.class);
        return ApiResult.ok(list);
    }


    /**
     * 查询业主设备
     *
     * @param serviceId
     * @return
     */
    @GetMapping(name = "业主设备查询", path = "/wuye/proprietor/{proprietorId}/queryDevice")
    public ApiResult queryDevices(@PathVariable("proprietorId") String proprietorId, String serviceId)
            throws Exception {
        Map<String, Object> map = miligcConnection
                .APIPost("wuye/proprietor/" + proprietorId + "/device/query?service_id=" + serviceId, null);
        Integer errorCode = Integer.parseInt(map.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = map.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = map.get("body");
        Serve item = JSON.toJavaObject(JSON.parseObject(body.toString()), Serve.class);
        return ApiResult.ok(item);
    }

    /**
     * 授权业主设备
     *
     * @param params
     * @return
     */
    @PostMapping(name = "授权业主设备", path = "/wuye/proprietor/device/add")
    public ApiResult addDevice(@RequestBody Map<String, Object> params) throws Exception {
        // 用户ID
        Object proprietor_id = params.get("proprietorId");
        // 服务ID
        Object service_id = params.get("serviceId");
        // 设备ID
        Object device_id = params.get("deviceId");
        // 服务状态
        Object service_status = params.get("serviceStatus");

        Map<String, Object> result = miligcConnection.APIPost(
                "wuye/proprietor/" + proprietor_id + "/device/add?service_id=" + service_id + "&&device_id=" +
                        device_id + "&&service_status=" + service_status,
                null);
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("body");
        return ApiResult.ok(body);
    }

    /**
     * 用户删除设备
     *
     * @param proprietorId
     * @return
     * @throws IOException
     */
    @PostMapping(name = "用户删除设备", path = "/wuye/proprietor/{proprietorId}/device/delete")
    public ApiResult deleteDevice(@PathVariable("proprietorId") String proprietorId,
                                  @RequestBody Map<String, Object> params) throws Exception {
        // 服务ID
        Object service_id = params.get("serviceId");
        // 设备ID
        Object device_id = params.get("deviceId");
        Map<String, Object> result = miligcConnection.APIPost(
                "wuye/proprietor/" + proprietorId + "/device/delete?service_id=" + service_id + "&device_id=" +
                        device_id,
                null);
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("errorMsg");
        return ApiResult.ok(body);
    }

    /**
     * 设备列表查询
     *
     * @param communityId
     * @return
     * @throws Exception
     */
    @GetMapping(name = "社区设备列表", path = "/wuye/community/{communityId}/device")
    public ApiResult queryDevice(@PathVariable("communityId") String communityId, String serviceId) throws Exception {
        Map<String, Object> result = miligcConnection
                .APIPost("wuye/community/" + communityId + "/device?service_id=" + serviceId, null);
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("body");
        List<Device> list = JSON.parseArray(body.toString(), Device.class);
        return ApiResult.ok(list);
    }

    /**
     * 查看设备运行状态
     *
     * @param communityId
     * @param diviceId
     * @return
     * @throws IOException
     */
    @GetMapping(name = "某设备运行状态查询", path = "/wuye/community/{communityId}/device/{diviceId}/data")
    public ApiResult getDeviceData(@PathVariable("communityId") String communityId,
                                   @PathVariable("diviceId") String diviceId) throws IOException {
        Map<String, Object> result = miligcConnection
                .APIGet("wuye/community/" + communityId + "/device/" + diviceId + "/data");
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("body");
        DeviceData item = JSON.toJavaObject(JSON.parseObject(body.toString()), DeviceData.class);
        return ApiResult.ok(item);
    }

    /**
     * 小区业主列表删除
     *
     * @param communityId
     * @return
     * @throws IOException
     */
    @GetMapping(name = "批量删除小区业主", path = "/wuye/community/{communityId}/proprietor/batch")
    public ApiResult queryProprietorBatchDelete(@PathVariable("communityId") String communityId) throws IOException {
        Map<String, Object> result = miligcConnection.APIGet("wuye/community/" + communityId + "/proprietor");
        Integer errorCode = Integer.parseInt(result.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = result.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = result.get("body");

        List<Proprietors> list = JSON.parseArray(body.toString(), Proprietors.class);
        list.forEach(proprietors -> {

            try {
                Map<String, Object> data = miligcConnection
                        .APIGet("wuye/proprietor/" + proprietors.getId() + "/delete");
            } catch (Exception e) {
                log.error("IOException", e);
            }
        });
        return ApiResult.ok();
    }

    /**
     * 直接同步米立社区房间到数据库(社区 楼栋 房间)
     *
     * @return
     * @throws Exception
     */
    @GetMapping(name = "直接同步米立社区房间到数据库(社区 楼栋 房间)", path = "/wuye/community/relevance/list")
    @Authorization
    public ApiResult getMiliCommunityList() {
        Map apiGet;
        apiGet = miligcConnection.APIGet("wuye/community");
        Integer errorCode = Integer.parseInt(apiGet.get("errorCode").toString());
        if (errorCode != 1) {
            String msg = apiGet.get("errorMsg").toString();
            return ApiResult.error(-1, msg);
        }
        Object body = apiGet.get("body");

        List<MiliCommunity> list = JSON.parseArray(body.toString(), MiliCommunity.class);

        // 防止重复添加
        List<Community> communities = communityFacade.queryMiliCommunities();
        List<Building> buildings = buildingFacade
                .queryMiliBuildings(communities.stream().map(Community::getId).collect(Collectors.toList()));
        List<Room> rooms = roomFacade
                .queryMiliRooms(buildings.stream().map(Building::getId).collect(Collectors.toList()));
        Set<Long> miliCIds = communities.stream().map(Community::getMiliCId).collect(Collectors.toSet());
        Set<Long> miliBIds = buildings.stream().map(Building::getMiliBId).collect(Collectors.toSet());
        Map<Long, Building> buildingMap = buildings.stream().collect(Collectors.toMap(Building::getMiliBId, b -> b));
        Set<Long> miliRIds = new HashSet<>();
        rooms.forEach(room -> miliRIds.add(Long.valueOf(room.getOutId().contains("-") ? "-1" : room.getOutId())));

        List<Building> toAddBuildings = new ArrayList<>();
        List<Room> toAddRooms = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (MiliCommunity entity : list) {
                Community item = new Community();
                item.setMiliCId(entity.getId());
                item.setOutId(
                        Collections.singletonMap(String.valueOf(ManufactureType.MILI.KEY), String.valueOf(entity.getId())));
                item.setName(entity.getName());
                item.setYun_community_id(entity.getYun_community_id());
                item.setDataStatus(DataStatusType.VALID.KEY);
                item.setCoordinate("109,40");
                item.setType(CommunityTypeEnum.RESIDENCE.value());
                item.setProvince("内蒙古");
                item.setCity("包头市");
                item.setAddress("和谐警苑");
                item.setCreateId(SessionUtil.getTokenSubject().getUid());
                item.setCreateAt(new Date());
                item.setCode("15020700");
                item.setCountry("中国");
                // 查询已有的米立社区，如果没有就新增入库
                Community community = communities.stream()
                        .filter(miliCm -> miliCm.getMiliCId().equals(entity.getId()))
                        .findFirst().get();
                        //.orElse(communityFacade.addCommunity(item));
                if (item.getMiliCId() != null) {
                    apiGet = miligcConnection.APIGet("wuye/community/" + item.getMiliCId() + "/building");

                    body = apiGet.get("body");

                    List<MiliBuilding> newBuildingList = JSON.parseArray(body.toString(), MiliBuilding.class);
                    int num = 0;
                    for (int i = 0; i < newBuildingList.size(); i++) {
                        //if (miliBIds.contains(newBuildingList.get(i).getId())) {
                        if (newBuildingList.get(i).getName().contains("A区")
                            || newBuildingList.get(i).getName().contains("B区")
                            || newBuildingList.get(i).getName().contains("物业")
                            || newBuildingList.get(i).getName().contains("E区")
                            || newBuildingList.get(i).getName().contains("E1区")
                            || newBuildingList.get(i).getName().contains("F区")) {
                            continue;
                        }
                        Building building = buildingMap.get(newBuildingList.get(i).getId());
                        Building itemBuilding = new Building();
                        itemBuilding.setId(new ObjectId());
                        itemBuilding.setName(newBuildingList.get(i).getName());
                        if (building != null) {
                            itemBuilding.setId(building.getId());
                            itemBuilding.setName(building.getName());
                        }

                        itemBuilding.setMiliBId(newBuildingList.get(i).getId());
                        itemBuilding.setRank(i);
                        //itemBuilding.setCoordinate("109,40");
                        itemBuilding.setUnderGround(0);
                        itemBuilding.setOverGround(0);
                        itemBuilding.setOutId(String.valueOf(newBuildingList.get(i).getId()));
                        itemBuilding.setCommunityId(community.getId());
                        itemBuilding.setCode(newBuildingList.get(i).getCode());
                        itemBuilding.setDataStatus(DataStatusType.VALID.KEY);
                        itemBuilding.setRoomNum(0);
                        itemBuilding.setOpen(false);
                        toAddBuildings.add(itemBuilding);
                        try {
                            apiGet = miligcConnection.APIGet("wuye/community/" + item.getMiliCId() +
                                    "/building/" + itemBuilding.getMiliBId() + "/room");
                        } catch (Exception e) {
                            log.error("IOException", e);
                        }

                        Object bodyRoom = apiGet.get("body");

                        List<MiliRoom> newRoomList = JSON.parseArray(bodyRoom.toString(), MiliRoom.class);
                        for (MiliRoom entityRoom : newRoomList) {
                            //if (entityRoom == null || miliRIds.contains(entityRoom.getId())) {
                            //    continue;
                            //}
                            //Integer floor = Integer.valueOf(entityRoom.getName().substring(3, entityRoom.getName().length() - 2));
                            //itemBuilding.setOverGround(itemBuilding.getOverGround() > floor ? itemBuilding.getOverGround() : floor);

                            Room itemRoom = new Room();
                            itemRoom.setId(new ObjectId());
                            //itemRoom.setFloorCode(String.valueOf(floor));
                            //itemRoom.setFloorNo(String.valueOf(floor));
                            itemRoom.setOutId(String.valueOf(entityRoom.getId()));
                            itemRoom.setName(entityRoom.getName());
                            itemRoom.setBuildingId(itemBuilding.getId());
                            itemRoom.setCommunityId(community.getId());
                            itemRoom.setYun_proprietor_id(entityRoom.getYun_community_id());
                            itemRoom.setDataStatus(DataStatusType.VALID.KEY);
                            //itemRoom.setArea(3000);
                            toAddRooms.add(itemRoom);
                            itemBuilding.setRoomNum(itemBuilding.getRoomNum() + 1);
                            if (toAddRooms.size() >= 1000) {
                                //roomFacade.insertAllRooms(toAddRooms, SessionUtil.getTokenSubject().getUid());
                                try {
                                    writeCsv("room" + ++num, toAddRooms);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                toAddRooms.clear();
                            }
                        }
                    }
                    if (CollectionUtils.isNotEmpty(toAddBuildings)) {
                        //buildingFacade.insertAllBuildings(toAddBuildings, SessionUtil.getTokenSubject().getUid());
                        try {
                            writeCsv("building", toAddBuildings);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (CollectionUtils.isNotEmpty(toAddRooms)) {
                        //roomFacade.insertAllRooms(toAddRooms, SessionUtil.getTokenSubject().getUid());
                        try {
                            writeCsv("room", toAddRooms);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ApiResult.ok(apiGet);
    }

    private void writeCsv(String fileName, List obj) throws Exception {
        FileOutputStream fos = new FileOutputStream(String.format("C:\\Users\\DELL\\Desktop\\mili\\%s.csv", fileName));
        OutputStreamWriter osw = new OutputStreamWriter(fos, "GBK");

        CSVFormat csvFormat = CSVFormat.DEFAULT;
        CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);

        for (Object o : obj) {
            List fieldList = new ArrayList();
            for (Method method : o.getClass().getMethods()) {
                if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                    Object result = method.invoke(o);
                    if (result != null) {
                        fieldList.add(result);
                    }
                }
            }
            csvPrinter.printRecord(fieldList);
        }

        csvPrinter.flush();
        csvPrinter.close();

    }
}
