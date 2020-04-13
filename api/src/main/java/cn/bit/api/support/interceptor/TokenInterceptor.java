package cn.bit.api.support.interceptor;

import cn.bit.api.support.AppSubject;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.system.dto.ApiDTO;
import cn.bit.common.facade.system.enums.ApiMethodEnum;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.exceptions.BizException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;

/**
 * Created by terry on 2018/1/15.
 */
public class TokenInterceptor implements HandlerInterceptor, InitializingBean {

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Resource
    private UserFacade userFacade;

    @Resource
    private SystemFacade systemFacade;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod))
            return true;

        // 处理token验证
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取方法上的Authorization注解
        Authorization authAnnotation = this.getMethodAuthAnnotation(handlerMethod.getMethod());

        // 如果方法上没有标注Authorization，说明无需做鉴权，直接返回
        if (authAnnotation == null) {
            return true;
        }

        String token = request.getHeader(authAnnotation.tokenField());
        String uid = request.getHeader(authAnnotation.uidField());
        token = StringUtils.isBlank(token) ? request.getParameter(authAnnotation.tokenField()) : token;
        uid = StringUtils.isBlank(uid) ? request.getParameter(authAnnotation.uidField()) : uid;
        if (StringUtils.isBlank(token) || StringUtils.isBlank(uid)) {
            throw BizException.TOKEN_INVALID;
        }

        ObjectId _uid;
        try {
            _uid = new ObjectId(uid);
        } catch (IllegalArgumentException e) {
            throw BizException.TOKEN_INVALID;
        }

        AppSubject appSubject = SessionUtil.getAppSubject();

        // 校验token
        userFacade.verifyToken(appSubject.getClient(), appSubject.getPartner(), appSubject.getOsEnum().platform().value(),
                appSubject.getOsEnum().appType().value(), appSubject.getAppId(), token, _uid);

        /**
         * 如Authorization注解的requiredPermissions字段不为空，则需校验用户权限
         */
        if (authAnnotation.requiredPermissions().length > 0
                && !checkPermissions(appSubject.getClient(), appSubject.getPartner(), _uid, authAnnotation.requiredPermissions())) {
            throw AUTHENCATION_FAILD;
        }

        SessionUtil.bindTokenSubject(token, _uid);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object handler, Exception e) throws Exception {
    }

    private Authorization getMethodAuthAnnotation(Method method) {
        Authorization annotation = method.getAnnotation(Authorization.class);
        return annotation;
    }

    /**
     * 检查用户权限
     *
     * @param client              客户端代码
     * @param uid                 用户id
     * @param requiredPermissions 必要权限
     * @return
     */
    private boolean checkPermissions(Integer client, Integer partner, ObjectId uid, String[] requiredPermissions) {
        UserVO uv = userFacade.getUserById(client, partner, uid);

        if (uv == null) {
            return false;
        }

        // 如用户是ADMIN角色，直接跳过
        if (uv.getRoles().contains("ADMIN")) {
            return true;
        }
        // 匹配权限 取交集
        if (CollectionUtils.intersection(
                Arrays.asList(uv.getPermissions()), Arrays.asList(requiredPermissions)).isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoMap = requestMappingHandlerMapping.getHandlerMethods();
        List<ApiDTO> apiDTOs = new ArrayList<>(500);
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : requestMappingInfoMap.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            for (String path : mappingInfo.getPatternsCondition().getPatterns()) {
                if (path.startsWith("/error")) {
                    continue;
                }
                for (RequestMethod method : mappingInfo.getMethodsCondition().getMethods()) {
                    try {
                        ApiMethodEnum apiMethodEnum = ApiMethodEnum.valueOf(method.name());
                        ApiDTO apiDTO = new ApiDTO();
                        apiDTO.setName(mappingInfo.getName());
                        apiDTO.setMethod(apiMethodEnum.value());
                        apiDTO.setPath(path);
                        apiDTO.setDeprecated(handlerMethod.hasMethodAnnotation(Deprecated.class));
                        apiDTOs.add(apiDTO);

                        if (apiDTOs.size() == 500) {
                            systemFacade.saveApis(apiDTOs);
                            apiDTOs.clear();
                        }
                    } catch (IllegalArgumentException e) {
                        // 忽略不支持的RequestMethod
                    }
                }
            }
        }
        if (apiDTOs.size() > 0) {
            systemFacade.saveApis(apiDTOs);
        }
    }
}
