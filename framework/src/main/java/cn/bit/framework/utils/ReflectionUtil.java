package cn.bit.framework.utils;

import cn.bit.framework.data.jdbc.DynamicField;
import cn.bit.framework.data.jdbc.NotDbField;
import cn.bit.framework.data.jdbc.PrimaryKeyField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射工具类
 * 
 * @author zhengj
 * @since 1.0 2011-4-4
 */
public class ReflectionUtil {
    private ReflectionUtil() {
    }

    /**
     * 取得 getter 方法列表
     * 
     * @param clazz 类
     * @return getter 方法列表
     */
    public static List<Method> getterList(Class<?> clazz) {
        List<Method> methodList = new ArrayList<Method>();

        Method[] getters = clazz.getMethods();
        for (Method getter : getters) {
            String name = getter.getName();
            if (name.equals("getClass") || getter.getParameterTypes().length != 0)
                continue;

            Class<?> returnClazz = getter.getReturnType();

            if (name.startsWith("is") && (returnClazz == Boolean.class || returnClazz == boolean.class))
                methodList.add(getter);
            else if (name.startsWith("get"))
                methodList.add(getter);
        }

        return methodList;
    }

    /**
     * 取得 setter 方法列表
     * 
     * @param clazz 类
     * @return setter 方法列表
     */
    public static List<Method> setterList(Class<?> clazz) {
        List<Method> methodList = new ArrayList<Method>();

        Method[] setters = clazz.getMethods();
        for (Method setter : setters) {
            String name = setter.getName();
            if (name.startsWith("set") && setter.getParameterTypes().length == 1) {
                methodList.add(setter);
            }

        }

        return methodList;
    }

    /**
     * 取得 getter 方法
     * 
     * @param clazz 类
     * @param field 字段
     * @return getter 方法
     */
    public static Method getter(Class<?> clazz, Field field) {
        String prefix = field.getName().substring(0, 1).toUpperCase();
        String name = prefix + field.getName().substring(1);
        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
            name = "is" + name;
        } else {
            name = "get" + name;
        }
        try {
            Method method = clazz.getMethod(name);
            return method;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 取得 setter 方法
     * 
     * @param clazz 类
     * @param field 字段
     * @return setter 方法
     */
    public static Method setter(Class<?> clazz, Field field) {
        String prefix = field.getName().substring(0, 1).toUpperCase();
        String name = "set" + prefix + field.getName().substring(1);
        try {
            Method method = clazz.getMethod(name, field.getType());
            return method;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 取得 getter 方法
     * 
     * @param clazz 类
     * @param setter 方法
     * @return getter 方法
     */
    public static Method getter(Class<?> clazz, Method setter) {
        String name = setter.getName().substring(3);
        String getter = name.substring(0, 1).toUpperCase() + name.substring(1);
        Class<?> type = setter.getParameterTypes()[0];
        if (type == Boolean.class || type == boolean.class) {
            getter = "is" + getter;
        } else {
            getter = "get" + getter;
        }
        try {
            Method method = clazz.getMethod(getter);
            return method;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 取得 setter 方法
     * 
     * @param clazz 类
     * @param getter 方法
     * @return setter 方法
     */
    public static Method setter(Class<?> clazz, Method getter) {
        Class<?> type = getter.getReturnType();
        String setter = null;
        if (type == Boolean.class || type == boolean.class) {
            String name = getter.getName().substring(2);
            setter = "set" + name;
        } else {
            String name = getter.getName().substring(3);
            setter = "set" + name;
        }
        try {
            Method method = clazz.getMethod(setter, type);
            return method;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从 setter/getter 方法取得字段
     * 
     * @param setter/getter 方法
     * @return field 字段
     */
    public static String field(Method setter) {
        String name = null;
        if (setter.getName().startsWith("is")) {
            name = setter.getName().substring(2);
        } else {
            name = setter.getName().substring(3);
        }
        String field = name.substring(0, 1).toLowerCase() + name.substring(1);

        return field;
    }

    /**
     * 把对象字段名称转化为表字段名称
     * 
     * @param field 对象字段名称
     * @return 表字段名称
     */
    public static String fieldToColumn(String field) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0)
                    sb.append('_');

                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 把表字段名称转化为对象字段名称
     * 
     * @param column 表字段名称
     * @return 对象字段名称
     */
    public static String columnToField(String column) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < column.length(); i++) {
            char c = column.charAt(i);
            if (c == '_') {
                if (i > 0 && i < column.length() - 1) {
                    char n = column.charAt(++i);
                    sb.append(Character.toUpperCase(n));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将po对象中有属性和值转换成map
     *
     * @param po
     * @return
     */
    public static Map po2Map(Object po) {
        Map poMap = new HashMap();
        Map map = new HashMap();
        try {
            map = org.apache.commons.beanutils.PropertyUtils.describe(po);
        } catch (Exception ex) {
        }
        Object[] keyArray = map.keySet().toArray();
        for (int i = 0; i < keyArray.length; i++) {
            String str = keyArray[i].toString();
            if (str != null && !str.equals("class")) {
                if (map.get(str) != null) {
                    poMap.put(str, map.get(str));
                }
            }
        }

        Method[] ms =po.getClass().getMethods();
        for(Method m:ms){
            String name = m.getName();

            if(name.startsWith("get")||name.startsWith("is")){
                if(m.getAnnotation(NotDbField.class)!=null||m.getAnnotation(PrimaryKeyField.class)!=null){
                    poMap.remove(getFieldName(name));
                }
            }

        }

        /**
         * 如果此实体为动态字段实体，将动态字段加入
         */
        if(po instanceof DynamicField){
            DynamicField dynamicField = (DynamicField) po;
            Map fields = dynamicField.getFields();
            poMap.putAll(fields);
        }
        return poMap;
    }

    private static String getFieldName(String methodName){

        methodName = methodName.substring(3);
        methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
        return methodName;
    }


}
