package cn.bit.user.service;

import cn.bit.common.facade.constant.RegexConstants;
import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.RelationshipType;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DeviceAuthVO;
import cn.bit.facade.vo.statistics.HouseholdRequest;
import cn.bit.facade.vo.statistics.HouseholdResponse;
import cn.bit.facade.vo.statistics.Region;
import cn.bit.facade.vo.statistics.Section;
import cn.bit.facade.vo.user.userToRoom.HouseholdPageQuery;
import cn.bit.facade.vo.user.userToRoom.HouseholdVO;
import cn.bit.facade.vo.user.userToRoom.MemberDTO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.IdentityCardUtils;
import cn.bit.framework.utils.number.AmountUtil;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.framework.utils.validate.IDCardUtils;
import cn.bit.framework.utils.validate.RegularUtil;
import cn.bit.user.dao.HouseholdRepository;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.date.InternalDateRange;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.constant.mq.TagConstant.*;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_HOUSEHOLD_IMPORT;
import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.user.UserBizException.*;

@Service("householdFacade")
@Slf4j
public class HouseholdFacadeImpl implements HouseholdFacade {

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private EsTemplate esTemplate;

    @Autowired
    private DefaultMQProducer producer;

    @Autowired
    private DefaultMQProducer mqProducer;

    private static final String INDEX_NAME = "cm_user";

    private static final String TYPE_NAME = "household";

    /**
     * 根据房间ID获取住户档案列表
     *
     * @param roomId
     * @return
     */
    @Override
    public List<Household> findByRoomId(ObjectId roomId) {
        return householdRepository.findByRoomIdAndDataStatusOrderByRelationshipAsc(roomId, DataStatusType.VALID.KEY);
    }

