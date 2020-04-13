package cn.bit.facade.vo.communityIoT.door.freeview;

import lombok.Data;

import java.io.Serializable;

/**
 * @author decai.liu
 * @desc 门禁设备在线状态实体
 * @date 2018-08-23 15:49
 */
@Data
public class DoorOnlineStatusVO implements Serializable {
	/**
	 * 终端编号
	 */
	private String terminalCode;

	/**
	 * 在线状态（0：在线，1：离线）
	 */
	private Integer connectStatus;

	/**
	 * 在线状态描述
	 */
	private String connectStatusName;
}
