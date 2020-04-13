package cn.bit.facade.service.user;

import cn.bit.facade.model.property.Registration;
import cn.bit.facade.model.user.UserDevice;
import cn.bit.facade.vo.statistics.StatisticsVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.Allocation;
import cn.bit.facade.vo.user.userToProperty.EmployeeRequest;
import cn.bit.facade.vo.user.userToProperty.EmployeeVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserToPropertyFacade {
    /**
     * 删除物业员工
     *
     * @param userId
     * @param communityId
     * @return
     */
    @CacheEvict(cacheNames = "cacheManager", key = "T(cn.bit.framework.constant.CacheConstant).USER_TO_PROPERTY_PREFIX+#userId.toString()")
    boolean deleteEmployee(Integer partner, ObjectId userId, ObjectId communityId) throws BizException;

    /**
     * 根据ID查询
     *
     * @param id
     * @param communityId
     * @return
     */
    UserToProperty findByIdAndCommunityId(ObjectId id, ObjectId communityId);

    /**
     * 根据社区、职业查询物业人员列表
     *
     * @param request
     * @return
     */
    List<UserToProperty> listEmployees(EmployeeRequest request);

    /**
     * 根据用户、社区、岗位查询物业人员信息
     *
     * @param userId
     * @param communityId
     * @param companyId
     * @return
     */
    UserToProperty findByUserIdAndCommunityIdAndCompanyId(ObjectId userId, ObjectId communityId, ObjectId companyId);

    /**
     * 修改员工信息
     *
     * @param userToProperty
     * @return
     */
    UserToProperty updateDistrictForProperty(UserToProperty userToProperty);

    /**
     * 将米立生成的id保存到关系表中
     *
     * @param user
     */
    boolean updateMiliUIds(UserToProperty user);

    /**
     * 修改物业人员的职能区域范围
     *
     * @param userToProperty
     * @param delDistrictIds
     * @return
     */
    UserToProperty updateUserToPropertyDistrictRange(UserToProperty userToProperty, Set<ObjectId> delDistrictIds);

    /**
     * 分页查询物业员工列表
     *
     * @param employeeVO
     * @param page
     * @param size
     * @return
     */
    Page<UserToProperty> findPageByCommunityIdAndUserToProperty(EmployeeVO employeeVO, Integer partner, Integer page, Integer size);

    /**
     * 物业人员根据预留信息注册
     *
     * @param partner
     * @param platform
     * @param appType
     * @param appId
     * @param userToProperty
     * @param password
     * @param code
     * @param pushId
     * @param userDevice
     * @param registrationList
     * @return
     */
    UserVO registerClientUserAndCMUserWithRegistration(
            Integer partner, Integer platform, Integer appType, ObjectId appId,
            UserToProperty userToProperty, String password, String code, String pushId,
            UserDevice userDevice, List<Registration> registrationList);

    /**
     * 新增物业员工
     *
     * @param employeeVO
     * @param userId
     * @return
     */
    UserToProperty addEmployee(EmployeeVO employeeVO, ObjectId userId);

    /**
     * 添加物业员工并注册
     *
     * @param userToProperty
     * @return
     */
    void upsertClientUserAndCMUserForCreatePropertyUser(Integer partner, UserToProperty userToProperty);

    /**
     * 分派物业人员
     *
     * @param allocation
     * @return
     */
    UserToProperty allocationEmployee(Allocation allocation);

    /**
     * 分派物业人员并注册端用户及社区用户
     *
     * @param userToProperty
     * @return
     */
    void upsertClientUserAndCMUserForAllocation(Integer partner, UserToProperty userToProperty);

    /**
     * 更新物业人员资料
     *
     * @param employeeVO
     * @return
     */
    UserToProperty modifyEmployee(EmployeeVO employeeVO);

    /**
     * 增量查询企业员工列表
     *
     * @param communityId
     * @param keyword
     * @param id
     * @param direction
     * @param size
     * @return
     */
    List<UserToProperty> incrementEmployees(ObjectId communityId, String keyword, ObjectId id, Integer direction, Integer size);

    /**
     * 根据userIds查询社区对应岗位的员工
     *
     * @param statisticsVO
     * @return
     */
    List<UserToProperty> findByStatisticsVO(StatisticsVO statisticsVO);

    /**
     * 注销企业员工
     *
     * @param userId
     * @param companyId
     * @param communityId
     */
    void deleteCompanyEmployee(Integer partner, ObjectId userId, ObjectId companyId, ObjectId communityId);

    /**
     * 企业解绑社区，需要注销社区下的所有物业人员
     *
     * @param companyId
     * @param communityId
     */
    void unbindCompany(Integer partner, ObjectId companyId, ObjectId communityId);

    /**
     * 移除员工在某个社区的某个岗位
     *
     * @param userToProperty
     */
    void removeClientUserAndCMUser(Integer partner, UserToProperty userToProperty);

    /**
     * 根据手机号查询企业员工
     *
     * @param partner
     * @param phone
     * @return
     */
    UserToProperty checkEmployee(Integer partner, String phone);

    /**
     * 查询多岗位社区员工列表
     *
     * @param communityId
     * @param companyId
     * @param roles
     * @return
     */
    List<UserToProperty> listEmployeesByCommunityIdAndCompanyIdAndRoles(ObjectId communityId,
                                                                        ObjectId companyId, Collection<String> roles);
}
