package cn.bit.api.support.interceptor;

import cn.bit.api.support.SessionUtil;
import cn.bit.common.facade.enums.OsEnum;
import cn.bit.common.facade.system.dto.AppDTO;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.enums.ClientType;
import cn.bit.framework.utils.string.StringUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;

/**
 * 应用拦截器，用于校验请求头“APP-ID”，并记录应用的相关信息
 *
 * @author jianming.fan
 * @date 2018-11-08
 */
public class AppInterceptor implements HandlerInterceptor {

    private static final String HEADER_APP_ID = "APP-ID";

    private static final String HEADER_CLIENT = "CLIENT";

    private static final String HEADER_OS = "OS";

    private static final int DEFAULT_PARTNER = 0;

    @Resource
    private ConfigurableBeanFactory beanFactory;

    @Resource
    private SystemFacade systemFacade;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        AppDTO app = getApp(request);
        OsEnum osEnum = OsEnum.fromValue(app.getOs());
        SessionUtil.bindAppSubject(app.getId(), app.getClient(), app.getPartner(), osEnum, app.getAccAppId(),
                app.getPushAppId());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    private AppDTO getApp(HttpServletRequest request) {
        String appIdStr = request.getHeader(HEADER_APP_ID);
        appIdStr = StringUtil.isBlank(appIdStr) ? request.getParameter(HEADER_APP_ID) : appIdStr;
        if (StringUtil.isBlank(appIdStr)) {
            return getAppByClientAndOs(request);
        }

        ObjectId appId;
        try {
            appId = new ObjectId(appIdStr);
        } catch (IllegalArgumentException e) {
            throw AUTHENCATION_FAILD;
        }

        AppDTO app = systemFacade.getAppBasicByAppId(appId);
        if (app == null) {
            throw AUTHENCATION_FAILD;
        }
        return app;
    }

    private AppDTO getAppByClientAndOs(HttpServletRequest request) {
        try {
            int client = request.getIntHeader(HEADER_CLIENT);
            int os = request.getIntHeader(HEADER_OS);
            ObjectId appId = new ObjectId(beanFactory.resolveEmbeddedValue("${" + client + "." + os + ".appid}"));

            AppDTO appDTO = new AppDTO();
            appDTO.setId(appId);
            appDTO.setClient(ClientType.MANAGER_BACKSTAGE.value() == client ? ClientType.PROPERTY.value() : client);
            appDTO.setPartner(DEFAULT_PARTNER);
            appDTO.setOs(os);
            return appDTO;
        } catch (Exception e) {
            throw AUTHENCATION_FAILD;
        }
    }
}
