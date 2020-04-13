package cn.bit.community.service;

import cn.bit.common.facade.community.constant.CodeConstants;
import cn.bit.common.facade.community.dto.CommunityDTO;
import cn.bit.common.facade.community.dto.LevelNoDTO;
import cn.bit.common.facade.community.enums.CodeEnum;
import cn.bit.common.facade.community.query.CommunityPageQuery;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.common.facade.data.Location;
import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.common.facade.exception.InvalidParameterException;
import cn.bit.common.facade.exception.MissingParameterException;
import cn.bit.common.facade.exception.UnknownException;
import cn.bit.community.dao.CommunityRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.vo.community.CommunityKv;
import cn.bit.facade.vo.user.UserCommunityVO;
import cn.bit.framework.constant.CacheConstant;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.redis.RedisTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.CommonBizException.UNKNOWN_ERROR;
import static cn.bit.facade.exception.community.CommunityBizException.*;

@Component("communityFacade")
@Slf4j
public class CommunityFacadeImpl implements CommunityFacade {

    @Resource
    CommunityRepository communityRepository;

    @Resource
    private cn.bit.common.facade.community.service.CommunityFacade commonCommunityFacade;

    @Resource
    private CompanyFacade companyFacade;

    @Override
    public Community addCommunity(Community entity) throws BizException {
        cn.bit.common.facade.community.model.Community community = convert(entity,
                cn.bit.common.facade.community.model.Community.class);
        String[] coordinates = entity.getCoordinate().split(",");
        Location location = new Location(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        community.setLocation(location);
        community.setLogo(entity.getImgUrl());
        community.setCreator(entity.getCreateId());
        //community.setAdCode(Integer.valueOf(entity.getCode()));
        community = commonCommunityFacade.createCommunity(community);

        Community toInsert = new Community();
        toInsert.setId(community.getId());
        toInsert.setMiliCId(entity.getMiliCId());
        toInsert.setYun_community_id(entity.getYun_community_id());
        toInsert.setBroadcastSchema(entity.getBroadcastSchema());
        toInsert.setOutId(entity.getOutId());
        toInsert.setPropertyId(entity.getPropertyId());
        toInsert.setCreateAt(community.getCreateAt());
        toInsert.setUpdateAt(community.getUpdateAt());
        toInsert.setDataStatus(community.getDataStatus());
        toInsert = communityRepository.insert(toInsert);

        // 更新jedis缓存
        editCommunityCache(entity.getId(), entity.getName(), false);

        entity.setId(community.getId());
        entity.setCreateAt(community.getCreateAt());
        entity.setUpdateAt(community.getUpdateAt());
        entity.setNo(community.getNo());
        entity.setOpen(community.getOpen());
        entity.setDataStatus(community.getDataStatus());
        return entity;
    }

    @Override
    public Community updateCommunity(Community entity) throws BizException {
        cn.bit.common.facade.community.model.Community community = convert(entity,
                cn.bit.common.facade.community.model.Community.class);
        if (StringUtils.isNotBlank(entity.getCoordinate())) {
            String[] coordinates = entity.getCoordinate().split(",");
            Location location = new Location(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            community.setLocation(location);
        }
        community.setLogo(entity.getImgUrl());
        try {
            community = commonCommunityFacade.modifyCommunity(community);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_COMMUNITY_NOT_EXIST.equals(e.getSubCode())) {
                throw COMMUNITY_NOT_EXISTS;
            }
            throw e;
        }

        Community toUpdate = new Community();
        toUpdate.setMiliCId(entity.getMiliCId());
        toUpdate.setYun_community_id(entity.getYun_community_id());
        toUpdate.setBroadcastSchema(entity.getBroadcastSchema());
        toUpdate.setOutId(entity.getOutId());
        toUpdate.setPropertyId(entity.getPropertyId());
        toUpdate.setPhotos(entity.getPhotos());
        toUpdate.setUpdateAt(community.getUpdateAt());
        toUpdate.setDataStatus(entity.getDataStatus());

        entity = communityRepository.updateById(toUpdate, entity.getId());
        // 更新jedis缓存
        editCommunityCache(entity.getId(), entity.getName(), false);

        return fillCommunity(entity, community);
    }

    @Override
    public Community openCommunity(ObjectId id, Boolean open) throws BizException {
        if (id == null) {
            throw COMMUNITY_ID_NULL;
        }
        // check propertyId is not null
        Community community = this.findOne(id);
        if(community.getPropertyId() == null){
            throw COMMUNITY_NOT_BIND_PROPERTY;
        }

        open = open == null ? true : open;
        commonCommunityFacade.openCommunityByCommunityId(community.getId(), open);
        community.setOpen(open);

        return community;
    }

    @Override
    public Community findOne(ObjectId id) throws BizException {
        if (id == null) {
            throw COMMUNITY_ID_NULL;
        }

        cn.bit.common.facade.community.model.Community community = commonCommunityFacade.getCommunityByCommunityId(id);
        if(community == null){
            throw COMMUNITY_NOT_EXISTS;
        }
        Community entity = communityRepository.findById(id);

        return fillCommunity(Optional.ofNullable(entity).orElse(new Community()), community);
    }

    @Override
    public List<Community> findByIds(Collection<ObjectId> ids) throws BizException {
        return commonCommunityFacade.listCommunitiesByCommunityIds(ids).stream()
                .map(communityDTO -> fillCommunity(new Community(), communityDTO))
                .collect(Collectors.toList());
    }

    @Override
    public List<Community> queryList(Integer partner, Community entity) throws BizException {
        CommunityPageQuery communityPageQuery = new CommunityPageQuery();
        communityPageQuery.setPartner(partner);
        communityPageQuery.setName(entity.getName());
        communityPageQuery.setOpen(entity.getOpen());
        communityPageQuery.setPage(1);
        communityPageQuery.setSize(1000);
        cn.bit.common.facade.data.Page<CommunityDTO> communityDTOPage =
                commonCommunityFacade.listCommunities(communityPageQuery);
        if (communityDTOPage.getRecords().isEmpty()) {
            return Collections.emptyList();
        }

        List<CommunityDTO> communityDTOs = communityDTOPage.getRecords();
        List<Community> communities = communityRepository.findByIdIn(
                communityDTOs.stream().map(CommunityDTO::getId).collect(Collectors.toList()));
        Map<ObjectId, Community> communityMap = communities.stream().collect(Collectors.toMap(Community::getId,
                community -> community));
        return communityDTOs.stream()
                .map(communityDTO ->
                        fillCommunity(communityMap.getOrDefault(communityDTO.getId(), new Community()), communityDTO))
                .collect(Collectors.toList());
    }

    @Override
    public boolean changeDataStatus(ObjectId id) throws BizException {
        // check delete condition
        this.checkDeleteCommunity(id);

        try {
            commonCommunityFacade.removeCommunityByCommunityId(id);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_COMMUNITY_NOT_EXIST.equals(e.getSubCode())) {
                return false;
            }
        }

        Community toUpdate = new Community();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        communityRepository.updateById(toUpdate, id);
        return true;
    }

