package cn.bit.user.service;

import cn.bit.facade.data.user.TenantApplication;
import cn.bit.facade.enums.*;
import cn.bit.facade.exception.user.UserBizException;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.user.CardFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.vo.communityIoT.door.CommunityDoorVO;
import cn.bit.facade.vo.communityIoT.door.DoorDeviceVO;
import cn.bit.facade.vo.communityIoT.elevator.AuthElevatorRequest;
import cn.bit.facade.vo.communityIoT.elevator.FloorVO;
import cn.bit.facade.vo.communityIoT.elevator.KeyNoListElevatorVO;
import cn.bit.facade.vo.communityIoT.elevator.KeyNoListElevatorVOResponse;
import cn.bit.facade.vo.statistics.Section;
import cn.bit.facade.vo.statistics.TenantApplicationRequest;
import cn.bit.facade.vo.statistics.TenantApplicationResponse;
import cn.bit.facade.vo.user.card.CardQueryRequest;
import cn.bit.facade.vo.user.card.CardRequest;
import cn.bit.facade.vo.user.card.CardVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.UUIDUitl;
import cn.bit.framework.utils.number.AmountUtil;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.user.dao.CardRepository;
import cn.bit.user.dao.HouseholdRepository;
import cn.bit.user.dao.UserToRoomRepository;
import cn.bit.user.support.CardGenerator;
import cn.bit.user.support.RedisService;
import cn.bit.user.utils.UserUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.TIME_UNIT_INVALID;
import static cn.bit.facade.exception.user.CardBizException.*;
import static cn.bit.facade.exception.user.UserBizException.*;

@Service("cardFacade")
@Slf4j
public class CardFacadeImpl implements CardFacade {

    /**
     * 请求路径
     */
    @Getter
    @Setter
    @Value("${card.request.url}")
    private String url;

    @Autowired
    private CardRepository cardRepository;

    @Resource
    private UserToRoomRepository userToRoomRepository;

    @Resource
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EsTemplate esTemplate;

    @Autowired
    private CardGenerator cardGenerator;

    @Value("${dtu.request.url}")
    private String dtuUrl;

    private static final String INDEX_NAME = "cm_tenant_record";

    private static final String TYPE_NAME = "application";

    // 电梯物联相关接口
    private static final String ELEVATOR_APPLY_URL = "/certificate/elevator/add";
    private static final String ELEVATOR_DELETE_URL = "/certificate/elevator/remove";
    private static final String DOOR_UPDATE_URL = "/certificate/door/add";
    private static final String DOOR_DELETE_URL = "/certificate/door/remove";

    private static final String FIND_LIST_KEYNO = "/elevator/findByKeyNo/list";

    private static final String CARD_QUERY = "/certificate/queryCertificate";

    private static final String CARD_AUTH_DELETE = "/certificate/deleteAll";

    private static final String QR_CODE_REDIS_KEY = "QR_CODE_CARD_KEY";
    /**
     * 查询DTU
     */
    private static final String DTU_QUERY = "/platformRestServices/QueryCertificate";
    /**
     * 插入信息到DTU
     */
    private static final String DTU_ADD = "/platformRestServices/AddCertificate";
    /**
     * 删除DTU信息
     */
    private static final String DTU_REMOVE = "/platformRestServices/DeleteCertificate";

    @Override
    public Card getUserCardInCommunity(Card card) throws BizException {
        return cardRepository.findByUserIdAndCommunityIdAndKeyTypeAndDataStatus(
                card.getUserId(), card.getCommunityId(), card.getKeyType(), DataStatusType.VALID.KEY);
    }

