package cn.bit.api.support.aop;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.annotation.InHand;
import cn.bit.facade.enums.FaultItemType;
import cn.bit.facade.model.property.Gtaskzs;
import cn.bit.facade.service.property.GtaskzsFacade;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by fxiao
 * on 2018/3/23
 */
@Aspect
@Component
@Slf4j
public class InHandAspect {

    private GtaskzsFacade gtaskzsFacade;

    @Autowired
    public InHandAspect(GtaskzsFacade gtaskzsFacade) {
        this.gtaskzsFacade = gtaskzsFacade;
    }

    @Pointcut("@annotation(cn.bit.api.support.annotation.InHand)") // 注解声明切点
    public void annotationPointcut() {
    };

    @AfterReturning(returning="rvt", pointcut="annotationPointcut()")
    public void after(JoinPoint joinPoint, Object rvt) throws ClassNotFoundException, NotFoundException {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 获取注解的字段
        InHand annotation = method.getAnnotation(InHand.class);
        // 数据状态（1：未处理；0：已处理）
        Integer disposeStatus = annotation.dataStatus().getKey();
        // 任务类型
        Integer taskType = annotation.taskType().getKey();
        // 获取返回数据
        ApiResult result = (ApiResult) rvt;
        if (result != null && result.getErrorCode() == 0) {
            editTask(result.getData(), taskType, disposeStatus);
        }
    }

    private void editTask(Object arg, Integer taskType, Integer disposeStatus){
        if (taskType == null) {
            return;
        }
        switch (taskType){
            case 0:
                break;
            // 故障报修
            case 1:
                recordFault(arg, taskType, disposeStatus);
                break;
            default:break;
        }
    }

    /**
     * 单个对象的某个键的值
     * @param obj 对象
     * @return Object 键在对象中所对应得值 没有查到时返回空字符串
     */
    public static Object getValueByKey(Object obj, String key) {
        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            // 设置些属性是可以访问的
            f.setAccessible(true);
            try {
                if (f.getName().endsWith(key)) {
                    return f.get(obj);
                }
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException:", e);
            } catch (IllegalAccessException e) {
                log.error("IllegalAccessException:", e);
            }
        }
        // 没有查到时返回空字符串
        return "";
    }


    private void recordFault(Object arg, Integer taskType, Integer disposeStatus) {
        // 获取故障ID
        Object id = getValueByKey(arg, "id");
        // 获取用户名称
        Object faultItem = getValueByKey(arg, "faultItem");

        // 获取故障地址
        Object address = getValueByKey(arg, "faultAddress");
        // 获取社区ID
        Object communityId = getValueByKey(arg, "communityId");
        // 标题
        StringBuffer title = new StringBuffer();
        if (faultItem == null) {
            title.append(address).append("发生").append("故障。请尽快处理！");
        } else {
            title.append(address).append("发生").append(FaultItemType.getValueByKey((int) faultItem)).append("故障。请尽快处理！");
        }
        Gtaskzs gtasks = new Gtaskzs();
        if (id != null) {
            gtasks.setOtherId(new ObjectId(id.toString()));
        }
        gtasks.setTaskType(taskType);
        gtasks.setTitle(title.toString());
        if (communityId != null) {
            gtasks.setCommunityId(new ObjectId(communityId.toString()));
        }
        // 删除待办任务
        if (disposeStatus == 0) {
            gtaskzsFacade.deleteGtaskzs(gtasks);
            return;
        }
        gtaskzsFacade.addGtaskzs(gtasks);
    }

}