    private void checkDeleteCommunity(ObjectId id) {
        cn.bit.common.facade.community.model.Community toGet = commonCommunityFacade.getCommunityByCommunityId(id);
        if(toGet.getOpen()){
            throw COMMUNITY_USED;
        }

        if(toGet.getRoomNum() > 0){
            throw BUILDING_EXIST;
        }
    }

    @Override
    public boolean deleteCommunity(ObjectId id) throws BizException {
        // check delete condition
        this.checkDeleteCommunity(id);
        if (communityRepository.remove(id) > 0) {
            // 删除缓存
            editCommunityCache(id, null, true);
            return true;
        }
        return false;
    }

    @Override
    public Page<UserCommunityVO> queryPage(Integer partner, String name, String code, Boolean open, int page, int size)
            throws BizException {
        CommunityPageQuery communityPageQuery = new CommunityPageQuery();
        communityPageQuery.setPartner(partner);
        communityPageQuery.setName(name);
        communityPageQuery.setOpen(open);
        communityPageQuery.setPage(page);
        communityPageQuery.setSize(size);
        cn.bit.common.facade.data.Page<CommunityDTO> communityDTOPage =
                commonCommunityFacade.listCommunities(communityPageQuery);
        List<UserCommunityVO> communityVOs = communityDTOPage.getRecords().stream()
                .map(communityDTO -> fillCommunity(new UserCommunityVO(), communityDTO))
                .collect(Collectors.toList());
        return new Page<>(communityDTOPage.getCurrentPage(), communityDTOPage.getTotal(), size, communityVOs);
    }

