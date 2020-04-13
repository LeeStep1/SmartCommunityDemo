package cn.bit.framework.utils.validate.field.annotation;

import java.lang.annotation.*;

/**
 * Created by terry on 2016/9/3.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface EmailField {
    String name() default "";
    boolean required() default false;
}
