package cn.bit.framework.data.mongodb;

import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2018/1/26 0026.
 */
public interface MongoDao<T, PK extends Serializable> {

    /**
     * 插入
     */
    T insert(T entity);

    void insertAll(List<T> entities);

    /**
     * @param id
     * @return
     */
    T findById(PK id);


    /**
     * 通过ID获取记录,并且指定了集合
     *
     * @param id
     * @param collection
     * @return
     */
    T findById(PK id, String collection);

    /**
     * @return
     */
    List<T> findAll();

    /**
     * @param collection
     * @return
     */
    List<T> findAll(String collection);

    /**
     * 根据条件查询
     */
    List<T> find(Query query, XSort sort);

    /**
     * 根据传入实体的属性查询
     */
    List<T> find(T entity, XSort sort);

    /**
     * 根据条件查询一个
     */
    T findOne(Query query);

    /**
     * 根据传入实体的属性查询一个
     */
    T findOne(T entity);

    /**
     * 分页查询
     */
    Page<T> findPage(Query query, int page, int size, XSort sort);

    /**
     * 根据传入实体的属性分页查询
     */
    Page<T> findPage(T entity, int page, int size, XSort sort);

    /**
     * 根据条件 获得总数
     */
    long count(Query query);

    /**
     * 根据条件 更新
     */
    int update(Query query, Update update);

    /**
     * 更新符合条件并sort之后的第一个文档 并返回更新后的文档
     */
    T updateOne(Query query, Update update);

    /**
     * 根据传入实体更新,实体对象id字段必须赋值
     * 传入实体中值为null的属性默认不更新,需要更新值为null的属性需要通过fieldsToUnsetIfNull指定需要删除的键
     * @param entity
     * @param fieldsToUnsetIfNull
     * @return
     */
    T updateOne(T entity, String... fieldsToUnsetIfNull);

    /**
     * 按样本更新
     * 传入实体中值为null的属性默认不更新,需要更新值为null的属性需要通过fieldsToUnsetIfNull指定需要删除的键
     * @param query query对象
     * @param entity 实体样本
     * @return
     */
    int updateByExample(Query query, T entity, String... fieldsToUnsetIfNull);

    /**
     * 根据条件 删除
     *
     * @param query
     */
    int remove(Query query);

    /**
     * 根据id 删除
     *
     * @param id
     * @return
     */
    int remove(PK id);

    /**
     * @param query
     * @return
     */
    boolean exist(Query query);

    /**
     * @param entity
     * @return
     */
    boolean exist(T entity);
}
