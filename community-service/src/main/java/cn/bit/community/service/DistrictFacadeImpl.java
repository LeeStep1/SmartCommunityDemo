package cn.bit.community.service;

import cn.bit.common.facade.community.model.Building;
import cn.bit.common.facade.community.service.CommunityFacade;
import cn.bit.community.dao.DistrictRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.community.District;
import cn.bit.facade.service.community.DistrictFacade;
import cn.bit.facade.vo.community.DistrictBuildingResponse;
import cn.bit.facade.vo.community.DistrictRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.*;

@Service("districtFacade")
@Slf4j
public class DistrictFacadeImpl implements DistrictFacade {

    @Autowired
    private DistrictRepository districtRepository;

    @Resource
    private CommunityFacade commonCommunityFacade;

    @Override
    public District addDistrict(District district, ObjectId userId) {
        if (StringUtil.isEmpty(district.getName())) {
            throw DISTRICT_NAME_NOT_EXIST;
        }

        int nameCount = districtRepository.countByNameAndCommunityIdAndDataStatus(
                district.getName(), district.getCommunityId(), DataStatusType.VALID.KEY);
        if (nameCount > 0) {
            throw DISTRICT_NAME_REPETITION;
        }

        if (CollectionUtils.isEmpty(district.getBuildingIds())) {
            throw DISTRICT_BUILDING_EMPTY;
        }

        if (Objects.isNull(district.getCommunityId())) {
            throw DISTRICT_COMMUNITY_EMPTY;
        }

        if (Objects.isNull(district.getName())) {
            throw DISTRICT_NAME_NULL;
        }

        Set<ObjectId> buildingIds = this.findAuthBuildingIdByCommunityId(district.getCommunityId());
        // 获取申请的职能区域楼栋是否有重复
        if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(buildingIds, district.getBuildingIds()))) {
            throw DISTRICT_BUILDING_REPEAT;
        }

        district.setCreateId(userId);
        district.setCreateAt(new Date());
        district.setDataStatus(DataStatusType.VALID.KEY);
        district.setOpen(Boolean.FALSE);
        return districtRepository.insert(district);
    }

    @Override
    public boolean removeDistrict(ObjectId id) {
        District district = districtRepository.findById(id);
        if (district == null || district.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DISTRICT_NOT_EXIST;
        }
        District toUpdate = new District();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate.setUpdateAt(new Date());
        toUpdate.setOpen(Boolean.FALSE);
        return districtRepository.updateById(toUpdate, id) != null;
    }

    @Override
    public List<DistrictBuildingResponse> findAvailableBuilding(DistrictRequest districtRequest) {
        List<DistrictBuildingResponse> districtBuildingResponseList = new ArrayList<>();
        List<Building> buildingList = commonCommunityFacade.listBuildingsByCommunityId(districtRequest.getCommunityId());

        Set<ObjectId> buildingIds = buildingList.stream().map(Building::getId).collect(Collectors.toSet());
        List<District> districtList = this.findAllByCommunityId(districtRequest.getCommunityId());

        for (District d : districtList) {
            buildingIds.removeAll(d.getBuildingIds());
        }

        District district = null;
        if (Objects.nonNull(districtRequest.getDistrictId())) {
            district = districtRepository.findOne(districtRequest.getDistrictId());
        }

        for (Building building : buildingList) {
            // 已选楼栋
            if (Objects.nonNull(district) && district.getBuildingIds().contains(building.getId())) {
                buildDistrictBuildingByType(districtBuildingResponseList, building, 1);
                continue;
            }

            // 可选楼栋
            if (buildingIds.contains(building.getId())) {
                buildDistrictBuildingByType(districtBuildingResponseList, building, 0);
            }
        }

        return districtBuildingResponseList;
    }

    @Override
    public District findById(ObjectId id) {
        District district = new District();
        district.setId(id);
        district.setDataStatus(DataStatusType.VALID.KEY);
        return districtRepository.findOne(district);
    }

    @Override
    public List<District> findAllByCommunityId(ObjectId communityId) {
        return districtRepository.findByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<District> queryPage(District entity, Integer page, Integer size) {
        if (!StringUtil.isNotNull(entity.getCommunityId())) {
            throw COMMUNITY_ID_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.ASC, "createAt"));
        org.springframework.data.domain.Page<District> districtPage = districtRepository.findByCommunityIdAndDataStatus(
                entity.getCommunityId(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(districtPage);
    }

    @Override
    public List<District> findInIds(Collection<ObjectId> districts) {
        return districtRepository.findByIdInAndDataStatus(districts, DataStatusType.VALID.KEY);
    }

    @Override
    public District updateDistrict(District entity) {
        if (StringUtil.isEmpty(entity.getName())) {
            throw DISTRICT_NAME_NOT_EXIST;
        }

        District district = districtRepository.findById(entity.getId());

        if (district == null || district.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DISTRICT_NOT_EXIST;
        }

        if (!StringUtil.equals(entity.getName(), district.getName())) {
            List<District> getDistrictName = districtRepository.findByNameAndCommunityIdAndDataStatus(
                    entity.getName(), entity.getCommunityId(), DataStatusType.VALID.KEY);
            for (District districtName : getDistrictName) {
                // 职能区域的名称跟其他区域重复
                if (!districtName.getId().equals(entity.getId())) {
                    throw DISTRICT_NAME_REPETITION;
                }
            }
        }

        if (Boolean.TRUE.equals(district.getOpen())) {
            throw DISTRICT_OPEN;
        }

        Set<ObjectId> buildingIds = this.findAuthBuildingIdByCommunityId(district.getCommunityId());
        Collection subtract = CollectionUtils.subtract(buildingIds, district.getBuildingIds());
        district.setBuildingIds(entity.getBuildingIds());
        // 获取申请的职能区域楼栋是否有重复
        if (CollectionUtils.isNotEmpty(CollectionUtils.intersection(subtract, district.getBuildingIds()))) {
            throw DISTRICT_BUILDING_REPEAT;
        }

        district.setName(entity.getName());
        district.setUpdateAt(new Date());
        district = districtRepository.updateByIdAndDataStatus(district, district.getId(), DataStatusType.VALID.KEY);
        return district;
    }

    @Override
    public District openDistrict(ObjectId id) {
        District district = districtRepository.findById(id);

        if (district == null || district.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DISTRICT_NOT_EXIST;
        }

        if (Boolean.TRUE.equals(district.getOpen())) {
            throw DISTRICT_OPEN;
        }
        district.setId(null);
        district.setOpen(Boolean.TRUE);
        district.setUpdateAt(new Date());
        return districtRepository.updateById(district, id);
    }

    @Override
    public District addThirdPartInfo(District entity) {
        District toGet = districtRepository.findById(entity.getId());
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw DISTRICT_NOT_EXIST;
        }
        entity.setUpdateAt(new Date());
        entity.setId(null);
        return districtRepository.updateById(entity, toGet.getId());
    }

    @Override
    public District findByBuildingIdsIn(Collection<ObjectId> buildingIds) {
        return districtRepository.findByBuildingIdsAndDataStatus(buildingIds, DataStatusType.VALID.KEY);
    }

    private Set<ObjectId> findAuthBuildingIdByCommunityId(ObjectId communityId) {
        List<District> districtList = this.findAllByCommunityId(communityId);
        return districtList.stream().flatMap(district -> district.getBuildingIds().stream())
                .collect(Collectors.toSet());
    }


    private void buildDistrictBuildingByType(List<DistrictBuildingResponse> districtBuildingResponseList,
                                             Building building, int availableType) {
        DistrictBuildingResponse districtBuildingResponse = new DistrictBuildingResponse();
        districtBuildingResponse.setAvailableType(availableType);
        districtBuildingResponse.setBuildingId(building.getId());
        districtBuildingResponse.setBuildingName(building.getName());
        districtBuildingResponseList.add(districtBuildingResponse);
    }
}
