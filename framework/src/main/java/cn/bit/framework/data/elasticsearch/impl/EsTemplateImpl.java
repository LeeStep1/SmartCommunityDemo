package cn.bit.framework.data.elasticsearch.impl;

import cn.bit.framework.data.common.BaseEntity;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.data.elasticsearch.query.DeleteQuery;
import cn.bit.framework.data.elasticsearch.query.SearchQuery;
import cn.bit.framework.queue.BatchTakeQueue;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/1/22 0022.
 */
@Slf4j
public class EsTemplateImpl implements EsTemplate, InitializingBean {

    private Client _client;
    private BatchTakeQueue<PersistentRequest> delayIndexRequestQueue;
    private ScheduledExecutorService scheduler;

    private Integer bulkQueueCapacity = 200000;

    private Integer bulkQueueTakeSize = 10000;

    private Integer bulkQueueTakeInterval = 3;

    public Integer getBulkQueueCapacity() {
        return bulkQueueCapacity;
    }

    public void setBulkQueueCapacity(Integer bulkQueueCapacity) {
        this.bulkQueueCapacity = bulkQueueCapacity;
    }

    public Integer getBulkQueueTakeSize() {
        return bulkQueueTakeSize;
    }

    public void setBulkQueueTakeSize(Integer bulkQueueTakeSize) {
        this.bulkQueueTakeSize = bulkQueueTakeSize;
    }

    public Integer getBulkQueueTakeInterval() {
        return bulkQueueTakeInterval;
    }

    public void setBulkQueueTakeInterval(Integer bulkQueueTakeInterval) {
        this.bulkQueueTakeInterval = bulkQueueTakeInterval;
    }

    public EsTemplateImpl(Client client) {
        this._client = client;
    }

    @Override
    public Client getClient() {
        return this._client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        delayIndexRequestQueue = new BatchTakeQueue<PersistentRequest>(bulkQueueCapacity, bulkQueueTakeSize) {
            @Override
            public void take(List<PersistentRequest> lst) {
                log.debug("take bulk >>>>>>>>>> " + lst.size());
                BulkRequestBuilder bulkRequest = _client.prepareBulk();
                lst.forEach(request -> {
                    switch (request.getAction()) {
                        case PersistentRequest.ACTION_ADD:
                            IndexRequestBuilder indexRequestBuilder = _client.prepareIndex(request.getIndex(),
                                    request.getType(), request.getId()).setSource(
                                    JSON.toJSONBytes(request.getSource()), XContentType.JSON);
                            if (StringUtils.isNotBlank(request.getRouting()))
                                indexRequestBuilder.setRouting(request.getRouting());
                            bulkRequest.add(indexRequestBuilder);
                            break;
                        case PersistentRequest.ACTION_UPDATE:
                            UpdateRequestBuilder updateRequestBuilder = _client.prepareUpdate(request.getIndex(),
                                    request.getType(), request.getId()).setDoc(
                                    JSON.toJSONBytes(request.getSource()), XContentType.JSON);
                            if (StringUtils.isNotBlank(request.getRouting()))
                                updateRequestBuilder.setRouting(request.getRouting());
                            bulkRequest.add(updateRequestBuilder);
                            break;
                        case PersistentRequest.ACTION_DELETE:
                            DeleteRequestBuilder deleteRequestBuilder = _client.prepareDelete(request.getIndex(),
                                    request.getType(), request.getId());
                            if (StringUtils.isNotBlank(request.getRouting()))
                                deleteRequestBuilder.setRouting(request.getRouting());
                            bulkRequest.add(deleteRequestBuilder);
                            break;
                        case PersistentRequest.ACTION_UPSERT:
                            UpdateRequestBuilder upsertRequestBuilder = _client.prepareUpdate(request.getIndex(),
                                    request.getType(), request.getId()).setDoc(JSON.toJSONBytes(request.getSource()),
                                    XContentType.JSON).setDocAsUpsert(true);
                            if (StringUtils.isNotBlank(request.getRouting()))
                                upsertRequestBuilder.setRouting(request.getRouting());
                            bulkRequest.add(upsertRequestBuilder);
                            break;
                        default:
                            break;
                    }
                });
                if (bulkRequest.numberOfActions() > 0) {
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        throw new RuntimeException("bulk fail! " + bulkResponse.buildFailureMessage());
                    }
                }
            }
        };

        scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), (r) -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(delayIndexRequestQueue, 1, bulkQueueTakeInterval, TimeUnit.SECONDS);
    }

    @Override
    public <T extends BaseEntity> String insert(String index, String type, String id, T source) {
        return insert(index, type, id, null, source);
    }

    @Override
    public <T extends BaseEntity> String insert(String index, String type, String id, String routing, T source) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.notNull(source, "source不能为空");
        IndexRequestBuilder indexRequestBuilder = _client.prepareIndex(index, type, id).
                setSource(JSON.toJSONBytes(source), XContentType.JSON);
        if (StringUtils.isNotBlank(routing))
            indexRequestBuilder.setRouting(routing);
        String docId = indexRequestBuilder.execute().actionGet().getId();
        source.setId(docId);
        return docId;
    }

    @Override
    public <T extends BaseEntity> void insertAsync(String index, String type, String id, T source) {
        insertAsync(index, type, id, null, source);
    }

    @Override
    public <T extends BaseEntity> void insertAsync(String index, String type,
                                                   String id, String routing, T source) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.notNull(source, "source不能为空");
        PersistentRequest<T> request = PersistentRequest.build().source(source).id(id).type(type).index(index)
                .action(PersistentRequest.ACTION_ADD);
        request.setRouting(routing);
        try {
            delayIndexRequestQueue.put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public <T extends BaseEntity> void upsert(String index, String type, String id, T source) {
        upsert(index, type, id, null, source);
    }

    @Override
    public <T extends BaseEntity> void upsert(String index, String type, String id, String routing, T source) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        Assert.notNull(source, "source不能为空");

        byte[] json = JSON.toJSONBytes(source);
        UpdateRequestBuilder upsertRequestBuilder = _client.prepareUpdate(index, type, id)
                .setDoc(json, XContentType.JSON).setDocAsUpsert(true);
        if (StringUtils.isNotBlank(routing))
            upsertRequestBuilder.setRouting(routing);
        upsertRequestBuilder.execute().actionGet();
    }

    @Override
    public <T extends BaseEntity> void upsertAsync(String index, String type, String id, T source) {
        upsertAsync(index, type, id, null, source);
    }

    @Override
    public <T extends BaseEntity> void upsertAsync(String index, String type, String id, String routing, T source) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        Assert.notNull(source, "source不能为空");
        PersistentRequest<T> request = PersistentRequest.build().source(source).id(id).type
                (type).index(index).action(PersistentRequest.ACTION_UPSERT);
        request.setRouting(routing);
        try {
            delayIndexRequestQueue.put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public <T extends BaseEntity> void update(String index, String type, String id, T source) {
        update(index, type, id, null, source);
    }

    @Override
    public <T extends BaseEntity> void update(String index, String type, String id, String routing, T source) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        Assert.notNull(source, "source不能为空");
        UpdateRequestBuilder upsertRequestBuilder = _client.prepareUpdate(index, type, id)
                .setDoc(JSON.toJSONBytes(source), XContentType.JSON);
        if (StringUtils.isNotBlank(routing))
            upsertRequestBuilder.setRouting(routing);

        upsertRequestBuilder.execute().actionGet();
    }

    @Override
    public void update(SearchQuery query, String script) {
        UpdateByQueryAction.INSTANCE.newRequestBuilder(_client).source(toArray(query.getIndices())).script(new
                Script(script)).filter(query.getQuery()).abortOnVersionConflict(false).execute().actionGet();
    }

    @Override
    public <T extends BaseEntity> void updateAsync(String index, String type, String id, T source) {
        updateAsync(index, type, id, null, source);
    }

    @Override
    public <T extends BaseEntity> void updateAsync(String index, String type, String id, String routing, T source) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        Assert.notNull(source, "source不能为空");
        PersistentRequest<T> request = PersistentRequest.build().source(source).id(id).type(type).index(index)
                .action(PersistentRequest.ACTION_UPDATE);
        if (StringUtils.isNotBlank(routing))
            request.setRouting(routing);
        try {
            delayIndexRequestQueue.put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends BaseEntity> T get(String index, String type, String id, Class<T> cls) {
        return get(index, type, id, null, cls);
    }

    @Override
    public <T extends BaseEntity> T get(String index, String type, String id, String routing, Class<T> cls) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        GetRequestBuilder getRequestBuilder = _client.prepareGet(index, type, id);
        if (StringUtils.isNotBlank(routing))
            getRequestBuilder.setRouting(routing);
        GetResponse response = getRequestBuilder.execute().actionGet();
        if (!response.isExists()) {
            return null;
        }
        T object = JSON.parseObject(response.getSourceAsBytes(), cls);
        object.setId(response.getId());
        return object;
    }

    @Override
    public <T extends BaseEntity> List<T> multiGet(String index, String type, Class<T> cls, String... ids) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.notEmpty(ids, "ids不能为空");
        List<T> lst = new LinkedList<>();
        if (ids.length == 0) {
            return lst;
        }
        MultiGetResponse response = _client.prepareMultiGet().add(index, type, ids).execute().actionGet();
        for (MultiGetItemResponse multiGetItemResponse : response) {
            T object = JSON.parseObject(multiGetItemResponse.getResponse().getSourceAsBytes(), cls);
            object.setId(multiGetItemResponse.getResponse().getId());
            lst.add(object);
        }
        return lst;
    }

    @Override
    public void delete(String index, String type, String id) {
        delete(index, type, null, id);
    }

    @Override
    public void delete(String index, String type, String routing, String id) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        DeleteRequestBuilder deleteRequestBuilder = _client.prepareDelete(index, type, id);
        if (StringUtils.isNotBlank(routing))
            deleteRequestBuilder.setRouting(routing);

        deleteRequestBuilder.execute().actionGet();
    }

    @Override
    public void deleteAsync(String index, String type, String id) {
        deleteAsync(index, type, null, id);
    }

    @Override
    public void deleteAsync(String index, String type, String routing, String id) {
        Assert.isTrue(StringUtils.isNotBlank(index), "index不能为空");
        Assert.isTrue(StringUtils.isNotBlank(type), "type不能为空");
        Assert.isTrue(StringUtils.isNotBlank(id), "id不能为空");
        PersistentRequest request = PersistentRequest.build(index, type, id)
                .action(PersistentRequest.ACTION_DELETE);
        if (StringUtils.isNotBlank(routing))
            request.setRouting(routing);
        try {
            delayIndexRequestQueue.put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(DeleteQuery query) {
        Assert.notNull(query);
        Assert.notNull(query.getIndex());
        Assert.notNull(query.getType());
        Assert.notNull(query.getQuery());
        DeleteByQueryAction.INSTANCE.newRequestBuilder(_client).filter(query.getQuery()).source(query
                .getIndex()).execute().actionGet();
    }

    @Override
    public <T extends BaseEntity> T searchForObject(SearchQuery query, Class<T> cls) {
        return searchForObject(query, null, cls);
    }

    @Override
    public <T extends BaseEntity> T searchForObject(SearchQuery query, String routing, Class<T> cls) {
        SearchRequestBuilder searchRequestBuilder = _client.prepareSearch(toArray(query.getIndices()))
                .setTypes(toArray(query.getTypes()))
                .setQuery(query.getQuery())
                .setPostFilter(query.getFilter())
                .setFrom(0)
                .setSize(1);
        if (StringUtils.isNotBlank(routing))
            searchRequestBuilder.setRouting(routing);
        if (query.getFields() != null) {
            searchRequestBuilder.setFetchSource(toArray(query.getFields()), null);
        }
        SearchResponse response = searchRequestBuilder.execute().actionGet();
        if (response.getHits().hits().length <= 0)
            return null;
        T object = JSON.parseObject(response.getHits().hits()[0].getSourceAsString(), cls);
        object.setId(response.getHits().hits()[0].getId());
        return object;
    }

    @Override
    public <T extends BaseEntity> List<T> searchForList(SearchQuery query, XSort sort, Class<T> cls) {
        return searchForList(query, null, sort, cls);
    }

    @Override
    public <T extends BaseEntity> List<T> searchForList(SearchQuery query, String routing, XSort sort, Class<T> cls) {
        List<T> result = new LinkedList<>();
        SearchRequestBuilder searchRequestBuilder = _client.prepareSearch(toArray(query.getIndices())).setTypes
                (toArray(query.getTypes()))
                .setScroll(TimeValue.timeValueSeconds(10))
                .setQuery(query.getQuery())
                .setSize(1000);
        if (StringUtils.isNotBlank(routing)) {
            searchRequestBuilder.setRouting(routing);
        }
        if (query.getFields() != null) {
            searchRequestBuilder.setFetchSource(toArray(query.getFields()), null);
        }
        if (sort != null) {
            addSort(searchRequestBuilder, sort);
        }
        SearchResponse scrollResp = searchRequestBuilder.execute().actionGet();
        do {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                T item = JSON.parseObject(hit.getSourceAsString(), cls);
                item.setId(hit.getId());
                result.add(item);
            }
            scrollResp = _client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(TimeValue.timeValueSeconds(10)).execute().actionGet();
        } while (scrollResp.getHits().getHits().length != 0);
        String scrollId = scrollResp.getScrollId();
        CompletableFuture.runAsync(() ->
                _client.prepareClearScroll().addScrollId(scrollId).execute().actionGet()
        );
        return result;
    }

    @Override
    public <T extends BaseEntity> Page<T> searchForPage(SearchQuery query, int pageNo, int pageSize,
                                                        XSort sort, Class<T> cls) {
        return searchForPage(query, pageNo, pageSize, null, sort, cls);
    }

    @Override
    public <T extends BaseEntity> Page<T> searchForPage(SearchQuery query, int pageNo, int pageSize,
                                                        String routing, XSort sort, Class<T> cls) {
        Assert.isTrue(pageNo >= 1, "pageNo 不能小于1");
        Assert.isTrue(pageSize > 0, "pageSize 必须大于0");
        int from = (pageNo - 1) * pageSize;
        List<T> records = new LinkedList<>();
        Page<T> page = new Page<T>(1, 0, pageSize, records);
        SearchRequestBuilder searchRequestBuilder = _client.prepareSearch(toArray(query.getIndices()))
                .setTypes(toArray(query.getTypes()))
                .setQuery(query.getQuery())
                .setPostFilter(query.getFilter())
                .setFrom(from)
                .setSize(pageSize);
        if (StringUtils.isNotBlank(routing))
            searchRequestBuilder.setRouting(routing);
        if (query.getFields() != null) {
            searchRequestBuilder.setFetchSource(toArray(query.getFields()), null);
        }
        if (sort != null) {
            addSort(searchRequestBuilder, sort);
        }
        SearchResponse response = searchRequestBuilder.execute().actionGet();

        if (response.getHits().hits().length <= 0)
            return page;
        for (SearchHit hit : response.getHits()) {
            page.getRecords().add(JSON.parseObject(hit.getSourceAsString(), cls));
        }
        page.setTotal(Long.valueOf(response.getHits().getTotalHits()).intValue());
        return page;
    }

    @Override
    public long count(SearchQuery query) {
        return count(query, null);
    }

    @Override
    public long count(SearchQuery query, String routing) {
        SearchRequestBuilder searchRequestBuilder = _client.prepareSearch(toArray(query.getIndices()))
                .setTypes(toArray(query.getTypes()))
                .setQuery(query.getQuery())
                .setPostFilter(query.getFilter())
                .setFrom(0)
                .setSize(0);
        if (StringUtils.isNotBlank(routing))
            searchRequestBuilder.setRouting(routing);
        SearchResponse response = searchRequestBuilder.execute().actionGet();
        return response.getHits().getTotalHits();
    }

    /**
     * 分词
     *
     *
     * @param content
     * @return
     */
    @Override
    public Set<String> getParticiplesByIkAnalyze(String content) {
        AnalyzeRequestBuilder ikRequestBuilder = new AnalyzeRequestBuilder(_client, AnalyzeAction.INSTANCE);
        ikRequestBuilder.setTokenizer("ik_max_word");
        ikRequestBuilder.setText(content);
        List<AnalyzeResponse.AnalyzeToken> ikTokenList = ikRequestBuilder.execute().actionGet().getTokens();
        return ikTokenList.stream().map(AnalyzeResponse.AnalyzeToken::getTerm).collect(Collectors.toSet());
    }

    private static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    private void addSort(SearchRequestBuilder searchRequest, XSort sort) {
        sort.forEach(order -> searchRequest.addSort(order.getProperty(),
                SortOrder.fromString(order.getDirection().name())));
    }
}
