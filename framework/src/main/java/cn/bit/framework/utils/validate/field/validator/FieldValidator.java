package cn.bit.framework.utils.validate.field.validator;

import cn.bit.framework.utils.validate.field.FieldValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created by terry on 2016/9/2.
 */
public interface FieldValidator<T extends Annotation> {

    void validate(Object target, Field field, T fieldAnnotation)throws FieldValidationException;

}
