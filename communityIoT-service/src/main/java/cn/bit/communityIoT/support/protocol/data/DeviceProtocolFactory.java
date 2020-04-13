package cn.bit.communityIoT.support.protocol.data;

import cn.bit.communityIoT.support.protocol.DeviceProtocol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author decai.liu
 * @desc 设备协议工厂类
 * @date 2018-07-06 11:09
 */
public class DeviceProtocolFactory {
	private static Map<String, DeviceProtocol> protocols = new HashMap<>();

	static{
		ServiceLoader<DeviceProtocol> serviceLoader = ServiceLoader.load(DeviceProtocol.class);
		Iterator<DeviceProtocol> iterator = serviceLoader.iterator();
		while(iterator.hasNext()){
			DeviceProtocol protocol = iterator.next();
			protocols.put(protocol.getName(), protocol);
		}
	}

	public static DeviceProtocol getInstance(String name){
		return protocols.get(name);
	}
}
