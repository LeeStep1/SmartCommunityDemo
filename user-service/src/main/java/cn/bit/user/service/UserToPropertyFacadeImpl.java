package cn.bit.user.service;

import cn.bit.common.facade.company.constant.CodeConstants;
import cn.bit.common.facade.company.dto.EmployeeDTO;
import cn.bit.common.facade.company.dto.EmployeeRegisterDTO;
import cn.bit.common.facade.company.enums.CompanyTypeEnum;
import cn.bit.common.facade.company.model.Company;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.model.Employee;
import cn.bit.common.facade.company.query.EmployeeIncrementalQuery;
import cn.bit.common.facade.company.query.EmployeePageQuery;
import cn.bit.common.facade.company.query.EmployeeQuery;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.common.facade.data.XSort;
import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.common.facade.exception.InvalidParameterException;
import cn.bit.common.facade.user.dto.ClientAndPartnerAndUserIdsDTO;
import cn.bit.common.facade.user.dto.ProfileDTO;
import cn.bit.common.facade.user.dto.RegistrationDTO;
import cn.bit.common.facade.user.dto.UserDTO;
import cn.bit.common.facade.user.service.UserFacade;
import cn.bit.facade.constant.RoleConstants;
import cn.bit.facade.enums.*;
import cn.bit.facade.exception.community.CommunityBizException;
import cn.bit.facade.model.property.Registration;
import cn.bit.facade.model.user.*;
import cn.bit.facade.service.property.RegistrationFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DeviceAuthVO;
import cn.bit.facade.vo.statistics.StatisticsVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.Allocation;
import cn.bit.facade.vo.user.userToProperty.EmployeeRequest;
import cn.bit.facade.vo.user.userToProperty.EmployeeVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.user.dao.CardRepository;
import cn.bit.user.dao.CommunityUserRepository;
import cn.bit.user.dao.UserToRoomRepository;
import cn.bit.user.support.CardGenerator;
import cn.bit.user.utils.UserUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.constant.mq.TagConstant.COVER;
import static cn.bit.facade.constant.mq.TagConstant.DELETE;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_DOOR_AUTH;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH;
import static cn.bit.facade.exception.CommonBizException.UNKNOWN_ERROR;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.user.UserBizException.*;

@Service("userToPropertyFacade")
@Slf4j
public class UserToPropertyFacadeImpl implements UserToPropertyFacade {

    @Autowired
    private CardRepository cardRepository;

    @Resource
    private UserToRoomRepository userToRoomRepository;

    @Autowired
    private CardGenerator cardGenerator;

    @Autowired
    private DefaultMQProducer producer;

    @Resource
    private CompanyFacade companyFacade;

    @Resource
    private RegistrationFacade registrationFacade;

    @Resource
    private CommunityUserRepository communityUserRepository;

    @Resource
    private UserFacade commonUserFacade;

