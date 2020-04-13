package cn.bit.communityIoT.support.protocol.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author decai.liu
 * @desc 协议信息
 * @date 2018-07-06 11:05
 */
public class Message {
	Map<String, Object> meta = Collections.emptyMap();

	public Message setMeta(String name, Object value){
		if(meta.isEmpty()){
			meta = new HashMap<>();
		}
		meta.put(name, value);
		return this;
	}

	public Object getMeta(String name){
		return meta.get(name);
	}
}