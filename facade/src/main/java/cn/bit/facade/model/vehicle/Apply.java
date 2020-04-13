package cn.bit.facade.model.vehicle;

import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * 车辆
 * @author Administrator
 *
 */
@Data
@Document(collection = "CAR_APPLY")
@CompoundIndex(def = "{'carNo' : 1, 'communityId' : 1}", background = true)
public class Apply implements Serializable
{

	@Id
	private ObjectId id;

	/**
	 * 社区ID
	 */
//	@NotNull(message = "社区ID不能为空")
	private ObjectId communityId;
	/**
	 * 车位名称
	 */
	private String parkingName;
	
	/**
	 * 车牌号
	 */
	@NotBlank(message = "车牌号不能为空")
	@Indexed(background = true)
	@Pattern(regexp = GlobalConstants.REGEX_CARNO, message = "车牌号码格式有误")
	private String carNo;
	
	/**
	 * 车主ID
	 */
	@NotNull(message = "车主ID不能为空")
	private ObjectId userId;
	
	/**
	 * 用户名称
	 */
	private String userName;
	
	/**
	 * 车辆型号
	 */
	private String carType;
	
	/**
	 * 审核状态（0：未审核，1：已审核，-1：未通过;）
	 */
	private Integer auditStatus;

	/**
	 * 创建人ID
	 */
	private ObjectId creatorId;

	/**
	 * 创建时间
	 */
	private Date createAt;

	/**
	 * 修改人ID
	 */
	private ObjectId modifierId;

	/**
	 * 修改时间
	 */
	private Date updateAt;

	/**
	 * 数据状态（1：有效；0：无效）
	 */
	private Integer dataStatus;
}
