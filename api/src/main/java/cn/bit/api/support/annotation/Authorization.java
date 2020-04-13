package cn.bit.api.support.annotation;

import java.lang.annotation.*;

/**
 * 用户授权注解，作用于Controller方法上.
 * 标注此注解的方法，均需要做token校验。即http request header中需带有BIT-TOKEN和BIT-UID字段.
 * 如果某方法需要校验权限，那么标注的注解上requiredPermissions值写入所需权限的keys,
 * 如: @Authorization(requiredPermissions = {"a","b"})
 *
 * Created by Terry on 2018/1/17 0017.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorization {

    /**
     * http head token字段名
     * @return
     */
    String tokenField() default "BIT-TOKEN";

    /**
     * http head uid字段名
     * @return
     */
    String uidField() default "BIT-UID";

    /**
     * 方法所需权限
     * @return
     */
    String[] requiredPermissions() default {};

    /**
     * 是否开启api权限校验
     * @return
     */
    boolean verifyApi() default true;
}
