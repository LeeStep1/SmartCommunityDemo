package cn.bit.facade.vo.communityIoT.protocol;

import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author decai.liu
 * @desc 离线设备协议入参实体
 * @date 2018-07-13 15:16
 */
public class ProtocolVO implements Serializable {

	/**
	 * 设施的层级
	 */
	private Integer level;

	/**
	 * 目标设施的id
	 */
	private ObjectId target;

	/**
	 * 读头设备mac信息
	 */
	private String mac;

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public ObjectId getTarget() {
		return target;
	}

	public void setTarget(ObjectId target) {
		this.target = target;
	}
}