    @Override
    public Page<Community> findCommunitysByPropertyId(ObjectId propertyId, Integer page, Integer size) {
        // 根据物业公司id获取绑定的社区列表
        List<CompanyToCommunity> communities = companyFacade.listCommunitiesByCompanyId(propertyId);
        if(communities.isEmpty()){
            log.info("物业公司({})没有绑定任何社区", propertyId);
            return new Page<>();
        }
        CommunityPageQuery communityPageQuery = new CommunityPageQuery();
        communityPageQuery.setIdsIn(communities.stream().map(CompanyToCommunity::getCommunityId)
                .collect(Collectors.toSet()));
        communityPageQuery.setPage(page);
        communityPageQuery.setSize(size);
        cn.bit.common.facade.data.Page<CommunityDTO> communityDTOPage =
                commonCommunityFacade.listCommunities(communityPageQuery);

        List<Community> communityList = communityDTOPage.getRecords().stream()
                .map(communityDTO -> fillCommunity(new Community(), communityDTO))
                .collect(Collectors.toList());

        return new Page<>(communityDTOPage.getCurrentPage(), communityDTOPage.getTotal(), size, communityList);
    }

    /**
     * 社区缓存的操作
     * @param id
     * @param name
     * @param flag flag为true删除，false新增
     */
    private void editCommunityCache(ObjectId id, String name, boolean flag) {
        try {
            if (flag) {
                RedisTemplateUtil.delMapField(CacheConstant.COMMUNITY_MAP, id.toString());
            }
            RedisTemplateUtil.addMap(CacheConstant.COMMUNITY_MAP, id.toString(), name);
        } catch (Exception e) {
            log.error("Exception:", e);
        }
    }

