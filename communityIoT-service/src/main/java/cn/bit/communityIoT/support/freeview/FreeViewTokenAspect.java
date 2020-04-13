package cn.bit.communityIoT.support.freeview;

import cn.bit.facade.exception.communityIoT.CommunityIoTBizException;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.FreeViewTokenFacade;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.INVALID_TOKEN;

/**
 * @author : xiaoxi.lao
 * @Description : 全视通切面
 * @Date ： 2018/9/17 10:13
 */
@Component
@Aspect
@Slf4j
public class FreeViewTokenAspect {
    @Autowired
    private DoorFacade doorFacade;

    @Autowired
    private FreeViewTokenFacade freeViewTokenFacade;

    @Before("@annotation(cn.bit.communityIoT.support.freeview.FreeViewToken)")
    public void beforeMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        FreeViewToken annotation = method.getAnnotation(FreeViewToken.class);
        if (annotation.token() == FreeViewToken.Token.None) {
            return;
        }
        getFreeViewToken();
    }


    @AfterThrowing(value = "@annotation(cn.bit.communityIoT.support.freeview.FreeViewToken)", throwing="exception")
    public void afterMethodThrowing(JoinPoint joinPoint, Throwable exception) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        FreeViewToken annotation = method.getAnnotation(FreeViewToken.class);
        if (annotation.token() == FreeViewToken.Token.None) {
            return;
        }
        if (exception instanceof CommunityIoTBizException && INVALID_TOKEN.getMessage().equals(exception.getMessage())) {
            freeViewTokenFacade.updateToken(doorFacade.getAccessToken());
        }
    }

    /**
     * 从redis中获取access_token
     */
    private void getFreeViewToken() {
        doorFacade.setAccessToken(freeViewTokenFacade.getToken());
    }
}
