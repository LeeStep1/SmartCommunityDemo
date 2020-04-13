package cn.bit.framework.redis;

import cn.bit.framework.redis.lock.RedisLock;
import cn.bit.framework.redis.template.RedisTemplate;
import cn.bit.framework.utils.string.StringUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * User: qiujingwang
 * Date: 2017/4/10
 * Description:
 */
public class RedisTemplateUtil {

    public static final long NONE_EXPIRE = 0L;

    private static RedisTemplate<String, Object> redisTemplate;

    private static StringRedisTemplate stringRedisTemplate;

    /**
     * 删除缓存<br>
     * 根据key精确匹配删除
     *
     * @param key
     */
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
     * 模糊批量删除<br>
     * （该操作会执行模糊查询，请尽量不要使用，以免影响性能或误删）
     *
     * @param pattern
     */
    public static void batchDel(String... pattern) {
        for (String kp : pattern) {
            redisTemplate.delete(redisTemplate.keys(kp + "*"));
        }
    }

    /**
     * 批量查找<br>. 如果不满足，只能使用管道，根据不同的类型进行解码
     *
     * @param keys
     */
    public static List multiGetObject(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量查找<br>. 如果不满足，只能使用管道，根据不同的类型进行解码
     *
     * @param keys
     */
    public static List<String> multiGetString(Collection<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }


    /**
     * 取得缓存（字符串类型）
     *
     * @param key
     * @return 为空则返回null
     */
    public static String getStr(String key) {
        return stringRedisTemplate.boundValueOps(key).get();
    }

    /**
     * 设置字符串类型值
     *
     * @param key
     * @param value
     */
    public static void setStr(String key, String value) {
//        setStr(key, value, NONE_EXPIRE);
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置字符串类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setStr(String key, String value, long expireSeconds) {
        if (expireSeconds > 0) {
            stringRedisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 取得缓存（int型）
     *
     * @param key
     * @return 为空则返回0
     */
    public static Integer getInt(String key) {
        String value = getStr(key);
        if (StringUtil.isNumeric(value)) {
            return Integer.valueOf(value);
        }
        return null;
    }

    /**
     * 设置int类型值
     *
     * @param key
     * @param value
     */
    public static void setInt(String key, int value) {
        setStr(key, Integer.toString(value));
    }

    /**
     * 设置int类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setInt(String key, int value, long expireSeconds) {
        setStr(key, Integer.toString(value), expireSeconds);
    }

    /**
     * 获取double类型值
     *
     * @param key
     * @return 为空则返回0
     */
    public static Double getDouble(String key) {
        String value = getStr(key);
        if (value != null && StringUtil.checkDecimals(value)) {
            return Double.valueOf(value);
        }
        return null;
    }

    /**
     * 设置double类型值
     *
     * @param key
     * @param value
     */
    public static void setDouble(String key, double value) {
        setStr(key, Double.toString(value));
    }

    /**
     * 设置double类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setDouble(String key, double value, long expireSeconds) {
        setStr(key, Double.toString(value), expireSeconds);
    }

    /**
     * 获取Float类型值
     *
     * @param key
     * @return 为空则返回0
     */
    public static Float getFloat(String key) {
        String value = getStr(key);
        if (value != null && StringUtil.checkDecimals(value)) {
            return Float.valueOf(value);
        }
        return null;
    }

    /**
     * 设置Float类型值
     *
     * @param key
     * @param value
     */
    public static void setFloat(String key, float value) {
        setStr(key, Float.toString(value));
    }

    /**
     * 设置Float类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setFloat(String key, float value, long expireSeconds) {
        setStr(key, Float.toString(value), expireSeconds);
    }

    /**
     * 获取Short类型值
     *
     * @param key
     * @return 为空则返回0
     */
    public static Short getShort(String key) {
        String value = getStr(key);
        if (StringUtil.isNumeric(value)) {
            return Short.valueOf(value);
        }
        return null;
    }

    /**
     * 设置Short类型值
     *
     * @param key
     * @param value
     */
    public static void setShort(String key, short value) {
        setStr(key, Short.toString(value));
    }

    /**
     * 设置Short类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setShort(String key, short value, long expireSeconds) {
        setStr(key, Short.toString(value), expireSeconds);
    }

    /**
     * 获取Long类型值
     *
     * @param key
     * @return 为空则返回0
     */
    public static Long getLong(String key) {
        String value = getStr(key);
        if (StringUtil.isNumeric(value)) {
            return Long.valueOf(value);
        }
        return null;
    }

    /**
     * 设置Long类型值
     *
     * @param key
     * @param value
     */
    public static void setLong(String key, long value) {
        setStr(key, Long.toString(value));
    }

    /**
     * 设置Long类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setLong(String key, long value, long expireSeconds) {
        setStr(key, Long.toString(value), expireSeconds);
    }

    /**
     * 获取Boolean类型值
     *
     * @param key
     * @return 为空则返回null
     */
    public static Boolean getBoolean(String key) {
        String value = getStr(key);
        if (StringUtil.isNotBlank(value)) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    /**
     * 设置Boolean类型值
     *
     * @param key
     * @param value
     */
    public static void setBoolean(String key, boolean value) {
        setStr(key, Boolean.toString(value));
    }

    /**
     * 设置Boolean类型值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void setBoolean(String key, boolean value, long expireSeconds) {
        setStr(key, Boolean.toString(value), expireSeconds);
    }

    /**
     * 获取缓存<br>
     * 注：java 8种基本类型的数据(Character除外)，请直接使用get(String key, Class<T> clazz)取值
     *
     * @param key
     * @return
     */
    public static Object getObj(String key) {
        return redisTemplate.boundValueOps(key).get();
    }

    /**
     * 设置缓存<br>
     * 注：java 基本类型的数据(Character除外)，请直接使用setXXX(String key)设置值
     *
     * @param key
     * @param value
     * @return
     */
    public static void setObj(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存<br>
     * 注：java 基本类型的数据(Character除外)，请直接使用setXXX(String key)设置值
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     * @return
     */
    public static void setObj(String key, Object value, long expireSeconds) {
        if (expireSeconds > 0) {
            redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 获取缓存<br>
     * 注：该方法暂不支持Character数据类型
     *
     * @param key   key
     * @param clazz 类型
     * @return 为空返回null
     */
    public static <T> T get(String key, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return (T) stringRedisTemplate.boundValueOps(key).get();
        } else if (clazz.equals(Integer.class)) {
            String value = stringRedisTemplate.boundValueOps(key).get();
            if (StringUtil.isNotBlank(value)) {
                return (T) Integer.valueOf(value);
            }
        } else if (clazz.equals(Long.class)) {
            String value = stringRedisTemplate.boundValueOps(key).get();
            if (StringUtil.isNotBlank(value)) {
                return (T) Long.valueOf(value);
            }
        } else if (clazz.equals(Double.class)) {
            String value = stringRedisTemplate.boundValueOps(key).get();
            if (StringUtil.isNotBlank(value)) {
                return (T) Double.valueOf(value);
            }
        } else if (clazz.equals(Boolean.class)) {
            String value = stringRedisTemplate.boundValueOps(key).get();
            if (StringUtil.isNotBlank(value)) {
                return (T) Boolean.valueOf(value);
            }
        } else if (clazz.equals(Float.class)) {
            String value = stringRedisTemplate.boundValueOps(key).get();
            if (StringUtil.isNotBlank(value)) {
                return (T) Float.valueOf(value);
            }
        } else if (clazz.equals(Short.class)) {
            String value = stringRedisTemplate.boundValueOps(key).get();
            if (StringUtil.isNotBlank(value)) {
                return (T) Short.valueOf(value);
            }
        } else {
            return (T) redisTemplate.boundValueOps(key).get();
        }
        return null;
    }

    /**
     * 将value对象写入缓存(不失效)
     *
     * @param key
     * @param value
     */
    public static void set(String key, Object value) {
        set(key, value, NONE_EXPIRE);
    }

    /**
     * 将value对象写入缓存
     *
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    public static void set(String key, Object value, long expireSeconds) {
        if (isBaseDataType(value.getClass())) {
            setStr(key, value.toString(), expireSeconds);
        } else {
            setObj(key, value, expireSeconds);
        }
    }

    /**
     * 判断一个类是否为基本数据类型。(只列出常用7种)
     *
     * @param clazz 要判断的类。
     */
    private static boolean isBaseDataType(Class clazz) {
        return clazz.equals(String.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Boolean.class);
    }

    /**
     * 获取缓存json对象<br>
     * 1.1.7版去掉
     * @param key   key
     * @param clazz 类型
     * @return
     */
    /*public static <T> T getJsonForBean(String key, Class<T> clazz) {
        String json = getStr(key);
        if (StringUtil.isNotBlank(json)) {
            return JacksonUtil.fromJson(json, clazz);
        }
        return null;
    }*/

    /**
     * 获取缓存json对象<br>
     * 1.1.7版去掉
     * @param key   key
     * @param clazz 类型
     * @return
     */
    /*public static <T> List<T> getJsonForBeanList(String key, Class<T> clazz) {
        String json = getStr(key);
        if (StringUtil.isNotBlank(json)) {
            return JacksonUtil.convertList(json, clazz);
        }
        return null;
    }*/

    /**
     * 将value对象以JSON格式写入缓存(不过期)
     * 1.1.7版去掉
     * @param key
     * @param value
     */
    /*public static void setJson(String key, Object value) {
        setJson(key, value, NONE_EXPIRE);
    }*/

    /**
     * 将value对象以JSON格式写入缓存
     * 1.1.7版去掉
     * @param key
     * @param value
     * @param expireSeconds 失效时间(秒)
     */
    /*public static void setJson(String key, Object value, long expireSeconds) {
        setStr(key, JacksonUtil.toJson(value), expireSeconds);
    }*/

    /**
     * 更新key对象field的值
     *
     * @param key   缓存key
     * @param field 缓存对象field
     * @param value 缓存对象field值
     */
    /*public static void setJsonField(String key, String field, String value) {
        JSONObject obj = JSON.parseObject(stringRedisTemplate.boundValueOps(key).get());
        obj.put(field, value);
        stringRedisTemplate.opsForValue().set(key, obj.toJSONString());
    }*/


    /**
     * 递减操作（减1）
     *
     * @param key
     * @return
     */
    public static double decrDouble(String key) {
        return decrDouble(key, 1);
    }

    /**
     * 递减操作
     *
     * @param key
     * @param by
     * @return
     */
    public static double decrDouble(String key, double by) {
        return redisTemplate.opsForValue().increment(key, -by);
    }

    /**
     * 递增操作(加1)
     *
     * @param key
     * @return
     */
    public static double incrDouble(String key) {
        return incrDouble(key, 1);
    }

    /**
     * 递增操作
     *
     * @param key
     * @param by
     * @return
     */
    public static double incrDouble(String key, double by) {
        return redisTemplate.opsForValue().increment(key, by);
    }

    /**
     * 获取递增double值
     *
     * @param key
     * @return
     */
    public static double getIncrDoubleVal(String key) {
        return incrDouble(key, 0d);
    }

    /**
     * 通过管道批量查找
     *
     * @param keys
     */
    public static List<Double> multiGetIncrDoubleValWithPip(Collection<String> keys) {
        List<Double> result;
        if (!CollectionUtils.isEmpty(keys)) {
            result = redisTemplate.executePipelined(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    keys.forEach(key -> operations.opsForValue().increment(key, 0d));
                    return null;
                }
            });
        } else {
            result = Collections.EMPTY_LIST;
        }
        return result;
    }

    /**
     * 通过管道批量查找
     *
     * @param keys
     */
    public static List<Long> multiGetIncrLongValWithPip(Collection<String> keys) {
        List<Long> result;
        if (keys != null && !keys.isEmpty()) {
            result = redisTemplate.executePipelined(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    keys.forEach(key -> operations.opsForValue().increment(key, 0L));
                    return null;
                }
            });
        } else {
            result = Collections.EMPTY_LIST;
        }
        return result;
    }

    /**
     * 递增操作(加1)
     *
     * @param key
     * @return
     */
    public static long incrLong(String key) {
        return incrLong(key, 1);
    }

    /**
     * 递增操作
     *
     * @param key
     * @param by
     * @return
     */
    public static long incrLong(String key, long by) {
        return redisTemplate.opsForValue().increment(key, by);
    }

    /**
     * 递减操作（减1）
     *
     * @param key
     * @return
     */
    public static long decrLong(String key) {
        return decrLong(key, 1);
    }

    /**
     * 递减操作
     *
     * @param key
     * @param by
     * @return
     */
    public static long decrLong(String key, long by) {
        return redisTemplate.opsForValue().increment(key, -by);
    }

    /**
     * 获取递增long值
     *
     * @param key
     * @return
     */
    public static long getIncrLongVal(String key) {
        return incrLong(key, 0L);
    }

    /**
     * 将map写入缓存
     *
     * @param key
     * @param map
     */
    public static <T> void setMap(String key, Map<String, T> map) {
        setMap(key, map, NONE_EXPIRE);
    }

    /**
     * 将map写入缓存
     *
     * @param key
     * @param map
     * @param expireSeconds 失效时间(秒)
     */
    public static <T> void setMap(String key, Map<String, T> map, long expireSeconds) {
        redisTemplate.opsForHash().putAll(key, map);
        if (expireSeconds > 0) {
            expire(key, expireSeconds);
        }
    }

    /**
     * 向key对应的map中添加缓存对象
     *
     * @param key   cache对象key
     * @param field map对应的key
     * @param value 值
     */
    public static void addMap(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 只有MAP不存在指定的filed，才能保存进去
     *
     * @param key   map对应的key
     * @param field map中该对象的key
     */
    public static Boolean addMapIfAbsent(String key, String field, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, field, value);
    }

    /**
     * 向key对应的map中添加缓存对象
     *
     * @param key   cache对象key
     * @param field map对应的key
     * @param obj   对象
     */
    public static <T> void addMap(String key, String field, T obj) {
        redisTemplate.opsForHash().put(key, field, obj);
    }

    /**
     * 只有MAP不存在指定的filed，才能保存进去
     *
     * @param key   map对应的key
     * @param field map中该对象的key
     */
    public static <T> Boolean addMapIfAbsent(String key, String field, T obj) {
        return redisTemplate.opsForHash().putIfAbsent(key, field, obj);
    }

    /**
     * 获取map缓存
     *
     * @param key
     * @param clazz
     * @return
     */
    public static <T> Map<String, T> mget(String key, Class<T> clazz) {
        BoundHashOperations<String, String, T> boundHashOperations = redisTemplate.boundHashOps(key);
        return boundHashOperations.entries();
    }

    /**
     * 获取map缓存
     *
     * @param key
     * @return
     */
    public static <T> Map<String, ?> mget(String key) {
        BoundHashOperations<String, String, ?> boundHashOperations = redisTemplate.boundHashOps(key);
        return boundHashOperations.entries();
    }

    public static Map<byte[], byte[]> mgetbytes(String mapKey) {
        return redisTemplate.entries(mapKey);
    }

    public static Map<String, byte[]> mgetstrbytes(String mapKey) {
        return deserializeHashMap(redisTemplate.entries(mapKey));
    }

    static Map<String, byte[]> deserializeHashMap(Map<byte[], byte[]> entries) {
        // connection in pipeline/multi mode
        if (entries == null) {
            return null;
        }

        Map<String, byte[]> map = new LinkedHashMap<>(entries.size());

        for (Map.Entry<byte[], byte[]> entry : entries.entrySet()) {
            map.put(String.valueOf(redisTemplate.getHashKeySerializer().deserialize(entry.getKey())), entry.getValue());
        }

        return map;
    }

    /**
     * 获取map中多个field对应的值
     *
     * @param key
     * @param fields
     * @return
     */
    public static List<Object> multiGet(String key, List<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }

    /**
     * map中field原子自增(加1)
     *
     * @param key
     * @param field
     * @return
     */
    public static Long mIncrLong(String key, String field) {
        return mIncrLong(key, field, 1);
    }

    /**
     * map中field原子自增
     *
     * @param key
     * @param field
     * @param by
     * @return
     */
    public static Long mIncrLong(String key, String field, long by) {
        return redisTemplate.opsForHash().increment(key, field, by);
    }

    /**
     * map中field原子自减(减1)
     *
     * @param key
     * @param field
     * @return
     */
    public static Long mDecrLong(String key, String field) {
        return mDecrLong(key, field, 1);
    }

    /**
     * map中field原子自减
     *
     * @param key
     * @param field
     * @param by
     * @return
     */
    public static Long mDecrLong(String key, String field, long by) {
        return redisTemplate.opsForHash().increment(key, field, -by);
    }

    /**
     * 获取map中field原子自增
     *
     * @param key
     * @param field
     * @return
     */
    public static Long mGetIncrLong(String key, String field) {
        return redisTemplate.opsForHash().increment(key, field, 0);
    }

    /**
     * map中field原子自增(加1)
     *
     * @param key
     * @param field
     * @return
     */
    public static Double mIncrDouble(String key, String field) {
        return mIncrDouble(key, field, 1);
    }

    /**
     * map中field原子自增
     *
     * @param key
     * @param field
     * @param by
     * @return
     */
    public static Double mIncrDouble(String key, String field, double by) {
        return redisTemplate.opsForHash().increment(key, field, by);
    }

    /**
     * map中field原子自减(减1)
     *
     * @param key
     * @param field
     * @return
     */
    public static Long mDecrDouble(String key, String field) {
        return mDecrDouble(key, field, 1);
    }

    /**
     * map中field原子自减
     *
     * @param key
     * @param field
     * @param by
     * @return
     */
    public static Long mDecrDouble(String key, String field, long by) {
        return redisTemplate.opsForHash().increment(key, field, -by);
    }

    /**
     * map中field原子自增
     *
     * @param key
     * @param field
     * @return
     */
    public static Double mGetIncrDouble(String key, String field) {
        return redisTemplate.opsForHash().increment(key, field, 0d);
    }

    /**
     * 通过管道批量查找
     *
     * @param keys
     */
    public static List<Double> multiGetIncrDoubleValWithPip(List<String> keys, List<String> fields) {
        List<Double> result;
        if (!CollectionUtils.isEmpty(keys) && !CollectionUtils.isEmpty(fields) && keys.size() == fields.size()) {
            result = redisTemplate.executePipelined(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    IntStream.range(0, keys.size()).forEach(i -> operations.opsForHash().increment(keys.get(i),
                            fields.get(i), 0d));
                    return null;
                }
            });
        } else {
            result = Collections.EMPTY_LIST;
        }
        return result;
    }

    /**
     * 通过管道批量查找
     *
     * @param keys
     */
    public static List<Long> multiGetIncrLongValWithPip(List<String> keys, List<String> fields) {
        List<Long> result;
        if (!CollectionUtils.isEmpty(keys) && !CollectionUtils.isEmpty(fields) && keys.size() == fields.size()) {
            result = redisTemplate.executePipelined(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    IntStream.range(0, keys.size()).forEach(i -> operations.opsForHash().increment(keys.get(i),
                            fields.get(i), 0L));
                    return null;
                }
            });
        } else {
            result = Collections.EMPTY_LIST;
        }
        return result;
    }

    public static Map<String, byte[]> convertToBytesMap(Map<String, Long> longMap) {
        Map<String, byte[]> longBytesMap = new HashMap<>(longMap.size());
        try {
            for (Map.Entry<String, Long> entry : longMap.entrySet()) {
                longBytesMap.put(entry.getKey(), entry.getValue().toString().getBytes("UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return longBytesMap;
    }

    public static Map<String, Long> convertToLongMap(Map<String, byte[]> longBytesMap) {
        Map<String, Long> longMap = new HashMap<>(longBytesMap.size());
        try {
            for (Map.Entry<String, byte[]> entry : longBytesMap.entrySet()) {
                longMap.put(entry.getKey(), Long.parseLong(new String(entry.getValue(), "UTF-8")));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return longMap;
    }

    /**
     * 获取map中field 数量
     *
     * @param key
     * @return
     */
    public static Long mSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 获取map缓存
     *
     * @param key
     * @param clazz
     * @return
     */
    /*public static <T> T getMap(String key, Class<T> clazz) {
        BoundHashOperations<String, String, String> boundHashOperations = redisTemplate.boundHashOps(key);
        Map<String, String> map = boundHashOperations.entries();
        return JsonMapper.parseObject(map, clazz);
    }*/

    /**
     * 获取map缓存中的某个对象
     *
     * @param key
     * @param field
     * @param clazz
     * @return
     */
    public static <T> T getMapField(String key, String field, Class<T> clazz) {
        return (T) redisTemplate.boundHashOps(key).get(field);
    }

    /**
     * 删除map中的某个对象
     *
     * @param key   map对应的key
     * @param field map中该对象的key
     */
    public static void delMapField(String key, String... field) {
        BoundHashOperations<String, String, ?> boundHashOperations = redisTemplate.boundHashOps(key);
        boundHashOperations.delete(field);
    }

    /**
     * 判断map中的是否存在某个field
     *
     * @param key   map对应的key
     * @param field map中该对象的key
     */
    public static boolean mHasKey(String key, String field) {
        return redisTemplate.boundHashOps(key).hasKey(field);
    }

    /**
     * 指定缓存的失效时间
     *
     * @param key           缓存KEY
     * @param expireSeconds 失效时间(秒)
     */
    public static void expire(String key, long expireSeconds) {
        if (expireSeconds > 0) {
            redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 指定缓存的失效时间（具体时间）
     *
     * @param key  缓存KEY
     * @param date 指定过期时间，eg.2017-07-07 到 2017-07-07日过期
     */
    public static void expire(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    /**
     * 返回指定key的失效时间
     *
     * @param key 缓存KEY
     * @return -2：key不存在（或已过期），-1：未设置过期时间(永久有效)
     */
    public static Long getExpire(String key) {
        return redisTemplate.boundValueOps(key).getExpire();
    }

    /**
     * 是否指定key的失效时间
     * expire>> -2：key不存在（或已过期），-1：未设置过期时间(永久有效)
     *
     * @param key 缓存KEY
     * @return true:存在过期时间，否则key不存在（或已过期）或未设置过期时间（永久有效）
     */
    public static Boolean hasSetExpire(String key) {
        return redisTemplate.boundValueOps(key).getExpire() >= 0;
    }

    /**
     * 删除指定key上的过期时间
     *
     * @param key
     */
    public static Boolean persist(String key) {
        return redisTemplate.boundValueOps(key).persist();
    }

    /**
     * 添加set
     *
     * @param key
     * @param value
     */
    public static void sadd(String key, String... value) {
        redisTemplate.boundSetOps(key).add(value);
    }

    /**
     * 删除set集合中的对象
     *
     * @param key
     * @param value
     */
    public static void sdel(String key, String... value) {
        redisTemplate.boundSetOps(key).remove(value);
    }

    /**
     * 删除并随机返回set集合中的对象
     *
     * @param key
     */
    public static void spop(String key) {
        redisTemplate.boundSetOps(key).pop();
    }

    /**
     * 随机返回set集合中的对象
     *
     * @param key
     */
    public static List<Object> srandomMembers(String key, long count) {
        return redisTemplate.boundSetOps(key).randomMembers(count);
    }

    /**
     * 随机返回set集合中的一个对象
     *
     * @param key
     */
    public static Object srandomMember(String key) {
        return redisTemplate.boundSetOps(key).randomMember();
    }

    /**
     * 返回set集合中所有对象
     *
     * @param key
     */
    public static Set<Object> smembers(String key) {
        return redisTemplate.boundSetOps(key).members();
    }

    /**
     * 返回set集合大小
     *
     * @param key
     */
    public static Long sSize(String key) {
        return redisTemplate.boundSetOps(key).size();
    }

    /**
     * 判断set集合是否包含此对象
     *
     * @param key
     */
    public static <T> Boolean isMember(String key, T obj) {
        return redisTemplate.boundSetOps(key).isMember(obj);
    }

    /**
     * set重命名
     *
     * @param oldkey
     * @param newkey
     */
    public static void srename(String oldkey, String newkey) {
        redisTemplate.boundSetOps(oldkey).rename(newkey);
    }

    /**
     * key是否存在
     *
     * @param key
     * @return
     */
    public static boolean hasKey(String key) {
        Boolean aBoolean = stringRedisTemplate.hasKey(key);
        return aBoolean != null && aBoolean;
    }

    /**
     * key是否存在
     * @param key
     * @return
     */
    /*public static boolean hasKey2(String key){
        Boolean aBoolean = redisTemplate.hasKey(key);
        return aBoolean != null && aBoolean;
    }*/

    /**
     * 保存到List集合中
     *
     * @param index
     */
    public static <T> void lSet(String key, long index, T obj) {
        redisTemplate.boundListOps(key).set(index, obj);
    }

    /**
     * 返回List集合中对象
     *
     * @param index
     */
    public static Object lGet(String key, long index) {
        return redisTemplate.boundListOps(key).index(index);
    }

    /**
     * 返回List集合中对象
     *
     * @param index
     */
    public static Object lIndex(String key, long index) {
        return redisTemplate.boundListOps(key).index(index);
    }

    /**
     * 模糊查询keys
     * (该操作会执行模糊查询，请尽量不要使用，以免影响性能）
     *
     * @param pattern
     * @return
     */
    public static Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 返回offset位置中的0:false, 1:true
     *
     * @param key
     * @param offset
     * @return
     */
    public static Boolean getBit(String key, long offset) {
        return stringRedisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 设置offset位置中的0:false, 1:true
     *
     * @param key
     * @param offset
     * @return
     */
    public static Boolean setBit(String key, long offset, boolean value) {
        return stringRedisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 设置offset位置标识为true
     *
     * @param key
     * @param offset
     * @return
     */
    public static Boolean setBitForTrue(String key, long offset) {
        return stringRedisTemplate.opsForValue().setBit(key, offset, true);
    }

    /**
     * 设置offset位置标识为false
     *
     * @param key
     * @param offset
     * @return
     */
    public static Boolean setBitForFalse(String key, long offset) {
        return stringRedisTemplate.opsForValue().setBit(key, offset, false);
    }

    /**
     * 设置值，当不存在时（setNX）
     * SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。
     *
     * @param key
     * @param value
     * @return false:设置失败(表示该key已经存在)，否则设置成功
     */
    public static Boolean setIfAbsent(String key, String value) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 设置指定的字符串值，并返回其旧值，如果键不存在，则返回null
     *
     * @param key
     * @param value
     * @return 返回其旧值，如果键不存在，则返回null
     */
    public static String getAndSetString(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 设置指定的值，并返回其旧值，如果键不存在，则返回null
     *
     * @param key
     * @param value
     * @return 返回其旧值，如果键不存在，则返回null
     */
    public static Object getAndSetObject(String key, Object value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        redisTemplate(redisTemplate);
    }

    public static void redisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisTemplateUtil.redisTemplate = redisTemplate;
        stringRedisTemplate(redisTemplate.getConnectionFactory());
    }

    private static void stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplateUtil.stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
    }

    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public static StringRedisTemplate getRedisTemplateForString() {
        return stringRedisTemplate;
    }

    /**
     * redis锁
     * @param key
     * @param timeoutMsecs 锁等待时间
     * @param expireMsecs 锁超时时间
     * @return
     */
    public static RedisLock getRedisLock(String key, Integer timeoutMsecs, Integer expireMsecs) {
        if (timeoutMsecs != null) {
            if (expireMsecs != null) {
                return new RedisLock(getRedisTemplate(), key, timeoutMsecs, expireMsecs);
            }
            return new RedisLock(getRedisTemplate(), key, timeoutMsecs);
        }
        return new RedisLock(getRedisTemplate(), key);
    }

}
