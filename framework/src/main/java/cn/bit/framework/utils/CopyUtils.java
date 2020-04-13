package cn.bit.framework.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 拷贝对象
 */
public class CopyUtils {
    /**
     * ===========================================【拷贝属性】===========================================
     */


    /**
     * <p>
     * 拷贝对象
     * </p>
     *
     * @param desc
     *            目标对象
     * @param orig
     *            源对象
     * @param excludeNull
     *            是否忽略NULL值的字段
     */
    public static void copy(Object desc, Object orig, boolean excludeNull) {
        if (desc == null || orig == null) {
            return;
        }
        List<Field> descFields = fields(desc.getClass());
        List<Field> origFields = fields(orig.getClass());
        Map<String, Field> descFieldMap = new java.util.HashMap<String, Field>();
        for (Field f : descFields) {
            descFieldMap.put(f.getName(), f);
        }
        Field descField;
        Object oValue;
        for (Field f : origFields) {
            descField = descFieldMap.get(f.getName());
            if (descField != null
                    && (descField.getType().equals(f.getType()) || descField.getType().isAssignableFrom(f.getType()))) {
                try {
                    oValue = fieldValue(orig, f);
                    if (oValue == null) {
                        if (!excludeNull) {
                            putFieldValue(desc, descField, null);
                        }
                    }
                    else {
                        putFieldValue(desc, descField, oValue);
                    }
                }
                catch (Exception e) {
                }
            }
        }
    }

    /**
     * <p>
     * 拷贝对象
     * </p>
     *
     * @param desc
     *            目标对象
     * @param orig
     *            源对象
     * @param includeFields
     *            指定字段
     * @param excludeFields
     *            忽略字段
     * @param excludeNull
     *            是否忽略NULL值的字段
     */
    public static void copy(Object desc, Object orig,
                            Set<String> includeFields, Set<String> excludeFields,
                            boolean excludeNull) {
        if (desc == null || orig == null) {
            return;
        }
        List<Field> descFields = fields(desc.getClass());
        List<Field> origFields = fields(orig.getClass());
        Map<String, Field> descFieldMap = new java.util.HashMap<String, Field>();
        for (Field f : descFields) {
            if (includeFields != null && !includeFields.contains(f.getName())
                    || excludeFields != null
                    && excludeFields.contains(f.getName())) {
                continue;
            }
            descFieldMap.put(f.getName(), f);
        }
        Field descField;
        Object oValue;
        for (Field f : origFields) {
            descField = descFieldMap.get(f.getName());
            if (descField != null
                    && (descField.getType().equals(f.getType()) || descField.getType().isAssignableFrom(f.getType()))) {
                try {
                    oValue = fieldValue(orig, f);
                    if (oValue == null) {
                        if (!excludeNull) {
                            putFieldValue(desc, descField, null);
                        }
                    }
                    else {
                        putFieldValue(desc, descField, oValue);
                    }
                }
                catch (Exception e) {
                }
            }
        }
    }

    /**
     * <p>
     * 获取类中所有字段,包括父类
     * </p>
     *
     * @param clazz
     *            类名
     * @param deep
     *            取父类深度，负数取所有字段
     * @return List [Field]
     */
    public static List<Field> fields(Class<?> clazz, int deep) {
        if (clazz == null) {
            return null;
        }
        java.util.List<Field> fields = new java.util.ArrayList<Field>();
        Class<?> tClazz = clazz;
        int n = 0;
        while (!tClazz.equals(Object.class) && (deep <= 0 || (n++) < deep)) {
            try {
                Field[] fieldArr = tClazz.getDeclaredFields();
                java.util.Collections.addAll(fields, fieldArr);
            }
            catch (SecurityException e) {
            }
            tClazz = tClazz.getSuperclass();
        }
        return fields;
    }

    /**
     * <p>
     * 获取类中所有字段
     * </p>
     *
     * @param clazz
     * @return List [Field]
     */
    public static List<Field> fields(Class<?> clazz) {
        return fields(clazz, -1);
    }

