package cn.bit.framework.param;/**
 * Created by terry on 2016/7/29.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * @author terry
 * @create 2016-07-29 18:18
 **/
public class ParamsBuilder {

    private Map<String, Object> map = new HashMap<>();

    private ParamsBuilder() {
    }

    private ParamsBuilder(String name, Object value) {
        map.put(name, value);
    }

    public ParamsBuilder param(String name, Object value) {
        map.put(name, value);
        return this;
    }

    public ParamsBuilder params(Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            map.putAll(params);
        }
        return this;
    }

    public Object param(String name) {
        return map.get(name);
    }

    public Map<String, Object> toMap() {
        return this.map;
    }

    public static ParamsBuilder build() {
        return new ParamsBuilder();
    }

    public static ParamsBuilder build(String name, Object value) {
        return new ParamsBuilder(name, value);
    }
}
