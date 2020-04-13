package cn.bit.facade.vo.communityIoT.protocol;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author decai.liu
 * @desc 离线IC卡协议入参实体
 * @date 2018-09-25
 */
@Data
public class IcCardVO implements Serializable {

	/**
	 * 使用期限时长
	 */
	private Integer processTime;

	/**
	 * 时间度量单位 {@link cn.bit.facade.enums.TimeUnitEnum}
	 */
	private Integer timeUnit;

	/**
	 * add at 2018-11-15 by decai.liu
	 * 指定的过期时间
	 */
	private Date expireAt;

	/**
	 * 目标设施的id
	 */
	private ObjectId targetId;

	/**
	 * 用户类型（1：住户，2：物业）
	 */
	@NotNull(message = "用户类型不能为空")
	private Integer userType;

}
