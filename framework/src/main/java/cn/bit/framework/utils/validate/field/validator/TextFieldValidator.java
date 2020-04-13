package cn.bit.framework.utils.validate.field.validator;/**
 * Created by terry on 2016/9/2.
 */

import cn.bit.framework.utils.validate.field.FieldValidationException;
import cn.bit.framework.utils.validate.field.annotation.TextField;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * @author terry
 * @create 2016-09-02 18:42
 **/
public class TextFieldValidator implements FieldValidator<TextField> {

    private static final String PATTERN_NOT_STRING = "field [%s] is not string field";
    private static final String PATTERN_IS_REQUIRED = "field [%s] is required";
    private static final String PATTERN_IS_TOO_LONG = "field [%s] is too long, the length cannot be greater than %s";
    private static final String PATTERN_IS_TOO_SHORT = "field [%s] is too short, the length cannot be less than %s";

    @Override
    public void validate(Object target, Field field, TextField fieldAnnotation) throws FieldValidationException {
        try {
            Object value = field.get(target);
            String fieldName = StringUtils.isBlank(fieldAnnotation.name()) ? field.getName() : fieldAnnotation.name();
            if (field.getType() != String.class)
                return;
            if (StringUtils.isBlank((String) value) && fieldAnnotation.required())
                throw new FieldValidationException(String.format(PATTERN_IS_REQUIRED, fieldName));

            if (!StringUtils.isBlank((String) value)) {

                if (fieldAnnotation.maxlength() > 0 && ((String) value).length() > fieldAnnotation.maxlength())
                    throw new FieldValidationException(String.format(PATTERN_IS_TOO_LONG, fieldName,
                            fieldAnnotation.maxlength()));

                if (fieldAnnotation.minlength() > 0 && ((String) value).length() < fieldAnnotation.minlength())
                    throw new FieldValidationException(String.format(PATTERN_IS_TOO_SHORT, fieldName,
                            fieldAnnotation.minlength()));
            }
        } catch (IllegalAccessException e) {
            throw new FieldValidationException();
        }
    }
}
