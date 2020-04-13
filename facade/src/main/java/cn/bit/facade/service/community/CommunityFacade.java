package cn.bit.facade.service.community;

import cn.bit.facade.model.community.Community;
import cn.bit.facade.vo.community.CommunityKv;
import cn.bit.facade.vo.user.UserCommunityVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommunityFacade {

    /**
     * 新增社区
     * @param entity
     * @return
     */
    Community addCommunity(Community entity) throws BizException;

    /**
     * 更新社区
     * @param entity
     * @return
     */
    Community updateCommunity(Community entity) throws BizException;

    /**
     * 修改社区的开放状态
     *
     * @param id
     * @param open
     * @return
     * @throws BizException
     */
    Community openCommunity(ObjectId id, Boolean open) throws BizException;

    /**
     * 按条件查询
     * @param id
     * @return
     */
    Community findOne(ObjectId id) throws BizException;

    List<Community> findByIds(Collection<ObjectId> ids) throws BizException;

    /**
     * 获取社区列表
     * @return
     */
    List<Community> queryList(Integer partner, Community entity) throws BizException;

    /**
     * 修改数据状态
     * @param id
     * @return
     */
    boolean changeDataStatus(ObjectId id) throws BizException;

    /**
     * 删除
     * @param id
     */
    boolean deleteCommunity(ObjectId id) throws BizException;

    /**
     * 分页
     * @param
     * @param page
     * @return
     */
    Page<UserCommunityVO> queryPage(Integer partner, String name, String code, Boolean open, int page, int size) throws BizException;

    Page<Community> findCommunitysByPropertyId(ObjectId propertyId, Integer page, Integer size);

    /**
     * 更新社区入住人数，入户房间数
     * @param toUpdate
     * @param communityId
     * @return
     */
    int updateWithIncHouseholdCntAndCheckInRoomCntById(Community toUpdate, ObjectId communityId);

    /**
     * 查询所有社区列表
     * @return
     */
    List<CommunityKv> findAllWithOpen();

    /**
     * 获取社区的id和名称
     * @return
     */
    List<CommunityKv> getCommunityList();

    /**
     * 根据社区编码获取小区信息
     * @param code
     * @return
     */
    Community findByCode(String code);

    List<Community> queryMiliCommunities();

    /**
     * 获取层级结构
     * @param level
     * @param target
     * @return
     */
    Map<Integer, Integer> listLevelNos(Integer level, ObjectId target);

    /**
     * 根据条件分页查询社区
     * @param partner
     * @param name
     * @param province
     * @param city
     * @param open
     * @param page
     * @param size
     * @return
     */
    Page<UserCommunityVO> listCommunities(Integer partner, String name,
                                          String province, String city, Boolean open, Integer page, Integer size);

    /**
     * 保存社区实景图片
     * @param entity
     * @return
     */
    Community saveCommunityPhotos(Community entity);

    /**
     * addToSet 设备厂商
     * @param id
     * @param brands
     * @return
     */
    Community addToSetBrandsById(ObjectId id, Collection<String> brands);

    /**
     * pullAll 设备厂商
     * @param id
     * @param brands
     * @return
     */
    Community pullAllBrandsById(ObjectId id, Collection<String> brands);
}
