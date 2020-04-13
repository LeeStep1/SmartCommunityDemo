package cn.bit.api.support.interceptor;

import cn.bit.api.support.SessionUtil;
import cn.bit.facade.service.moment.SilentFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class MomentInterceptor implements HandlerInterceptor {

    @Autowired
    private SilentFacade silentFacade;

    /**
     * 调用接口之前执行，仅当 return true 才会继续执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        // 校验是否被禁言
        return !silentFacade.checkSilentUser(SessionUtil.getTokenSubject().getUid(), SessionUtil.getCommunityId());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception e) throws Exception {
    }
}
