package cn.bit.communityIoT.service;

import cn.bit.communityIoT.dao.DoorRepository;
import cn.bit.communityIoT.support.door.DoorStrategy;
import cn.bit.communityIoT.support.freeview.FreeViewToken;
import cn.bit.communityIoT.support.freeview.FreeViewUtil;
import cn.bit.communityIoT.support.miligc.MiligcConnection;
import cn.bit.facade.enums.*;
import cn.bit.facade.exception.communityIoT.CommunityIoTBizException;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.community.zhfreeview.CommunityParams;
import cn.bit.facade.vo.community.zhfreeview.DeviceParam;
import cn.bit.facade.vo.community.zhfreeview.DeviceParams;
import cn.bit.facade.vo.communityIoT.DeviceRequest;
import cn.bit.facade.vo.communityIoT.door.*;
import cn.bit.facade.vo.communityIoT.door.freeview.FreeViewCardRequest;
import cn.bit.facade.vo.communityIoT.door.freeview.FreeViewRequest;
import cn.bit.facade.vo.communityIoT.door.freeview.UserFeature;
import cn.bit.facade.vo.mq.FreeViewDoorAuthVO;
import cn.bit.facade.vo.mq.KangTuDoorAuthVO;
import cn.bit.facade.vo.mq.MiliDoorAuthVO;
import cn.bit.facade.vo.user.Proprietor;
import cn.bit.facade.vo.user.card.CardRequest;
import cn.bit.facade.vo.user.card.CardVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.UUIDUitl;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.*;
import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.*;

@Service("doorFacade")
@Slf4j
public class DoorFacadeImpl implements DoorFacade {

    /**
     * 请求路径
     */
    @Value("${card.request.url}")
    private String url;

    @Value("${freeView.url}")
    private String freeViewUrl;
    @Value("${freeView.basic.uuid}")
    private String freeViewUUID;
    @Value("${freeView.username}")
    private String freeViewUserName;
    @Value("${freeView.password}")
    private String freeViewPassword;
    @Value("${freeView.tenantcode}")
    private String freeViewTenantCode;
    @Value("${freeView.secretcode.times}")
    private Integer secretCodeTimes;

    private String access_token;

    /**
     * 康途远程开门
     */
    private static final String KANGTU_REMOTE_OPEN_DOOR = "/certificate/remoteOpenDoor";

    /**
     * 全视通远程开门
     */
    private static final String FREEVIEW_REMOTE_OPEN_DOOR = ":21664/api/RemoteUnlock/OpenDoor";

    /**
     * 全视通获取钥匙包
     */
    private static final String FREEVIEW_REMOTE_GET_DOOR = ":21664/api/RemoteUnlock/GetDoors";

    /**
     * 门禁列表
     */
    private static final String FIND_KEYNO_DOOR = "/certificate/door/findByKeyNos";

    /**
     * 设备详情
     */
    private static final String CARD_VIEW = "/certificate/view";

    /**
     * 申请门禁权限
     */
    private static final String DOOR_UPDATE_URL = "/certificate/door/add";

    /**
     * 删除康途门禁权限
     */
    private static final String DOOR_DELETE_URL = "/certificate/door/remove";

    /**
     * 覆盖康途门禁权限
     */
    private static final String DOOR_COVER_URL = "/certificate/door/keep";


    private static final String CARD_COPY = "/certificate/copy";

    /**
     * 全视通更新权限
     */
    private static final String FREEVIEW_APP_TOKEN = ":9700/api/appaccesstoken";

    /**
     * 全视通代理注册
     */
    private static final String FREEVIEW_AGENT_REGISTER = ":9700/api/AgentRegister";

    /**
     * 全视通更新权限
     */
    private static final String FREEVIEW_CHANGE_URL = ":21664/api/userrooms";

    /**
     * 全视通申请二维码
     */
    private static final String FREEVIEW_APPLY_SECRET = ":21664/api/GeneralSecretCodeWithOpenDoor";

    /**
     * 全视通开门
     */
    private static final String FREEVIEW_NOTIFY_CARD_READ = ":21664/api/NotifyDeviceReadCard";

    /**
     * 全视通卡操作
     */
    private static final String FREEVIEW_APPLY_CARD = ":21664/api/usercards";

    /**
     * 全视通更新权限
     */
    private static final String FREEVIEW_CARD_AUTH = ":21664/api/cards";

    /**
     * 全视通机器发卡
     */
    private static final String FREEVIEW_DEVICE_AUTH = ":21664/api/GeneralCards";

    /**
     * 全视通人脸识别
     */
    private static final String FREEVIEW_USER_FEATURE = ":21664/api/UserHumanFeatures";

    /**
     * 全视通查看人脸
     */
    private static final String FREEVIEW_FEATURE_PIC = ":21664/api/HumanFaceMaterial";


    @Autowired
    private DoorRepository doorRepository;

    @Autowired
    private MiligcConnection miligcConnection;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private List<DoorStrategy> doorStrategies;