    /**
     * <p>
     * 获取字段值
     * </p>
     *
     * @param obj
     *            Object
     * @param field
     *            Field
     * @return Object
     */
    public static Object fieldValue(Object obj, Field field) {
        if (obj == null || field == null) {
            return null;
        }
        String fieldName = field.getName();
        Method method = null;
        String methodName = (char) (fieldName.charAt(0) & 0xDF)
                + fieldName.substring(1);
        Class<?> clazz = obj.getClass();
        if (field.getType() == Boolean.class
                || field.getType() == boolean.class) {
            try {
                method = clazz.getMethod("is" + methodName);
                if (Modifier.isPublic(method.getModifiers())) {
                    return method.invoke(obj);
                }
            }
            catch (Exception e) {
            }
        }
        try {
            method = clazz.getMethod("get" + methodName);
            if (Modifier.isPublic(method.getModifiers())) {
                return method.invoke(obj);
            }
        }
        catch (Exception e) {
        }
        if (Modifier.isPublic(field.getModifiers())) {
            try {
                return field.get(obj);
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * <p>
     * 获取字段值
     * </p>
     *
     * @param obj
     *            Object
     * @param fieldName
     *            String
     * @return Object
     */
    public static Object fieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null
                || (fieldName = fieldName.trim()).isEmpty()) {
            return null;
        }
        Method method = null;
        String methodName = (char) (fieldName.charAt(0) & 0xDF)
                + fieldName.substring(1);
        Class<?> clazz = obj.getClass();
        try {
            method = clazz.getMethod("is" + methodName);
            if (Modifier.isPublic(method.getModifiers())) {
                Object val = method.invoke(obj);
                if (val instanceof Boolean) {
                    return val;
                }
            }
        }
        catch (Exception e) {
        }
        try {
            method = clazz.getMethod("get" + methodName);
            if (Modifier.isPublic(method.getModifiers())) {
                return method.invoke(obj);
            }
        }
        catch (Exception e) {
        }
        Field field = null;
        while (!clazz.equals(Object.class)) {
            try {
                field = clazz.getDeclaredField(fieldName);
            }
            catch (Exception e) {
            }
            if (field != null) {
                try {
                    if (field.isAccessible()) {
                        return field.get(obj);
                    }
                    else {
                        field.setAccessible(true);
                        Object val = field.get(obj);
                        field.setAccessible(false);
                        return val;
                    }
                }
                catch (Exception e) {
                    return null;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * <p>
     * 设置对象指定字段值
     * </p>
     *
     * @param object
     * @param fieldName
     * @param oValue
     * @return boolean
     */
    public static boolean putFieldValue(Object object, String fieldName,
                                        Object oValue) {
        if (object == null) {
            return false;
        }
        Field field = field(object.getClass(), fieldName);
        return putFieldValue(object, field, oValue);
    }

    /**
     * <p>
     * 设置对象指定字段值
     * </p>
     *
     * @param object
     *            Object
     * @param field
     *            Field
     * @param oValue
     *            Object
     * @return boolean
     */
    public static boolean putFieldValue(Object object, Field field,
                                        Object oValue) {
        if (object == null || field == null) {
            return false;
        }
        boolean flag = false;
        Class<?> clazz = object.getClass();
        try {
            if (Modifier.isPublic(field.getModifiers())) {
                field.set(object, oValue);
            }
            else {
                String methodName = "set"
                        + ((char) (field.getName().charAt(0) & 0xDF))
                        + field.getName().substring(1);
                Method method = clazz.getMethod(methodName, new Class[] { field.getType() });
                if (Modifier.isPublic(method.getModifiers())) {
                    method.invoke(object, new Object[] { oValue });
                }
            }
        }
        catch (Exception e) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
                try {
                    field.set(object, oValue);
                    flag = true;
                }
                catch (Exception e1) {
                }
                field.setAccessible(false);
            }
        }
        return flag;
    }

    /**
     * <p>
     * 获取类中的属性
     * </p>
     *
     * @param clazz
     *            类名
     * @param fieldName
     *            字段名称
     * @return Field 字段属性
     */
    public static Field field(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null
                || (fieldName = fieldName.trim()).length() == 0) {
            return null;
        }
        Class<?> tClazz = clazz;
        Field field = null;
        while (!tClazz.equals(Object.class)) {
            try {
                field = tClazz.getDeclaredField(fieldName);
                if (field != null) {
                    break;
                }
            }
            catch (Exception e) {
            }
            tClazz = tClazz.getSuperclass();
        }
        return field;
    }
}