    /**
     * 新增企业员工
     *
     * @param vo
     * @param creator
     * @return
     */
    @Override
    public UserToProperty addEmployee(EmployeeVO vo, ObjectId creator) {
        try {
            Employee employee = companyFacade.getEmployeeByCompanyIdAndPhone(vo.getPropertyId(), vo.getPhone());
            if (employee == null) {
                EmployeeDTO employeeDTO = new EmployeeDTO();
                employeeDTO.setCreator(creator);
                employeeDTO.setUserId(vo.getUserId());
                employeeDTO.setRegistered(vo.getRegistered());
                employeeDTO.setOfficial(vo.getOfficial());
                employeeDTO.setName(vo.getUserName());
                employeeDTO.setPhone(vo.getPhone());
                employeeDTO.setCompanyId(vo.getPropertyId());
                employeeDTO.setSex(vo.getSex());
                employeeDTO.setNo(vo.getEmployeeId());
                // 员工角色
                employeeDTO.setRoles(Collections.singleton(vo.getPostCode()));
                employee = companyFacade.createEmployee(employeeDTO);
            } else {
                if (employee.getRoles() != null && !employee.getRoles().isEmpty()) {
                    throw EMPLOYEE_EXIST;
                }
	            /*// TODO 待确认
	        	if(employee.getRoles() != null && !employee.getRoles().isEmpty()
				        && !employee.getRoles().contains(vo.getPostCode()) && employee.getUserId() == null){
	        		throw WAITING_USER_REGISTER;
		        }
		        companyFacade.addEmployeeRolesByEmployeeId(employee.getId(), Collections.singleton(vo.getPostCode()));*/
                // 如果员工没有角色，则赋予当前角色
                employee = companyFacade.addEmployeeRolesByEmployeeId(
                        employee.getId(), Collections.singleton(vo.getPostCode()));
            }

            Company company = companyFacade.getCompanyByCompanyId(employee.getCompanyId());
            UserToProperty userToProperty = fillingUserToProperty(employee);
            userToProperty.setPostCode(Collections.singleton(vo.getPostCode()));
            userToProperty.setCommunityId(vo.getCommunityId());
            userToProperty.setPropertyName(company.getName());
            return userToProperty;
        } catch (InvalidParameterException | cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_EMPLOYEE_PHONE_HAS_REGISTERED:
                    throw EMPLOYEE_PHONE_REGISTERED;
                case CodeConstants.CODE_EMPLOYEE_NO_EXIST:
                    throw EMPLOYEE_NO_EXIST;
                case CodeConstants.CODE_COMPANY_NOT_EXIST:
                    throw COMPANY_NOT_EXIST;
                default:
                    throw e;
            }
        }
    }

    /**
     * 分派物业人员
     *
     * @param allocation
     * @return
     */
    @Override
    public UserToProperty allocationEmployee(Allocation allocation) {
        try {
            Employee employee = companyFacade.addEmployeeRolesByEmployeeId(
                    allocation.getId(), Collections.singleton(allocation.getPostCode()));

            Company company = companyFacade.getCompanyByCompanyId(employee.getCompanyId());

            UserToProperty userToProperty = fillingUserToProperty(employee);
            userToProperty.setPostCode(Collections.singleton(allocation.getPostCode()));
            userToProperty.setCommunityId(allocation.getCommunityId());
            userToProperty.setPropertyName(company.getName());
            return userToProperty;
        } catch (cn.bit.common.facade.exception.BizException e) {
            log.error(e.getMessage());
            switch (e.getSubCode()) {
                case CodeConstants.CODE_EMPLOYEE_NOT_EXIST:
                    throw EMPLOYEE_NOT_EXIST;
                case CodeConstants.CODE_EMPLOYEE_PHONE_HAS_REGISTERED:
                    throw EMPLOYEE_PHONE_REGISTERED;
                case CodeConstants.CODE_EMPLOYEE_NO_EXIST:
                    throw EMPLOYEE_NO_EXIST;
                case CodeConstants.CODE_COMPANY_NOT_EXIST:
                    throw COMPANY_NOT_EXIST;
                default:
                    log.error(e.getSubCode());
                    throw OPERATION_FAILURE;
            }
        }
    }

    /**
     * 物业人员注册
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
    @Override
    public UserVO registerClientUserAndCMUserWithRegistration(
            Integer partner, Integer platform, Integer appType, ObjectId appId,
            UserToProperty userToProperty, String password, String code, String pushId, UserDevice userDevice,
            List<Registration> registrationList) {
        Set<String> roles = new HashSet<>(userToProperty.getPostCode());
        Set<Object> communityIds = new HashSet<>();
        if (registrationList != null && !registrationList.isEmpty()) {
            registrationList.forEach(registration -> {
                roles.addAll(registration.getRoles());
                communityIds.add(registration.getCommunityId());
            });
        }
        UserDTO userDTO = registerClientUserForUserToProperty(partner, platform, appType, appId,
                userToProperty, password, code, pushId, userDevice, roles, communityIds);
        userToProperty.setUserId(userDTO.getId());
        Set<ObjectId> registrationIds = new HashSet<>();
        if (registrationList != null) {
            for (Registration registration : registrationList) {
                userToProperty.setPostCode(registration.getRoles());
                userToProperty.setCommunityId(registration.getCommunityId());
                // 注册 cm user
                upsertCommunityUserForUserToProperty(userToProperty);
                // 记录需要删除登记信息ID
                registrationIds.add(registration.getId());
            }
        }
        if (!registrationIds.isEmpty()) {
            log.info("需要清除社区人员注册登记信息：{}", registrationIds);
            registrationFacade.deleteRegistrationByIds(registrationIds);
        }

        try {
            EmployeeRegisterDTO dto = new EmployeeRegisterDTO();
            dto.setPartner(partner);
            dto.setUserId(userDTO.getId());
            dto.setEmployeePhone(userToProperty.getPhone());
            // 根据手机号更新企业员工的关联userId
            companyFacade.registerEmployee(dto);
        } catch (cn.bit.common.facade.exception.BizException e) {
            log.error("根据手机号更新企业员工关联的userId 异常：{}", e.getMessage());
        }

        User local = UserUtils.convert(userDTO, User.class);
        local.setHeadImg(userDTO.getAvatar());
        return new UserVO(local, userDTO.getToken());
    }

    /**
     * 添加物业员工并注册
     *
     * @param userToProperty
     * @return
     */
    @Override
    public void upsertClientUserAndCMUserForCreatePropertyUser(Integer partner, UserToProperty userToProperty) {
        upsertClientUserForUserToProperty(partner, userToProperty);
        upsertCommunityUserForUserToProperty(userToProperty);
    }

    /**
     * 分派物业人员并注册端用户及社区用户
     *
     * @param userToProperty
     * @return
     */
    @Override
    public void upsertClientUserAndCMUserForAllocation(Integer partner, UserToProperty userToProperty) {
        upsertClientUserForUserToProperty(partner, userToProperty);
        upsertCommunityUserForUserToProperty(userToProperty);
    }

    /**
     * 更新物业人员资料
     *
     * @param employeeVO
     * @return
     */
    @Override
    public UserToProperty modifyEmployee(EmployeeVO employeeVO) {
        try {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(employeeVO.getId());
            employeeDTO.setNo(employeeVO.getEmployeeId());
            employeeDTO.setSex(employeeVO.getSex());
            employeeDTO.setName(employeeVO.getUserName());
            employeeDTO.setOfficial(employeeVO.getOfficial());
            Employee employee = companyFacade.modifyEmployee(employeeDTO);
            UserToProperty userToProperty = fillingUserToProperty(employee);
            userToProperty.setCommunityId(employeeVO.getCommunityId());
            return userToProperty;
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case CodeConstants.CODE_EMPLOYEE_NOT_EXIST:
                    throw EMPLOYEE_NOT_EXIST;
                case CodeConstants.CODE_EMPLOYEE_PHONE_HAS_REGISTERED:
                    throw EMPLOYEE_PHONE_REGISTERED;
                case CodeConstants.CODE_EMPLOYEE_NO_EXIST:
                    throw EMPLOYEE_NO_EXIST;
                default:
                    log.error(e.getSubCode());
                    throw OPERATION_FAILURE;
            }
        }
    }

    @Override
    public boolean deleteEmployee(Integer partner, ObjectId id, ObjectId communityId) throws BizException {
        Employee employee = getEmployeeById(id);
        if (employee == null) {
            throw EMPLOYEE_NOT_EXIST;
        }
        ObjectId userId = employee.getUserId();
        // 查询用户在所有社区下的记录
        List<CommunityUser> commUserList = communityUserRepository.findByUserIdAndClientsInAndDataStatus(
                userId, Collections.singleton(ClientType.PROPERTY.value()), DataStatusType.VALID.KEY);
        if (commUserList.isEmpty()) {
            log.info("社区已经不存在该物业人员信息({})，无需操作(注意检测是否属于问题数据)", userId);
            return true;
        }
        CommunityUser communityUser = null;
        for (CommunityUser communityUser_ : commUserList) {
            if (communityId.equals(communityUser_.getCommunityId())) {
                communityUser = communityUser_;
            }
        }
        if (communityUser == null) {
            log.info("社区{}已经不存在该物业人员信息({})，无需操作(注意检测是否属于问题数据)", communityId, userId);
            return true;
        }

        UserToProperty userToProperty = fillingUserToProperty(employee);

        ObjectId companyId = employee.getCompanyId();
        List<CompanyToCommunity> companyToCommunityList = companyFacade.listCommunitiesByCompanyId(companyId);
        // 相同企业下的社区id集合
        Set<ObjectId> sameCompanyCommunityIds = new HashSet<>();
        companyToCommunityList.forEach(companyToCommunity -> {
            if (!communityId.equals(companyToCommunity.getCommunityId())) {
                sameCompanyCommunityIds.add(companyToCommunity.getCommunityId());
            }
        });
        log.info("企业绑定的其他社区：{}", sameCompanyCommunityIds);
        try {
            Set<String> removeRoles = new HashSet<>(userToProperty.getPostCode());
            // 相同企业是否绑定其他社区
            if (!sameCompanyCommunityIds.isEmpty()) {
                // 检测其他社区是否存在相同的角色
                for (CommunityUser cmUser : commUserList) {
                    if (sameCompanyCommunityIds.contains(cmUser.getCommunityId()) && !removeRoles.isEmpty()) {
                        for (String role : removeRoles) {
                            if (cmUser.getRoles().contains(role)) {
                                log.info("deleteEmployee 其他社区存在相同角色（{}），企业员工不移除这个角色", role);
                                removeRoles.remove(role);
                            }
                        }
                    }
                }
            }
            if (!removeRoles.isEmpty()) {
                // 业务企业管理员ID 转换成为 系统企业管理员ID
                if (removeRoles.contains(RoleConstants.ROLE_STR_COMPANY_ADMIN)
                        || removeRoles.contains(RoleType.COMPANY_ADMIN.name())) {
                    removeRoles.add(cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN);
                    removeRoles.remove(RoleConstants.ROLE_STR_COMPANY_ADMIN);
                    removeRoles.remove(RoleType.COMPANY_ADMIN.name());
                }
                log.info("deleteEmployee 同企业下关联的其他社区不存在相同角色，开始移除企业员工的角色...{}", removeRoles);
                // 需要移除企业员工的角色
                companyFacade.removeEmployeeRolesByEmployeeId(employee.getId(), removeRoles);
            }
        } catch (cn.bit.common.facade.exception.BizException e) {
            log.error(e.getMessage());
            switch (e.getSubCode()) {
                case CodeConstants.CODE_EMPLOYEE_NOT_EXIST:
                    throw EMPLOYEE_NOT_EXIST;
                default:
                    throw UNKNOWN_ERROR;
            }
        }

        log.info("deleteEmployee 开始删除client/cm-user 及物业权限，roles:{}", userToProperty.getPostCode());
        fillingUserToPropertyWithCommunityUser(userToProperty, communityUser);
        sendRelieveMessage2Device(userToProperty);
        userToProperty.getPostCode().forEach(role -> {
            log.info("deleteEmployee 删除角色：{}", role);
            removeRoleAndClientFromCommunityUser(userId, communityId, role);
            removeCommunityIdAndRoleFromClientUser(partner, userId, communityId, role);
        });
        log.info("deleteEmployee 删除完成");
        return true;
    }

    /**
     * 注销企业员工
     *
     * @param userId
     * @param companyId
     * @param communityId
     */
    @Override
    public void deleteCompanyEmployee(Integer partner, ObjectId userId, ObjectId companyId, ObjectId communityId) {
        Employee employee = companyFacade.getEmployeeByCompanyIdAndUserId(companyId, userId);
        if (employee == null) {
            throw EMPLOYEE_NOT_EXIST;
        }
        CommunityUser communityUser =
                communityUserRepository.findByCommunityIdAndUserIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        communityId, userId, Collections.singleton(ClientType.PROPERTY.value()),
                        null, DataStatusType.VALID.KEY);
        if (communityUser == null) {
            log.info("社区已经没有该员工的信息");
            return;
        }
        UserToProperty userToProperty = fillingUserToProperty(employee);
        fillingUserToPropertyWithCommunityUser(userToProperty, communityUser);
        log.info("开始删除client/cm-user 及物业权限");
        sendRelieveMessage2Device(userToProperty);
        userToProperty.getPostCode().forEach(role -> {
            removeRoleAndClientFromCommunityUser(userId, communityId, role);
            removeCommunityIdAndRoleFromClientUser(partner, userId, communityId, role);
        });
        log.info("删除完成");
    }

    /**
     * 移除员工在某个社区的岗位
     *
     * @param userToProperty
     */
    @Override
    public void removeClientUserAndCMUser(Integer partner, UserToProperty userToProperty) {
        ObjectId userId = userToProperty.getUserId();
        ObjectId communityId = userToProperty.getCommunityId();
        CommunityUser communityUser =
                communityUserRepository.findByCommunityIdAndUserIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        communityId, userId, Collections.singleton(ClientType.PROPERTY.value()),
                        userToProperty.getPostCode(), DataStatusType.VALID.KEY);
        if (communityUser == null) {
            log.info("员工({})在社区({})没有这个岗位({})", userId, communityId, userToProperty.getPostCode());
            return;
        }
        fillingUserToPropertyWithCommunityUser(userToProperty, communityUser);
        log.info("开始删除client/cm-user 及物业权限");
        sendRelieveMessage2Device(userToProperty);
        userToProperty.getPostCode().forEach(role -> {
            removeRoleAndClientFromCommunityUser(userId, communityId, role);
            removeCommunityIdAndRoleFromClientUser(partner, userId, communityId, role);
        });
        log.info("删除完成");
    }

    /**
     * 企业解绑社区，需要注销社区下的所有物业人员
     *
     * @param companyId
     * @param communityId
     */
    @Override
    public void unbindCompany(Integer partner, ObjectId companyId, ObjectId communityId) {
        List<CommunityUser> communityUserList =
                communityUserRepository.findByCommunityIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        communityId, Collections.singleton(ClientType.PROPERTY.value()),
                        null, DataStatusType.VALID.KEY);
        if (communityUserList.isEmpty()) {
            log.info("社区（{}）下已经没有任何物业人员", communityId);
            return;
        }
        for (CommunityUser communityUser : communityUserList) {
            // 查询用户在所有社区下的记录
            List<CommunityUser> commUserList = communityUserRepository.findByUserIdAndClientsInAndDataStatus(
                    communityUser.getUserId(), Collections.singleton(ClientType.PROPERTY.value()), DataStatusType.VALID.KEY);
            Employee employee = companyFacade.getEmployeeByCompanyIdAndUserId(companyId, communityUser.getUserId());
            if (employee == null) {
                throw EMPLOYEE_NOT_EXIST;
            }
            UserToProperty userToProperty = fillingUserToProperty(employee);
            fillingUserToPropertyWithCommunityUser(userToProperty, communityUser);
            List<CompanyToCommunity> companyToCommunityList = companyFacade.listCommunitiesByCompanyId(companyId);
            // 相同企业下的社区id集合
            Set<ObjectId> sameCompanyCommunityIds = new HashSet<>();
            companyToCommunityList.forEach(companyToCommunity -> {
                if (!communityId.equals(companyToCommunity.getCommunityId())) {
                    sameCompanyCommunityIds.add(companyToCommunity.getCommunityId());
                }
            });
            log.info("企业绑定的其他社区：{}", sameCompanyCommunityIds);
            try {
                Set<String> removeRoles = new HashSet<>(userToProperty.getPostCode());
                // 需要保留企业管理员，因为企业管理员是跟企业走的
                removeRoles.remove(RoleConstants.ROLE_STR_COMPANY_ADMIN);

                // 相同企业是否绑定其他社区
                if (!sameCompanyCommunityIds.isEmpty()) {
                    // 检测其他社区是否存在相同的角色
                    for (CommunityUser cmUser : commUserList) {
                        if (sameCompanyCommunityIds.contains(cmUser.getCommunityId()) && !removeRoles.isEmpty()) {
                            for (String role : removeRoles) {
                                if (cmUser.getRoles().contains(role)) {
                                    log.info("unbindCompany 其他社区存在相同角色（{}），企业员工不移除这个角色", role);
                                    removeRoles.remove(role);
                                }
                            }
                        }
                    }
                }
                if (!removeRoles.isEmpty()) {
                    log.info("unbindCompany 同企业下关联的其他社区不存在相同角色，开始移除企业员工的角色...{}", removeRoles);
                    // 需要移除企业员工的角色
                    companyFacade.removeEmployeeRolesByEmployeeId(employee.getId(), removeRoles);
                }
            } catch (cn.bit.common.facade.exception.BizException e) {
                log.error(e.getMessage());
                switch (e.getSubCode()) {
                    case CodeConstants.CODE_EMPLOYEE_NOT_EXIST:
                        throw EMPLOYEE_NOT_EXIST;
                    default:
                        throw UNKNOWN_ERROR;
                }
            }

            log.info("unbindCompany 开始删除client/cm-user 及物业权限，roles:{}", userToProperty.getPostCode());
            sendRelieveMessage2Device(userToProperty);
            userToProperty.getPostCode().forEach(role -> {
                log.info("unbindCompany 删除角色：{}", role);
                removeRoleAndClientFromCommunityUser(communityUser.getUserId(), communityId, role);
                removeCommunityIdAndRoleFromClientUser(partner, communityUser.getUserId(), communityId, role);
            });
            log.info("unbindCompany 删除完成");
        }
    }

    @Override
    public UserToProperty findByIdAndCommunityId(ObjectId id, ObjectId communityId) {
        Employee employee = getEmployeeById(id);
        if (employee == null) {
            log.info("没有找到对应的企业员工，return null");
            return null;
        }
        if (employee.getUserId() == null || employee.getRegistered() == null || !employee.getRegistered()) {
            throw EMPLOYEE_NOT_REGISTER;
        }
        CommunityUser communityUser =
                communityUserRepository.findByCommunityIdAndUserIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        communityId, employee.getUserId(), Collections.singleton(ClientType.PROPERTY.value()),
                        null, DataStatusType.VALID.KEY);
        if (communityUser == null) {
            log.info("没有找到对应的社区员工，return null");
            return null;
        }
        UserToProperty userToProperty = fillingUserToProperty(employee);
        fillingUserToPropertyWithCommunityUser(userToProperty, communityUser);
        userToProperty.setPostCode(communityUser.getRoles());
        return userToProperty;
    }

    @Override
    public List<UserToProperty> listEmployees(EmployeeRequest request) {
        EmployeePageQuery query = new EmployeePageQuery();
        query.setCompanyId(request.getCompanyId());
        String role = null;
        if (CollectionUtils.isNotEmpty(request.getRoles())) {
            role = request.getRoles().iterator().next();
        }
        query.setRole(RoleConstants.ROLE_STR_COMPANY_ADMIN.equals(role) ? cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN : role);
        query.setRegistered(Boolean.TRUE);
        query.setPage(1);
        query.setSize(1000);
        cn.bit.common.facade.data.Page<Employee> employeePage = companyFacade.listEmployees(query);
        if (employeePage.getTotal() == 0) {
            return Collections.EMPTY_LIST;
        }
        Set<ObjectId> userIds = employeePage.getRecords().stream().map(Employee::getUserId).collect(Collectors.toSet());
        List<CommunityUser> communityUsers =
                communityUserRepository.findByCommunityIdAndUserIdInAndClientsAndRolesIgnoreNullAndDataStatus(
                        request.getCommunityId(), userIds, ClientType.PROPERTY.value(), role, DataStatusType.VALID.KEY);
        if (communityUsers.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<UserToProperty> userToPropertyList = fillingUserToPropertyList(employeePage.getRecords(), communityUsers, false);

        fillingUserToPropertyAccId(request.getPartner(), userToPropertyList);
        return userToPropertyList;
    }

    /**
     * 根据userIds查询社区对应岗位的员工
     *
     * @param statisticsVO
     * @return
     */
    @Override
    public List<UserToProperty> findByStatisticsVO(StatisticsVO statisticsVO) {
        if (statisticsVO == null || statisticsVO.getCompanyId() == null) {
            throw CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
        }
        List<CommunityUser> communityUsers =
                communityUserRepository.findByCommunityIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        statisticsVO.getCommunityId(), Collections.singleton(ClientType.PROPERTY.value()),
                        statisticsVO.getRoles(), DataStatusType.VALID.KEY);
        if (communityUsers.isEmpty()) {
            return Collections.emptyList();
        }
        Set<ObjectId> userIds = statisticsVO.getUserIds();
        Set<ObjectId> onDutyUserIds = communityUsers.stream().map(CommunityUser::getUserId).collect(Collectors.toSet());
        // 添加当前在职的员工
        userIds.addAll(onDutyUserIds);
        // 根据userIds 和 企业id查询在职的企业员工列表（在当前社区工作）
        List<Employee> employeeList = companyFacade.listEmployeesByCompanyIdAndUserIds(statisticsVO.getCompanyId(), userIds);
        return fillingUserToPropertyList(employeeList, communityUsers, false);
    }

    @Override
    public UserToProperty findByUserIdAndCommunityIdAndCompanyId(ObjectId userId, ObjectId communityId,
                                                                 ObjectId companyId) {
        if (communityId != null && companyId == null) {
            // 获取社区绑定的物业公司
            List<CompanyToCommunity> companies = companyFacade.listCompaniesByCommunityIdAndCompanyType(
                    communityId, CompanyTypeEnum.PROPERTY.value());
            if (companies.isEmpty()) {
                throw CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
            }
            companyId = companies.get(0).getCompanyId();
        }

        if (companyId == null) {
            throw CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
        }
        CommunityUser communityUser =
                communityUserRepository.findByCommunityIdAndUserIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        communityId, userId, Collections.singleton(ClientType.PROPERTY.value()),
                        null, DataStatusType.VALID.KEY);
        if (communityUser == null) {
            log.info("没有找到对应的社区员工，return null");
            return null;
        }
        Employee employee = companyFacade.getEmployeeByCompanyIdAndUserId(companyId, userId);
        if (employee == null) {
            log.info("没有找到对应的社区员工，return null");
            return null;
        }
        UserToProperty userToProperty = fillingUserToProperty(employee);
        fillingUserToPropertyWithCommunityUser(userToProperty, communityUser);
        userToProperty.setPostCode(communityUser.getRoles());
        return userToProperty;
    }

    @Override
    public UserToProperty updateDistrictForProperty(UserToProperty userToProperty) {
        CommunityUser toUpdate = new CommunityUser();
        toUpdate.setDistrictIds(userToProperty.getDistrictIds());
        toUpdate.setBuildingIds(userToProperty.getBuildingIds());
        toUpdate.setUpdateAt(new Date());
        toUpdate = communityUserRepository.updateByUserIdAndCommunityIdAndDataStatus(
                toUpdate, userToProperty.getUserId(), userToProperty.getCommunityId(), DataStatusType.VALID.KEY);
        // TODO 如果更新失败要如何处理
        if (toUpdate == null) {
            log.info("更新物业人员区域楼栋授权失败");
        }
        return userToProperty;
    }

    @Override
    public boolean updateMiliUIds(UserToProperty userToProperty) {
        CommunityUser toUpdate = new CommunityUser();
        toUpdate.setMiliUIds(userToProperty.getMiliUIds());
        toUpdate.setUpdateAt(new Date());
        toUpdate = communityUserRepository.updateByUserIdAndCommunityIdAndDataStatus(
                toUpdate, userToProperty.getUserId(), userToProperty.getCommunityId(), DataStatusType.VALID.KEY);
        // TODO 如果更新失败要如何处理
        if (toUpdate == null) {
            log.info("更新物业人员区域楼栋授权失败");
        }
        return true;
    }

    /**
     * 根据手机号查询企业员工
     *
     * @param partner
     * @param phone
     * @return
     */
    @Override
    public UserToProperty checkEmployee(Integer partner, String phone) {
        if (StringUtil.isBlank(phone)) {
            log.info("checkEmployee 电话号码为空，直接返回 null");
            return null;
        }
        EmployeeQuery query = new EmployeeQuery();
        query.setCompanyType(CompanyTypeEnum.PROPERTY.value());
        query.setEmployeePhone(phone);
        query.setPartner(partner);
        List<EmployeeDTO> dtoList = companyFacade.listEmployees(query);
        if (dtoList == null || dtoList.isEmpty()) {
            log.info("partner:{}, phone:{}查询员工信息返回为空", partner, phone);
            return null;
        }
        EmployeeDTO dto = dtoList.get(0);
        UserToProperty userToProperty = new UserToProperty();
        userToProperty.setPhone(dto.getPhone());
        userToProperty.setUserName(dto.getName());
        userToProperty.setUserId(dto.getUserId());
        userToProperty.setPostCode(dto.getRoles());

        if (dtoList.size() > 1) {
            for (int i = 1; i < dtoList.size(); i++) {
                userToProperty.getPostCode().addAll(dtoList.get(i).getRoles());
            }
        }
        // 系统企业管理员ID 转换成为 业务企业管理员ID
        if (userToProperty.getPostCode() != null && !userToProperty.getPostCode().isEmpty()) {
            Set<String> roles = new HashSet<>(userToProperty.getPostCode().size());
            for (String role : userToProperty.getPostCode()) {
                if (cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN.equals(role)
                        || RoleType.COMPANY_ADMIN.name().equals(role)) {
                    role = RoleConstants.ROLE_STR_COMPANY_ADMIN;
                }
                roles.add(role);
            }
            userToProperty.setPostCode(roles);
        }
        return userToProperty;
    }

    @Override
    public List<UserToProperty> listEmployeesByCommunityIdAndCompanyIdAndRoles(ObjectId communityId,
                                                                               ObjectId companyId,
                                                                               Collection<String> roles) {
        if (roles.contains(RoleConstants.ROLE_STR_COMPANY_ADMIN)) {
            roles.add(cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN);
        }
        List<EmployeeDTO> employeeDTOS = companyFacade.listEmployeesByCompanyIdAndRoles(companyId, roles);
        if (CollectionUtils.isEmpty(employeeDTOS)) {
            return Collections.emptyList();
        }
        List<Employee> employeeList = employeeDTOS.stream().map(dto -> {
            Employee employee = new Employee();
            BeanUtils.copyProperties(dto, employee);
            return employee;
        }).collect(Collectors.toList());

        List<CommunityUser> cmUsers = communityUserRepository.findByCommunityIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                communityId, Collections.singleton(ClientType.PROPERTY.value()), roles, DataStatusEnum.VALID.value());
        return fillingUserToPropertyList(employeeList, cmUsers, false);
    }

    @Override
    public UserToProperty updateUserToPropertyDistrictRange(UserToProperty userToProperty, Set<ObjectId> delDistrictIds) {
        Card card = cardGenerator.applyUserCard(
                userToProperty.getUserId(), userToProperty.getCommunityId(), userToProperty.getUserName());

        CommunityUser toUpdate = new CommunityUser();
        toUpdate.setDistrictIds(userToProperty.getDistrictIds());
        toUpdate.setBuildingIds(userToProperty.getBuildingIds());
        toUpdate.setUpdateAt(new Date());
        communityUserRepository.updateByUserIdAndCommunityIdAndDataStatus(
                toUpdate, userToProperty.getUserId(), userToProperty.getCommunityId(), DataStatusType.VALID.KEY);

        // TODO 如果更新失败要如何处理
        sendCoverMessage2Device(userToProperty, card, delDistrictIds);
        return userToProperty;
    }

    /**
     * 分页查询物业员工列表
     *
     * @param employeeVO
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<UserToProperty> findPageByCommunityIdAndUserToProperty(
            EmployeeVO employeeVO, Integer partner, Integer page, Integer size) {
        ObjectId communityId = employeeVO.getCommunityId();
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        ObjectId companyId = employeeVO.getPropertyId();
        if (companyId == null) {
            throw CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
        }

        String role = employeeVO.getPostCode();
        List<String> roles = null;
        if (StringUtil.isNotBlank(role)) {
            roles = new ArrayList<>();
            roles.add(role);
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        EmployeePageQuery query = new EmployeePageQuery();
        query.setCompanyId(companyId);
        query.setPhone(employeeVO.getPhone());
        query.setName(employeeVO.getUserName());
        query.setRegistered(Boolean.TRUE);
        query.setRole(RoleConstants.ROLE_STR_COMPANY_ADMIN.equals(role) ? cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN : role);
        query.setPage(1);
        query.setSize(1000);
        cn.bit.common.facade.data.Page<Employee> employeePage = companyFacade.listEmployees(query);
        if (employeePage.getTotal() == 0) {
            log.info("没有找到对应的企业员工，EmployeePageQuery:{}", query);
            return new Page<>();
        }
        org.springframework.data.domain.Page<CommunityUser> communityUserPage = communityUserRepository
                .findByCommunityIdAndUserIdInIgnoreNullAndClientsAndRolesInIgnoreNullAndDataStatus(
                        communityId, employeePage.getRecords().stream().map(Employee::getUserId).collect(Collectors.toSet()),
                        ClientType.PROPERTY.value(), roles, DataStatusType.VALID.KEY, pageable);
        if (communityUserPage.getTotalElements() == 0) {
            return new Page<>();
        }
        List<UserToProperty> list =
                fillingUserToPropertyList(employeePage.getRecords(), communityUserPage.getContent(), true);
        Page<UserToProperty> resultPage = new Page<>(page, communityUserPage.getTotalElements(), size, list);
        if (resultPage.getTotal() > 0) {
            fillingUserToPropertyAccId(partner, resultPage.getRecords());
        }
        return resultPage;
    }

    /**
     * 查询企业员工列表
     *
     * @param communityId
     * @param keyword
     * @param id
     * @param direction
     * @param size
     * @return
     */
    @Override
    public List<UserToProperty> incrementEmployees(ObjectId communityId, String keyword,
                                                   ObjectId id, Integer direction, Integer size) {
        // 获取社区绑定的物业公司
        List<CompanyToCommunity> companies = companyFacade.listCompaniesByCommunityIdAndCompanyType(
                communityId, CompanyTypeEnum.PROPERTY.value());
        if (companies.isEmpty()) {
            throw CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
        }
        ObjectId companyId = companies.get(0).getCompanyId();
        EmployeeIncrementalQuery query = new EmployeeIncrementalQuery();
        query.setKeyword(keyword);
//        query.setRegistered(Boolean.TRUE);
        query.setCompanyId(companyId);
        query.setPrevious(id);
        if (direction == 0) {
            // 升序
            query.setDirection(XSort.Direction.ASC);
        } else {
            // 倒序
            query.setDirection(XSort.Direction.DESC);
        }
        query.setLimit(size);
        log.info("查询企业员工列表，query:{}", query);
        // 根据userIds 和 企业id查询企业员工列表
        List<Employee> employeeList = companyFacade.listEmployees(query);
        if (employeeList.isEmpty()) {
            log.info("没有查询到任何的企业员工");
            return Collections.emptyList();
        }
        List<UserToProperty> list = employeeList.stream().map(
                employee -> fillingUserToProperty(employee)).collect(Collectors.toList());
        list.forEach(userToProperty -> userToProperty.setCommunityId(communityId));
        return list;
    }

    private void sendCoverMessage2Device(UserToProperty userToProperty, Card card, Set<ObjectId> delDistrictIds) {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setCommunityId(userToProperty.getCommunityId());
        deviceAuthVO.setName(userToProperty.getUserName());
        deviceAuthVO.setPhone(userToProperty.getPhone());
        deviceAuthVO.setUserId(userToProperty.getUserId());
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setKeyNo(card.getKeyNo());
        deviceAuthVO.setKeyId(card.getKeyId());
        Date startDate = new Date();
        deviceAuthVO.setProcessTime((int) DateUtils.secondsBetween(startDate, DateUtils.addYear(startDate, 50)));
        deviceAuthVO.setCorrelationId(userToProperty.getId());
        deviceAuthVO.setHandleCount(0);
        deviceAuthVO.setSex(userToProperty.getSex());
        deviceAuthVO.setDistrictIds(userToProperty.getDistrictIds());
        deviceAuthVO.setDelDistrictIds(delDistrictIds);
        deviceAuthVO.setOutUIds(userToProperty.getMiliUIds());

        deviceAuthVO.setBuildingList(userToProperty.getBuildingIds().stream().map(buildingId -> {
            BuildingListVO buildingListVO = new BuildingListVO();
            buildingListVO.setBuildingId(buildingId);
            return buildingListVO;
        }).collect(Collectors.toSet()));

        // 需要保留设备的地址信息
        addPropertyAuthAddress(deviceAuthVO);
        List<DeviceAuthVO> allDeviceAuthVOS = getPhysicalCardsDeviceAuthVOS(deviceAuthVO);
        for (DeviceAuthVO authVO : allDeviceAuthVOS) {
            Message doorMessage = new Message(TOPIC_COMMUNITY_IOT_DOOR_AUTH, COVER,
                    JSON.toJSONString(authVO).getBytes(Charset.forName("UTF-8")));
            Message elevatorMessage = new Message(TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH, COVER,
                    JSON.toJSONString(authVO).getBytes(Charset.forName("UTF-8")));
            try {
                producer.send(doorMessage);
                producer.send(elevatorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("队列发送异常 : ", e);
            }
        }
    }

    private void addPropertyAuthAddress(DeviceAuthVO deviceAuthVO) {
        List<UserToRoom> existUserToRoom = userToRoomRepository
                .findByCommunityIdAndUserIdAndAuditStatusAndDataStatus(deviceAuthVO.getCommunityId(),
                        deviceAuthVO.getUserId(), AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);
        Set<ObjectId> collect = deviceAuthVO.getBuildingList().stream().map(BuildingListVO::getBuildingId)
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(existUserToRoom)) {
            buildAddPropertyBuildingListVO(deviceAuthVO, existUserToRoom, collect);
        }
    }

    private void buildAddPropertyBuildingListVO(DeviceAuthVO deviceAuthVO, List<UserToRoom> existUserToRoom,
                                                Set<ObjectId> collect) {
        Set<ObjectId> insertBuilding = new HashSet<>(existUserToRoom.size());
        for (UserToRoom userToRoom : existUserToRoom) {
            if (collect.contains(userToRoom.getBuildingId())) {
                continue;
            }
            if (insertBuilding.contains(userToRoom.getBuildingId())) {
                deviceAuthVO.getBuildingList().forEach(buildingListVO -> {
                    if (buildingListVO.getBuildingId().equals(userToRoom.getBuildingId())) {
                        buildingListVO.getRooms().add(userToRoom.getRoomId());
                    }
                });
            } else {
                BuildingListVO buildingListVO = new BuildingListVO();
                buildingListVO.setBuildingId(userToRoom.getBuildingId());
                Set<ObjectId> set = new HashSet<>();
                set.add(userToRoom.getRoomId());
                buildingListVO.setRooms(set);
                deviceAuthVO.getBuildingList().add(buildingListVO);
                insertBuilding.add(userToRoom.getBuildingId());
            }
        }
    }

    private void keepTenementAuthAddress(DeviceAuthVO deviceAuthVO) {
        List<UserToRoom> existUserToRoom = userToRoomRepository
                .findByCommunityIdAndUserIdAndAuditStatusAndDataStatus(deviceAuthVO.getCommunityId(),
                        deviceAuthVO.getUserId(), AuditStatusType.REVIEWED.getType(), DataStatusType.VALID.KEY);

        if (CollectionUtils.isNotEmpty(existUserToRoom)) {
            Set<ObjectId> buildingIds = new HashSet<>(existUserToRoom.size());
            for (UserToRoom userToRoom : existUserToRoom) {
                if (!buildingIds.contains(userToRoom.getBuildingId())) {
                    BuildingListVO buildingListVO = new BuildingListVO();
                    buildingListVO.setBuildingId(userToRoom.getBuildingId());
                    deviceAuthVO.getBuildingList().add(buildingListVO);
                }
                deviceAuthVO.getBuildingList().stream()
                        .filter(buildingListVO -> buildingListVO.getBuildingId().equals(userToRoom.getBuildingId()))
                        .forEach(buildingListVO -> {
                            buildingListVO.getRooms().add(userToRoom.getRoomId());
                            buildingIds.add(userToRoom.getBuildingId());
                        });
            }
        }
    }

    private void sendRelieveMessage2Device(UserToProperty userToProperty) {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setUserId(userToProperty.getUserId());
        deviceAuthVO.setCommunityId(userToProperty.getCommunityId());
        deviceAuthVO.setName(userToProperty.getUserName());
        deviceAuthVO.setPhone(userToProperty.getPhone());

        Set<BuildingListVO> vos = new HashSet<>(userToProperty.getBuildingIds().size());
        userToProperty.getBuildingIds().forEach(buildingId -> {
            BuildingListVO buildingListVO = new BuildingListVO();
            buildingListVO.setBuildingId(buildingId);
            vos.add(buildingListVO);
        });
        deviceAuthVO.setBuildingList(vos);

        // 查找用户虚拟卡
        Card card = cardRepository.findByUserIdAndCommunityIdAndKeyTypeAndDataStatus(userToProperty.getUserId(),
                userToProperty.getCommunityId(), CertificateType.PHONE_MAC.KEY, DataStatusType.VALID.KEY);
        if (card == null) {
            return;
        }
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setKeyNo(card.getKeyNo());
        deviceAuthVO.setKeyId(card.getKeyId());
        deviceAuthVO.setCorrelationId(userToProperty.getId());
        deviceAuthVO.setHandleCount(0);
        deviceAuthVO.setOutUIds(userToProperty.getMiliUIds());

        keepTenementAuthAddress(deviceAuthVO);
        // 注销物业人员直接用删除用户的逻辑
        List<DeviceAuthVO> allDeviceAuthVOS = getPhysicalCardsDeviceAuthVOS(deviceAuthVO);
        for (DeviceAuthVO authVO : allDeviceAuthVOS) {
            Message doorMessage = new Message(TOPIC_COMMUNITY_IOT_DOOR_AUTH, DELETE,
                    JSON.toJSONString(authVO).getBytes(Charset.forName("UTF-8")));
            Message elevatorMessage = new Message(TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH, DELETE,
                    JSON.toJSONString(authVO).getBytes(Charset.forName("UTF-8")));
            try {
                producer.send(doorMessage);
                producer.send(elevatorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                log.error("队列发送异常 : ", e);
            }
        }
    }

    private List<DeviceAuthVO> getPhysicalCardsDeviceAuthVOS(DeviceAuthVO deviceAuthVO) {
        List<Card> physicalCards = cardRepository
                .findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(deviceAuthVO.getUserId(),
                        deviceAuthVO.getCommunityId(),
                        Arrays.asList(CertificateType.BLUETOOTH_CARD.KEY, CertificateType.IC_CARD.KEY),
                        DataStatusType.VALID.KEY);
        List<DeviceAuthVO> deviceAuthVOS = new ArrayList<>(physicalCards.size() + 1);
        deviceAuthVOS.add(deviceAuthVO);
        deviceAuthVOS.addAll(physicalCards.stream().map(physicalCard -> {
            DeviceAuthVO physicalDeviceAuthVO = new DeviceAuthVO();
            BeanUtils.copyProperties(deviceAuthVO, physicalDeviceAuthVO);
            physicalDeviceAuthVO.setKeyType(physicalCard.getKeyType());
            physicalDeviceAuthVO.setKeyId(physicalCard.getKeyId());
            physicalDeviceAuthVO.setKeyNo(physicalCard.getKeyNo());
            return physicalDeviceAuthVO;
        }).collect(Collectors.toSet()));
        return deviceAuthVOS;
    }

    /**
     * 更新 clientUser
     *
     * @param userToProperty
     */
    private void upsertClientUserForUserToProperty(Integer partner, UserToProperty userToProperty) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setTags(Collections.singleton(userToProperty.getCommunityId()));
        profileDTO.setClient(ClientType.PROPERTY.value());
        profileDTO.setPartner(partner);
        profileDTO.setRoles(userToProperty.getPostCode());
        profileDTO.setUserId(userToProperty.getUserId());
        commonUserFacade.appendProfileByClientAndPartnerAndUserId(profileDTO);
    }

    /**
     * 更新/注册社区人员
     *
     * @param userToProperty
     */
    private void upsertCommunityUserForUserToProperty(UserToProperty userToProperty) {
        CommunityUser communityUser = new CommunityUser();
        communityUser.setClients(Collections.singleton(ClientType.PROPERTY.value()));
        communityUser.setUserId(userToProperty.getUserId());
        communityUser.setCommunityId(userToProperty.getCommunityId());
        communityUser.setRoles(userToProperty.getPostCode());
        communityUser.setCreateAt(userToProperty.getCreateAt() == null ? new Date() : userToProperty.getCreateAt());
        communityUser.setUpdateAt(new Date());
        communityUser.setDataStatus(DataStatusType.VALID.KEY);
        log.info("开始更新/注册社区人员，communityUser:{}", communityUser);
        communityUser = communityUserRepository.upsertWithAddToSetClientsAndRolesByCommunityIdAndUserIdAndDataStatus(
                communityUser, userToProperty.getCommunityId(), userToProperty.getUserId(), DataStatusType.VALID.KEY);
        log.info("更新/注册社区人员完成，result = {}", communityUser);
    }

    /**
     * 注册clientUser
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
     * @param roles
     * @param communityIds
     * @return
     */
    private UserDTO registerClientUserForUserToProperty(
            Integer partner, Integer platform, Integer appType, ObjectId appId,
            UserToProperty userToProperty, String password, String code, String pushId,
            UserDevice userDevice, Set<String> roles, Set<Object> communityIds) {

        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setPhone(userToProperty.getPhone());
        registrationDTO.setName(userToProperty.getUserName());
        registrationDTO.setNickName(StringUtil.desensitize(userToProperty.getPhone(), 3, 2));
        registrationDTO.setPassword(password);
        registrationDTO.setCode(code);
        registrationDTO.setPushId(pushId);
        cn.bit.common.facade.user.model.UserDevice device = new cn.bit.common.facade.user.model.UserDevice();
        BeanUtils.copyProperties(userDevice, device);
        registrationDTO.setUserDevice(device);
        registrationDTO.setClient(ClientType.PROPERTY.value());

        registrationDTO.setPartner(partner);
        registrationDTO.setPlatform(platform);
        registrationDTO.setAppType(appType);
        registrationDTO.setAppId(appId);

        registrationDTO.setRoles(roles);
        registrationDTO.setTags(communityIds);
        registrationDTO.setRepeatable(true);
        try {
            log.info("物业app注册:{}", registrationDTO);
            UserDTO userDTO = commonUserFacade.register(registrationDTO);
            log.info("物业app注册client完成");
            return userDTO;
        } catch (InvalidParameterException | cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case cn.bit.common.facade.user.constant.CodeConstants.CODE_INVALID_CODE:
                    throw CODE_NOT_CORRECT;
                case cn.bit.common.facade.user.constant.CodeConstants.CODE_PHONE_REGISTERED:
                    throw PHONE_REGISTERED;
                default:
                    throw e;
            }
        }
    }

    /**
     * 移除物业人员对应的client及角色，删除授权
     *
     * @param userId
     * @param communityId
     * @param postCode
     */
    private void removeRoleAndClientFromCommunityUser(ObjectId userId, ObjectId communityId, String postCode) {
        CommunityUser toGet =
                communityUserRepository.findByCommunityIdAndUserIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
                        communityId, userId, Collections.singleton(ClientType.PROPERTY.value()),
                        Collections.singleton(postCode), DataStatusType.VALID.KEY);
        if (toGet == null) {
            log.info("社区({})用户({})已经不存在当前岗位({})", communityId, userId, postCode);
            return;
        }
        CommunityUser toRemove = new CommunityUser();
        toRemove.setRoles(Collections.singleton(postCode));
        Set<String> roles = toGet.getRoles();
        roles.remove(postCode);
        toRemove.setClients(Collections.singleton(ClientType.PROPERTY.value()));
        log.info("待移除的物业人员信息：{}", toRemove);
        communityUserRepository
                .updateWithUnsetIfNullBuildingIdsAndDistrictIdsAndMiliUIdsThenPullAllClientsAndRolesByCommunityIdAndUserIdAndDataStatus(
                        toRemove, communityId, userId, DataStatusType.VALID.KEY);
        log.info("已移除社区用户的端跟岗位信息");
    }

    /**
     * 从clientUser 移除社区ID / 角色
     *
     * @param userId
     * @param communityId
     * @param postCode
     */
    private void removeCommunityIdAndRoleFromClientUser(Integer partner, ObjectId userId,
                                                        ObjectId communityId, String postCode) {
        List<CommunityUser> communityUsers = communityUserRepository.findByUserIdAndDataStatus(userId,
                DataStatusType.VALID.KEY);
        if (communityUsers.isEmpty()) {
            log.info("removeCommunityIdAndRoleFromClientUser --> community user 已经没有此用户资料，userId:{}", userId);
            return;
        }

        boolean exists = false;
        for (CommunityUser communityUser : communityUsers) {
            if (communityUser.getRoles().contains(postCode) && !communityUser.getCommunityId().equals(communityId)) {
                exists = true;
                break;
            }
        }
        log.info("在其他社区是否存在相同的岗位：{}", exists);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setTags(Collections.singleton(communityId));
        // 如果在其他社区还存在同样的角色，就不删除端用户中对应的角色
        profileDTO.setRoles(exists ? null : Collections.singleton(postCode));
        profileDTO.setClient(ClientType.PROPERTY.value());
        profileDTO.setPartner(partner);
        profileDTO.setUserId(userId);
        try {
            commonUserFacade.removeProfileByClientAndPartnerAndUserId(profileDTO);
        } catch (cn.bit.common.facade.exception.BizException e) {
            switch (e.getSubCode()) {
                case cn.bit.common.facade.user.constant.CodeConstants.CODE_USER_NOT_EXIST:
                    throw USER_NOT_EXITS;
                default:
                    throw e;
            }
        }
    }

    /**
     * 转换实体
     *
     * @param employee
     */
    private UserToProperty fillingUserToProperty(Employee employee) {
        UserToProperty userToProperty = new UserToProperty();
        BeanUtils.copyProperties(employee, userToProperty);
        userToProperty.setId(employee.getId());
        userToProperty.setUserName(employee.getName());
        userToProperty.setPropertyId(employee.getCompanyId());
        userToProperty.setEmployeeId(employee.getNo());
        // 系统企业管理员ID 转换成为 业务企业管理员ID
        if (employee.getRoles() != null && !employee.getRoles().isEmpty()) {
            Set<String> roles = new HashSet<>(employee.getRoles().size());
            for (String role : employee.getRoles()) {
                if (cn.bit.common.facade.system.constant.RoleConstants.ROLE_STR_TENANT_ADMIN.equals(role)
                        || RoleType.COMPANY_ADMIN.name().equals(role)) {
                    role = RoleConstants.ROLE_STR_COMPANY_ADMIN;
                }
                roles.add(role);
            }
            userToProperty.setPostCode(roles);
        }
        return userToProperty;
    }

    /**
     * 填充物业人员授权及岗位信息
     *
     * @param userToProperty
     * @param communityUser
     */
    private void fillingUserToPropertyWithCommunityUser(UserToProperty userToProperty, CommunityUser communityUser) {
        // 回调队列需要用到
        userToProperty.setBuildingIds(communityUser.getBuildingIds());
        userToProperty.setDistrictIds(communityUser.getDistrictIds());
        userToProperty.setMiliUIds(communityUser.getMiliUIds());
        userToProperty.setCommunityId(communityUser.getCommunityId());
//        userToProperty.setPostCode(communityUser.getRoles());
    }

    private List<UserToProperty> fillingUserToPropertyList(List<Employee> employeeList,
                                                           List<CommunityUser> cmUsers, boolean forPaging) {
        Map<ObjectId, Employee> employeeMap = employeeList.stream()
                .collect(Collectors.toMap(Employee::getUserId, employee -> employee));
        List<UserToProperty> list = new ArrayList<>(cmUsers.size());
        cmUsers.forEach(cmUser -> {
            Employee employee = employeeMap.get(cmUser.getUserId());
            UserToProperty userToProperty = null;
            if (employee == null) {
                log.warn("该物业人员不存在公司档案: {}", cmUser);
                if (forPaging) {
                    userToProperty = new UserToProperty();
                }
            } else {
                userToProperty = fillingUserToProperty(employee);
            }

            if (userToProperty != null) {
                fillingUserToPropertyWithCommunityUser(userToProperty, cmUser);
                userToProperty.setPostCode(cmUser.getRoles());
                list.add(userToProperty);
            }
        });
        return list;
    }

    private void fillingUserToPropertyAccId(Integer partner, List<UserToProperty> userToPropertyList) {
        if (userToPropertyList != null && userToPropertyList.size() > 0) {
            // 客服人员需要获取accid
            Set<ObjectId> userIds = userToPropertyList.stream()
                    .map(UserToProperty::getUserId).collect(Collectors.toSet());

            if (userIds.size() > 0) {
                ClientAndPartnerAndUserIdsDTO dto = new ClientAndPartnerAndUserIdsDTO();
                dto.setClient(ClientType.PROPERTY.value());
                dto.setPartner(partner);
                dto.setUserIds(userIds);
                List<cn.bit.common.facade.user.model.User> users = commonUserFacade.listUsersByClientAndPartnerAndUserIds(dto);
                log.info("有客服人员，需要获取网易云信ID，supportStaff imUserList:{}", users);
                Map<ObjectId, String> accIdMap = new HashMap<>();
                users.forEach(user -> accIdMap.put(user.getId(), user.getAccId()));
                userToPropertyList.forEach(
                        userToProperty -> userToProperty.setAccid(accIdMap.get(userToProperty.getUserId())));
            }
        }
    }

    private Employee getEmployeeById(ObjectId id) {
        Employee employee;
        try {
            // 查询企业员工信息
            employee = companyFacade.getEmployeeByEmployeeId(id);
        } catch (cn.bit.common.facade.exception.BizException e) {
            throw UNKNOWN_ERROR;
        }
        return employee;
    }
}
