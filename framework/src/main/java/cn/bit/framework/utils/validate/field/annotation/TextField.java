package cn.bit.framework.utils.validate.field.annotation;

import java.lang.annotation.*;

/**
 * Created by terry on 2016/9/2.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface TextField{

    String name() default "";
    int minlength() default 0;
    int maxlength() default 0;
    boolean required() default false;
}
