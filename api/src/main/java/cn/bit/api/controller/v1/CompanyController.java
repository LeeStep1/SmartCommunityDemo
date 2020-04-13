package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.system.dto.RoleDTO;
import cn.bit.common.facade.system.model.Role;
import cn.bit.common.facade.system.query.RolePageQuery;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.vo.company.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物业公司角色及功能配置控制类
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/company", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CompanyController {

    @Resource
    private SystemFacade systemFacade;

/*    @Value("${attendance.funcs}")
    private String attendance;

    @Value("${maintenance.funcs}")
    private String maintenanceFuncs;*/

    @Autowired
    private PushFacade pushFacade;

    // -------------------------------------功能权限---------------------------------------------------------------------
    /**
     * 物业角色列表
     *
     * @return
     */
    @GetMapping(name = "获取物业角色列表", path = "/roles")
    @Authorization
    public ApiResult listRoles(String roleName,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size){
        RolePageQuery query = new RolePageQuery();
        query.setName(roleName);
        // 社区角色只能依赖于物业公司
        query.setTenantId(SessionUtil.getCompanyId());
        query.setPage(page);
        query.setSize(size);
        return ApiResult.ok(systemFacade.listRoles(query));
    }

    @GetMapping(name = "查询角色详情", path = "/roles/{roleId}")
    @Authorization
    public ApiResult getRole(@PathVariable ObjectId roleId) {
        Role role = systemFacade.getRoleByRoleId(roleId);
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        List<PushConfig> configList = pushFacade.listPushConfigsByRoleId(roleId);
        if (CollectionUtils.isNotEmpty(configList)) {
            Set<String> set = new HashSet<>(3);
            for (PushConfig config : configList) {
                if (PushPointEnum.getRoomPointIds().contains(config.getPointId())) {
                    set.add(PushPointEnum.APPLY_ROOM_ATTESTATION.name());
                    continue;
                }
                set.add(config.getPointId());
            }
            roleVO.setPushPoints(set);
        }
        return ApiResult.ok(roleVO);
    }

    @PostMapping(name = "保存角色", path = "/roles/save")
    @Authorization
    public ApiResult saveRole(@RequestBody RoleVO roleVO) {
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(roleVO, roleDTO);
        if (roleDTO.getId() == null && roleDTO.getTenantId() == null) {
            roleDTO.setTenantId(SessionUtil.getCompanyId());
        }
        Set<String> points = roleVO.getPushPoints();
        if (roleDTO.getId() == null ) {
            Role role = systemFacade.createRole(roleDTO);
            if (CollectionUtils.isNotEmpty(points)) {
                Set<String> needAddToSetPoints = new HashSet<>();
                if (points.contains(PushPointEnum.ALARM.name())) {
                    needAddToSetPoints.add(PushPointEnum.ALARM.name());
                }
                if (points.contains(PushPointEnum.FAULT.name())) {
                    needAddToSetPoints.add(PushPointEnum.FAULT.name());
                }
                if (points.contains(PushPointEnum.FAULT_ALLOCATED.name())) {
                    needAddToSetPoints.add(PushPointEnum.FAULT_ALLOCATED.name());
                }
                if (points.contains(PushPointEnum.APPLY_ROOM_ATTESTATION.name())) {
                    needAddToSetPoints.addAll(PushPointEnum.getRoomPointIds());
                }
                pushFacade.updatePushConfigWithAddToSetTargetsByPointIdsAndCompanyId(role.getId(), needAddToSetPoints, SessionUtil.getCompanyId());
            }
            return ApiResult.ok(role);
        }
        // 待移除roleId的推送节点集合
        Set<String> needRemovePoints = new HashSet<>();
        // 待加入roleId的推送节点集合
        Set<String> needAddToSetPoints = new HashSet<>();
        if (CollectionUtils.isEmpty(points)) {
            needRemovePoints.addAll(PushPointEnum.getRoomPointIds());
            needRemovePoints.add(PushPointEnum.ALARM.name());
            needRemovePoints.add(PushPointEnum.FAULT.name());
            needRemovePoints.add(PushPointEnum.FAULT_ALLOCATED.name());
        } else {
            if (points.contains(PushPointEnum.ALARM.name())) {
                needAddToSetPoints.add(PushPointEnum.ALARM.name());
            } else {
                needRemovePoints.add(PushPointEnum.ALARM.name());
            }
            if (points.contains(PushPointEnum.FAULT.name())) {
                needAddToSetPoints.add(PushPointEnum.FAULT.name());
            } else {
                needRemovePoints.add(PushPointEnum.FAULT.name());
            }
            if (points.contains(PushPointEnum.FAULT_ALLOCATED.name())) {
                needAddToSetPoints.add(PushPointEnum.FAULT_ALLOCATED.name());
            } else {
                needRemovePoints.add(PushPointEnum.FAULT_ALLOCATED.name());
            }
            if (points.contains(PushPointEnum.APPLY_ROOM_ATTESTATION.name())) {
                needAddToSetPoints.addAll(PushPointEnum.getRoomPointIds());
            } else {
                needRemovePoints.addAll(PushPointEnum.getRoomPointIds());
            }
        }
        if (CollectionUtils.isNotEmpty(needRemovePoints)) {
            pushFacade.updatePushConfigWithPullAllTargetsByPointIdsAndCompanyId(
                    roleDTO.getId(), needRemovePoints, SessionUtil.getCompanyId());
        }
        if (CollectionUtils.isNotEmpty(needAddToSetPoints)) {
            pushFacade.updatePushConfigWithAddToSetTargetsByPointIdsAndCompanyId(
                    roleDTO.getId(), needAddToSetPoints, SessionUtil.getCompanyId());
        }
        return ApiResult.ok(systemFacade.modifyRole(roleDTO));
    }

    @PostMapping(name = "删除角色", path = "/roles/{roleId}/remove")
    @Authorization
    public ApiResult removeRole(@PathVariable ObjectId roleId) {
        systemFacade.removeRoleByRoleId(roleId);
        return ApiResult.ok();
    }

    @GetMapping(name = "查询租客功能", path = "/{tenantId}/funcs")
    @Authorization
    public ApiResult getTenantFunctions(@PathVariable ObjectId tenantId) {
        return ApiResult.ok(systemFacade.listFunctionsByTenantId(tenantId));
    }

    @GetMapping(name = "通过标准功能的类型及子类型查询功能树", path = "/templates/standard/func/tree")
    @Authorization
    public ApiResult getFuncTree(@RequestParam Integer type, Integer subtype) {
        return ApiResult.ok(systemFacade.getFuncTreeByTypeAndSubtype(type, subtype));
    }

    @GetMapping(name = "通过标准功能的类型及子类型查询功能组", path = "/templates/standard/func/group/tree")
    @Authorization
    public ApiResult getFuncGroupTree(@RequestParam Integer type, Integer subtype) {
        return ApiResult.ok(systemFacade.getFuncGroupTreeByTypeAndSubtype(type, subtype));
    }

    /*@GetMapping(name = "打卡角色列表", path = "/roles/attendance")
    @Authorization
    public ApiResult listRolesForAttendance() {
        return ApiResult.ok(
                systemFacade.listRolesByTenantIdAndFuncs(SessionUtil.getCompanyId(), Collections.singleton(attendance)));
    }

    @GetMapping(name = "维修工单角色列表", path = "/roles/maintenance")
    @Authorization
    public ApiResult listRolesForMaintenance() {

        return ApiResult.ok(
                systemFacade.listRolesByTenantIdAndFuncs(SessionUtil.getCompanyId(), Collections.singleton(maintenanceFuncs)));
    }*/

    @GetMapping(name = "维修工单角色列表", path = "/roles/maintenance")
    @Authorization
    public ApiResult listRolesForMaintenance() {
        PushConfig pushConfig = pushFacade.findPushConfigByCompanyIdAndPointId(
                SessionUtil.getCompanyId(), PushPointEnum.FAULT_ALLOCATED.name());
        if (pushConfig == null || CollectionUtils.isEmpty(pushConfig.getTargets())) {
            return ApiResult.ok();
        }
        Set<ObjectId> roleIds = pushConfig.getTargets().stream().map(ObjectId::new).collect(Collectors.toSet());
        return ApiResult.ok(systemFacade.listRolesByRoleIds(roleIds));
    }

}