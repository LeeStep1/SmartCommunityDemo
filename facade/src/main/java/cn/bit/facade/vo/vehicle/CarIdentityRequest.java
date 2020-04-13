package cn.bit.facade.vo.vehicle;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 车禁
 * @author fangxiaoyu
 *
 */
@Data
public class CarIdentityRequest implements Serializable {

	/**
	 * ID
	 */
	private int p_KEY;
	
	/**
	 * 车牌号码
	 */
	private String carNo;
	
	/**
	 * 姓名
	 */
	private String cName;
	
	/**
	 * 身份证号
	 */
	private String cID;
	
	/**
	 * 联系电话
	 */
	private String phoneNumber;
	
	/**
	 * 发行日期
	 */
	private Date releaseDate;
	
	/**
	 * 卡片状态
	 */
	private Integer cardStatus = 0;
	
	/**
	 * 住址
	 */
	private String address;
	
	/**
	 * 开始日期
	 */
	private Date beginDate;
	
	/**
	 * 截止时间
	 */
	private Date closingDate;
	
	/**
	 * 车辆类型 1=民用  2=军队    3=警用   4=武警
	 */
	private Integer vehicleType = 1;
	
	/**
	 * 车牌类型 1=民用  2=军队    3=警用   4=武警
	 */
	private Integer licencePlateType = 1;
	
	/**
	 * 收费类型 1=月卡  2=临时车  3=免费车 4=储值卡
	 */
	private Integer chargeType = 1;
	
	/**
	 * 停放位置
	 */
	private String parkedPosition;
	
	/**
	 * 开闸机号
	 */
	private String openingMachineNumber = "111111111111111111111111111111111111111111111111";
	
	/**
	 * 同属组号
	 */
	private int grpNo;
	
	/**
	 * 是否主车
	 */
	private int mCar = 1;
	
	/**
	 * 可以在场内固定车数
	 */
	private int inCar = 1;
	
	/**
	 * 收费类型
	 */
	private String chargeCode = "-1";
	
	/**
	 * 允许进出开始\结束时间 000000000000
	 */
	private String passTime;
	
	/**
	 * 备注
	 */
	private String remark;
	
	/**
	 * 开户金额，默认为0
	 */
	private float accountSize = 0;
	
	/**
	 * 统计车位 1参与 0不参与
	 */
	private int isCountParkingLot = 1;
	
	/**
	 * IC或蓝牙卡号
	 */
	private String icNo;
	
	/**
	 * 是否已经下载白名单：0未下载   1下载
	 */
	private int download = 0;
	
	/**
	 * 全部控制器成功下载完成时间
	 */
	private Date downloadTime;
}
