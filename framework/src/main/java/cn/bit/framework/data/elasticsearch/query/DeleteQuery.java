package cn.bit.framework.data.elasticsearch.query;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * Created by Administrator on 2017/6/13.
 */
public class DeleteQuery {

    private QueryBuilder query;
    private String index;
    private String type;

    public QueryBuilder getQuery() {
        return query;
    }

    public DeleteQuery setQuery(QueryBuilder query) {
        this.query = query;
        return this;
    }

    public String getIndex() {
        return index;
    }

    public DeleteQuery setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public DeleteQuery setType(String type) {
        this.type = type;
        return this;
    }

    public static DeleteQuery build() {
        return new DeleteQuery();
    }
}
