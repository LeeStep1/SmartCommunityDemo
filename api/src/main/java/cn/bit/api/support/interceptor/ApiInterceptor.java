package cn.bit.api.support.interceptor;

import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.system.dto.ApiVerificationDTO;
import cn.bit.common.facade.system.enums.ApiMethodEnum;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.common.util.ObjectIdUtils;
import cn.bit.facade.constant.RoleConstants;
import cn.bit.facade.enums.RoleType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

/**
 * API 拦截器
 *
 * @author decai.liu
 * @date 2019-03-13
 */
public class ApiInterceptor implements HandlerInterceptor, InitializingBean {

    @Resource
    private SystemFacade systemFacade;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (!(o instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) o;

        // 获取方法上的 Authorization 注解及 verifyApi 属性
        Authorization annotation = handlerMethod.getMethod().getAnnotation(Authorization.class);
        if (annotation == null || !annotation.verifyApi()) {
            return true;
        }

        String path = (String) httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestMethod = httpServletRequest.getMethod();
        Set<String> roleSet = SessionUtil.getCurrentUser().getRoles();
        // 当前用户没有任何角色
        if (roleSet == null || roleSet.isEmpty()) {
            throw AUTHENCATION_FAILD;
        }
        try {
            ApiMethodEnum apiMethodEnum = ApiMethodEnum.valueOf(requestMethod);
            ApiVerificationDTO dto = new ApiVerificationDTO();
            dto.setClient(SessionUtil.getAppSubject().getClient());
            dto.setApiMethod(apiMethodEnum.value());
            dto.setApiPath(path);
            if (roleSet.contains(RoleType.COMPANY_ADMIN.name())) {
                dto.setRoleIds(Collections.singleton(RoleConstants.ROLE_ID_COMPANY_ADMIN));
            } else {
                if (roleSet.contains(RoleType.HOUSEHOLD.name())) {
                    roleSet.add(RoleConstants.ROLE_STR_HOUSEHOLD);
                }
                dto.setRoleIds(ObjectIdUtils.getValidObjectIdListFromStringCollection(roleSet));
            }
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                throw AUTHENCATION_FAILD;
            }
            List<ObjectId> tenantIds = new ArrayList<>(2);
            ObjectId communityId = SessionUtil.getCommunityId();
            ObjectId companyId = SessionUtil.getCompanyId();
            if (communityId != null) {
                tenantIds.add(communityId);
            }
            if (companyId != null) {
                tenantIds.add(companyId);
            }
            dto.setTenantIds(tenantIds);
            systemFacade.verifyApi(dto);
        } catch (IllegalArgumentException e) {
            // 忽略不支持的API方法，直接通过校验
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
