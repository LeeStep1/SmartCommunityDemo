package cn.bit.community.service;

import cn.bit.common.facade.community.constant.CodeConstants;
import cn.bit.common.facade.community.enums.CodeEnum;
import cn.bit.common.facade.community.query.BuildingPageQuery;
import cn.bit.common.facade.community.service.CommunityFacade;
import cn.bit.common.facade.data.Location;
import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.common.facade.exception.UnknownException;
import cn.bit.community.dao.BuildingRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.vo.community.UpdateDoorElevatorLinkage;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.*;

@Component("buildingFacade")
@Slf4j
public class BuildingFacadeImpl implements BuildingFacade {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    CommunityFacade commonCommunityFacade;

    private static final String FIND_FLOOR_MAP = "/build/floors/detail";

    @Override
    public Building addBuilding(Building entity) throws BizException {
        if(entity.getCommunityId() == null){
            throw COMMUNITY_ID_NULL;
        }
        if(!StringUtil.isNotNull(entity.getName())){
            throw NAME_IS_NULL;
        }
        if(entity.getOverGround() == null){
            throw OVERGROUND_IS_NULL;
        }
        if(entity.getUnderGround() == null){
            throw UNDERGROUND_IS_NULL;
        }
        if(entity.getOverGround() < 0 || (entity.getUnderGround() == 0 && entity.getOverGround() == 0) || entity.getUnderGround() < 0){//无效楼层数
            throw FLOOR_NUM_INVALID;
        }
        if(entity.getRoomNum() == null || entity.getRoomNum() <= 0){
            throw ROOMNUM_INVALID;
        }

        cn.bit.common.facade.community.model.Building building = convert(entity,
                cn.bit.common.facade.community.model.Building.class);
        String[] coordinates = entity.getCoordinate().split(",");
        Location location = new Location(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        building.setLocations(Collections.singletonList(location));
        try {
            building = commonCommunityFacade.createBuilding(building);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_SAME_NAME_BUILDING_UNDER_SAME_PARENT_EXIST.equals(e.getSubCode())) {
                throw NAME_EXIST;
            }
            throw e;
        }

        Building toInsert = new Building();
        toInsert.setId(building.getId());
        toInsert.setMiliBId(entity.getMiliBId());
        toInsert.setOutId(entity.getOutId());
        toInsert.setDataStatus(building.getDataStatus());

        toInsert = buildingRepository.insert(toInsert);

        entity.setId(building.getId());
        entity.setNo(building.getNo());
        entity.setOpen(building.getOpen());
        entity.setCreateAt(building.getCreateAt());
        entity.setUpdateAt(building.getUpdateAt());
        entity.setDataStatus(building.getDataStatus());
        return entity;
    }