    @Override
    public Card findCardByKeyNoAndKeyId(String keyNo, String keyId) throws BizException {
        return cardRepository.findByKeyNoAndKeyIdAndDataStatus(keyNo, keyId, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Card> findUserCardInCommunityByKeyType(Card entity, Integer page, Integer size) throws BizException {
        if (page == null) {
            page = 1;
        }
        if (size == null) {
            size = 10;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Card> cardPage =
                cardRepository.findByCommunityIdAndUserIdAndKeyIdAndKeyNoAndKeyTypeAndDataStatusAllIgnoreNull(
                        entity.getCommunityId(), entity.getUserId(), entity.getKeyId(), entity.getKeyNo(),
                        entity.getKeyType(), DataStatusType.VALID.KEY, pageable);
        Date now = new Date();
        cardPage.getContent().forEach(c -> {
            if (DateUtils.compareDate(c.getProcessTime(), now, Calendar.SECOND) < 0) {
                c.setValidState(CardStatusType.INVALID.KEY);
            }
        });
        return PageUtils.getPage(cardPage);
    }

    @Override
    public boolean removeAuthAndDeleteCard(Card card) {
        if (card == null || StringUtil.isEmpty(card.getKeyNo())) {
            throw CARD_NOT_EXIST;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());

        HttpEntity<Object> request = new HttpEntity<>(JSON.toJSON(card), httpHeaders);
        restTemplate.postForObject(url + CARD_AUTH_DELETE, request, JSONObject.class);
        card.setDataStatus(DataStatusType.INVALID.KEY);
        card.setUpdateAt(new Date());
        return cardRepository.updateById(card, card.getId()) > 0;
    }

    // 兼容旧版APP接口
    @Override
    public Card applyCardAndElevatorPermission(CardVO cardVO) {
        Card card = this.findCardByKeyNoAndKeyType(cardVO.getKeyNo(), cardVO.getKeyType());
        // 该一卡通已存在
        if (card != null) {
            throw CARD_EXIST;
        }

        // 申请到电梯物联
        CardRequest cardRequest = new CardRequest();
        cardRequest.setKeyType(cardVO.getKeyType());

        // 蓝牙卡 IC卡可以添加卡号来新增
        if ((cardVO.getKeyType() == CertificateType.BLUETOOTH_CARD.KEY
                || cardVO.getKeyType() == CertificateType.IC_CARD.KEY)
                && StringUtil.isNotNull(cardVO.getKeyNo())) {
            cardRequest.setKeyNo(fixCardLength(cardVO.getKeyNo(), 12));
        }

        cardRequest.setBuilds(cardVO.getBuilds());

        // 防止注册时间超过50年, 超过50年按照50年计算
        Date startDate = new Date();
        int processTime = (int) DateUtils.secondsBetween(startDate, DateUtils.addYear(startDate, 50));
        if (cardVO.getProcessTime() >= processTime) {
            cardVO.setProcessTime(processTime);
        }
        cardRequest.setProcessTime(cardVO.getProcessTime());
        cardRequest.setUsesTime(cardVO.getUsesTime());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);

        JSONObject result = restTemplate.postForObject(url + ELEVATOR_APPLY_URL, entity, JSONObject.class);

        if (!result.getBoolean("success")) {
            log.warn(result.getString("errorMsg"));
            throw new UserBizException(result.getInteger("errorCode"), result.getString("errorMsg"));
        }

        JSONObject data = result.getJSONObject("data");
        String keyId = data.getString("keyId");
        String keyNo = data.getString("keyNo");

        card = saveCardBy(cardVO, keyId, keyNo);
        // 二维码凭证是临时访客申请
        if (card.getKeyType() == CertificateType.QR_CODE.KEY) {
            addTenantApplication(card);
        }
        return card;
    }

    private void addTenantApplication(Card card) {
        TenantApplication tenantApplication = new TenantApplication();
        tenantApplication.setCommunityId(card.getCommunityId());
        tenantApplication.setCreateAt(card.getCreateAt());
        esTemplate.insertAsync(INDEX_NAME, TYPE_NAME, card.getId().toString(), tenantApplication);
    }

    @Override
    public boolean updateCardElevatorPermission(CardVO cardVO) {
        applyKangtuDeviceAuthAsync(cardVO, ELEVATOR_APPLY_URL);
        return true;
    }

    @Override
    public boolean deleteCardElevatorPermission(CardVO cardVO) {
        cardVO.setProcessTime(0);
        cardVO.setUsesTime(0);
        return modifyPermission(cardVO, ELEVATOR_DELETE_URL, false);
    }

    /**
     * 修改卡权限
     * @param cardVO
     * @param url
     * @param isKeep
     * @return
     */
    private boolean modifyPermission(CardVO cardVO, String url, Boolean isKeep) {
        if (cardVO.getKeyNo() == null && cardVO.getKeyType() == null && cardVO.getKeyId() == null) {
            throw CARD_INFO_LACK;
        }

        Card card = this.findCardByKeyNoAndKeyType(cardVO.getKeyNo(), cardVO.getKeyType());
        // 该一卡通不存在
        if (card == null) {
            throw CARD_NOT_EXIST;
        }

        // 申请到电梯物联
        CardRequest cardRequest = new CardRequest();
        cardRequest.setKeyType(cardVO.getKeyType());
        cardRequest.setKeyNo(fixCardLength(cardVO.getKeyNo(), 12));
        cardRequest.setKeyId(cardVO.getKeyId());
        cardRequest.setUsesTime(cardVO.getUsesTime());
        cardRequest.setProcessTime(cardVO.getProcessTime());
        cardRequest.setIsKeep(isKeep);
        // 请求电梯权限修改
        cardRequest.setBuilds(cardVO.getBuilds());
        // 请求门禁权限修改
        cardRequest.setHouses(cardVO.getHouses());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);

        JSONObject jsonObject = restTemplate.postForObject(this.url + url, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new UserBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }

        return jsonObject.getBoolean("success");
    }

    @Override
    public Card findCardByKeyNoAndKeyType(String keyNo, Integer keyType) {
        return cardRepository.findByKeyNoAndKeyTypeAndDataStatus(keyNo, keyType, DataStatusType.VALID.KEY);
    }

    @Override
    public KeyNoListElevatorVOResponse getAuthElevatorDevice(Card card) {
        AuthElevatorRequest authElevatorRequest = new AuthElevatorRequest();
        authElevatorRequest.setKeyType(card.getKeyType());
        authElevatorRequest.setKeyId(card.getKeyId());
        return this.getAuthElevatorDeviceByMacAddress(authElevatorRequest);
    }

    private KeyNoListElevatorVOResponse getAuthElevatorDeviceByMacAddress(AuthElevatorRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), httpHeaders);

        ResponseEntity<KeyNoListElevatorVOResponse> keyNoListElevatorVOResponse = restTemplate
                .postForEntity(this.url + FIND_LIST_KEYNO, entity, KeyNoListElevatorVOResponse.class);