    /**
     * 更新社区入住人数，入户房间数
     *
     * @param toUpdate
     * @param communityId
     * @return
     */
    @Override
    public int updateWithIncHouseholdCntAndCheckInRoomCntById(Community toUpdate, ObjectId communityId) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }

        toUpdate.setId(communityId);
        toUpdate.setDataStatus(DataStatusEnum.VALID.value());
        toUpdate.setUpdateAt(new Date());
        return communityRepository.upsertWithIncHouseholdCntAndCheckInRoomCntById(toUpdate, communityId);
    }

    /**
     * 查询所有已开放社区列表<id, name>
     * TODO 待处理，不应该直接返回所有的社区列表
     * @return
     */
    @Override
    public List<CommunityKv> findAllWithOpen() {
        return communityRepository.findByDataStatusAndOpenIgnoreNull(
                DataStatusType.VALID.KEY, Boolean.TRUE, CommunityKv.class);
    }

    // TODO 待处理，不应该直接返回所有的社区列表
    @Override
    public List<CommunityKv> getCommunityList() {
        return communityRepository.findByDataStatusAndOpenIgnoreNull(
                DataStatusType.VALID.KEY, null, CommunityKv.class);
    }

    @Override
    public Community findByCode(String code) {
        Community toGet = communityRepository.findByCode(code);
        if(toGet == null){
            return toGet;
        }
        cn.bit.common.facade.community.model.Community community = commonCommunityFacade
                .getCommunityByCommunityId(toGet.getId());
        return fillCommunity(toGet, community);
    }

    @Override
    public List<Community> queryMiliCommunities() {
        return communityRepository.findByMiliCIdIsNotNullAndDataStatus(DataStatusType.VALID.KEY);
    }

    /**
     * 获取层级结构
     *
     * @param level
     * @param target
     * @return
     */
    @Override
    public Map<Integer, Integer> listLevelNos(Integer level, ObjectId target) {
        try {
            List<LevelNoDTO> levelNoDTOList = commonCommunityFacade.listLevelNos(level, target);
            Map<Integer, Integer> levelNoMap = new HashMap<>();
            levelNoDTOList.stream().forEach(levelNoDTO -> levelNoMap.put(levelNoDTO.getLevel(), levelNoDTO.getNo()));
            return levelNoMap;
        }catch (InvalidParameterException e){
            switch (e.getSubCode()){
                case CodeConstants.CODE_UNKNOWN_ERROR :
                    throw UNKNOWN_ERROR;
                default:throw e;
            }
        }catch (MissingParameterException e){
            switch (e.getSubCode()){
                case CodeConstants.CODE_MISSING_LEVEL :
                    throw MISSING_LEVEL;
                case CodeConstants.CODE_MISSING_TARGET :
                    throw MISSING_TARGET;
                default:throw e;
            }
        }catch (cn.bit.common.facade.exception.BizException e){
            switch (e.getSubCode()){
                case CodeConstants.CODE_INVALID_LEVEL :
                    throw INVALID_LEVEL;
                case CodeConstants.CODE_COMMUNITY_HAS_LOCKED :
                    throw COMMUNITY_IS_LOCKED;
                case CodeConstants.CODE_COMMUNITY_NOT_EXIST :
                    throw COMMUNITY_NOT_EXISTS;
                case CodeConstants.CODE_ZONE_NOT_EXIST :
                    throw ZONE_NOT_EXISTS;
                case CodeConstants.CODE_BUILDING_NOT_EXIST :
                    throw BUILDING_NOT_EXISTS;
                case CodeConstants.CODE_ROOM_NOT_EXIST :
                    throw ROOM_NOT_EXISTS;
                default:throw e;
            }
        }
    }

    /**
     * 根据条件分页查询社区
     *
     * @param partner
     * @param name
     * @param province
     * @param city
     * @param open
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<UserCommunityVO> listCommunities(Integer partner, String name, String province, String city,
                                                 Boolean open, Integer page, Integer size) {
        CommunityPageQuery communityPageQuery = new CommunityPageQuery();
        communityPageQuery.setPartner(partner);
        communityPageQuery.setName(name);
        communityPageQuery.setOpen(open);
        communityPageQuery.setProvince(province);
        communityPageQuery.setCity(city);
        communityPageQuery.setPage(page);
        communityPageQuery.setSize(size);
        cn.bit.common.facade.data.Page<CommunityDTO> communityDTOPage =
                commonCommunityFacade.listCommunities(communityPageQuery);
        List<UserCommunityVO> communityVOs = communityDTOPage.getRecords().stream()
                .map(communityDTO -> fillCommunity(new UserCommunityVO(), communityDTO))
                .collect(Collectors.toList());
        return new Page<>(communityDTOPage.getCurrentPage(), communityDTOPage.getTotal(), size, communityVOs);
    }

    /**
     * 保存社区实景图片
     *
     * @param entity
     * @return
     */
    @Override
    public Community saveCommunityPhotos(Community entity) {
        Community toUpdate = new Community();
        toUpdate.setPhotos(entity.getPhotos());
        toUpdate.setUpdateAt(new Date());
        toUpdate = communityRepository.updateById(toUpdate, entity.getId());
        return toUpdate;
    }

    /**
     * addToSet 设备厂商
     *
     * @param id
     * @param brands
     * @return
     */
    @Override
    public Community addToSetBrandsById(ObjectId id, Collection<String> brands) {
        if (id == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (brands == null || brands.isEmpty()) {
            throw DEVICE_BRANDS_NULL;
        }
        Community community = new Community();
        community.setDeviceBrands(new HashSet<>(brands));
        community.setUpdateAt(new Date());
        community.setCreateAt(community.getUpdateAt());
        community.setDataStatus(DataStatusType.VALID.KEY);
        return communityRepository.upsertWithAddToSetDeviceBrandsThenSetOnInsertDataStatusAndCreateAtById(community, id);
    }

    /**
     * pullAll 设备厂商
     *
     * @param id
     * @param brands
     * @return
     */
    @Override
    public Community pullAllBrandsById(ObjectId id, Collection<String> brands) {
        if (id == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (brands == null || brands.isEmpty()) {
            throw DEVICE_BRANDS_NULL;
        }
        Community community = new Community();
        community.setDeviceBrands(new HashSet<String>(brands));
        community.setUpdateAt(new Date());
        return communityRepository.updateWithPullAllDeviceBrandsById(community, id);
    }

    private static <S, T> T convert(S source, Class<T> clazz) {
        if (source == null) {
            return null;
        }

        try {
            T target = clazz.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new UnknownException(CodeEnum.UNKNOWN_ERROR, e);
        }
    }

    private static <T extends Community> T fillCommunity(T community,
                                                         cn.bit.common.facade.community.model.Community data) {
        BeanUtils.copyProperties(data, community);
        if (data.getLocation() != null) {
            community.setCoordinate(new StringBuilder().append(data.getLocation().getLng()).append(",")
                    .append(data.getLocation().getLat()).toString());
        }
        community.setImgUrl(data.getLogo());
        return community;
    }

    private static <T extends Community> T fillCommunity(T community, CommunityDTO data) {
        BeanUtils.copyProperties(data, community);
        if (data.getLocation() != null) {
            community.setCoordinate(new StringBuilder().append(data.getLocation().getLng()).append(",")
                    .append(data.getLocation().getLat()).toString());
        }
        community.setImgUrl(data.getLogo());
        return community;
    }
}