    @Override
    public boolean addBuildings(List<Building> buildings) throws BizException {
        try {
            List<cn.bit.common.facade.community.model.Building> buildingList = buildings.stream()
                    .map(building -> {
                        cn.bit.common.facade.community.model.Building b = convert(building,
                                cn.bit.common.facade.community.model.Building.class);
                        String[] coordinates = building.getCoordinate().split(",");
                        Location location = new Location(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                        b.setLocations(Collections.singletonList(location));
                        return b;
                    })
                    .collect(Collectors.toList());
            commonCommunityFacade.createBuildings(buildingList);

            buildings = buildings.stream()
                    .map(building -> {
                        Building b = new Building();
                        b.setId(building.getId());
                        b.setMiliBId(building.getMiliBId());
                        b.setOutId(building.getOutId());
                        b.setDataStatus(building.getDataStatus());
                        return b;
                    })
                    .collect(Collectors.toList());
            buildingRepository.insertAll(buildings);
            return true;
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        return false;
    }

    @Override
    public Building updateBuilding(Building entity) throws BizException {
        if(entity.getId() == null){
            throw BUILDING_ID_NULL;
        }
        Building item = this.checkBuildingStatus(entity.getId());
        if(entity.getRoomNum() != null && entity.getRoomNum() <= 0){
            throw ROOMNUM_INVALID;
        }

        cn.bit.common.facade.community.model.Building building = convert(entity,
                cn.bit.common.facade.community.model.Building.class);
        String[] coordinates = entity.getCoordinate().split(",");
        Location location = new Location(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        building.setLocations(Collections.singletonList(location));
        try {
            building = commonCommunityFacade.modifyBuilding(building);
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_BUILDING_NOT_EXIST:
                    throw BUILDING_NOT_EXISTS;
                case CodeConstants.CODE_SAME_NAME_BUILDING_UNDER_SAME_PARENT_EXIST:
                    throw NAME_EXIST;
                default:
                    throw e;
            }
        }

        Building toUpdate = new Building();
        toUpdate.setMiliBId(entity.getMiliBId());
        toUpdate.setOutId(entity.getOutId());
        toUpdate.setDataStatus(entity.getDataStatus());
        toUpdate = buildingRepository.updateById(toUpdate, item.getId());

        BeanUtils.copyProperties(building, entity);
        entity.setMiliBId(toUpdate.getMiliBId());
        entity.setOutId(toUpdate.getOutId());
        return entity;
    }

    @Override
    public List<Building> queryList(Building entity) throws BizException {
        BuildingPageQuery pageQuery = new BuildingPageQuery();
        pageQuery.setCommunityId(entity.getCommunityId());
        pageQuery.setName(entity.getName());
        pageQuery.setOpen(entity.getOpen());
        pageQuery.setPage(1);
        pageQuery.setSize(1000);
        return commonCommunityFacade.listBuildings(pageQuery).getRecords()
                .stream()
                .map(building -> convert(building, Building.class))
                .collect(Collectors.toList());
    }

    @Override
    public Building findOne(ObjectId id) throws BizException {
        if (id == null) {
            throw BUILDING_ID_NULL;
        }

        cn.bit.common.facade.community.model.Building comBuilding = commonCommunityFacade.getBuildingByBuildingId(id);
        if (comBuilding == null) {
            return null;
        }

        Building building = convert(comBuilding, Building.class);
        Building addition = buildingRepository.findByIdAndDataStatus(id, DataStatusEnum.VALID.value());
        if (addition == null) {
            return building;
        }

        building.setOutId(addition.getOutId());
        building.setMiliBId(addition.getMiliBId());
        if (comBuilding.getLocations() != null && !comBuilding.getLocations().isEmpty()) {
            building.setCoordinate(new StringBuilder().append(comBuilding.getLocations().get(0).getLng()).append(",")
                    .append(comBuilding.getLocations().get(0).getLat()).toString());
        }
        //门梯联动字段
        building.setDoorElevatorLinkage(addition.getDoorElevatorLinkage());
        return building;
    }

    @Override
    public List<Building> findByIds(Collection<ObjectId> ids) {
        return commonCommunityFacade.listBuildingsByBuildingIds(ids)
                .stream()
                .map(building -> convert(building, Building.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteBuilding(ObjectId id) throws BizException {
        this.checkBuildingStatus(id);
        try {
            commonCommunityFacade.removeBuildingByBuildingId(id);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_BUILDING_NOT_EXIST.equals(e.getSubCode())) {
                return false;
            }
        }
        return true;
    }


    @Override
    public Page<Building> queryPage(Building entity, int page, int size) throws BizException {
        // 小区ID
        if (entity.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        BuildingPageQuery buildingPageQuery = new BuildingPageQuery();
        buildingPageQuery.setCommunityId(entity.getCommunityId());
        buildingPageQuery.setName(entity.getName());
        buildingPageQuery.setPage(page);
        buildingPageQuery.setSize(size);
        buildingPageQuery.setOpen(entity.getOpen());

        cn.bit.common.facade.data.Page<cn.bit.common.facade.community.model.Building> buildingPage =
                commonCommunityFacade.listBuildings(buildingPageQuery);
        List<Building> buildings = buildingPage.getRecords().stream()
                .map(building -> convert(building, Building.class))
                .collect(Collectors.toList());

        return new Page<>(buildingPage.getCurrentPage(), buildingPage.getTotal(), size, buildings);
    }

    @Override
    public boolean changeDataStatus(ObjectId id) throws BizException {
        this.checkBuildingStatus(id);
        Building toUpdate = new Building();

        try {
            commonCommunityFacade.removeBuildingByBuildingId(id);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_BUILDING_NOT_EXIST.equals(e.getSubCode())) {
                return false;
            }
        }

        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        buildingRepository.updateById(toUpdate, id);
        return true;
    }

    private Building checkBuildingStatus(ObjectId id) {
        cn.bit.common.facade.community.model.Building building = commonCommunityFacade.getBuildingByBuildingId(id);
        if(building == null || building.getDataStatus() == DataStatusType.INVALID.KEY){
            throw BUILDING_NOT_EXISTS;
        }
        if(building.getOpen() != null && building.getOpen()){
            throw BUILDING_USED;
        }
//        Community community = communityFacade.findOne(toGet.getCommunityId());
//        if(community.getOpen() != null && community.getOpen()){
//            throw COMMUNITY_USED;
//        }
//        Long roomNum = roomFacade.countRoomByBuildingId(id);
//        if(roomNum > 0 ){
//            throw ROOM_EXIST;
//        }
        return convert(building, Building.class);
    }

    @Override
    public List<ObjectId> getBuildingIdsByCommunityId(ObjectId communityId) {
        List<cn.bit.common.facade.community.model.Building> buildings =
                commonCommunityFacade.listBuildingsByCommunityId(communityId);
        if(buildings == null || buildings.size() == 0){
            throw new BizException("没有对应的楼栋");
        }

        return buildings.stream().filter(building -> building.getOpen() != null && building.getOpen())
                .map(cn.bit.common.facade.community.model.Building::getId).collect(Collectors.toList());
    }

    /**
     * 修改楼宇开放状态
     *
     * @param id
     * @param open
     * @return
     */
    @Override
    public boolean openBuilding(ObjectId id, Boolean open) {
        if(id == null){
            throw BUILDING_ID_NULL;
        }

        try {
            commonCommunityFacade.openBuildingByBuildingId(id, open);
        } catch (cn.bit.common.facade.exception.BizException e) {
            if (CodeConstants.CODE_BUILDING_NOT_EXIST.equals(e.getSubCode())) {
                throw BUILDING_NOT_EXISTS;
            }
        }

        return true;
    }

    /**
     * 根据社区ID查询已开放的楼栋列表
     *
     * @param communityId
     * @return
     */
    @Override
    public List<Building> findByCommunityIdAndOpen(ObjectId communityId) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }
        return commonCommunityFacade.listBuildingsByCommunityId(communityId)
                .stream()
                .filter(building -> building != null && building.getOpen() != null && building.getOpen())
                .map(building -> convert(building, Building.class))
                .collect(Collectors.toList());
    }

    /**
     * 分页查询楼栋列表
     *
     * @param communityId
     * @param open
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Building> findPageByCommunityIdAndOpen(ObjectId communityId, Boolean open, Integer page, Integer size) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }

        BuildingPageQuery pageQuery = new BuildingPageQuery();
        pageQuery.setCommunityId(communityId);
        pageQuery.setOpen(open);
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        cn.bit.common.facade.data.Page<cn.bit.common.facade.community.model.Building> buildingPage =
                commonCommunityFacade.listBuildings(pageQuery);
        List<Building> buildings = buildingPage.getRecords().stream()
                .map(building -> convert(building, Building.class))
                .collect(Collectors.toList());

        return new Page<>(buildingPage.getCurrentPage(), buildingPage.getTotal(), size, buildings);
    }

    @Override
    public Building findByOutId(String outId) {
        Building building = buildingRepository.findByOutIdAndDataStatus(outId, DataStatusType.VALID.KEY);
        if (building == null) {
            return null;
        }

        cn.bit.common.facade.community.model.Building comBuildings = commonCommunityFacade.getBuildingByBuildingId(
                building.getId());

        Building result = convert(comBuildings, Building.class);
        result.setOutId(building.getOutId());
        result.setMiliBId(building.getMiliBId());

        return result;
    }

    @Override
    public void insertAllBuildings(List<Building> toAddBuildings, ObjectId createId) {
        toAddBuildings.forEach(building -> {
            building.setDataStatus(DataStatusType.VALID.KEY);
            building.setCreateAt(new Date());
            building.setUpdateAt(new Date());
            building.setCreateId(createId);
        });
        buildingRepository.insertAll(toAddBuildings);
    }

    @Override
    public List<Building> queryMiliBuildings(List<ObjectId> communityIds) {
        List<Building> buildings = buildingRepository.findAllByCommunityIdInAndMiliBIdIsNotNull(communityIds);
        if (CollectionUtils.isEmpty(buildings)) {
            return buildings;
        }
        Map<ObjectId, Building> buildingMap = buildings.stream()
                .collect(Collectors.toMap(Building::getId, building -> building));
        List<cn.bit.common.facade.community.model.Building> comBuildings =
                commonCommunityFacade.listBuildingsByBuildingIds(buildingMap.keySet());
        return comBuildings.stream()
                .map(building -> {
                    Building result = convert(building, Building.class);
                    if (buildingMap.containsKey(building.getId())) {
                        result.setMiliBId(buildingMap.get(building.getId()).getMiliBId());
                        result.setOutId(buildingMap.get(building.getId()).getOutId());
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Building> queryDoorElevatorLinkageInfo(ObjectId communityId) {
        List<Building> commonBuildings = commonCommunityFacade.listBuildingsByCommunityId(communityId)
                                                        .stream()
                                                        .filter(building -> building != null
                                                                            && building.getOpen() != null
                                                                            && building.getOpen())
                                                        .map(building -> convert(building, Building.class))
                                                        .collect(Collectors.toList());;
        if (commonBuildings.size() == 0) {
            throw BUILDING_NOT_EXISTS;
        }

        Map<ObjectId, Building> buildingsWithDeviceInfo = buildingRepository.findByIdInAndDataStatus(commonBuildings.stream()
                                                                                                                   .map(Building::getId)
                                                                                                                   .collect(Collectors.toList()),
                                                                                                    DataStatusType.VALID.KEY)
                                                                           .stream()
                                                                           .collect(Collectors.toMap(Building::getId, b -> b));

        for (Building building : commonBuildings) {
            Building info = buildingsWithDeviceInfo.get(building.getId());
            if (null == info) {
                continue;
            }
            building.setDoorElevatorLinkage(info.getDoorElevatorLinkage());
        }

        return commonBuildings;
    }

    @Override
    public void updateBuildingLinkage(UpdateDoorElevatorLinkage linkage) {
        // 开启联动的楼栋列表
        List<ObjectId> enableLinkageBuildingIds = linkage.getAuths()
                                                         .stream()
                                                         .filter(UpdateDoorElevatorLinkage.LinkageMap::getDoorElevatorLinkage)
                                                         .map(UpdateDoorElevatorLinkage.LinkageMap::getBuildingId)
                                                         .collect(Collectors.toList());

        Map<ObjectId, Building> existBuildingMap = buildingRepository.findByIdInAndDataStatus(enableLinkageBuildingIds,
                                                                                           DataStatusType.VALID.KEY)
                                                                     .stream()
                                                                     .collect(Collectors.toMap(Building::getId, b->b));


        Building toUpdate = new Building();
        if (CollectionUtils.isNotEmpty(enableLinkageBuildingIds)) {
            // 数据库存在的数据
            List<ObjectId> updateBuildingIdList = enableLinkageBuildingIds.stream()
                                                                          .filter(existBuildingMap::containsKey)
                                                                          .collect(Collectors.toList());

            toUpdate.setDoorElevatorLinkage(Boolean.TRUE);
            buildingRepository.updateByIdIn(toUpdate, updateBuildingIdList);

            // 不存在的数据新建
            insertNotExistLinkageBuildingsByIds(enableLinkageBuildingIds, existBuildingMap, linkage.getCommunityId());
        }

        // 取消联动的楼栋列表
        List<ObjectId> disableLinkageBuildingIds = linkage.getAuths()
                                                          .stream()
                                                          .filter(l -> !l.getDoorElevatorLinkage())
                                                          .map(UpdateDoorElevatorLinkage.LinkageMap::getBuildingId)
                                                          .collect(Collectors.toList());
        existBuildingMap = buildingRepository.findByIdInAndDataStatus(disableLinkageBuildingIds,
                                                                                              DataStatusType.VALID.KEY)
                                                                     .stream()
                                                                     .collect(Collectors.toMap(Building::getId, b->b));
        if (CollectionUtils.isNotEmpty(disableLinkageBuildingIds)) {
            List<ObjectId> updateBuildingIdList = disableLinkageBuildingIds.stream()
                                                                           .filter(existBuildingMap::containsKey)
                                                                           .collect(Collectors.toList());
            toUpdate.setDoorElevatorLinkage(Boolean.FALSE);
            buildingRepository.updateByIdIn(toUpdate, updateBuildingIdList);

            // 不存在的数据新建
            insertNotExistLinkageBuildingsByIds(disableLinkageBuildingIds, existBuildingMap, linkage.getCommunityId());
        }
    }

    /**
     * 批量插入不存在的门梯联动数据
     * @param ids
     * @param existBuildingMap
     * @param communityId
     */
    private void insertNotExistLinkageBuildingsByIds(List<ObjectId> ids,
                                                     Map<ObjectId, Building> existBuildingMap,
                                                     ObjectId communityId) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        List<Building> newBuildingList = ids.stream()
                                            .filter(id -> !existBuildingMap.containsKey(id))
                                            .map(id -> {
                                                Building newBuilding = new Building();
                                                newBuilding.setId(id);
                                                newBuilding.setCommunityId(communityId);
                                                newBuilding.setDataStatus(DataStatusType.VALID.KEY);
                                                newBuilding.setDoorElevatorLinkage(Boolean.TRUE);
                                                return newBuilding;
                                            })
                                            .collect(Collectors.toList());
        buildingRepository.insertAll(newBuildingList);
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
}