        if (keyNoListElevatorVOResponse.getBody() == null) {
            log.info("restTemplate.postForEntity() 返回数据格式错误 getBody() == null !!!");
            return null;
        }
        return keyNoListElevatorVOResponse.getBody();
    }

    @Override
    public CardVO buildCardVOForElevator(CardVO cardVO, Room room) {
        // 遍历
        FloorVO floorInfo = this.buildContains(cardVO.getBuilds(), room.getBuildingId().toString());

        // 没有相同的楼栋
        if (null == floorInfo) {
            FloorVO floorVO = new FloorVO();
            floorVO.setBuildId(room.getBuildingId().toString());
            cardVO.getBuilds().add(floorVO);
            floorVO.setFloors(new HashSet<>());
            floorVO.setSubFloors(new HashSet<>());
            floorInfo = floorVO;
        }

        if (room.getMainDoor() == null && room.getSubDoor() == null) {
            floorInfo.getFloors().add(room.getFloorCode());
            floorInfo.getSubFloors().add(room.getFloorCode());
        }

        // 主门
        if (room.getMainDoor() != null && room.getMainDoor()) {
            floorInfo.getFloors().add(room.getFloorCode());
        }

        // 副门
        if (room.getSubDoor() != null && room.getSubDoor()) {
            floorInfo.getSubFloors().add(room.getFloorCode());
        }

        return cardVO;
    }

    /**
     * 根据楼栋授权 FloorVO中只需要buildId 不需要floors具体楼层
     * @param cardVO
     * @param buildingIds
     * @return
     */
    private CardVO buildCardVOForElevatorFrom(CardVO cardVO, Set<ObjectId> buildingIds) {
        for (ObjectId id : buildingIds) {
            FloorVO floorVO = new FloorVO();
            floorVO.setBuildId(id.toString());
            cardVO.getBuilds().add(floorVO);
        }

        return cardVO;
    }

    private CardVO buildCardVOForDoor(CardVO cardVO, List<Door> doors) {
        CommunityDoorVO communityDoorVO = new CommunityDoorVO();
        cardVO.setHouses(new HashSet<>((int) doors.stream().map(Door::getCommunityId).count()));
        cardVO.getHouses().add(communityDoorVO);
        communityDoorVO.setHouseId(cardVO.getCommunityId().toString());

        for (Door door : doors) {
            communityDoorVO.getDoors().add(
                    new DoorDeviceVO(door.getId().toString(), door.getTerminalCode(), door.getTerminalPort()));
        }

        return cardVO;
    }

    @Override
    public boolean updateCardDoorPermission(CardVO cardVO, List<Door> doors) {
        if (doors.size() == 0) {
            return true;
        }
        // 过滤非康途的门
        List<Door> kangTuDoors = doors.stream().filter(door -> door.getBrandNo() == ManufactureType.KANGTU_DOOR.KEY)
                                      .collect(Collectors.toList());
        this.buildCardVOForDoor(cardVO, kangTuDoors);
        applyKangtuDeviceAuthAsync(cardVO, DOOR_UPDATE_URL);
        return true;
    }

    @Override
    public boolean deleteCardDoorPermission(CardVO cardVO, List<Door> doors) {
        if (doors.size() == 0) {
            return true;
        }

        this.buildCardVOForDoor(cardVO, doors);
        cardVO.setProcessTime(0);
        cardVO.setUsesTime(0);
        return modifyPermission(cardVO, DOOR_DELETE_URL, null);
    }

    @Override
    public TenantApplicationResponse getTenantApplicationStatistics(TenantApplicationRequest tenantApplicationRequest) {
        if (tenantApplicationRequest.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        if (tenantApplicationRequest.getStartAt() == null) {
            tenantApplicationRequest.setStartAt(DateUtils.getFirstDateOfMonth(new Date()));
        }

        if (tenantApplicationRequest.getEndAt() == null) {
            tenantApplicationRequest.setEndAt(DateUtils.getLastDateOfMonth(new Date()));
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery("communityId",
                                        tenantApplicationRequest.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(tenantApplicationRequest.getStartAt()))
                                        .to(DateUtils.getShortDateStr(tenantApplicationRequest.getEndAt())))))
                .addAggregation(AggregationBuilders.dateHistogram("day_group")
                        .field("createAt")
                        .dateHistogramInterval(DateHistogramInterval.DAY)
                        .format("yyyy-MM-dd")
                        .minDocCount(0)
                        .extendedBounds(
                                new ExtendedBounds(DateUtils.getShortDateStr(tenantApplicationRequest.getStartAt()),
                                DateUtils.getShortDateStr(tenantApplicationRequest.getEndAt()))));

        SearchResponse searchResponse = searchRequestBuilder.get();

        TenantApplicationResponse tenantApplicationResponse = new TenantApplicationResponse();
        long total = searchResponse.getHits().getTotalHits();
        tenantApplicationResponse.setTotal(total);

        List<Section> daySections = new LinkedList<>();
        Histogram dayGroup = searchResponse.getAggregations().get("day_group");
        for (Histogram.Bucket bucket : dayGroup.getBuckets()) {
            Section section = new Section();
            section.setName(bucket.getKeyAsString());
            section.setCount(bucket.getDocCount());
            section.setProportion((total > 0
                    ? AmountUtil.roundDownStr(section.getCount() * 100.0D / total)
                    : "0.00") + "%");
            daySections.add(section);
        }
        tenantApplicationResponse.setDaySections(daySections);

        return tenantApplicationResponse;
    }

    @Override
    public void updateCardRoomNameByRoomLocation(CardVO cardVO, Set<String> roomLocations) {
        Card card = cardRepository.findByKeyNoAndKeyIdAndDataStatus(
                cardVO.getKeyNo(), cardVO.getKeyId(), DataStatusType.VALID.KEY);

        if (card.getRoomName() == null) {
            Set<String> set = new HashSet<>();
            card.setRoomName(set);
        }

        card.getRoomName().addAll(roomLocations);
        cardRepository.updateOne(card);
    }

    @Override
    public void pullAllCardRoomNameByKeyIdAndKeyNo(Card card) {
        Card toUpdate = new Card();
        toUpdate.setRoomName(card.getRoomName());
        toUpdate.setUpdateAt(new Date());
        cardRepository.updateWithPullAllRoomNameByKeyIdAndKeyNoAndDataStatus(
                toUpdate, card.getKeyId(), card.getKeyNo(), DataStatusType.VALID.KEY);
    }

    @Override
    public List<Card> findUsefulCardByHouseholdId(ObjectId householdId) {
        Household household = householdRepository.findByIdAndDataStatus(householdId, DataStatusType.VALID.KEY);

        if (household == null) {
            throw HOUSEHOLD_NOT_EXIST;
        }

        return this.findUsefulCardByUserIdAndCommunityId(household.getUserId(), household.getCommunityId());
    }

    /**
     * 根据用户id和社区id查询有效期内的卡
     * @param userId
     * @param communityId
     * @return
     */
    @Override
    public List<Card> findUsefulCardByUserIdAndCommunityId(ObjectId userId, ObjectId communityId) {
        return cardRepository.findByUserIdAndCommunityIdAndEndDateGreaterThanAndDataStatus(userId,
                                                                                           communityId,
                                                                                           new Date(),
                                                                                           DataStatusType.VALID.KEY);
    }

    // 兼容旧版APP接口
    @Override
    public Card applyCardForElevatorPermissionBy(CardVO cardVO, List<Room> rooms) {
        for (Room room : rooms) {
            cardVO = this.buildCardVOForElevator(cardVO, room);
        }
        return applyCardAndElevatorPermission(cardVO);
    }

    // 兼容旧版APP接口
    @Override
    public Card applyCardForElevatorPermissionBy(CardVO cardVO, Set<ObjectId> buildingIds,
                                                 UserToProperty userToProperty, Set<String> roomLocations) {
        cardVO.setName(userToProperty.getUserName());
        cardVO.setUserId(userToProperty.getUserId());
        cardVO.setRoomName(roomLocations);
        cardVO.setCommunityId(userToProperty.getCommunityId());
        cardVO = this.buildCardVOForElevatorFrom(cardVO, buildingIds);
        return applyCardAndElevatorPermission(cardVO);
    }

    @Override
    public boolean updateCardForElevatorPermissionBy(CardVO cardVO, List<Room> rooms) {
        for (Room room : rooms) {
            cardVO = this.buildCardVOForElevator(cardVO, room);
        }

        return updateCardElevatorPermission(cardVO);
    }

    @Override
    public boolean deleteCardForElevatorPermissionBy(CardVO cardVO, List<Room> rooms) {
        for (Room room : rooms) {
            cardVO = this.buildCardVOForElevator(cardVO, room);
        }
        return deleteCardElevatorPermission(cardVO);
    }

    @Override
    public CardVO updateCardElevatorPermissionFrom(CardVO cardVO, Set<ObjectId> buildingIds) {
        cardVO = this.buildCardVOForElevatorFrom(cardVO, buildingIds);
        this.updateCardElevatorPermission(cardVO);
        return cardVO;
    }

    @Override
    public List<KeyNoListElevatorVO> findUserAuthElevatorList(AuthElevatorRequest request)
            throws BizException {

        Card card = cardRepository.findByUserIdAndCommunityIdAndKeyTypeAndDataStatus(
                request.getUserId(), request.getCommunityId(),
                CertificateType.PHONE_MAC.KEY, DataStatusType.VALID.KEY);
        if (card == null) {
            log.info("当前用户({})在社区({})没有手机蓝牙卡", request.getUserId(), request.getCommunityId());
            return Collections.emptyList();
        }
        request.setKeyId(card.getKeyId());
        request.setKeyType(card.getKeyType());
        log.info("to get elevator info... request:{}", request);
        List<KeyNoListElevatorVO> list = Optional
                .ofNullable(this.getAuthElevatorDeviceByMacAddress(request))
                .orElse(new KeyNoListElevatorVOResponse())
                .getData();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(keyNoListElevatorVO -> keyNoListElevatorVO.setProtocolKey(card.getProtocolKey()));
        }

        // 把空的电梯过滤并排序
        return list.stream()
                   .filter(k -> StringUtil.isNotBlank(k.getName()))
                   .sorted(Comparator.comparing(KeyNoListElevatorVO::getName))
                   .collect(Collectors.toList());
    }

    // 兼容旧版APP接口
    @Override
    public Card applyQRCardForElevatorByRooms(ObjectId userId, CardVO cardVO, List<Room> rooms) {
        UserToRoom userToRoom = new UserToRoom();
        userToRoom.setCommunityId(cardVO.getCommunityId());
        userToRoom.setRoomId(cardVO.getRooms().stream().findFirst().orElse(null));

        if (userToRoom.getRoomId() == null) {
            throw QR_CARD_ROOMS_NULL;
        }

        userToRoom.setUserId(userId);
        userToRoom.setAuditStatus(AuditStatusType.REVIEWED.getType());
        List<UserToRoom> user = this.getRoomsByUserId(userToRoom);

        if (Objects.isNull(user) || user.size() == 0) {
            throw ROOMS_AUTH_NOT_EXIST;
        }

        Set<String> roomLocations = user.stream().map(UserToRoom::getRoomLocation).collect(Collectors.toSet());
        List<Card> cards = cardRepository.findByUserIdAndKeyTypeAndRoomNameInAndDataStatus(
                userId, CertificateType.QR_CODE.KEY, roomLocations, DataStatusType.VALID.KEY);

        if (cards != null && cards.size() > 0) {
            throw QR_CARD_EXIST;
        }

        userToRoom = user.stream().findFirst().orElse(new UserToRoom());
        cardVO.setUserId(userId);
        cardVO.setName(userToRoom.getName());
        cardVO.setRoomName(roomLocations);

        return this.applyCardForElevatorPermissionBy(cardVO, rooms);
    }

    @Override
    public Card applyCardForUser(ObjectId userId, ObjectId communityId, String name) {
        return cardGenerator.applyUserCard(userId, communityId, name);
    }

    @Override
    public CardVO applyHouseholdPhysicalCard(CardVO cardVO) {
        if (cardVO.getKeyType() == CertificateType.PHONE_MAC.KEY) {
            throw PHONE_MAC_CAN_NOT_APPLY;
        }

        physicalCardParamVerify(cardVO);
        List<UserToRoom> userToRooms;
        if(cardVO.getUserId() != null){
            userToRooms = userToRoomRepository.findByCommunityIdAndUserIdInAndAuditStatusAndDataStatus(
                    cardVO.getCommunityId(), Collections.singleton(cardVO.getUserId()),
                    AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);

            if (userToRooms == null || userToRooms.isEmpty()) {
                throw ROOMS_AUTH_NOT_EXIST;
            }
            cardVO.setRoomName(userToRooms.stream().map(UserToRoom::getRoomLocation).collect(Collectors.toSet()));
        }else{
            // 不存在userId的情况，需要创建一个userToRoom. update at 2018.11.15 by decai.liu
            UserToRoom entity = new UserToRoom();
            entity.setRoomId(cardVO.getRooms().iterator().next());
            entity.setBuildingId(cardVO.getBuildingIds().iterator().next());
            userToRooms = Arrays.asList(entity);
        }
        // name 已修改为必填项了 delete at 2018.11.15 by decai.liu
//        cardVO.setName(userToRooms.iterator().next().getName());
        cardVO.setUserToRooms(userToRooms);
        cardVO.setBuildingIds(
                new HashSet<>(userToRooms.stream().map(UserToRoom::getBuildingId).collect(Collectors.toSet())));

        Card card = cardGenerator.applyPhysicalCard(cardVO);
        // 获取有效期时长（单位秒）
        cardVO.setProcessTime((int) DateUtils.secondsBetween(card.getStartDate(), card.getEndDate()));

        cardVO.setKeyId(card.getKeyId());
        cardVO.setKeyNo(card.getKeyNo());
        return cardVO;
    }

    @Override
    public CardVO applyPropertyPhysicalCard(CardVO cardVO) {
        if (cardVO.getKeyType() == CertificateType.PHONE_MAC.KEY) {
            throw PHONE_MAC_CAN_NOT_APPLY;
        }
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                cardVO.getUserId(), cardVO.getCommunityId(), null);

        if (userToProperty == null) {
            throw PROPERTY_NOT_EXIST;
        }

        physicalCardParamVerify(cardVO);
        Card card = cardGenerator.applyPhysicalCard(cardVO);
        // 获取有效期时长（单位秒）
        cardVO.setProcessTime((int) DateUtils.secondsBetween(card.getStartDate(), card.getEndDate()));

        cardVO.setKeyId(card.getKeyId());
        cardVO.setKeyNo(card.getKeyNo());
        Set<FloorVO> set = new HashSet<>(userToProperty.getBuildingIds().size());
        cardVO.setBuilds(set);
        if (userToProperty.getBuildingIds() == null || userToProperty.getBuildingIds().isEmpty()) {
            return cardVO;
        }

        userToProperty.getBuildingIds().forEach(b -> set.add(new FloorVO(b.toString())));
        cardVO.setBuildingIds(new HashSet<>(userToProperty.getBuildingIds()));
        return cardVO;
    }

    @Override
    public CardVO applyHouseholdQRCard(CardVO cardVO) {
        if (cardVO.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        String nowTime = String.valueOf(System.currentTimeMillis());
        String key = String.format("%s%s", QR_CODE_REDIS_KEY, cardVO.getUserId().toString());
        // 加锁防并发申请访客通行
        if (!RedisService.lock(key, nowTime)) {
            return null;
        }
        RedisService.expire(key, 20);

        if (cardVO.getKeyType() == CertificateType.PHONE_MAC.KEY) {
            throw PHONE_MAC_CAN_NOT_APPLY;
        }

        if (cardVO.getRooms() == null || cardVO.getRooms().stream().findFirst().orElse(null) == null) {
            throw QR_CARD_ROOMS_NULL;
        }

        List<UserToRoom> userToRoomList = userToRoomRepository.findByUserIdAndRoomIdInAndDataStatusAndAuditStatus(
                cardVO.getUserId(), cardVO.getRooms(), DataStatusType.VALID.KEY, AuditStatusType.REVIEWED.getType());

        if (Objects.isNull(userToRoomList) || userToRoomList.size() == 0) {
            throw ROOMS_AUTH_NOT_EXIST;
        }
        Set<String> roomNames = userToRoomList.stream().map(UserToRoom::getRoomLocation).collect(Collectors.toSet());

        Card qrCode = cardRepository
                .findByUserIdAndCommunityIdAndKeyTypeAndRoomNameInAndProcessTimeGreaterThanAndDataStatus(
                        cardVO.getUserId(), cardVO.getCommunityId(), CertificateType.QR_CODE.KEY, roomNames,
                        new Date(), DataStatusType.VALID.KEY);

        if (qrCode != null) {
            throw QR_CARD_EXIST;
        }
        cardVO.setUserToRooms(userToRoomList);
        cardVO.setRoomName(roomNames);
        cardVO.setBuildingIds(
                new HashSet<>(userToRoomList.stream().map(UserToRoom::getBuildingId).collect(Collectors.toSet())));

        Card card = cardGenerator.applyHouseholdQRCard(cardVO);
        addTenantApplication(card);
        cardVO.setKeyId(card.getKeyId());
        cardVO.setKeyNo(card.getKeyNo());
        cardVO.setCardId(card.getId());
        //add at 20180814
        cardVO.setExpireAt(card.getProcessTime());
        RedisService.unlock(key, nowTime);
        return cardVO;
    }

    @Override
    public CardVO applyOffLineICCard(CardVO vo) {
        Date expireAt = vo.getExpireAt();
        Integer processTime = vo.getProcessTime();
        Integer timeUnit = vo.getTimeUnit() == null ? TimeUnitEnum.SECOND.value() : vo.getTimeUnit();
        if(TimeUnitEnum.fromValue(timeUnit) == null){
            throw TIME_UNIT_INVALID;
        }
        if(expireAt == null && processTime == null){
            // 有效期默认50年
            expireAt = DateUtils.addYear(new Date(), 50);
        }else {
            if(expireAt == null){
                expireAt = UserUtils.getExpireAt(processTime, timeUnit);
            }
        }

        Card card = new Card();
        card.setKeyNo(vo.getKeyNo());
        card.setKeyType(vo.getKeyType());
        card.setUserId(vo.getUserId());
        card.setCommunityId(vo.getCommunityId());
        card.setName(vo.getName());
        card.setPhone(vo.getPhone());
        card.setRoomName(vo.getRoomName());
        if(vo.getRooms() != null && !vo.getRooms().isEmpty()){
            card.setRoomId(vo.getRooms().iterator().next());
        }
        card.setProcessTime(DateUtils.getEndTime(expireAt));
        card.setCreateAt(new Date());
        card.setStartDate(card.getCreateAt());
        card.setEndDate(card.getProcessTime());
        card.setUpdateAt(card.getCreateAt());
        card.setDataStatus(DataStatusType.VALID.KEY);
        card = cardRepository.upsertWithUnsetIfNullRoomNameAndRoomIdByCommunityIdAndKeyNoAndKeyTypeAndDataStatus(
                card, card.getCommunityId(), card.getKeyNo(), card.getKeyType(), DataStatusType.VALID.KEY);
        if(card != null){
            vo.setCardId(card.getId());
        }
        return vo;
    }

    @Override
    public List<Card> findUserPhysicalCardInCommunity(Card entity) {
        // 若keyType没有传参数, 默认查询蓝牙卡和IC卡的卡片集合
        List<Integer> keyTypes = entity.getKeyType() != null && entity.getKeyType() != CertificateType.PHONE_MAC.KEY ?
                Collections.singletonList(entity.getKeyType()) :
                Arrays.asList(CertificateType.BLUETOOTH_CARD.KEY, CertificateType.IC_CARD.KEY);
        return cardRepository.findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(entity.getUserId(), entity.getCommunityId(), keyTypes, DataStatusType.VALID.KEY);
    }

    @Override
    public Card getById(ObjectId id) {
        return cardRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    /**
     * 大屏统计访客数据
     *
     * @param request
     * @return
     */
    @Override
    public TenantApplicationResponse getTenantApplicationStatisticsForScreen(TenantApplicationRequest request) {
        if (request.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        if (request.getStartAt() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR, 2);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            request.setStartAt(calendar.getTime());
        }

        if (request.getEndAt() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtils.addDay(new Date(), 1));
            calendar.set(Calendar.HOUR, 2);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            request.setEndAt(calendar.getTime());
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery("communityId",
                                        request.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(request.getStartAt()))
                                        .to(DateUtils.getShortDateStr(request.getEndAt())))));

        SearchResponse searchResponse = searchRequestBuilder.get();
        TenantApplicationResponse tenantApplicationResponse = new TenantApplicationResponse();
        long total = searchResponse.getHits().getTotalHits();
        tenantApplicationResponse.setTotal(total);

        searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery("communityId", request.getCommunityId().toString()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(DateUtils.addDay(new Date(), -7)))
                                        .to(DateUtils.getShortDateStr()))))
                .addAggregation(AggregationBuilders.dateHistogram("day_group")
                        .field("createAt")
                        .dateHistogramInterval(DateHistogramInterval.DAY)
                        .format("yyyy-MM-dd")
                        .minDocCount(0)
                        .extendedBounds(new ExtendedBounds(
                                DateUtils.getShortDateStr(DateUtils.addDay(new Date(), -7)),
                                DateUtils.getShortDateStr(DateUtils.addDay(new Date(), -1)))));
        SearchResponse searchResponseDay = searchRequestBuilder.get();
        List<Section> daySections = new LinkedList<>();
        Histogram dayGroup = searchResponseDay.getAggregations().get("day_group");
        for (Histogram.Bucket bucket : dayGroup.getBuckets()) {
            Section section = new Section();
            section.setName(bucket.getKeyAsString());
            section.setCount(bucket.getDocCount());
            daySections.add(section);
        }
        tenantApplicationResponse.setDaySections(daySections);

        return tenantApplicationResponse;
    }

    @Override
    public List<Card> findByCommunityIdAndKeyNoAndKeyType(ObjectId communityId, String keyNo, Integer keyType) {
        return cardRepository.findByCommunityIdAndKeyNoAndKeyTypeAndDataStatusOrderByCreateAtDesc(
                communityId, keyNo, keyType, DataStatusType.VALID.KEY);
    }

    private void physicalCardParamVerify(CardVO cardVO) {
        if (StringUtil.isBlank(cardVO.getKeyNo())) {
            throw KEYNO_NOT_NULL;
        }
        if (!cardVO.getKeyNo().matches("^[A-Fa-f0-9]{1,12}$")) {
            throw KEYNO_ILLEGAL;
        }

        if (this.existKeyTypeAndKeyNoInCommunity(cardVO.getCommunityId(), cardVO.getKeyNo(), cardVO.getKeyType())) {
            if (cardVO.getKeyType() == CertificateType.IC_CARD.KEY
                    || cardVO.getKeyType() == CertificateType.BLUETOOTH_CARD.KEY) {
                throw IC_CARD_EXIST;
            }
        }
    }

    @Override
    public Card findByKeyNoAndCmId(String keyNo, ObjectId communityId) {
        return cardRepository.findByKeyNoAndCommunityIdAndDataStatus(keyNo,
                                                                     communityId,
                                                                     DataStatusType.VALID.KEY);
    }

    @Override
    public Card findByKeyNo(String keyNo) {
        return cardRepository.findByKeyNoAndDataStatus(keyNo,
                                                       DataStatusType.VALID.KEY);
    }

    @Override
    public Card updateIsProcessedById(ObjectId id, Integer isProcessed) {
        return cardRepository.updateOne(new Query(Criteria.where("id").is(id)), Update.update("isProcessed", isProcessed));
    }

    @Override
    public JSONObject queryCardInfoFromDTU(CardQueryRequest request) {
        return CardOperation2DTU(request, DTU_QUERY);
    }

    @Override
    public Object addCardInfo2DTU(CardQueryRequest request) {
        return CardOperation2DTU(request, DTU_ADD);
    }

    @Override
    public Object deleteCardInfoFromDTU(CardQueryRequest request) {
        return CardOperation2DTU(request, DTU_REMOVE);
    }

    @Override
    public Boolean existKeyTypeAndKeyNoInCommunity(ObjectId communityId, String keyNo, Integer keyType) {
        return cardRepository.existsByCommunityIdAndKeyNoAndKeyTypeAndDataStatus(
                communityId, keyNo, keyType, DataStatusType.VALID.KEY);
    }

    @Override
    public Card updateCardForProtocol(ObjectId cardId, String protocolKey) {
        Card toUpdate = new Card();
        toUpdate.setValidState(CardStatusType.VALID.KEY);
        toUpdate.setProtocolKey(protocolKey);
        toUpdate.setUpdateAt(new Date());
        return cardRepository.updateByIdAndDataStatus(toUpdate, cardId, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Card> getUserCardRecord(Card entity, Integer page, Integer size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        List<Integer> keyTypes = Arrays.asList(
                CertificateType.BLUETOOTH_CARD.KEY, CertificateType.IC_CARD.KEY, CertificateType.QR_CODE.KEY);
        if (entity.getKeyType() != null && entity.getKeyType() != CertificateType.PHONE_MAC.KEY) {
            keyTypes = Collections.singletonList(entity.getKeyType());
        }
        org.springframework.data.domain.Page<Card> cardPage =
                cardRepository.findByCommunityIdAndKeyNoIgnoreNullAndKeyTypeInAndNameIgnoreNullAndDataStatus(
                    entity.getCommunityId(), entity.getKeyNo(), keyTypes,
                        entity.getName(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(cardPage);
    }

    /**
     * 获取社区用户的手机蓝牙卡
     *
     * @param communityId
     * @param userId
     * @param keyType
     * @return
     */
    @Override
    public Card findByCommunityIdAndUserIdAndKeyType(ObjectId communityId, ObjectId userId, int keyType) {
        return cardRepository.findByUserIdAndCommunityIdAndKeyTypeAndDataStatus(
                userId, communityId, keyType, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Card> findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(ObjectId userId, ObjectId communityId, List<Integer> keyType) {
        return cardRepository.findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(userId, communityId, keyType, DataStatusType.VALID.KEY);
    }

    @Override
    public Card getByIdAndUserId(ObjectId id, ObjectId userId) {
        return cardRepository.findByIdAndUserIdAndDataStatus(id, userId, DataStatusType.VALID.KEY);
    }

    @Override
    public Card applyQRCardForElevator(CardVO cardVO) {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setKeyType(cardVO.getKeyType());
        cardRequest.setKeyNo(fixCardLength(cardVO.getKeyNo(), 12));
        cardRequest.setKeyId(cardVO.getKeyId());
        cardRequest.setUsesTime(cardVO.getUsesTime());
        cardRequest.setProcessTime(cardVO.getProcessTime());
        // 请求电梯权限修改
        cardRequest.setBuilds(cardVO.getBuilds());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);

        log.info("申请二维码");
        JSONObject jsonObject = restTemplate.postForObject(this.url + ELEVATOR_APPLY_URL, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new UserBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
        JSONObject data = jsonObject.getJSONObject("data");
        return updateCardForQRCode(data.getString("keyNo"), data.getString("keyId"));
    }

    private Card updateCardForQRCode(String keyNo, String keyId) {
        Card toUpdate = new Card();
        toUpdate.setKeyNo(keyNo);
        toUpdate.setValidState(CardStatusType.VALID.KEY);
        toUpdate.setUpdateAt(new Date());
        return cardRepository.updateByKeyIdAndDataStatus(toUpdate, keyId, DataStatusType.VALID.KEY);
    }

    /**
     * 申请实体卡与二维码进行重试
     * @param cardVO
     * @param url
     */
    private void applyKangtuDeviceAuthAsync(CardVO cardVO, String url) {
        CompletableFuture.runAsync(() -> {
            AtomicInteger counter = new AtomicInteger(10);
            boolean success = false;
            while (!success && counter.decrementAndGet() > 0) {
                success = modifyPermission(cardVO, url, null);
                try {
                    if (!success) {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    private JSONObject CardOperation2DTU(CardQueryRequest request, String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), httpHeaders);

        try {
            return restTemplate.postForObject(this.dtuUrl + url, entity, JSONObject.class);
        } catch (RestClientException e) {
            log.warn("DTU连接异常", e);
            throw DTU_NET_EXCEPTION;
        }
    }

    /**
     * 将申请好的卡片保存到数据库
     * @param cardVO
     * @param keyId
     * @param keyNo
     * @return
     */
    private Card saveCardBy(CardVO cardVO, String keyId, String keyNo) {
        Card card = new Card();
        card.setRoomName(cardVO.getRoomName());
        card.setCommunityId(cardVO.getCommunityId());
        card.setKeyType(cardVO.getKeyType());
        card.setKeyNo(keyNo);
        card.setKeyId(keyId);
        card.setUserId(cardVO.getUserId());
        card.setName(cardVO.getName());
        // 控制类型默认先设置为8
        card.setControlType(8);
        card.setDataStatus(DataStatusType.VALID.KEY);
        card.setCreateAt(new Date());
        card.setCreateId(cardVO.getUserId());
        card.setStartDate(new Date());
        card.setProcessTime(DateUtils.addSecond(new Date(), cardVO.getProcessTime()));
        card.setUseTimes(cardVO.getUsesTime());
        card.setEndDate(card.getProcessTime());
        return cardRepository.upsertWithUnsetIfNullRoomNameAndRoomIdByCommunityIdAndKeyNoAndKeyTypeAndDataStatus(
                card, card.getCommunityId(), card.getKeyNo(), card.getKeyType(), DataStatusType.VALID.KEY);
    }

    /**
     * 遍历是否有相同的楼栋
     * @param builds
     * @param buildingIdStr
     * @return
     */
    private FloorVO buildContains(Set<FloorVO> builds, String buildingIdStr) {
        for (FloorVO build : builds) {
            if (build.getBuildId() != null && build.getBuildId().equals(buildingIdStr)) {
                return build;
            }
        }
        return null;
    }

    private List<UserToRoom> getRoomsByUserId(UserToRoom userToRoom) {
        if(userToRoom.getCommunityId() == null){
            throw COMMUNITY_ID_NULL;
        }
        userToRoom.setDataStatus(DataStatusType.VALID.KEY);
        return userToRoomRepository.find(userToRoom, XSort.desc("createAt"));
    }

    /**
     * 保持卡位数
     * @param keyNo
     * @return
     */
    private String fixCardLength(String keyNo, int length) {
        // 将不够指定位数位的字符串补齐指定位数位
        if (keyNo.length() < length) {
            keyNo = UUIDUitl.toFixdLengthString(keyNo, length);
        }
        // 将长度大于指定位数位的字符串截取前面指定位数位
        else if (keyNo.length() > length) {
            keyNo = keyNo.substring(0, length);
        }
        keyNo = keyNo.toUpperCase();
        return keyNo;
    }
}
