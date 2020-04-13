package cn.bit.framework.data.mongodb.impl;

import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import sun.misc.Contended;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/1/26 0026.
 */
@Slf4j
public abstract class AbstractMongoDao<T, PK extends Serializable> implements MongoDao<T, PK> {

    protected abstract MongoTemplate getMongoTemplate();

    protected Class<T> entityClass;

    // 缓存方法名中的查询字段名数组
    @Contended("fieldNamesCache")
    private static final Map<String, String[]> FIELD_NAMES_CACHE = new WeakHashMap<>();

    @Contended("fieldNamesCache")
    private static final AtomicBoolean CHECKER = new AtomicBoolean(false);

    public AbstractMongoDao() {
        entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected String getRemoveFlagFiled() {
        return null;
    }

    @Override
    public T insert(T entity) {
        getMongoTemplate().insert(entity);
        return entity;
    }

    @Override
    public void insertAll(List<T> entities) {
        getMongoTemplate().insertAll(entities);
    }

    @Override
    public T findById(PK id) {
        return getMongoTemplate().findById(id, entityClass);
    }

    @Override
    public T findById(PK id, String collection) {
        return getMongoTemplate().findById(id, entityClass, collection);
    }

    @Override
    public List<T> findAll() {
        return find(new Query(), null);
    }

    @Override
    public List<T> findAll(String collection) {
        return getMongoTemplate().findAll(entityClass, collection);
    }

    @Override
    public List<T> find(Query query, XSort sort) {
        return getMongoTemplate().find(queryWithSort(query, sort), entityClass);
    }

    @Override
    public List<T> find(T entity, XSort sort) {
        Query query = buildExample(entity);
        return find(query, sort);
    }

    @Override
    public T findOne(Query query) {
        return getMongoTemplate().findOne(query, entityClass);
    }

    @Override
    public T findOne(T entity) {
        Query query = buildExample(entity);
        return findOne(query);
    }

    @Override
    public Page<T> findPage(Query query, int page, int size, XSort sort) {
        long total = count(query);
        Assert.notNull(query);
        Assert.isTrue(page >= 1, "page 必须大于等于1");
        Assert.isTrue(size > 0, "size 必须大于0");
        Page _page = new Page(page, total, size, getMongoTemplate().find(queryWithSort(query, sort).limit(size).skip((page - 1) * size),
                entityClass));
        return _page;
    }

    @Override
    public Page<T> findPage(T entity, int page, int size, XSort sort) {
        Query query = buildExample(entity);
        return findPage(query, page, size, sort);
    }

    @Override
    public long count(Query query) {
        return getMongoTemplate().count(query, entityClass);
    }

    @Override
    public int update(Query query, Update update) {
        if (update == null) {
            return 0;
        }
        return getMongoTemplate().updateMulti(query, update, entityClass).getN();
    }

    @Override
    public T updateOne(Query query, Update update) {
        if (update == null) {
            return null;
        }
        return getMongoTemplate().findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), entityClass);
    }

    @Override
    public T updateOne(T entity, String... fieldsToUnsetIfNull) {
        Object id = getIdValue(entity);
        if (id == null) {
            return null;
        }
        Update update = getUpdateObj(entity, fieldsToUnsetIfNull);
        if (update == null) {
            return null;
        }

        return getMongoTemplate().findAndModify(Query.query(Criteria.where("id").is(id)),
                update, FindAndModifyOptions.options().returnNew(true), entityClass);
    }

    @Override
    public int updateByExample(Query query, T entity, String... fieldsToUnsetIfNull) {

        Update update = getUpdateObj(entity, fieldsToUnsetIfNull);
        if (update == null) {
            return 0;
        }
        return getMongoTemplate().updateMulti(query, update, entityClass).getN();
    }


    @Override
    public int remove(Query query) {
        return getMongoTemplate().remove(query, entityClass).getN();
    }

    @Override
    public int remove(PK id) {
        return getMongoTemplate().remove(Query.query(Criteria.where("_id").is(id)), entityClass).getN();
    }

    @Override
    public boolean exist(Query query) {
        return getMongoTemplate().exists(query, entityClass);
    }

    @Override
    public boolean exist(T entity) {
        Query query = buildExample(entity);
        return exist(query);
    }

    /**
     * 根据调用方的方法名称获取所有and条件作为更新的查询条件更新第一条数据
     * 注意：
     *   1.传入的toUpdate实体对象与and条件中对应的字段必须有值，
     *     若不存在对应的字段的值，and条件将会忽略此字段
     *   2.dataStatus会作为默认and条件，必须先赋值为1
     * @param toUpdate
     * @param returnNew
     * @return
     */
    protected T updateOneBy(T toUpdate, boolean returnNew, Object... queryValues) {
        // TODO: 缓兵之计，后面需要把数据状态的字段由子类指定
        return updateOneBy(toUpdate, returnNew,
                concatArray(getAndFieldNamesByMethodName(2, "By"), new String[]{"dataStatus"}),
                queryValues);
    }

    protected T updateOneBy(T toUpdate, boolean returnNew, String[] queryFields, Object[] queryValues) {
        Query query = getQueryByFieldsAndValues(queryFields, queryValues);
        if (query == null) {
            return null;
        }

        Update update = getUpdateObj(toUpdate);
        if (update == null) {
            return null;
        }

        return getMongoTemplate().findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(returnNew), entityClass);
    }

    /**
     * 根据调用方的方法名称获取所有and条件作为更新的查询条件进行批量更新
     * 注意：
     *   1.传入的toUpdate实体对象与and条件中对应的字段必须有值，
     *     若不存在对应的字段的值，and条件将会忽略此字段
     *   2.dataStatus会作为默认and条件，必须先赋值为1
     * @param toUpdate
     * @return
     */
    protected int updateMultiBy(T toUpdate, Object... queryValues) {
        // TODO: 缓兵之计，后面需要把数据状态的字段由子类指定
        return updateMultiBy(toUpdate,
                concatArray(getAndFieldNamesByMethodName(2, "By"), new String[]{"dataStatus"}),
                queryValues);
    }

    protected int updateMultiBy(T toUpdate, String[] queryFields, Object[] queryValues) {
        Query query = getQueryByFieldsAndValues(queryFields, queryValues);
        if (query == null) {
            return 0;
        }

        // 把更新实体中的查询字段的值置为null
//        BeanUtils.filteProperties(toUpdate, queryFields);
        return updateByExample(query, toUpdate);
    }

    /**
     *
     * @param toUpdate
     * @return
     */
    protected int pullAllBy(T toUpdate, Object... queryValues) {
        // TODO: 缓兵之计，后面需要把数据状态的字段由子类指定
        return pullAllBy(toUpdate,
                concatArray(getAndFieldNamesByMethodName(2, "By"), new String[]{"dataStatus"}),
                queryValues);
    }

    protected int pullAllBy(T toUpdate, String[] queryFields, Object... queryValues) {
        Query query = getQueryByFieldsAndValues(queryFields, queryValues);
        if (query == null) {
            return 0;
        }

        Update update = buildPullAll(toUpdate);
        if (update == null) {
            return 0;
        }

        return update(query, update);
    }

    protected int upsertBy(T toUpsert, Object... queryValues) {
        // TODO: 缓兵之计，后面需要把数据状态的字段由子类指定
        return upsertBy(toUpsert,
                concatArray(getAndFieldNamesByMethodName(2, "By"), new String[]{"dataStatus"}),
                queryValues);
    }

    protected int upsertBy(T toUpsert, String[] queryFields, Object[] queryValues) {
        Query query = getQueryByFieldsAndValues(queryFields, queryValues);
        if (query == null) {
            return 0;
        }

        Update update = getUpdateObj(toUpsert);
        if (update == null) {
            return 0;
        }

        return getMongoTemplate().upsert(query, update, entityClass).getN();
    }

    protected T upsertOneBy(T toUpsert, Object... queryValues) {
        // TODO: 缓兵之计，后面需要把数据状态的字段由子类指定
        return upsertOneBy(toUpsert,
                concatArray(getAndFieldNamesByMethodName(2, "By"), new String[]{"dataStatus"}),
                queryValues);
    }

    protected T upsertOneBy(T toUpsert, String[] queryFields, Object[] queryValues) {
        Query query = getQueryByFieldsAndValues(queryFields, queryValues);
        if (query == null) {
            return null;
        }

        Update update = getUpdateObj(toUpsert);
        if (update == null) {
            return null;
        }

        return getMongoTemplate().findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true).upsert(true), entityClass);
    }

    /**
     * 通过查询字段名数组和查询字段值数组创建Query对象
     * 字段名或字段值为空的字段将被忽略
     *
     * @param queryFields
     * @param queryValues
     * @return
     */
    private Query getQueryByFieldsAndValues(String[] queryFields, Object[] queryValues) {
        if (queryFields == null || queryValues == null || queryFields.length == 0 ||
                queryFields.length != queryValues.length) {
            return null;
        }

        Query query = new Query();
        // 查询实体从更新实体中获取查询字段的值
        int count = Math.min(queryFields.length, queryValues.length);
        for (int i = 0; i < count; i++) {
            if (queryFields[i] == null || queryValues[i] == null) {
                continue;
            }

            Criteria criteria = Criteria.where(queryFields[i]);
            if (queryValues[i].getClass().isArray()) {
                criteria.in((Object[]) queryValues[i]);
            } else if (queryValues[i] instanceof Collection) {
                criteria.in((Collection) queryValues[i]);
            } else {
                criteria.is(queryValues[i]);
            }
            query.addCriteria(criteria);
        }

        return query;
    }

    protected Query queryWithSort(Query query, XSort xSort) {
        if (xSort == null || !xSort.iterator().hasNext()) {
            return query;
        }

        List<Sort.Order> orders = new ArrayList<>();
        xSort.forEach(xOrder -> {
            Sort.Order order = new Sort.Order(
                    Sort.Direction.fromString(xOrder.getDirection().name()),
                    xOrder.getProperty()
            );
            orders.add(order);
        });

        return orders.size() > 0 ? query.with(new Sort(orders)) : query;
    }

    protected Query buildExample(Object obj) {
        Query query = new Query();
        if (obj == null)
            return query;

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Transient.class) != null)
                continue;

            try {
                field.setAccessible(true);
                Object value = getFieldValue(obj, field);
                if (value == null) {
                    continue;
                }

                String fieldName = getFieldName(field);
                if (field.getAnnotation(Id.class) != null) {
                    query.addCriteria(Criteria.where("id").is(value));
                } else if (field.getDeclaringClass().isArray()) {
                    if (((Object[]) value).length > 0)
                        query.addCriteria(Criteria.where(fieldName).in((Object[]) value));
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    if (!((Collection) value).isEmpty())
                        query.addCriteria(Criteria.where(fieldName).in((Collection) value));
                } else {
                    query.addCriteria(Criteria.where(fieldName).is(value));
                }
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException", e);
            }
        }
        return query;
    }

    protected Update getUpdateObj(Object obj, String... fieldsToUnsetIfNull) {
        if (obj == null)
            return null;

        Set<String> unsets = Collections.emptySet();
        if (fieldsToUnsetIfNull != null && fieldsToUnsetIfNull.length > 0)
            unsets = new HashSet<>(Arrays.asList(fieldsToUnsetIfNull));

        Field[] fields = obj.getClass().getDeclaredFields();
        Update update = null;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.getAnnotation(Id.class) != null)
                    continue;

                if (field.getAnnotation(Transient.class) != null)
                    continue;

                Object value = getFieldValue(obj, field);
                String fieldName = getFieldName(field);
                if (value != null) {
                    if (update == null) {
                        update = new Update();
                    }

                    if (field.getDeclaringClass().isArray()) {
                        if (((Object[]) value).length == 0) {
                            continue;
                        }

                        update.addToSet(fieldName).each((Object[]) value);
                        continue;
                    }

                    if (Collection.class.isAssignableFrom(field.getType())) {
                        if (((Collection) value).isEmpty()) {
                            continue;
                        }

                        update.addToSet(fieldName).each(((Collection) value).toArray());
                        continue;
                    }

                    update.set(fieldName, value);
                } else if (unsets.contains(fieldName)) {
                    if (update == null) {
                        update = new Update();
                    }
                    update.unset(fieldName);
                }
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException", e);
            }
        }
        return update;
    }

    protected Update buildPullAll(T toUpdate) {
        if (toUpdate == null) {
            return null;
        }

        Field[] fields = toUpdate.getClass().getDeclaredFields();
        Update update = null;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getAnnotation(Id.class) != null)
                continue;

            if (field.getAnnotation(Transient.class) != null)
                continue;

            Object value = getFieldValue(toUpdate, field);
            if (value == null) {
                continue;
            }

            if (update == null) {
                update = new Update();
            }

            String fieldName = getFieldName(field);
            if (field.getDeclaringClass().isArray()) {
                update.pullAll(fieldName, (Object[]) value);
                continue;
            }

            if (Collection.class.isAssignableFrom(field.getType())) {
                update.pullAll(fieldName, ((Collection) value).toArray());
                continue;
            }

            update.set(fieldName, value);
        }

        return update;
    }

    protected Object getFieldValue(Object obj, Field field) {
//        if (!FieldUtils.isSimpleField(field))
//            return null;
        Object value = null;
        try {
            value = field.get(obj);
            if (CharSequence.class.isAssignableFrom(field.getType())
                    && StringUtils.isBlank((CharSequence) value)) {
                value = null;
            }
        } catch (IllegalAccessException e) {
            log.error("IllegalArgumentException", e);
        }
        return value;
    }

    protected String getFieldName(Field field) {
        org.springframework.data.mongodb.core.mapping.Field fieldMeta =
                field.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
        if (fieldMeta == null) {
            return field.getName();
        }

        return fieldMeta.value();
    }

    protected Object getIdValue(T entity) {
        Field[] fields = entityClass.getDeclaredFields();
        if (fields == null || fields.length <= 0) {
            return null;
        }
        Field idField = null;
        // 查找ID的field
        for (Field field : fields) {
            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation != null) {
                idField = field;
                break;
            }
        }
        if (idField == null) {
            return null;
        }
        idField.setAccessible(true);
        Object id = null;
        try {
            id = idField.get(entity);
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException", e);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException", e);
        }
        return id;
    }

    /**
     * 获取调用栈中距离本方法指定层数的方法的名称中的And字段名组数
     * @param level 目标方法在调用栈中距离本方法的层数
     * @return
     */
    protected static String[] getAndFieldNamesByMethodName(int level, String beginWith) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[level + 1];
        String className = ste.getClassName();
        String methodName = ste.getMethodName();
        String cacheKey = new StringBuilder(className).append(".").append(methodName).toString();
        String[] fieldNames = FIELD_NAMES_CACHE.get(cacheKey);
        if (fieldNames != null) {
            return fieldNames;
        }

        // 自旋直至设置成功
        while (!CHECKER.compareAndSet(false, true));

        fieldNames = FIELD_NAMES_CACHE.get(cacheKey);
        if (fieldNames != null) {
            CHECKER.set(false);
            return fieldNames;
        }

        String fieldStr = methodName.substring(methodName.indexOf(beginWith) + beginWith.length());
        fieldNames = fieldStr.split("And");
        for (int i = 0; i < fieldNames.length; i++) {
            char[] charArray = fieldNames[i].toCharArray();
            charArray[0] += 32;
            fieldNames[i] = String.valueOf(charArray);
        }

        FIELD_NAMES_CACHE.put(cacheKey, fieldNames);
        CHECKER.set(false);
        return fieldNames;
    }

    private static String[] concatArray(String[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return null;
        }

        if (arrays.length == 1) {
            return arrays[0];
        }

        List<String[]> arrayList = Arrays.stream(arrays)
                .collect(Collectors.toCollection(ArrayList::new));
        int len = arrayList.stream().mapToInt(array -> array.length).sum();
        String[] newArray = new String[len];
        int pos = 0;
        for (String[] array : arrayList) {
            System.arraycopy(array, 0, newArray, pos, array.length);
            pos += array.length;
        }

        return newArray;
    }

}
