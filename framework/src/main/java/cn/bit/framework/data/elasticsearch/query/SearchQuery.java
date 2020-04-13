package cn.bit.framework.data.elasticsearch.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/13.
 */
public class SearchQuery {

    private QueryBuilder query;
    private QueryBuilder filter;
    private List<String> indices = new LinkedList<>();
    private List<String> types = new LinkedList<>();
    private List<String> fields;

    public QueryBuilder getQuery() {
        return query;
    }

    public SearchQuery setQuery(QueryBuilder query) {
        this.query = query;
        return this;
    }

    public QueryBuilder getFilter() {
        return filter;
    }

    public SearchQuery setFilter(QueryBuilder filter) {
        this.filter = filter;
        return this;
    }
    public List<String> getIndices() {
        return indices;
    }

    public List<String> getTypes() {
        return types;
    }

    public SearchQuery addIndices(String... indices) {
        this.indices.addAll(Arrays.asList(indices));
        return this;
    }

    public SearchQuery addTypes(String... types) {
        this.types.addAll(Arrays.asList(types));
        return this;
    }

    public SearchQuery fields(String... fields) {
        if (this.fields == null) {
            this.fields = new LinkedList<>();
        }
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public static SearchQuery build() {
        return new SearchQuery();
    }
}
