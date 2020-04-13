package cn.bit.facade.vo.communityIoT.protocol;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author decai.liu
 * @desc 禁卡/解禁入参实体
 * @date 20190508
 */
@Data
public class BanCardVO implements Serializable {

	/**
	 * 楼栋ID
	 */
	private ObjectId buildingId;

	/**
	 * 房间id集合
	 */
	private List<ObjectId> roomIds;

	/**
	 * 协议
	 */
	private String key;

	/**
	 * 房间对应位置
	 */
	private List<IdName> roomLocations;

	/**
	 * 梯控开始时间
	 */
	private Date startAt;

	/**
	 * 梯控结束时间
	 */
	private Date endAt;

	@Data
	public class IdName implements Serializable{
		private ObjectId id;
		private String name;
	}
}
