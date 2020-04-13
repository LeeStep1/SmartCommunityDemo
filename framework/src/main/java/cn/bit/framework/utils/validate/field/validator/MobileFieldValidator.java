package cn.bit.framework.utils.validate.field.validator;/**
 * Created by terry on 2016/9/3.
 */

import cn.bit.framework.utils.validate.field.annotation.MobileField;
import cn.bit.framework.utils.validate.ValidateUtils;
import cn.bit.framework.utils.validate.field.FieldValidationException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * @author terry
 * @create 2016-09-03 11:12
 **/
public class MobileFieldValidator implements FieldValidator<MobileField> {

    private static final String PATTERN_IS_REQUIRED = "field [%s] is required";
    private static final String PATTERN_MOBILE_INVALID = "field [%s] mobile pattern invalid";
    @Override
    public void validate(Object target, Field field, MobileField fieldAnnotation) throws FieldValidationException {
        try {
            Object value = field.get(target);
            String fieldName = StringUtils.isBlank(fieldAnnotation.name()) ? field.getName() : fieldAnnotation.name();
            if (field.getType() != String.class)
                return;
            if (StringUtils.isBlank((String) value)) {
                if (fieldAnnotation.required())
                    throw new FieldValidationException(String.format(PATTERN_IS_REQUIRED, fieldName));
            } else {
                if (!ValidateUtils.isMobile((String) value))
                    throw new FieldValidationException(String.format(PATTERN_MOBILE_INVALID, fieldName));
            }

        } catch (IllegalAccessException e) {
            throw new FieldValidationException();
        }

    }
}
