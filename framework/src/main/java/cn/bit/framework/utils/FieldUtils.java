package cn.bit.framework.utils;/**
 * Created by terry on 2016/9/7.
 */

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author terry
 * @create 2016-09-07 18:48
 **/
public class FieldUtils extends org.apache.commons.lang3.reflect.FieldUtils {

    public static Collection<Field> getAllDeclaredFields(Class<?> cls) {
        List<Field> fields = new LinkedList<>();
        Stack<Class> stack = new Stack<>();
        stack.push(cls);
        while (!stack.empty()) {
            cls = stack.pop();
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
            if (cls.getSuperclass() != Object.class)
                stack.push(cls.getSuperclass());
        }
        return fields;
    }

    public static boolean isSimpleField(final Class<?> cls, final String fieldName) {
        Field field = getField(cls, fieldName, true);
        return isSimpleField(field);
    }

    public static boolean isSimpleField(final Field field) {
        if (field == null) {
            return false;
        }
        Class type = field.getType();
        if (type.isPrimitive()) {
            return true;
        }
        if (type.isAssignableFrom(String.class)) {
            return true;
        }
        if (type.isAssignableFrom(Long.class)) {
            return true;
        }
        if (type.isAssignableFrom(Integer.class)) {
            return true;
        }
        if (type.isAssignableFrom(Double.class)) {
            return true;
        }
        if (type.isAssignableFrom(BigDecimal.class)) {
            return true;
        }
        if (type.isAssignableFrom(Character.class)) {
            return true;
        }
        if (type.isAssignableFrom(Float.class)) {
            return true;
        }
        if (type.isAssignableFrom(Boolean.class)) {
            return true;
        }
        if (type.isAssignableFrom(Byte.class)) {
            return true;
        }
        return false;
    }
}
