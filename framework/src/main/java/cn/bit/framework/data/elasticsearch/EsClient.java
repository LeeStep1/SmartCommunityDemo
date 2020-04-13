package cn.bit.framework.data.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldstats.FieldStatsRequest;
import org.elasticsearch.action.fieldstats.FieldStatsRequestBuilder;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.termvectors.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * Created by terry on 2018/1/20.
 */
@Slf4j
public class EsClient implements Client, InitializingBean {

    private TransportClient _client;

    private String clusterName;
    private Boolean transportSniff = false;
    private String hosts;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Boolean getTransportSniff() {
        return transportSniff;
    }

    public void setTransportSniff(Boolean transportSniff) {
        this.transportSniff = transportSniff;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public TransportClient getClient() {
        return _client;
    }

    public EsClient() {
    }

    public EsClient(String clusterName, Boolean transportSniff, String hosts) {
        this.clusterName = clusterName;
        this.transportSniff = transportSniff;
        this.hosts = hosts;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Settings settings = Settings.builder().put("cluster.name", clusterName)
                .put("client.transport.sniff", transportSniff).build();
        _client = new PreBuiltTransportClient(settings);
        String[] hosts = this.hosts.split(",");
        for (String h : hosts) {
            String[] address = h.split(":");
            _client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address[0]),
                    Integer.parseInt(address[1])));
        }
    }

    public List<DiscoveryNode> listedNodes() {
        return _client.listedNodes();
    }

    public List<DiscoveryNode> filteredNodes() {
        return _client.filteredNodes();
    }

    public TransportClient removeTransportAddress(TransportAddress transportAddress) {
        return _client.removeTransportAddress(transportAddress);
    }

    public TransportClient addTransportAddress(TransportAddress transportAddress) {
        return _client.addTransportAddress(transportAddress);
    }

    public TransportClient addTransportAddresses(TransportAddress... transportAddress) {
        return _client.addTransportAddresses(transportAddress);
    }

    public List<DiscoveryNode> connectedNodes() {
        return _client.connectedNodes();
    }

    @Override
    public void close() {
        _client.close();
    }

    public List<TransportAddress> transportAddresses() {
        return _client.transportAddresses();
    }

    @Override
    public ActionFuture<IndexResponse> index(IndexRequest request) {
        return _client.index(request);
    }

    @Override
    public void index(IndexRequest request, ActionListener<IndexResponse> listener) {
        _client.index(request, listener);
    }

    @Override
    public IndexRequestBuilder prepareIndex() {
        return _client.prepareIndex();
    }

    @Override
    public IndexRequestBuilder prepareIndex(String index, String type) {
        return _client.prepareIndex(index, type);
    }

    @Override
    public IndexRequestBuilder prepareIndex(String index, String type, @Nullable String id) {
        return _client.prepareIndex(index, type, id);
    }

    @Override
    public ActionFuture<UpdateResponse> update(UpdateRequest request) {
        return _client.update(request);
    }

    @Override
    public void update(UpdateRequest request, ActionListener<UpdateResponse> listener) {
        _client.update(request, listener);
    }

    @Override
    public UpdateRequestBuilder prepareUpdate() {
        return _client.prepareUpdate();
    }

    @Override
    public UpdateRequestBuilder prepareUpdate(String index, String type, String id) {
        return _client.prepareUpdate(index, type, id);
    }

    @Override
    public ActionFuture<DeleteResponse> delete(DeleteRequest request) {
        return _client.delete(request);
    }

    @Override
    public void delete(DeleteRequest request, ActionListener<DeleteResponse> listener) {
        _client.delete(request, listener);
    }

    @Override
    public DeleteRequestBuilder prepareDelete() {
        return _client.prepareDelete();
    }

    @Override
    public DeleteRequestBuilder prepareDelete(String index, String type, String id) {
        return _client.prepareDelete(index, type, id);
    }

    @Override
    public ActionFuture<BulkResponse> bulk(BulkRequest request) {
        return _client.bulk(request);
    }

    @Override
    public void bulk(BulkRequest request, ActionListener<BulkResponse> listener) {
        _client.bulk(request, listener);
    }

    @Override
    public BulkRequestBuilder prepareBulk() {
        return _client.prepareBulk();
    }

    @Override
    public ActionFuture<GetResponse> get(GetRequest request) {
        return _client.get(request);
    }

    @Override
    public void get(GetRequest request, ActionListener<GetResponse> listener) {
        _client.get(request, listener);
    }

    @Override
    public GetRequestBuilder prepareGet() {
        return _client.prepareGet();
    }

    @Override
    public GetRequestBuilder prepareGet(String index, String type, String id) {
        return _client.prepareGet(index, type, id);
    }

    @Override
    public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest request) {
        return _client.multiGet(request);
    }

    @Override
    public void multiGet(MultiGetRequest request, ActionListener<MultiGetResponse> listener) {
        _client.multiGet(request, listener);
    }

    @Override
    public MultiGetRequestBuilder prepareMultiGet() {
        return _client.prepareMultiGet();
    }

    @Override
    public ActionFuture<SearchResponse> search(SearchRequest request) {
        return _client.search(request);
    }

    @Override
    public void search(SearchRequest request, ActionListener<SearchResponse> listener) {
        _client.search(request, listener);
    }

    @Override
    public SearchRequestBuilder prepareSearch(String... indices) {
        return _client.prepareSearch(indices);
    }

    @Override
    public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request) {
        return _client.searchScroll(request);
    }

    @Override
    public void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener) {
        _client.searchScroll(request, listener);
    }

    @Override
    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        return _client.prepareSearchScroll(scrollId);
    }

    @Override
    public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
        return _client.multiSearch(request);
    }

    @Override
    public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
        _client.multiSearch(request, listener);
    }

    @Override
    public MultiSearchRequestBuilder prepareMultiSearch() {
        return _client.prepareMultiSearch();
    }

    @Override
    public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest request) {
        return _client.termVectors(request);
    }

    @Override
    public void termVectors(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {
        _client.termVectors(request, listener);
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors() {
        return _client.prepareTermVectors();
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors(String index, String type, String id) {
        return _client.prepareTermVectors(index, type, id);
    }

    @Override
    @Deprecated
    public ActionFuture<TermVectorsResponse> termVector(TermVectorsRequest request) {
        return _client.termVector(request);
    }

    @Override
    @Deprecated
    public void termVector(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {
        _client.termVector(request, listener);
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVector() {
        return _client.prepareTermVector();
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVector(String index, String type, String id) {
        return _client.prepareTermVector(index, type, id);
    }

    @Override
    public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest request) {
        return _client.multiTermVectors(request);
    }

    @Override
    public void multiTermVectors(MultiTermVectorsRequest request, ActionListener<MultiTermVectorsResponse> listener) {
        _client.multiTermVectors(request, listener);
    }

    @Override
    public MultiTermVectorsRequestBuilder prepareMultiTermVectors() {
        return _client.prepareMultiTermVectors();
    }

    @Override
    public ExplainRequestBuilder prepareExplain(String index, String type, String id) {
        return _client.prepareExplain(index, type, id);
    }

    @Override
    public ActionFuture<ExplainResponse> explain(ExplainRequest request) {
        return _client.explain(request);
    }

    @Override
    public void explain(ExplainRequest request, ActionListener<ExplainResponse> listener) {
        _client.explain(request, listener);
    }

    @Override
    public void clearScroll(ClearScrollRequest request, ActionListener<ClearScrollResponse> listener) {
        _client.clearScroll(request, listener);
    }

    @Override
    public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest request) {
        return _client.clearScroll(request);
    }

    @Override
    public ClearScrollRequestBuilder prepareClearScroll() {
        return _client.prepareClearScroll();
    }

    @Override
    public void fieldStats(FieldStatsRequest request, ActionListener<FieldStatsResponse> listener) {
        _client.fieldStats(request, listener);
    }

    @Override
    public ActionFuture<FieldStatsResponse> fieldStats(FieldStatsRequest request) {
        return _client.fieldStats(request);
    }

    @Override
    public FieldStatsRequestBuilder prepareFieldStats() {
        return _client.prepareFieldStats();
    }

    @Override
    public Client filterWithHeader(Map<String, String> headers) {
        return _client.filterWithHeader(headers);
    }

    public String nodeName() {
        return _client.nodeName();
    }

    @Override
    public AdminClient admin() {
        return _client.admin();
    }

    @Override
    public Settings settings() {
        return _client.settings();
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends
            ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(Action<Request,
            Response, RequestBuilder> action, Request request) {
        return _client.execute(action, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends
            ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(Action<Request, Response,
            RequestBuilder> action, Request request, ActionListener<Response> listener) {
        _client.execute(action, request, listener);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends
            ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(Action<Request,
            Response, RequestBuilder> action) {
        return _client.prepareExecute(action);
    }

    @Override
    public ThreadPool threadPool() {
        return _client.threadPool();
    }
}
