package cn.bit.framework.data.elasticsearch.impl;

import cn.bit.framework.data.common.BaseEntity;


/**
 * Created by Administrator on 2017/6/14.
 */
class PersistentRequest<T extends BaseEntity> {

    private String index;
    private String type;
    private String id;
    private String parent;
    private T source;
    private int action;
    private String routing;
    public static final int ACTION_ADD = 0;
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_UPSERT = 3;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public static PersistentRequest build() {
        return new PersistentRequest();
    }

    public static PersistentRequest build(String index, String type, String id) {
        return build().index(index).type(type).id(id);
    }

    public PersistentRequest<T> index(String index) {
        this.index = index;
        return this;
    }

    public PersistentRequest<T> type(String type) {
        this.type = type;
        return this;
    }

    public PersistentRequest<T> id(String id) {
        this.id = id;
        return this;
    }

    public PersistentRequest<T> parent(String parent) {
        this.parent = parent;
        return this;
    }

    public PersistentRequest<T> source(T source) {
        this.source = source;
        return this;
    }

    public PersistentRequest<T> action(int action) {
        this.action = action;
        return this;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return this.action;
    }


}
