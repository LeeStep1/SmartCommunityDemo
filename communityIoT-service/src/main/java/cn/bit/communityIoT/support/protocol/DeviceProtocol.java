package cn.bit.communityIoT.support.protocol;

import cn.bit.communityIoT.support.protocol.data.Message;

/**
 * 设备协议接口
 *
 * @author decai.liu
 * @date 2018-07-06 10:40
 */
public interface DeviceProtocol {

	String encode(Message message);
	String getName();
}