    /**
     * 保存住房档案
     *
     * @param householdVO
     */
    @Override
    public List<Household> saveHouseholds(HouseholdVO householdVO) {
        if(householdVO.getContacts() != null && householdVO.getContacts().size() > 3){
            throw CONTACTS_OVER_MAX;
        }
        Set<String> userNames = new HashSet<>();
        userNames.add(householdVO.getUserName().trim().toLowerCase());
        if (householdVO.getMembers() != null && !householdVO.getMembers().isEmpty()) {
            for (MemberDTO dto : householdVO.getMembers()) {
                // 添加不成功，则说明已经存在这个用户名
                if(RelationshipType.RELATION.KEY.equals(dto.getRelationship())
                        && !userNames.add(dto.getUserName().trim().toLowerCase())){
                    userNames.clear();
                    throw HOUSEHOLD_USERNAME_REPEAT;
                }
                if (dto != null && StringUtil.isNotBlank(dto.getIdentityCard())
                        && !IDCardUtils.verifi(dto.getIdentityCard())) {
                    userNames.clear();
                   throw IDENTITY_CARD_ILLEGAL;
                }
                if(dto != null && StringUtil.isNotBlank(dto.getPhone())
                        && !RegularUtil.validatePattern(RegexConstants.REGEX_PHONE, dto.getPhone())){
                    userNames.clear();
                    throw PHONE_ILLEGAL;
                }
            }
        }
        userNames.clear();
        ObjectId roomId = householdVO.getRoomId();
        List<MemberDTO> dtoList = householdVO.getMembers();
        List<Household> list = householdRepository.findByRoomIdAndDataStatusOrderByRelationshipAsc(roomId, DataStatusType.VALID.KEY);
        if(!list.isEmpty()){
            throw HOUSEHOLD_EXIST;
        }

        List<Household> inserts = new ArrayList<>();
        // 业主房屋档案信息
        Household household = new Household();
        BeanUtils.copyProperties(householdVO, household);
        household.setDataStatus(DataStatusType.VALID.KEY);
        household.setCreateAt(new Date());
        household.setUpdateAt(household.getCreateAt());
        household.setActivated(false);
        household.setRemark("物业录入住户档案");
        inserts.add(household);
        if(dtoList != null && !dtoList.isEmpty()){
            // 成员档案
            for(MemberDTO dto : dtoList){
                Household toInsert = new Household();
                BeanUtils.copyProperties(dto, toInsert);
                toInsert.setCommunityId(household.getCommunityId());
                toInsert.setZoneId(household.getZoneId());
                toInsert.setBuildingId(household.getBuildingId());
                toInsert.setRoomId(household.getRoomId());
                toInsert.setRoomLocation(household.getRoomLocation());
                toInsert.setCreateAt(household.getCreateAt());
                toInsert.setUpdateAt(toInsert.getCreateAt());
                toInsert.setActivated(household.getActivated());
                toInsert.setDataStatus(household.getDataStatus());
                toInsert.setRemark(household.getRemark());
                toInsert.setId(null);
                toInsert.setContacts(null);
                inserts.add(toInsert);
            }
        }
        List<Household> households = householdRepository.save(inserts);
        log.info("result:" + households);
        return households;
    }

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    @Override
    public Page<Household> listHouseholds(HouseholdPageQuery query) {
        if(query == null){
            query = new HouseholdPageQuery();
        }
        query.setPage(Optional.ofNullable(query.getPage()).orElse(1));
        query.setSize(Optional.ofNullable(query.getSize()).orElse(10));
        Pageable pageable = new PageRequest(query.getPage() - 1, query.getSize(),
                new Sort(Sort.Direction.ASC, "buildingId", "roomId", "relationship"));
        org.springframework.data.domain.Page<Household> householdPage = householdRepository
            .findByCommunityIdAndBuildingIdAndRoomIdAndRoomNameRegexAndUserNameRegexAndPhoneAndRelationshipAndActivatedAndDataStatusAllIgnoreNull(
                    query.getCommunityId(), query.getBuildingId(), query.getRoomId(),
                    StringUtil.makeQueryStringAllRegExp(query.getRoomName()),
                    StringUtil.makeQueryStringAllRegExp(query.getUserName()),
                    query.getPhone(), query.getRelationship(), query.getActivated(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(householdPage);
    }

    /**
     * 查看详细
     *
     * @param id
     * @return
     */
    @Override
    public Household getHouseholdDetail(ObjectId id) {
        return householdRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    /**
     * 编辑档案
     *
     * @param household
     * @return
     */
    @Override
    public Household modifyHousehold(Household household) {
        if (StringUtil.isNotBlank(household.getIdentityCard()) && !IDCardUtils.verifi(household.getIdentityCard())) {
            throw IDENTITY_CARD_ILLEGAL;
        }
        if(StringUtil.isNotBlank(household.getPhone())
                && !RegularUtil.validatePattern(RegexConstants.REGEX_PHONE, household.getPhone())){
            throw PHONE_ILLEGAL;
        }
        if(StringUtil.isNotBlank(household.getUserName()) && household.getUserName().length() > 16){
            throw HOUSEHOLD_USERNAME_ILLEGAL;
        }
        Household toGet = householdRepository.findByIdAndDataStatus(household.getId(), DataStatusType.VALID.KEY);
        if(toGet == null){
            throw DATA_INVALID;
        }
        if (toGet.getActivated() != null && toGet.getActivated()) {
            household.setPhone(null);
        }
        if(StringUtil.isNotBlank(household.getUserName())
                && !household.getUserName().trim().toLowerCase().equals(toGet.getUserName().trim().toLowerCase())){
            Household toCheck = householdRepository.findByRoomIdAndUserNameIgnoreCaseAndDataStatus(
                    toGet.getRoomId(), household.getUserName().trim(), DataStatusType.VALID.KEY);
            if(toCheck != null){
                throw HOUSEHOLD_USERNAME_REPEAT;
            }
        }
        household.setUpdateAt(new Date());
        // 不可编辑以下内容
        household.setRelationship(null);
        household.setDataStatus(null);
        household.setCommunityId(null);
        household.setBuildingId(null);
        household.setZoneId(null);
        household.setRoomId(null);
        household.setRoomLocation(null);
        household.setCreateAt(null);
        household = householdRepository.updateById(household, household.getId());
        upsertHouseholdToEs(household);
        return household;
    }

    /**
     * 获取房间下的业主档案
     *
     * @param roomId
     * @return
     */
    @Override
    public Household findAuthOwnerByRoom(ObjectId roomId) {
        List<Household> list = householdRepository.findByRoomIdAndRelationshipAndDataStatus(
                roomId, RelationshipType.OWNER.KEY, DataStatusType.VALID.KEY);
        if(list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    /**
     * 根据手机号匹配未激活的业主档案
     *  @param phone
     */
    @Override
    public List<Household> listUnactivatedOwnerHouseholdsByPhone(String phone) {
        // 未激活的业主档案
        List<Household> owners = householdRepository.findByPhoneAndRelationshipAndActivatedNotAndDataStatus(
                phone, RelationshipType.OWNER.KEY, true, DataStatusType.VALID.KEY);
        return owners;
    }

    /**
     * 根据 householdIds 批量激活档案
     *
     * @param userId
     * @param householdIds
     */
    @Override
    public Long activatedHouseholdByIds(ObjectId userId, Collection<ObjectId> householdIds) {
        if(householdIds == null || householdIds.isEmpty()){
            return 0L;
        }
        Household toUpdate = new Household();
        toUpdate.setUserId(userId);
        toUpdate.setActivated(true);
        toUpdate.setUpdateAt(new Date());
        return householdRepository.updateByIdIn(toUpdate, householdIds);
    }

    /**
     * 查询房屋档案详情
     *
     * @param roomId
     * @return
     */
    @Override
    public HouseholdVO findDetailByRoom(ObjectId roomId) {
        List<Household> list = householdRepository.findByRoomIdAndDataStatusOrderByRelationshipAsc(roomId, DataStatusType.VALID.KEY);
        if(list.isEmpty()){
            return null;
        }
        HouseholdVO householdVO = new HouseholdVO();
        List<MemberDTO> members = new ArrayList<>();
        for(Household household : list){
            if(RelationshipType.OWNER.KEY.equals(household.getRelationship())){
                BeanUtils.copyProperties(household, householdVO);
                householdVO.setHouseholdId(household.getId());
                continue;
            }
            MemberDTO memberDTO = new MemberDTO();
            BeanUtils.copyProperties(household, memberDTO);
            memberDTO.setHouseholdId(household.getId());
            members.add(memberDTO);
        }
        householdVO.setMembers(members);
        return householdVO;
    }

    /**
     * 批量插入住户档案
     *
     * @param households
     * @return
     */
    @Override
    public List<Household> saveHouseholds(List<Household> households) {
        if(households == null || households.isEmpty()){
            return Collections.emptyList();
        }
        List<Household> householdList = householdRepository.findByRoomIdAndDataStatusOrderByRelationshipAsc(
                households.get(0).getRoomId(), DataStatusType.VALID.KEY);

        Set<String> userNames = new HashSet<>();
        if(!householdList.isEmpty()){
            householdList.forEach(household -> userNames.add(household.getUserName().trim().toLowerCase()));
        }
        households.forEach(household -> {
            // 添加不成功，则说明已经存在这个用户名
            if(RelationshipType.RELATION.KEY.equals(household.getRelationship())
                    && !userNames.add(household.getUserName().trim().toLowerCase())){
                userNames.clear();
                throw HOUSEHOLD_USERNAME_REPEAT;
            }
            if (StringUtil.isNotBlank(household.getIdentityCard()) && !IDCardUtils.verifi(household.getIdentityCard())) {
                userNames.clear();
                throw IDENTITY_CARD_ILLEGAL;
            }
            if(StringUtil.isNotBlank(household.getPhone())
                    && !RegularUtil.validatePattern(RegexConstants.REGEX_PHONE, household.getPhone())){
                userNames.clear();
                throw PHONE_ILLEGAL;
            }
            household.setDataStatus(DataStatusType.VALID.KEY);
            household.setCreateAt(new Date());
            household.setUpdateAt(household.getCreateAt());
            household.setRemark("物业录入住户档案");
        });
        userNames.clear();
        List<Household> savedList = householdRepository.save(households);
        savedList.forEach(this::upsertHouseholdToEs);
        return savedList;
    }

    /**
     * 注销房屋档案
     *
     * @param roomId
     * @return
     */
    @Override
    public long removeByRoomId(ObjectId roomId) {
        Household toRemove = new Household();
        toRemove.setDataStatus(DataStatusType.INVALID.KEY);
        toRemove.setRemark("住房档案被管理员注销");
        toRemove.setUpdateAt(new Date());
        Long result = householdRepository.updateByRoomIdAndDataStatus(toRemove, roomId, DataStatusType.VALID.KEY);
        if(result == null || result <= 0){
            throw DATA_INVALID;
        }
        List<Household> list = householdRepository.findByRoomIdAndDataStatusOrderByRelationshipAsc(
                roomId, DataStatusType.INVALID.KEY);
        removeHouseholdFromEs(list.stream().map(Household::getId).collect(Collectors.toSet()));
        return result;
    }

    /**
     * 根据ID注销住户档案
     *
     * @param householdId
     * @return roomId
     */
    @Override
    public Household removeByHouseholdId(ObjectId householdId) {
        Household toGet = householdRepository.findByIdAndDataStatus(householdId, DataStatusType.VALID.KEY);
        if(toGet == null){
            throw DATA_INVALID;
        }
        log.info("待移除档案：{}", toGet);
        Household toRemove = new Household();
        toRemove.setDataStatus(DataStatusType.INVALID.KEY);
        toRemove.setRemark("住房档案被管理员注销");
        toRemove.setUpdateAt(new Date());
        // 如果是业主档案，则需要同时注销其他成员的档案
        if(RelationshipType.OWNER.KEY.equals(toGet.getRelationship())){
            Long result = householdRepository.updateByRoomIdAndDataStatus(toRemove, toGet.getRoomId(), DataStatusType.VALID.KEY);
            if(result == null || result == 0){
                throw DATA_INVALID;
            }
            List<Household> list = householdRepository.findByRoomIdAndDataStatusOrderByRelationshipAsc(
                    toGet.getRoomId(), DataStatusType.INVALID.KEY);
            removeHouseholdFromEs(list.stream().map(Household::getId).collect(Collectors.toSet()));
            // 要将删除后的业主档案反馈回去
            toGet.setDataStatus(toRemove.getDataStatus());
            toGet.setRemark(toRemove.getRemark());
            toGet.setUpdateAt(toRemove.getUpdateAt());
            return toGet;
        }
        toRemove = householdRepository.updateById(toRemove, householdId);
        removeHouseholdFromEs(Collections.singleton(householdId));
        return toRemove;
    }

    /**
     * 根据房间及用户注销档案
     *
     * @param roomId
     * @param userId
     * @return
     */
    @Override
    public Household removeByRoomIdAndUserId(ObjectId roomId, ObjectId userId) {
        Household toRemove = new Household();
        toRemove.setDataStatus(DataStatusType.INVALID.KEY);
        toRemove.setRemark("住房档案被管理员注销");
        toRemove.setUpdateAt(new Date());
        toRemove = householdRepository.updateByRoomIdAndUserIdAndDataStatus(toRemove, roomId, userId, DataStatusType.VALID.KEY);
        removeHouseholdFromEs(Collections.singleton(toRemove.getId()));
        return toRemove;
    }

    /**
     * 录入或激活档案
     *
     * @param household
     * @return
     */
    @Override
    public Household upsertHouseholdForNotOwner(Household household) {
        if(RelationshipType.OWNER.KEY.equals(household.getRelationship())){
            return null;
        }
        household.setDataStatus(DataStatusType.VALID.KEY);
        household.setCreateAt(new Date());
        household.setUpdateAt(household.getCreateAt());
        household.setActivated(true);
        household.setRemark("审核认证通过，系统录入/激活住户档案");
        if(RelationshipType.RELATION.KEY.equals(household.getRelationship())){
            household = householdRepository.upsertByRoomIdAndUserNameAndRelationship(
                    household, household.getRoomId(), household.getUserName(), household.getRelationship());
        }else{
            household = householdRepository.save(household);
        }
        upsertHouseholdToEs(household);
        return household;
    }

    /**
     * 录入或者激活业主档案
     *
     * @param household
     * @return
     */
    @Override
    public Household upsertHouseholdForOwner(Household household) {
        List<Household> owner = householdRepository.findByRoomIdAndRelationshipAndDataStatus(
                household.getRoomId(), RelationshipType.OWNER.KEY, DataStatusType.VALID.KEY);
        if(!owner.isEmpty()){
            // 房间有业主档案，只需要覆盖手机号，同时激活档案
            Household toActivated = new Household();
            toActivated.setUserId(household.getUserId());
            toActivated.setActivated(true);
            toActivated.setPhone(household.getPhone());
            toActivated.setUpdateAt(new Date());
            toActivated.setRemark("审核认证通过，系统自动激活住户档案");
            household = householdRepository.updateById(toActivated, owner.get(0).getId());
        }else{
            // 房间没有业主档案，需要录入一条已激活的档案
            household.setDataStatus(DataStatusType.VALID.KEY);
            household.setCreateAt(new Date());
            household.setUpdateAt(household.getCreateAt());
            household.setActivated(true);
            household.setRemark("审核认证通过，系统自动录入住户档案");
            household = householdRepository.save(household);
        }
        upsertHouseholdToEs(household);
        return household;
    }

    @Override
    public List<Household> findByRoomIdOrUserId(ObjectId roomId, ObjectId userId) {
        if(roomId == null && userId == null){
            return Collections.emptyList();
        }
        return householdRepository.findByRoomIdAndUserIdAndDataStatusAllIgnoreNull(roomId, userId, DataStatusType.VALID.KEY);
    }

    /**
     * 根据房间ID集合查询业主档案列表
     *
     * @param roomIds
     * @return
     */
    @Override
    public List<Household> findOwnerByRoomIds(Collection<ObjectId> roomIds) {
        return householdRepository.findByRoomIdInAndRelationshipAndDataStatus(
                roomIds, RelationshipType.OWNER.KEY, DataStatusType.VALID.KEY);
    }

    /**
     * 查询社区下已经存在住户档案的房间ID集合
     *
     * @param communityId
     * @return
     */
    @Override
    public Set<ObjectId> listRoomIdsByCommunityId(ObjectId communityId) {
        List<Household> list = householdRepository.findByCommunityIdAndRelationshipAndDataStatus(
                communityId, RelationshipType.OWNER.KEY, DataStatusType.VALID.KEY);
        if (list.isEmpty()) {
            return Collections.emptySet();
        }
        return list.stream().map(Household::getRoomId).collect(Collectors.toSet());
    }

    /**
     * excel导入档案写入数据库
     *
     * @param households
     * @param sendMsg
     * @return
     */
    @Override
    public List<Household> saveHouseholdsForImporting(List<Household> households, Boolean sendMsg) {
        if(households == null || households.isEmpty()){
            return Collections.emptyList();
        }
        List<Household> savedList = new ArrayList<>();
        List<Household> tempList = new ArrayList<>();
        Date today = new Date();
        for (Household household : households) {
            household.setRelationship(RelationshipType.OWNER.KEY);
            IdentityCardUtils.IdentityCardMeta meta = IdentityCardUtils.getIdentityCardMeta(household.getIdentityCard());
            household.setSex(meta.getSex());
            household.setActivated(false);
            household.setDataStatus(DataStatusType.VALID.KEY);
            household.setCreateAt(today);
            household.setUpdateAt(household.getCreateAt());
            household.setRemark("物业批量导入住户档案");
            tempList.add(household);
            if (tempList.size() == 1000) {
                savedList.addAll(householdRepository.save(tempList));
                tempList = new ArrayList<>();
            }
        }
        if (!tempList.isEmpty()) {
            savedList.addAll(householdRepository.save(tempList));
        }
        savedList.forEach(household -> {
            upsertHouseholdToEs(household);

            Map<String, Object> map = new HashMap<>();
            map.put("household", household);
            map.put("sendMsg", sendMsg == null ? false : sendMsg);
            // 消息队列处理后续业务逻辑
            Message householdMessage = new Message(TOPIC_HOUSEHOLD_IMPORT, TAG_HOUSEHOLD_IMPORT_PROCESS,
                    JSON.toJSONString(map).getBytes(Charset.forName("UTF-8")));
            try {
                mqProducer.send(householdMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("物业批量导入住户档案队列发送异常 : ", e);
            }
        });
        return savedList;
    }

    /**
     * 查询用户档案列表
     *
     * @param communityId
     * @param userId
     * @return
     */
    @Override
    public List<Household> findByCommunityIdAndUserId(ObjectId communityId, ObjectId userId) {
        return householdRepository.findByCommunityIdAndUserIdAndActivatedAndDataStatus(
                communityId, userId, Boolean.TRUE, DataStatusType.VALID.KEY);
    }

    /**
     * 住户统计分析结果
     *
     * @param householdRequest
     * @return
     */
    @Override
    public HouseholdResponse getHouseholdStatistics(HouseholdRequest householdRequest) {
        if (householdRequest.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .filter(QueryBuilders.matchQuery("communityId",
                                        householdRequest.getCommunityId().toString()))))
                .addAggregation(AggregationBuilders.terms("relationship_group")
                        .field("relationship")
                        .order(Terms.Order.term(true)))
                .addAggregation(AggregationBuilders.filter("sex", QueryBuilders.rangeQuery("sex")
                        .from(1))
                        .subAggregation(AggregationBuilders.terms("sex_group")
                                .field("sex")
                                .order(Terms.Order.term(true))));

        List<Region> regions = null;
        if (CollectionUtils.isNotEmpty(householdRequest.getAgeRegions())) {
            regions = householdRequest.getAgeRegions();
            Collections.reverse(regions);
            AggregationBuilder aggregationBuilder = AggregationBuilders.dateRange("age_group").field("birthday");
            for (Region region : householdRequest.getAgeRegions()) {
                boolean fromIsBlank = region.getFrom() == null;
                boolean toIsBlank = region.getTo() == null;
                if (fromIsBlank && toIsBlank) {
                    continue;
                }

                String from = toIsBlank ? null : "now-" + region.getTo() + "y";
                String to = fromIsBlank ? null : "now-" + region.getFrom() + "y";
                ((DateRangeAggregationBuilder) aggregationBuilder).addRange(from, to);
            }
            searchRequestBuilder.addAggregation(aggregationBuilder);
        }

        SearchResponse searchResponse = searchRequestBuilder.get();

        HouseholdResponse householdResponse = new HouseholdResponse();
        long total = searchResponse.getHits().getTotalHits();
        householdResponse.setTotal(total);

        Terms relationshipGroup = searchResponse.getAggregations().get("relationship_group");
        List<Section> relationshipSections = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            boolean exists = relationshipGroup.getBuckets().size() > i;

            Section section = new Section();
            section.setName(RelationshipType.getValueByKey(i + 1));
            section.setCount(exists ? relationshipGroup.getBuckets().get(i).getDocCount() : 0L);
            section.setProportion((exists ? AmountUtil.roundDownStr(section.getCount() * 100.0D / total)
                    : "0.00") + "%");
            relationshipSections.add(section);
        }
        householdResponse.setRelationshipSections(relationshipSections);

        Filter sexFilter = searchResponse.getAggregations().get("sex");
        Terms sexGroup = sexFilter.getAggregations().get("sex_group");
        List<Section> tenantSections = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            boolean exists = sexGroup.getBuckets().size() > i;

            Section section = new Section();
            section.setCount(exists ? sexGroup.getBuckets().get(i).getDocCount() : 0L);
            section.setName(i == 0 ? "男性" : "女性");
            section.setProportion((exists && total > 0
                    ? AmountUtil.roundDownStr(section.getCount() * 100.0D / total)
                    : "0.00") + "%");
            tenantSections.add(section);
        }
        householdResponse.setSexSections(tenantSections);

        if (regions != null) {
            InternalDateRange ageGroup = searchResponse.getAggregations().get("age_group");
            List<InternalDateRange.Bucket> ageBuckets = ageGroup.getBuckets();
            List<Section> ageSections = new LinkedList<>();
            for (int i = 0; i < ageBuckets.size(); i++) {
                Section section = new Section();
                section.setCount(ageBuckets.get(i).getDocCount());
                section.setName(regions.get(i).getName());
                section.setProportion((total > 0
                        ? AmountUtil.roundDownStr(ageBuckets.get(i).getDocCount() * 100.0D / total)
                        : "0.00") + "%");
                ageSections.add(section);
            }
            Collections.reverse(ageSections);
            householdResponse.setAgeSections(ageSections);
        }

        return householdResponse;
    }

    /**
     * 根据住户名称模糊匹配用户ID列表
     *
     * @param communityId
     * @param userName
     * @return
     */
    @Override
    public Set<ObjectId> listHouseholds(ObjectId communityId, String userName) {
        List<Household> list = householdRepository.findByCommunityIdAndUserNameRegexIgnoreNullAndActivatedAndDataStatus(
                communityId, StringUtil.makeQueryStringAllRegExp(userName), Boolean.TRUE, DataStatusType.VALID.KEY);
        return list.stream().map(Household::getUserId).collect(Collectors.toSet());
    }


    @Override
    public void updateDeviceLicense(ObjectId id, List<Card> cards, boolean operate) {
        Household household = householdRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);

        List<DeviceAuthVO> deviceAuthVOS = createDeviceLicense(household, cards, operate);

        for (DeviceAuthVO authVO : deviceAuthVOS) {
            Message elevatorMessage = new Message(TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH,
                                                  operate ? ADD : DELETE,
                                                  JSON.toJSONString(authVO).getBytes());
            try {
                producer.send(elevatorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("设备权限新增信息发送异常 : ", e);
            }
        }

        household.setDeviceLicense(operate);
        householdRepository.updateById(household, id);
    }

    @Override
    public List<Household> findByRoomIdsAndUserId(Collection<ObjectId> roomIds, ObjectId userId) {
        return householdRepository.findByRoomIdInAndUserIdAndDataStatus(roomIds, userId, DataStatusType.VALID.KEY);
    }

    private List<DeviceAuthVO> createDeviceLicense(Household household, List<Card> cards, boolean operate) {
        Date startDate = new Date();
        Integer processTime = operate ? (int) DateUtils.secondsBetween(startDate, DateUtils.addYear(startDate, 50)) : null;
        return cards.stream().map(c -> {
            DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
            deviceAuthVO.setUserId(household.getUserId());
            deviceAuthVO.setCommunityId(household.getCommunityId());

            BuildingListVO buildingListVO = new BuildingListVO();
            buildingListVO.setBuildingId(household.getBuildingId());
            buildingListVO.setRooms(Collections.singleton(household.getRoomId()));
            Set<BuildingListVO> vos = Collections.singleton(buildingListVO);
            deviceAuthVO.setBuildingList(vos);
            deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
            deviceAuthVO.setKeyNo(c.getKeyNo());
            deviceAuthVO.setKeyId(c.getKeyId());

            deviceAuthVO.setProcessTime(processTime);
            // 使用次数暂时设定为0
            deviceAuthVO.setUsesTime(0);
            deviceAuthVO.setHandleCount(0);
            return deviceAuthVO;
        }).collect(Collectors.toList());
    }

    /**
     * 住户档案插入es
     * @param householdEs
     */
    private void upsertHouseholdToEs(Household householdEs) {
        cn.bit.facade.data.user.Household household = new cn.bit.facade.data.user.Household();
        if(StringUtil.isNotBlank(householdEs.getIdentityCard())){
            IdentityCardUtils.IdentityCardMeta meta = IdentityCardUtils.getIdentityCardMeta(householdEs.getIdentityCard());
            if(meta == null){
                log.info("住户档案身份证异常，不插入es:{}", householdEs);
                return;
            }
            household.setBirthday(meta.getBirthday());
        }
        household.setCommunityId(householdEs.getCommunityId());
        household.setRelationship(householdEs.getRelationship());
        household.setSex(householdEs.getSex());
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, householdEs.getId().toString(), household);
    }

    /**
     * 根据id，移除es记录
     * @param householdIds
     */
    private void removeHouseholdFromEs(Collection<ObjectId> householdIds) {
        householdIds.forEach(id -> esTemplate.deleteAsync(INDEX_NAME, TYPE_NAME, id.toString()));
    }
}
