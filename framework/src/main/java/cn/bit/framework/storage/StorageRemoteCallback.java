package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/6.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * @author terry
 * @create 2016-08-06 18:54
 **/
public class StorageRemoteCallback {

    private String url;
    private String body;
    private Map<String, String> vars = new HashMap<>();


    private StorageRemoteCallback() {
    }

    private StorageRemoteCallback(String url) {
        this.url = url;
    }

    private StorageRemoteCallback(String url, String body) {
        this.url = url;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getVars() {
        return vars;
    }

    public void setVars(Map<String, String> vars) {
        this.vars = vars;
    }

    public static StorageRemoteCallback build() {
        return new StorageRemoteCallback();
    }

    public static StorageRemoteCallback build(String url) {
        return new StorageRemoteCallback(url);
    }

    public static StorageRemoteCallback build(String url, String body) {
        return new StorageRemoteCallback(url, body);
    }

    public StorageRemoteCallback url(String url) {
        this.url = url;
        return this;
    }

    public StorageRemoteCallback body(String body) {
        this.body = body;
        return this;
    }

    public StorageRemoteCallback var(String key, String value) {
        this.vars.put(key, value);
        return this;
    }

    public StorageRemoteCallback vars(Map<String,String> vars) {
        this.vars.clear();
        if (vars != null && !vars.isEmpty()) {
            this.vars.putAll(vars);
        }
        return this;
    }

    public String var(String key) {
        return this.vars.get(key);
    }



}
