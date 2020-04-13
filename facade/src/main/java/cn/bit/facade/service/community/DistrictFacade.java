package cn.bit.facade.service.community;

import cn.bit.facade.model.community.District;
import cn.bit.facade.vo.community.DistrictBuildingResponse;
import cn.bit.facade.vo.community.DistrictRequest;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;

public interface DistrictFacade {
    /**
     * 新增职能区域
     * @param district
     * @return
     */
    District addDistrict(District district, ObjectId userId);

    /**
     * 删除职能区域
     * @param id
     * @return
     */
    boolean removeDistrict(ObjectId id);

    /**
     * 获取当前社区已经授权的楼栋列表
     * @param districtRequest
     * @return
     */
    List<DistrictBuildingResponse> findAvailableBuilding(DistrictRequest districtRequest);

    /**
     * 根据id查找职能区域
     * @param id
     * @return
     */
    District findById(ObjectId id);

    /**
     * 根据社区查找职能区域
     * @param communityId
     * @return
     */
    List<District> findAllByCommunityId(ObjectId communityId);

    /**
     * 根据社区分页查找职能区域
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<District> queryPage(District entity, Integer page, Integer size);

    /**
     *
     * @param districts
     * @return
     */
    List<District> findInIds(Collection<ObjectId> districts);

    /**
     * 修改职能区域
     * @param district
     * @return
     */
    District updateDistrict(District district);

    /**
     * 开放职能区域
     * @param id
     * @return
     */
    District openDistrict(ObjectId id);


    /**
     *
     * @param entity
     * @return
     */
    District addThirdPartInfo(District entity);

    District findByBuildingIdsIn(Collection<ObjectId> buildingIds);
}
