package cn.bit.facade.data.property;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * 社区-物业公司返回实体
 *
 * @author decai.liu
 * @date 2018-09-25
 */
@Data
public class PropertyDTO implements Serializable {

	/**
	 * 社区ID
	 */
	private ObjectId communityId;

	/**
	 * 企业id
	 */
	private ObjectId companyId;

	/**
	 * 企业名称
	 */
	private String companyName;

}
