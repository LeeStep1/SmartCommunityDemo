package cn.bit.framework.utils.validate.field;/**
 * Created by terry on 2016/9/2.
 */


import cn.bit.framework.utils.FieldUtils;
import cn.bit.framework.utils.validate.field.annotation.MobileField;
import cn.bit.framework.utils.validate.field.validator.MobileFieldValidator;
import cn.bit.framework.utils.validate.field.validator.FieldValidator;
import cn.bit.framework.utils.validate.field.validator.TextFieldValidator;
import cn.bit.framework.utils.validate.field.annotation.TextField;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author terry
 * @create 2016-09-02 18:44
 **/
public class FieldValidationUtil {

    private static Map<Class<? extends Annotation>, FieldValidator> validators = new HashMap<>();

    public static Collection<FieldValidator> getValidators() {
        return validators.values();
    }

    public void setValidators(Collection<FieldValidator> validators) {
        validators.forEach(FieldValidationUtil::registerValidator);
    }

    public static <T extends Annotation> void registerValidator(FieldValidator<T> validator) {
        Class<T> annotationCls = getAnnotationCls(validator);
        validators.putIfAbsent(annotationCls, validator);
    }

    private static <T extends Annotation> Class<T> getAnnotationCls(FieldValidator<T> validator) {

        try {
            Type itf = null;
            for (Type intf : validator.getClass().getGenericInterfaces()) {
                if (intf.getTypeName().contains(".FieldValidator")) {
                    itf = intf;
                    break;
                }
            }

            Method method = itf.getClass().getMethod("getActualTypeArguments");

            Type[] types = (Type[]) method.invoke(itf);
            return (Class<T>) types[0];
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void validate(Object bean) throws FieldValidationException {

        Collection<Field> fields = FieldUtils.getAllDeclaredFields(bean.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            for (Annotation annotation : field.getAnnotations()) {
                FieldValidator validator = validators.get(annotation.annotationType());
                if (validator != null)
                    validator.validate(bean, field, annotation);
            }
        }
    }

    private static class TestBean implements Serializable {



        @TextField(maxlength = 20, required = true)
        private String name;

        @MobileField
        private String mobile;


        public TestBean(String name,String mobile) {
            this.name = name;
            this.mobile = mobile;
        }
    }

    public static void main(String[] args) {
        TestBean testBean = new TestBean("222","13a56389860921");
        FieldValidationUtil.registerValidator(new TextFieldValidator());
        FieldValidationUtil.registerValidator(new MobileFieldValidator());
        FieldValidationUtil.validate(testBean);
    }
}
