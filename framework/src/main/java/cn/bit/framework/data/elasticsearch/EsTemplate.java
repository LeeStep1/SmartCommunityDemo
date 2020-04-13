package cn.bit.framework.data.elasticsearch;


import cn.bit.framework.data.common.BaseEntity;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.query.DeleteQuery;
import cn.bit.framework.data.elasticsearch.query.SearchQuery;
import cn.bit.framework.exceptions.BizException;
import org.elasticsearch.client.Client;

import java.util.List;
import java.util.Set;

/**
 * ElasticSearch基本方法封装,复杂操作例如aggregation请直接通过getClient获取ElasticSearch Client进行
 * Created by Terry on 2018/1/20.
 */
public interface EsTemplate {

    /**
     * 获取ElasticSearch Client
     *
     * @return
     */
    Client getClient();

    /**
     * 插入文档
     *
     * @param index  索引名称
     * @param type   type名称
     * @param id     文档id,可选
     * @param source 文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> String insert(String index, String type, String id, T source);

    /**
     * 插入文档，并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id,可选
     * @param routing 路由key
     * @param source  文档内容
     * @param <T>
     * @return 文档id
     * @throws BizException
     */
    <T extends BaseEntity> String insert(String index, String type, String id, String routing, T source);

    /**
     * 异步插入文档
     *
     * @param index  索引名称
     * @param type   type名称
     * @param id     文档id, 可选
     * @param source 文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void insertAsync(String index, String type, String id, T source);

    /**
     * 异步插入文档，并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id,可选
     * @param routing 路由key
     * @param source  文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void insertAsync(String index, String type, String id, String routing, T source);


    /**
     * upsert文档
     *
     * @param index  索引名称
     * @param type   type名称
     * @param id     文档id
     * @param source 文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void upsert(String index, String type, String id, T source);

    /**
     * upsert文档, 并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id
     * @param routing 路由key
     * @param source  文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void upsert(String index, String type, String id, String routing, T source);

    /**
     * 异步upsert文档
     *
     * @param index  索引名称
     * @param type   type名称
     * @param id     文档id
     * @param source 文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void upsertAsync(String index, String type, String id, T source);

    /**
     * 异步upsert文档, 并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id
     * @param routing 路由key
     * @param source  文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void upsertAsync(String index, String type, String id, String routing, T source);

    /**
     * 更新文档
     *
     * @param index  索引名称
     * @param type   type名称
     * @param id     文档id
     * @param source 所需更新的文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void update(String index, String type, String id, T source);

    /**
     * 更新文档，并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id
     * @param routing 路由key
     * @param source  所需更新的文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void update(String index, String type, String id, String routing, T source);

    /**
     * 根据查询条件更新文档
     *
     * @param query  query对象
     * @param script 更新脚本
     * @
     */
    void update(SearchQuery query, String script);

    /**
     * 异步更新文档
     *
     * @param index  索引名称
     * @param type   type名称
     * @param id     文档id
     * @param source 所需更新的文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void updateAsync(String index, String type, String id, T source);

    /**
     * 异步更新文档，并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id
     * @param routing 路由key
     * @param source  所需更新的文档内容
     * @param <T>
     * @
     */
    <T extends BaseEntity> void updateAsync(String index, String type, String id, String routing, T source);

    /**
     * 按照id获取文档
     *
     * @param index 索引名称
     * @param type  type名称
     * @param id    文档id
     * @param cls   实体class
     * @return 文档实体
     * @
     */
    <T extends BaseEntity> T get(String index, String type, String id, Class<T> cls);

    /**
     * 按照id获取文档，并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param id      文档id
     * @param routing 路由key
     * @param cls     实体class
     * @param <T>
     * @return 文档实体
     */
    <T extends BaseEntity> T get(String index, String type, String id, String routing, Class<T> cls);

    /**
     * 按照id数组，一次性获取多个文档
     *
     * @param index 索引名称
     * @param type  type名称
     * @param ids   id数组
     * @param cls   实体class
     * @param <T>
     * @return 文档实体List
     * @
     */
    <T extends BaseEntity> List<T> multiGet(String index, String type, Class<T> cls, String... ids);

    /**
     * 删除文档
     *
     * @param index 索引名称
     * @param type  type名称
     * @param id    文档id
     * @
     */
    void delete(String index, String type, String id);

    /**
     * 删除文档, 并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param routing 路由key
     * @param id      文档id
     * @
     */
    void delete(String index, String type, String routing, String id);

    /**
     * 异步删除文档
     *
     * @param index 索引名称
     * @param type  type名称
     * @param id    文档id
     * @
     */
    void deleteAsync(String index, String type, String id);

    /**
     * 异步删除文档,并指定路由key
     *
     * @param index   索引名称
     * @param type    type名称
     * @param routing 路由key
     * @param id      文档id
     * @
     */
    void deleteAsync(String index, String type, String routing, String id);

    /**
     * 按照查询条件批量删除文档
     *
     * @param query
     * @
     */
    void delete(DeleteQuery query);

    /**
     * 搜索单个文档
     *
     * @param query
     * @param <T>
     * @return
     */
    <T extends BaseEntity> T searchForObject(SearchQuery query, Class<T> cls);

    /**
     * 搜索单个文档，并指定路由key
     *
     * @param query
     * @param routing
     * @param cls
     * @param <T>
     * @return
     */
    <T extends BaseEntity> T searchForObject(SearchQuery query, String routing, Class<T> cls);

    /**
     * 搜索文档list
     *
     * @param query
     * @param cls
     * @param sort
     * @param <T>
     * @return
     */
    <T extends BaseEntity> List<T> searchForList(SearchQuery query, XSort sort, Class<T> cls);

    /**
     * 搜索文档list，并指定路由key
     *
     * @param query
     * @param routing
     * @param sort
     * @param cls
     * @param <T>
     * @return
     */
    <T extends BaseEntity> List<T> searchForList(SearchQuery query, String routing, XSort sort, Class<T> cls);

    /**
     * 分页搜索文档
     *
     * @param query
     * @param cls
     * @param <T>
     * @return
     */
    <T extends BaseEntity> Page<T> searchForPage(SearchQuery query, int pageNo,
                                                 int pageSize, XSort sort, Class<T> cls);

    /**
     * 分页搜索文档，并指定路由key
     *
     * @param query
     * @param routing
     * @param cls
     * @param <T>
     * @return
     */
    <T extends BaseEntity> Page<T> searchForPage(SearchQuery query, int pageNo, int pageSize, String routing,
                                                 XSort sort, Class<T> cls);

    /**
     * 获取文档总数
     *
     * @param query
     * @return
     */
    long count(SearchQuery query);

    /**
     * 获取文档总数，指定路由key
     *
     * @param query
     * @param routing
     * @return
     */
    long count(SearchQuery query, String routing);

    /**
     * 分词
     *
     * @param content
     * @return
     */
    Set<String> getParticiplesByIkAnalyze(String content);
}
