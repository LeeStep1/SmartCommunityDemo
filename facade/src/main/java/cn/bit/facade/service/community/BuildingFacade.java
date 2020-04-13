package cn.bit.facade.service.community;

import cn.bit.facade.model.community.Building;
import cn.bit.facade.vo.community.UpdateDoorElevatorLinkage;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BuildingFacade {

    /**
     * 新增楼宇
     * @param entity
     * @return
     */
    Building addBuilding(Building entity) throws BizException;

    /**
     * 批量新增楼宇
     * @param buildings
     * @throws BizException
     */
    boolean addBuildings(List<Building> buildings) throws BizException;

    /**
     * 更新楼宇
     * @param entity
     * @return
     */
    Building updateBuilding(Building entity) throws BizException;

    /**
     * 获取社区楼宇
     * @param entity
     * @return
     */
    List<Building> queryList(Building entity) throws BizException;

    /**
     * 根据ID获取楼宇信息
     * @param id
     * @return
     */
    Building findOne(ObjectId id);

    List<Building> findByIds(Collection<ObjectId> ids);

    /**
     * 删除
     * @param id
     */
    boolean deleteBuilding(ObjectId id) throws BizException;

    /**
     * 分页
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<Building> queryPage(Building entity, int page, int size) throws BizException;

    /**
     * 修改数据状态
     * @param id
     * @return
     */
    boolean changeDataStatus(ObjectId id) throws BizException;

    /**
     * 根据社区id获取楼宇id
     * @param communityId 
     */
    List<ObjectId> getBuildingIdsByCommunityId(ObjectId communityId);

    /**
     * 修改楼宇开放状态
     * @param id
     * @param open
     * @return
     */
    boolean openBuilding(ObjectId id, Boolean open);

    /**
     * 根据社区ID查询已开放的楼栋列表
     * @param communityId
     * @return
     */
    List<Building> findByCommunityIdAndOpen(ObjectId communityId);

    /**
     * 分页查询楼栋列表
     * @param communityId
     * @param open
     * @param page
     * @param size
     * @return
     */
    Page<Building> findPageByCommunityIdAndOpen(ObjectId communityId, Boolean open, Integer page, Integer size);

    /**
     * 根据编号获取楼栋信息
     * @param outId
     * @return
     */
    Building findByOutId(String outId);

    void insertAllBuildings(List<Building> toAddBuildings, ObjectId createId);

    List<Building> queryMiliBuildings(List<ObjectId> communityIds);

    /**
     * 根据社区查询楼栋门梯联动
     * @param communityId
     * @return
     */
    List<Building> queryDoorElevatorLinkageInfo(ObjectId communityId);

    /**
     * 更新楼栋门梯联动信息
     * @param toUpdate
     */
    void updateBuildingLinkage(UpdateDoorElevatorLinkage toUpdate);
}
