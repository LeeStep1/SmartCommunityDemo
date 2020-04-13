package cn.bit.communityIoT.support.protocol.data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author decai.liu
 * @desc 4B55协议信息
 * @date 2018-07-06 11:05
 */
public class Message4B55 extends Message {
	private static final String MSG_CMD = "msgCMD";
	private static final String MSG_CID = "msgCID";
	private static final String MSG_TYPE = "msgType";
	// 操作系统
	private static final String MSG_SYS = "msgSys";
	// 初始时间
	private static final String MSG_TIME = "msgTime";
	// 终止时间
	private static final String MSG_TIME_TO = "msgTimeTo";

	private static final String MSG_HOUSES = "msgHouses";

	public Integer getMsgCMD() {
		return getMeta(MSG_CMD) == null ? null : Integer.parseInt(getMeta(MSG_CMD).toString());
	}

	public void setMsgCMD(Integer msgCMD) {
		setMeta(MSG_CMD, msgCMD);
	}

	public Integer getMsgCID() {
		return getMeta(MSG_CID) == null ? null : Integer.parseInt(getMeta(MSG_CID).toString());
	}

	public void setMsgCID(Integer msgCID) {
		setMeta(MSG_CID, msgCID);
	}

	public Integer getMsgType() {
		return getMeta(MSG_TYPE) == null ? null : Integer.parseInt(getMeta(MSG_TYPE).toString());
	}

	public void setMsgType(Integer msgType) {
		setMeta(MSG_TYPE, msgType);
	}

	public Integer getMsgSys() {
		return getMeta(MSG_SYS) == null ? null : Integer.parseInt(getMeta(MSG_SYS).toString());
	}

	public void setMsgSys(Integer msgSys) {
		setMeta(MSG_SYS, msgSys);
	}

	public Date getMsgTime() {
		return getMeta(MSG_TIME) == null ? null : (Date) getMeta(MSG_TIME);
	}

	public void setMsgTime(Date msgTime) {
		setMeta(MSG_TIME, msgTime);
	}

	public Date getMsgTimeTo() {
		return getMeta(MSG_TIME_TO) == null ? null : (Date) getMeta(MSG_TIME_TO);
	}

	public void setMsgTimeTo(Date msgTimeTo) {
		setMeta(MSG_TIME_TO, msgTimeTo);
	}

	public List<MsgHouse> getMsgHouses() {
		return getMeta(MSG_HOUSES) == null ? Collections.EMPTY_LIST : (List)getMeta(MSG_HOUSES);
	}

	public void setMsgHouses(List<MsgHouse> msgHouses) {
		setMeta(MSG_HOUSES, msgHouses);
	}
}