    @Override
    public Door getDoorById(ObjectId id) {
        if (id == null) {
            throw DOOR_ID_NULL;
        }
        return doorRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Door addDoor(Door entity) {
        if (Objects.nonNull(entity.getBrandNo())) {
            entity.setBrand(ManufactureType.getValueByKey(entity.getBrandNo()));
        }
        entity.setCreateAt(new Date());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return doorRepository.insert(entity);
    }

    @Override
    public void saveDoors(List<Door> doors) {
        doorRepository.insertAll(doors);
    }

    @Override
    public Door deleteDoor(ObjectId id) {
        if (id == null) {
            throw DOOR_ID_NULL;
        }
        Door door = new Door();
        door.setUpdateAt(new Date());
        door.setDataStatus(DataStatusType.INVALID.KEY);
        return doorRepository.updateByIdAndDataStatus(door, id, DataStatusType.VALID.KEY);
    }

    @Override
    public Door updateDoor(Door entity) {
        if (entity == null || entity.getId() == null) {
            throw DOOR_ID_NULL;
        }
        ObjectId id = entity.getId();
        if (Objects.nonNull(entity.getBrandNo())) {
            entity.setBrand(ManufactureType.getValueByKey(entity.getBrandNo()));
        }
        entity.setUpdateAt(new Date());
        entity.setId(null);
        return doorRepository.updateByIdAndDataStatus(entity, id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Door> getServiceDoors(DoorRequest doorRequest) throws BizException {
        // 蓝牙设备
        if (doorRequest.getServiceId() == null) {
            throw NOT_SERVICE_ID;
        }
        doorRequest.setDataStatus(DataStatusType.VALID.KEY);
        return doorRepository.findByDoorRequest(doorRequest);
    }

    /**
     * 查询社区下的所有门禁列表
     *
     * @param communityId
     * @return
     */
    @Override
    public List<Door> getDoorsByCommunityId(ObjectId communityId) {
        return doorRepository.findByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public String getAccessToken() {
        return access_token;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    @Override
    public List<Door> getDoorByTerminalCode(DeviceRequest deviceRequest) {
        // 暂时只有康途的门禁，社区ID和终端号皆为空
        return doorRepository.findByCommunityIdIgnoreNullAndTerminalCodeIgnoreNullAndBrandNoInAndDataStatus(deviceRequest.getCommunityId(),
                deviceRequest.getTerminalCode(), Collections.singleton(ManufactureType.KANGTU_DOOR.KEY), DataStatusType.VALID.KEY);
    }

    @Override
    public void deleteFreeViewCardWithoutUserInfo(Room room, String keyNo) {
        List<Door> doors = this.getBuildingAndCommunityDoorByBrandNo(Collections.singleton(room.getBuildingId()),
                                                                           room.getCommunityId(),
                                                                           ManufactureType.FREEVIEW_DOOR.KEY);
        for (Door door : doors) {
            Map<String, Object> uriVar = new HashMap<>();
            uriVar.put("tenantCode", freeViewTenantCode);
            uriVar.put("cardSerialNumber", keyNo);
            uriVar.put("deviceLocalDirectory", door.getDeviceCode());

            RequestEntity<Void> requestEntity = RequestEntity.delete(URI.create(expandURL(freeViewUrl +
                                                                                          FREEVIEW_DEVICE_AUTH, uriVar)))
                                                             .header("Authorization", access_token)
                                                             .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();
            callFreeViewURL(requestEntity);
        }
    }

    @Override
    public Page<Door> getBluetoothDoors(DoorRequest doorRequest, int page, int size) {
        doorRequest.setDataStatus(DataStatusType.VALID.KEY);
        return doorRepository.findByDoorRequest(doorRequest, page, size);
    }

    @Override
    public boolean getDoorByDeviceId(Long deviceId) {
        List<Door> door = doorRepository.findByDeviceIdAndDataStatus(deviceId, DataStatusType.VALID.KEY);
        return door.size() == 0;
    }

    @Override
    public Door getDoorByCommunityIdAndMacAddress(ObjectId communityId, String macAddress) {
        return doorRepository.findByCommunityIdAndMac(communityId, macAddress);
    }

    @Override
    public Door getDoorByDeviceIdAndBrandNoAndDeviceCode(Long deviceId, Integer brandNo, String deviceCode) {
        return doorRepository.findByDeviceIdAndBrandNoAndDeviceCodeAndDataStatus(deviceId,brandNo,deviceCode, DataStatusType.VALID.KEY);
    }

    @Override
    public Door bindDoor(Door entity) {
        Door door = this.getDoorById(entity.getId());
        if(door == null){
            throw DOOR_NOT_EXIST;
        }
        entity.setId(null);
        // 判断是楼栋门还是社区门(若没有)
        if (entity.getBuildingId() != null) {
            entity.setDoorType(DoorType.BUILDING_DOOR.getValue());
        } else {
            entity.setDoorType(DoorType.COMMUNITY_DOOR.getValue());
        }
        return doorRepository.updateByIdAndDataStatus(entity, door.getId(), DataStatusType.VALID.KEY);
    }

    @Override
    public Page<DoorVo> getAllDoorsRecord(Door entity, Integer page, Integer size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.ASC, "createAt"));
        org.springframework.data.domain.Page<DoorVo> doorPage =
                doorRepository.findByCommunityIdAndBuildingIdAndDeviceCodeAndBrandNoAndNameRegexAndOnlineStatusAndDataStatusAllIgnoreNull(
                        entity.getCommunityId(), entity.getBuildingId(), entity.getDeviceCode(), entity.getBrandNo(),
                        StringUtil.makeQueryStringAllRegExp(entity.getName()), entity.getOnlineStatus(),
                        DataStatusType.VALID.KEY, pageable, DoorVo.class);
        return PageUtils.getPage(doorPage);
    }

    @Override
    public List<Door> getDoorByCommunityIdAndBuildingId(ObjectId communityId, ObjectId buildingId) {
        return doorRepository.findByCommunityIdAndBuildingIdAndDataStatus(communityId, buildingId, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Door> getDoorsInIds(Set<ObjectId> doorIds) {
        return doorRepository.findByIdInAndDataStatus(doorIds, DataStatusType.VALID.KEY);
    }

    @Override
    public DoorInfoResult getAuthDoorList(Card card) {
        if (card == null) {
            throw CARD_EMPTY;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> findByKeyNosRequest = new HttpEntity<>(JSON.toJSON(card), headers);

        DoorInfoResult findByKeyNosResponse = restTemplate.postForObject(url + FIND_KEYNO_DOOR, findByKeyNosRequest, DoorInfoResult.class);
        if (!findByKeyNosResponse.isSuccess()) {
            log.warn(findByKeyNosResponse.getErrorMsg());
            throw new CommunityIoTBizException(findByKeyNosResponse.getErrorCode(), findByKeyNosResponse.getErrorMsg());
        }
        return findByKeyNosResponse;
    }

    private Boolean kangTuRemoteOpenDoor(Door door) {
        DoorDeviceVO doorDeviceVO = new DoorDeviceVO(door.getTerminalPort(), door.getMac(), door.getTerminalCode());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> certificateRemoteOpenDoorRequest = new HttpEntity<>(JSON.toJSON(doorDeviceVO), headers);

        JSONObject remoteOpenDoorResponse = restTemplate.postForObject(url + KANGTU_REMOTE_OPEN_DOOR, certificateRemoteOpenDoorRequest, JSONObject.class);
        if (!remoteOpenDoorResponse.getBoolean("success")) {
            throw new CommunityIoTBizException(8010008, remoteOpenDoorResponse.getString("errorMsg"));
        }
        return remoteOpenDoorResponse.getBoolean("success");
    }

    @Override
    public List<Door> getBuildingAndCommunityDoor(Set<ObjectId> buildingIds, ObjectId communityId) {
        // 查询社区门与楼栋门
        List<Door> doors = doorRepository.findByCommunityIdAndBuildingIdInAndDoorTypeAndDataStatusOrCommunityIdAndDoorTypeAndDataStatus(
                communityId, buildingIds, DoorType.BUILDING_DOOR.getValue(), DataStatusType.VALID.KEY,
                communityId, DoorType.COMMUNITY_DOOR.getValue(), DataStatusType.VALID.KEY
        );
        return doors;
    }

    @Override
    public List<Door> getBuildingAndCommunityDoorByBrandNo(Set<ObjectId> buildingIds, ObjectId communityId, Integer brandNo) {
        // 查询社区门与楼栋门
        List<Door> doors = doorRepository.findByCommunityIdAndBuildingIdInAndDoorTypeAndBrandNoAndDataStatusOrCommunityIdAndDoorTypeAndBrandNoAndDataStatus(
                communityId, buildingIds, DoorType.BUILDING_DOOR.getValue(), brandNo, DataStatusType.VALID.KEY,
                communityId, DoorType.COMMUNITY_DOOR.getValue(), brandNo, DataStatusType.VALID.KEY
        );
        return doors;
    }

    @Override
    public List<Door> getBuildingAndCommunityDoorByDoorRequest(DoorRequest doorRequest) {
	    List<Door> doors = doorRepository.findByCommunityIdAndBuildingIdInAndDoorTypeAndBrandNoInAndServiceIdAndDataStatusOrCommunityIdAndDoorTypeAndBrandNoInAndServiceIdAndDataStatus(
			    doorRequest.getCommunityId(), doorRequest.getBuildingId(), DoorType.BUILDING_DOOR.getValue(), doorRequest.getBrandNo(), doorRequest.getServiceId(), DataStatusType.VALID.KEY,
			    doorRequest.getCommunityId(), DoorType.COMMUNITY_DOOR.getValue(), doorRequest.getBrandNo(), doorRequest.getServiceId(), DataStatusType.VALID.KEY
	    );
        return doors;
    }

    @Override
    public List<Object> getAllAuthListInDoors(Card card, List<Object> authDeviceList) {
        // 从电梯物联查询后查询数据库详细信息
        Set<ObjectId> ids = new HashSet<>();
        // 查询卡门禁设备
        DoorInfoResult authDoorList = this.getAuthDoorList(card);
        if (authDoorList != null && authDoorList.getData() != null && authDoorList.getData().size() > 0) {
            authDoorList.getData().forEach(doorInfo -> ids.add(doorInfo.getId()));
            List<Door> doorsInIds = this.getDoorsInIds(ids);
            authDoorList.getData().forEach(doorInfo -> {
                for (Door door : doorsInIds) {
                    if (Objects.equals(door.getId(), doorInfo.getId())) {
                        if (door.getDoorType() == null) {
                            door.setDoorType(DoorType.UNKNOWN.getValue());
                        }
                        doorInfo.setDoor(door);
                    }
                }
            });
            authDeviceList.addAll(authDoorList.getData());
        }
        return authDeviceList;
    }

    /* ==============================================[远程门禁调用接口]===================================================*/

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public Boolean remoteOpenDoor(Door entity, String appId) throws Exception {
        Door door = this.getDoorById(entity.getId());

        if (door == null) {
            throw DOOR_NOT_EXIST;
        }

        if (Objects.equals(ManufactureType.KANGTU_DOOR.KEY, door.getBrandNo())) {
            return kangTuRemoteOpenDoor(door);
        }

        if (Objects.equals(ManufactureType.FREEVIEW_DOOR.KEY, door.getBrandNo())) {
            return freeViewRemoteOpenDoor(door, appId);
        }

        return null;
    }

    private Boolean freeViewRemoteOpenDoor(Door door, String appId) throws URISyntaxException {

        Map<String, String> uriVar = new HashMap<>();
        // 房屋编号
        uriVar.put("tenantCode", freeViewTenantCode);
        uriVar.put("devUserName", appId);
        uriVar.put("deviceDirectory", door.getDeviceCode());

        RequestEntity remoteUnlockOpenDoorRequest = RequestEntity
                .get(new URI(expandURL(freeViewUrl + FREEVIEW_REMOTE_OPEN_DOOR, uriVar)))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .build();
        ResponseEntity<JSONObject> remoteUnlockOpenDoorResponse;
        try {
            remoteUnlockOpenDoorResponse = restTemplate.exchange(remoteUnlockOpenDoorRequest, JSONObject.class);
        } catch (HttpClientErrorException e) {
            // 全视通token失效
            if (Objects.equals(e.getStatusCode(), HttpStatus.FORBIDDEN)) {
                throw INVALID_TOKEN;
            }
            log.warn("远程开门失败", e);
            throw REMOTE_OPEN_DOOR_FAILED;
        }
        return Objects.equals(HttpStatus.OK, remoteUnlockOpenDoorResponse.getStatusCode());
    }

    /* ==============================================[远程门禁调用接口]===================================================*/


    // ============================================【device auth start】=================================================

    @Override
    public Long countDoorByCommunityId(ObjectId communityId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        return doorRepository.countByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public Long countFaultedDoorByCommunityId(ObjectId communityId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        return doorRepository.countByCommunityIdAndOnlineStatusAndDataStatus(communityId, 0, DataStatusType.VALID.KEY);
    }

    @Override
    public boolean updateKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO) {
        log.info("新增门禁权限服务");
        return modifyKangTuDoorAuth(deviceAuthVO, DOOR_UPDATE_URL);
    }

    @Override
    public Set<Long> updateMiliDoorAuth(MiliDoorAuthVO miliDoorVO) throws Exception {
        return applyUserToMili(miliDoorVO);
    }

    private void agentRegisterFreeView(FreeViewDoorAuthVO freeViewDoorAuthVO) {

        FreeViewRequest freeViewRequest = new FreeViewRequest();
        // 代理注册用userId作为唯一标识
        freeViewRequest.setDevUserName(freeViewDoorAuthVO.getUserId().toHexString());
        freeViewRequest.setMobile(String.valueOf(Instant.now().toEpochMilli()));

        RequestEntity<Object> requestEntity = RequestEntity
                .post(URI.create(freeViewUrl + FREEVIEW_AGENT_REGISTER))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .body(JSON.toJSON(freeViewRequest));
        try {
            restTemplate.exchange(requestEntity, JSONObject.class);
        } catch (HttpClientErrorException e) {
            // 错误码是405代表已注册 409代表冲突 其余则是注册失败
            if (Objects.equals(e.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED) || Objects.equals(e.getStatusCode(), HttpStatus.CONFLICT)) {
                log.info("用户已在全视通完成代理注册");
                return;
            }
            // 403为非法token
            if (Objects.equals(e.getStatusCode(), HttpStatus.FORBIDDEN)) {
                throw INVALID_TOKEN;
            }
            log.warn("全视通代理注册失败");
            throw FREEVIEW_REGISTER_FAILED;
        }
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void updateFreeViewDoorAuth(FreeViewDoorAuthVO freeViewDoorAuthVO) throws Exception {

        // 用户代理注册
        agentRegisterFreeView(freeViewDoorAuthVO);

        for (String outRoomCode : freeViewDoorAuthVO.getOutRoomCodes()) {

            FreeViewRequest freeViewRequest = new FreeViewRequest();
            freeViewRequest.setUserName(freeViewDoorAuthVO.getUserId().toHexString());
            freeViewRequest.setTenantCode(freeViewTenantCode);

            Map<String, String> uriVar = new HashMap<>();
            // 房屋编号
            uriVar.put("structureDirectory", outRoomCode);

            log.info("全视通申请房屋");
            RequestEntity<Object> requestEntity = RequestEntity
                    .put(new URI(expandURL(freeViewUrl + FREEVIEW_CHANGE_URL, uriVar)))
                    .header("Authorization", access_token)
                    .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                    .body(JSON.toJSON(freeViewRequest));
            callFreeViewURL(requestEntity);
        }

        FreeViewCardRequest freeViewCardRequest = new FreeViewCardRequest();
        freeViewCardRequest.buildCardRequestForApplyAuth(freeViewDoorAuthVO.getKeyNo(), 6, 3);
        FreeViewRequest toApplyPhoneCard = new FreeViewRequest(freeViewDoorAuthVO.getUserId().toHexString(),
                                                               freeViewTenantCode,
                                                               freeViewCardRequest);

        log.info("全视通申请实体卡");
        RequestEntity<Object> requestEntity = RequestEntity
                .post(new URI(freeViewUrl + FREEVIEW_CARD_AUTH))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .body(JSON.toJSON(toApplyPhoneCard));
        callFreeViewURL(requestEntity);
    }

    private void callFreeViewURL(RequestEntity requestEntity) {
        try {
            restTemplate.exchange(requestEntity, Object.class);
        } catch (HttpClientErrorException e) {
            // 全视通token失效
            if (Objects.equals(e.getStatusCode(), HttpStatus.FORBIDDEN)) {
                throw INVALID_TOKEN;
            }
        }
    }

    @Override
    public void deleteMiliDoorAuth(String miliUId) throws Exception {
        deleteMiliUser(miliUId);
    }

    @Override
    public boolean deleteKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO) {
        log.info("删除门禁权限服务");
        return modifyKangTuDoorAuth(deviceAuthVO, DOOR_DELETE_URL);
    }


    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void deleteFreeViewDoorAuth(FreeViewDoorAuthVO freeViewDoorAuthVO) throws Exception {

        for (String outRoomCode : freeViewDoorAuthVO.getOutRoomCodes()) {
            deleteFreeViewAuth(freeViewDoorAuthVO, outRoomCode);
        }
    }

    @Override
    public boolean coverKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO) {
        log.info("覆盖门禁权限服务");
        return modifyKangTuDoorAuth(deviceAuthVO, DOOR_COVER_URL);
    }

    @Override
    public Set<Long> coverMiliDoorAuth(MiliDoorAuthVO miliDoorAuthVO) throws Exception {
        // 先清除已有权限
        if (miliDoorAuthVO.getOutUIds() != null) {
            for (Long id : miliDoorAuthVO.getOutUIds()) {
                try {
                    this.deleteMiliDoorAuth(String.valueOf(id));
                } catch (Exception e) {
                    // 删除不了也要继续往下授权 不需要做任何处理
                }
            }
        }

        return updateMiliDoorAuth(miliDoorAuthVO);
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void coverFreeViewDoorAuth(FreeViewDoorAuthVO freeViewDoorAuthVO) throws Exception {

        for (String outRoomCode : freeViewDoorAuthVO.getDelOutRoomCodes()) {
            try {
                deleteFreeViewAuth(freeViewDoorAuthVO, outRoomCode);
            } catch (Exception e) {
                // 删除失败不做处理
                log.warn("删除全视通旧账号异常", e);
            }
        }

        this.updateFreeViewDoorAuth(freeViewDoorAuthVO);
    }

    private void deleteFreeViewAuth(FreeViewDoorAuthVO freeViewDoorAuthVO, String outRoomCode) {
        Map<String, String> uriVar = new HashMap<>();
        // 房屋编号
        uriVar.put("structureDirectory", outRoomCode);
        uriVar.put("tenantCode", freeViewTenantCode);
        uriVar.put("userName", freeViewDoorAuthVO.getUserId().toHexString());

        RequestEntity requestEntity = RequestEntity
                .delete(URI.create(expandURL(freeViewUrl + FREEVIEW_CHANGE_URL, uriVar)))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .build();
        callFreeViewURL(requestEntity);
    }

    // ============================================【device auth start】=================================================

    // ===============================================【kangtu start】==================================================

    private boolean modifyKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO, String url) {
        // 申请到电梯物联
        CardRequest cardRequest = new CardRequest();
        cardRequest.setKeyType(deviceAuthVO.getKeyType());
        cardRequest.setKeyNo(fixCardLength(deviceAuthVO.getKeyNo(), 12));
        cardRequest.setKeyId(deviceAuthVO.getKeyId());
        cardRequest.setUsesTime(deviceAuthVO.getUsesTime());
        cardRequest.setProcessTime(deviceAuthVO.getProcessTime());
        // 请求门禁权限修改
        cardRequest.setHouses(Collections.singleton(new CommunityDoorVO(deviceAuthVO.getCommunityId().toHexString(), deviceAuthVO.getDoorDevices())));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);

        JSONObject jsonObject = restTemplate.postForObject(this.url + url, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
        return jsonObject.getBoolean("success");
    }

    @Override
    public JSONObject viewCardPermissionDetail(Card card) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(card), httpHeaders);

        JSONObject response = restTemplate.postForObject(this.url + CARD_VIEW, entity, JSONObject.class);
        if (!response.getBoolean("success")) {
            throw IOT_FAILED;
        }
        return response;
    }

    @Override
    public JSONObject getCardPermissionDetail(Card card) {
        JSONObject response = viewCardPermissionDetail(card);
        JSONObject data = response.getJSONObject("data");

        // 没有门禁设备直接返回
        if (data == null || data.size() == 0 || data.getJSONArray("door") == null || data.getJSONArray("door").size() == 0) {
            return response;
        }

        // door里面有communityId 一张卡只对应一个社区
        Set<ObjectId> id = new HashSet<>();
        for (JSONObject door : data.getJSONArray("door").toJavaList(JSONObject.class)) {
            List<DoorInfo> list = door.getJSONArray("doors").toJavaList(DoorInfo.class);

            id.addAll(list.stream()
                    .filter(d -> d.getId() != null)
                    .map(DoorInfo::getId)
                    .collect(Collectors.toSet()));

            List<Door> doors = this.getDoorsInIds(id);

            if (doors == null || doors.size() == 0) {
                return response;
            }

            Map<ObjectId, Door> doorMap = new HashMap<>();

            doors.forEach(d -> doorMap.put(d.getId(), d));
            list.forEach(doorInfo -> doorInfo.setDoor(doorMap.get(doorInfo.getId())));
            list.removeIf(DoorInfo::isDoorExist);
            door.getJSONArray("doors").fluentClear().addAll(list);
        }
        return response;
    }
    // ===============================================【kangtu end】==================================================

    // ================================================【mili start】===================================================

    /**
     * 将审核通过的用户注册到米立, 并且绑定到米立的设备
     * @param miliDoorVO
     */
    private Set<Long> applyUserToMili(MiliDoorAuthVO miliDoorVO) throws Exception {
        Set<Long> ids = new HashSet<>();
        for (String roomRId : miliDoorVO.getMiliRId()) {
            // 使用用户注册时的手机号 身份证先随便写("123456789") 性别默认"1"（男） 米立不验证
            Proprietor proprietor = new Proprietor(
                    miliDoorVO.getName(), miliDoorVO.getPhone(), "123465789", miliDoorVO.getMiliCId(),
                    Long.valueOf(roomRId), String.valueOf(miliDoorVO.getSex() == null ? 1 : miliDoorVO.getSex()));
            if (RelationshipType.OWNER.KEY.equals(miliDoorVO.getRelationship())) {
                proprietor.setRole("1");
            } else {
                proprietor.setRole("2");
            }

            Proprietor response = this.proprietorAdd(proprietor);
            if (response == null) {
                log.info("response is null return null.");
                return null;
            }
            ids.add(response.getProprietor_id());
            if (Objects.equals("1", proprietor.getRole())) {
                proprietor.setProprietor_id(response.getProprietor_id());
                bindProprietorToDevice(miliDoorVO, proprietor);
            }
        }
        return ids;
    }

    /**
     * 将用户绑定到米立
     * @param entity
     * @return
     * @throws Exception
     */
    private Proprietor proprietorAdd(Proprietor entity) throws Exception {
        log.info("register user to mili");
        Map apiResult = miligcConnection.APIPost("wuye/proprietor/add", entity);
        if (apiResult == null) {
            log.info("米立返回异常");
            return null;
        }
        Integer errorCode = Integer.parseInt(apiResult.get("errorCode").toString());
        // 当住户已存在(8001)或者房号不存在(3001)时不需要进行重试
        if (8001 == errorCode || 3001 == errorCode) {
            log.info("住户已存在(8001)或者房号不存在(3001)时不需要进行重试, errorCode:{}", errorCode);
            return null;
        }
        if (errorCode != 1) {
            log.info("返回参数:" + apiResult);
            String msg = apiResult.get("errorMsg").toString();
            throw new BizException(errorCode, msg);
        }
        JSONObject body = (JSONObject) apiResult.get("body");
        return body.toJavaObject(Proprietor.class);
    }

    /**
     * 为米立的用户绑定门禁设备
     * @param miliDoorVO
     * @param proprietor
     */
    private void bindProprietorToDevice(MiliDoorAuthVO miliDoorVO, Proprietor proprietor) throws Exception {

        for (Door door : miliDoorVO.getDoorList()) {
            log.info("米立绑定用户设备");
            miligcConnection.APIPost(
                    "wuye/proprietor/" + proprietor.getProprietor_id() + "/device/add?service_id=" + door.getServiceId()
                            .toArray()[0] + "&&device_id=" + door.getDeviceId() + "&&service_status=" + 1, null);
        }
    }

    /**
     * 删除米立用户
     * @param proprietorId
     * @return
     * @throws Exception
     */
    private Map deleteMiliUser(String proprietorId) throws Exception {

        log.info("remove device that user have been bind...");
        log.info("请求参数 : " + proprietorId);
        Map map = miligcConnection.APIGet("wuye/proprietor/" + proprietorId + "/delete");
        if (map != null && map.get("errorCode") != null && (int) map.get("errorCode") != 1) {
            log.info("返回参数:" + map);
            if ((Integer) map.get("errorCode") != 8002) {
                throw new BizException("米立删除用户失败");
            }
        }
        return map;
    }
    // ==================================================【mili end】===================================================

    // ================================================【freeview start】===============================================

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void openDeviceReadCard(Door door, String phoneLast4Num) {
        FreeViewRequest freeViewRequest = new FreeViewRequest();
        freeViewRequest.setTenantCode(freeViewTenantCode);
        freeViewRequest.setMobile(phoneLast4Num);
        freeViewRequest.setDeviceDirectory(door.getDeviceCode());
        freeViewRequest.setRequestID("test");

        RequestEntity<Object> requestEntity = RequestEntity
                .post(URI.create(freeViewUrl + FREEVIEW_NOTIFY_CARD_READ))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .body(JSON.toJSON(freeViewRequest));
        try {
            restTemplate.exchange(requestEntity, JSONObject.class);
        } catch (HttpClientErrorException e) {
            // 全视通token失效
            if (Objects.equals(e.getStatusCode(), HttpStatus.FORBIDDEN)) {
                throw INVALID_TOKEN;
            }
        }
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void applyFreeViewUserCard(CardVO cardVO, List<Door> doors, Integer client) {
        doors = doors.stream().filter(d -> d.getBrandNo() == ManufactureType.FREEVIEW_DOOR.KEY).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(doors)) {
            return;
        }

        doors.removeIf(door -> !Objects.equals(ManufactureType.FREEVIEW_DOOR.KEY, door.getBrandNo()));

        if (null == cardVO.getUserId()) {
            // 非掌居宝用户执行流程
            doApplyWithoutUserId(cardVO, doors);
        } else {
            // 掌居宝用户下放设备权限执行流程
            doApplyWithUserId(cardVO, doors);
        }
    }

    private void doApplyWithoutUserId(CardVO cardVO, List<Door> doors) {
        for (Door door : doors) {
            Date start = new Date();

            FreeViewRequest request = new FreeViewRequest();
            request.setTenantCode(freeViewTenantCode);
            request.setCardSerialNumber(cardVO.getKeyNo());
            request.setDeviceDirectory(door.getDeviceCode());
            request.setValidStartTime(start);
            request.setValidEndTime(DateUtils.addSecond(start, cardVO.getProcessTime()));

            request.setCardTypeID(3);
            request.setCardMediaTypeID(cardVO.getKeyType() == CertificateType.IC_CARD.KEY ? 1 : 6);

            RequestEntity<Object> entity = RequestEntity
                    .post(URI.create(freeViewUrl + FREEVIEW_DEVICE_AUTH))
                    .header("Authorization", access_token)
                    .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                    .body(JSON.toJSON(request));
            ResponseEntity<JSONObject> response = restTemplate.exchange(entity, JSONObject.class);
            log.info("返回参数" + response);
        }
    }

    private void doApplyWithUserId(CardVO cardVO, List<Door> doors) {
        FreeViewCardRequest freeViewCardRequest = new FreeViewCardRequest();
        freeViewCardRequest.buildCardRequestForApplyAuth(cardVO.getKeyNo(),
                                                         cardVO.getKeyType() == CertificateType.IC_CARD.KEY
                                                         ? 1 : 6,
                                                         3);
        FreeViewRequest freeViewRequest = new FreeViewRequest(cardVO.getUserId().toHexString(),
                                                              freeViewTenantCode,
                                                              freeViewCardRequest);

        log.info("全视通申请卡片");
        RequestEntity<Object> requestEntity = RequestEntity
                .post(URI.create(freeViewUrl + FREEVIEW_APPLY_CARD))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .body(JSON.toJSON(freeViewRequest));
        ResponseEntity<JSONObject> result = restTemplate.exchange(requestEntity, JSONObject.class);
        log.info("全视通申请卡片返回参数 : " + result);
        for (Door door : doors) {
            Date start = new Date();
            FreeViewRequest request = new FreeViewRequest();
            request.setTenantCode(freeViewTenantCode);
            request.setCardSerialNumber(cardVO.getKeyNo());
            request.setDeviceDirectory(door.getDeviceCode());
            request.setValidStartTime(start);
            request.setValidEndTime(DateUtils.addSecond(start, cardVO.getProcessTime()));

            RequestEntity<Object> entity = RequestEntity
                    .put(URI.create(freeViewUrl + FREEVIEW_CARD_AUTH))
                    .header("Authorization", access_token)
                    .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                    .body(JSON.toJSON(request));
            ResponseEntity<JSONObject> response = restTemplate.exchange(entity, JSONObject.class);
            log.info("返回参数" + response);
        }
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void deleteFreeViewUserCard(String keyNo) {
        Map<String, Object> uriVar = new HashMap<>();
        uriVar.put("tenantCode", freeViewTenantCode);
        uriVar.put("cardSerialNumber", keyNo);

        RequestEntity<Void> requestEntity = RequestEntity.delete(URI.create(expandURL(freeViewUrl + FREEVIEW_APPLY_CARD, uriVar)))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();
        callFreeViewURL(requestEntity);
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public List<DoorInfo> getFreeViewAuthDoor(Set<ObjectId> buildingId, ObjectId userId, ObjectId communityId) {
        Map<String, Object> uriVar = new HashMap<>();
        uriVar.put("tenantCode", freeViewTenantCode);
        uriVar.put("devUserName", userId.toHexString());

        List<Door> doorList = getBuildingAndCommunityDoorByBrandNo(buildingId, communityId, ManufactureType.FREEVIEW_DOOR.KEY);

        RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(expandURL(freeViewUrl + FREEVIEW_REMOTE_GET_DOOR, uriVar)))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();
        ResponseEntity<JSONArray> response = restTemplate.exchange(requestEntity, JSONArray.class);
        log.info("全视通返回参数 : " + response.toString());
        List<DoorInfo> doorInfos = new ArrayList<>(doorList.size());

        doorList.forEach(result -> {
            DoorInfo doorInfo = new DoorInfo();
            doorInfos.add(doorInfo);
            doorInfo.setDoor(result);
            for (Object responseDoor : response.getBody()) {
                JSONObject door = (JSONObject) responseDoor;
                if (door.getString("LocalDirectory") == null || !door.getString("LocalDirectory").equals(result.getDeviceCode())) {
                    continue;
                }
                doorInfo.setHasBluetooth(door.getBoolean("HasBluetooth"));
                String macAddress = doorInfo.getHasBluetooth() ? door.getString("BluetoothModuleAddress") : "";
                if (doorInfo.getHasBluetooth() && !StringUtil.isEmpty(macAddress)) {
                    Pattern compile = Pattern.compile("([A-Fa-f0-9]{2})");
                    Matcher m = compile.matcher(macAddress);
                    String output = m.replaceAll("$1:");
                    macAddress = output.substring(0, output.length() - 1);
                    doorInfo.setMac(macAddress);
                }
                doorInfo.setAppDigest(door.getString("AppDigest"));
                doorInfo.setDevDigest(door.getString("DevDigest"));
                doorInfo.setOnlineStatus(door.getBoolean("IsOnline") ? DoorOnlineStatusType.ONLINE.key : DoorOnlineStatusType.OFFLINE.key);
                doorInfo.setDeviceCode(door.getString("LocalDirectory"));
            }
        });

        return doorInfos;
    }

    @Override
    public List<DoorInfo> listDoorInfoByDoorRequest(DoorRequest doorRequest) {
        List<DoorInfo> doorInfos = new ArrayList<>();
        CompletableFuture[] completableFutures = doorStrategies.stream()
                .map(doorStrategy -> CompletableFuture
                        .supplyAsync(() -> doorStrategy.listDoorInfo(doorRequest))
                        .whenComplete((s, e) -> {
                            if (e != null) {
                                log.warn("查询门禁列表异常", e);
                                return;
                            }
                            doorInfos.addAll(s);
                        }))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();

        // 排序-按door名称升序(这里要求door.name不能为null，否则改成常规写法)
        doorInfos.sort(Comparator.comparing(DoorInfo::getName));

        return doorInfos;
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void applyFreeViewSecretCode(CardVO cardVO, List<Door> freeViewDoors) {
        FreeViewRequest freeViewRequest = new FreeViewRequest();
        freeViewRequest.setDeviceLocalDirectoryArray(freeViewDoors.stream().map(Door::getDeviceCode).collect(Collectors.toList()));
        freeViewRequest.setTenantCode(freeViewTenantCode);
        String password = "4B540D" + cardVO.getKeyNo() + FreeViewUtil.getCheckSUMByte("0D", cardVO.getKeyNo()).trim().toUpperCase() + "FE";
        freeViewRequest.setPassword(password);
        Date start = new Date();
        freeViewRequest.setValidStartTime(start);
        freeViewRequest.setValidEndTime(DateUtils.addSecond(start, cardVO.getProcessTime()));
        freeViewRequest.setRequestID(cardVO.getKeyId());
        freeViewRequest.setMaxAvailableTimes(secretCodeTimes);

        RequestEntity<Object> requestEntity = RequestEntity
                .post(URI.create(freeViewUrl + FREEVIEW_APPLY_SECRET))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .body(JSON.toJSON(freeViewRequest));
        callFreeViewURL(requestEntity);
    }

    @Override
    public Door updateFreeViewDeviceState(DeviceParam deviceParam, DeviceParams deviceParams) {
        Door door = new Door();
        door.setCommunityId(deviceParams == null ? null : deviceParams.getCommunityId());
        door.setBuildingId(deviceParams == null ? null : deviceParams.getBuildingId());
        Set<Integer> serviceIds = new HashSet<>();
        serviceIds.add(DoorService.BLUETOOTH.KEY);
        serviceIds.add(DoorService.QR_CODE.KEY);
        serviceIds.add(DoorService.REMOTE.KEY);
        door.setServiceId(serviceIds);

        door.setDeviceId(deviceParam.getDeviceID().longValue());
        door.setDeviceCode(deviceParam.getDeviceLocalDirectory());
        door.setName(deviceParam.getDeviceName());
        door.setRank(0);
        door.setDoorType(DoorOnlineStatusType.UNKNOWN.key);
        door.setBrand(ManufactureType.FREEVIEW_DOOR.VALUE);
        door.setBrandNo(ManufactureType.FREEVIEW_DOOR.KEY);
        door.setCreateAt(new Date());
        door.setUpdateAt(door.getCreateAt());

        // 状态转化
        byte stateValue = deviceParam.getStateValue();
        byte stateType = deviceParam.getStateType();
        switch (stateType){
            case 1 : // 初装状态，涉及位运算
                Integer onlineState = BitwiseAndCheck(stateValue, FreeViewDeviceStatus.ONLINE_CHECK)
                        ? DoorOnlineStatusType.ONLINE.key
                        : DoorOnlineStatusType.OFFLINE.key;
                door.setOnlineStatus(onlineState);

                Integer alarmState = BitwiseAndCheck(stateValue, FreeViewDeviceStatus.ALARM_CHECK)
                        ? FreeViewDeviceStatus.ALARM.key
                        : FreeViewDeviceStatus.NORMAL.key;
                door.setAlarmStatus(alarmState);

                Integer enabledState = BitwiseAndCheck(stateValue, FreeViewDeviceStatus.ENABLED_CHECK)
                        ? FreeViewDeviceStatus.ENABLED.key
                        : FreeViewDeviceStatus.DISABLED.key;
                door.setDoorStatus(enabledState);
                break;
            case 2 : // 可用状态
                door.setDoorStatus(Integer.valueOf(stateValue));
                break;
            case 3 : // 在线状态
                door.setOnlineStatus(stateValue == FreeViewDeviceStatus.ONLINE.key
                        ? DoorOnlineStatusType.ONLINE.key
                        : DoorOnlineStatusType.OFFLINE.key);
                break;
            case 4 : // 报警状态
                door.setAlarmStatus(Integer.valueOf(stateValue));
                break;
            default:
                break;
        }

        if (DoorOnlineStatusType.OFFLINE.key.equals(door.getOnlineStatus())
                && FreeViewDeviceStatus.DISABLED.key.equals(door.getDoorStatus())) {
            door.setDataStatus(DataStatusType.INVALID.KEY);
        } else {
            door.setDataStatus(DataStatusType.VALID.KEY);
        }

        // 门禁类型：根据directory判断
        String[] directories = deviceParam.getDeviceLocalDirectory().split("-",-1);
        door.setDoorType(directories.length > 2 ? DoorType.BUILDING_DOOR.getValue() : DoorType.COMMUNITY_DOOR.getValue());

        // 门禁名称：本地化名称,仅初装状态
        if (stateType == 1) {
            if (directories.length == 3 && deviceParams != null) {
                if ("1".equals(directories[2])) {
                    door.setName(String.format("%s大门", deviceParams.getName()));
                } else {
                    door.setName(String.format("%s大门%s", deviceParams.getName(), directories[2]));
                }
            } else if (directories.length == 2) {
                if ("1".equals(directories[1])) {
                    door.setName("社区大门");
                } else {
                    door.setName(String.format("社区大门%s", directories[1]));
                }
            }
        }else {
            door.setName(null);
        }

        return doorRepository.findAndModify(door);
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public String addUserFeatureInDoor(UserFeature userFeature) {
        userFeature.setTenantCode(freeViewTenantCode);
        userFeature.setDevUserName(userFeature.getUserId().toString());
        UserFeature.HumanFeature humanFeature = new UserFeature.HumanFeature();
        humanFeature.setFeatureType(HumanFeatureStatusEnum.SUCCESS.KEY);
        humanFeature.setValidStartTime(new Date());
        humanFeature.setValidEndTime(DateUtils.addYear(new Date(), 50));
        userFeature.setHumanFeature(humanFeature);

        RequestEntity<Object> requestEntity = RequestEntity
                .post(URI.create(freeViewUrl + FREEVIEW_USER_FEATURE))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .body(JSON.toJSON(userFeature));

        try {
            ResponseEntity<UserFeature> responseEntity = restTemplate.exchange(requestEntity, UserFeature.class);
            UserFeature userFeatureResponse = responseEntity.getBody();
            return userFeatureResponse.getFeatureCode();
        } catch (HttpClientErrorException e) {
            // 全视通token失效
            if (Objects.equals(e.getStatusCode(), HttpStatus.FORBIDDEN)) {
                throw INVALID_TOKEN;
            }
            log.info("申请人脸识别异常", e);
            throw USER_FEATURE_FAILED;
        }

    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public void deleteUserFeatureInDoor(String featureCode) {
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("tenantCode", freeViewTenantCode);
        urlMap.put("featureCode", featureCode);

        RequestEntity requestEntity = RequestEntity
                .delete(URI.create(expandURL(freeViewUrl + FREEVIEW_USER_FEATURE, urlMap)))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .build();
        try {
            restTemplate.exchange(requestEntity, JSONObject.class);
        } catch (HttpClientErrorException e) {
            if (Objects.equals(e.getStatusCode(), HttpStatus.NO_CONTENT)) {
                log.info("人脸信息删除成功");
                return;
            }
            // 全视通token失效
            if (Objects.equals(e.getStatusCode(), HttpStatus.FORBIDDEN)) {
                throw INVALID_TOKEN;
            }
            log.warn("全视通人脸信息删除异常");
            throw FEATURE_DELETE_FAILED;
        }
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public JSONArray getUserFeatureInDoor(ObjectId userId) {
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("tenantCode", freeViewTenantCode);
        urlMap.put("devUserName", userId.toHexString());

        RequestEntity requestEntity = RequestEntity
                .get(URI.create(expandURL(freeViewUrl + FREEVIEW_FEATURE_PIC, urlMap)))
                .header("Authorization", access_token)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .build();
        ResponseEntity<JSONObject> featurePictureResponse = restTemplate.exchange(requestEntity, JSONObject.class);
        if (featurePictureResponse == null) {
            throw FEATURE_NULL;
        }
        JSONObject featurePicture = featurePictureResponse.getBody();
        if (featurePicture == null){
            return new JSONArray ();
        }

        return featurePicture.getJSONArray("Materials");
    }

    private static String expandURL(String url, Map<?, ?> params) {
        if (params == null) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");

        for (Map.Entry<?, ?> param : params.entrySet()) {
            sb.append(param.getKey()).append("=").append(param.getValue()).append("&");
        }
        url = sb.toString();

        if (url.endsWith("&")) {
            url = StringUtils.substringBeforeLast(url, "&");
        }

        return url;
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public String getFreeViewByUrlPOST(String url, CommunityParams params) throws UnsupportedEncodingException {
        params.setTenantCode(freeViewTenantCode);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.set("Accept", MediaType.APPLICATION_JSON.toString());
        headers.set("Authorization", access_token);

        Object requestJson = JSON.toJSON(params);
        HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers);
        String result = restTemplate.postForObject(URI.create(freeViewUrl + url), entity, String.class);
        return new String(result.getBytes("ISO-8859-1"), "UTF-8");
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public String getFreeViewByUrlGET(String url, Map params) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.set("Accept", MediaType.APPLICATION_JSON.toString());
        headers.set("Authorization", access_token);

        HttpEntity<Map> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(expandURL(freeViewUrl + url, params), HttpMethod.GET, entity, String.class);
        String result_ = new String(response.getBody().getBytes("ISO-8859-1"), "UTF-8");
        return result_;
    }

    @Override
    @FreeViewToken(token = FreeViewToken.Token.Get)
    public String getFreeViewByUrlPUT(String url, Object params) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.set("Accept", MediaType.APPLICATION_JSON.toString());
        headers.set("Authorization", access_token);

        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(params), headers);
        ResponseEntity<String> response = restTemplate.exchange(freeViewUrl + url, HttpMethod.PUT, entity, String.class);
        return new String(response.getBody().getBytes("ISO-8859-1"), "UTF-8");
    }


    /**
     * 按位与运算校验
     * @param value 状态值
     * @param check 校验值
     * @return
     */
    private boolean BitwiseAndCheck(byte value, byte check){
        return  (value & check) == check;
    }

    /**
     *
     * @param keyNo
     * @param length
     * @return
     */
    private String fixCardLength(String keyNo, int length) {
        //将不够指定位数位的字符串补齐指定位数位
        if (keyNo.length() < length) {
            keyNo = UUIDUitl.toFixdLengthString(keyNo, length);
        }
        //将长度大于指定位数位的字符串截取前面指定位数位
        else if (keyNo.length() > length) {
            keyNo = keyNo.substring(0, length);
        }
        keyNo = keyNo.toUpperCase();
        return keyNo;
    }
}
