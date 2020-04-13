package cn.bit.framework.utils.validate;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * 
 * @描述: 身份证工具类.
 * @作者: WuShuicheng .
 * @创建时间: 2013-8-9,上午9:50:22 .
 * @版本: 1.0 .
 */
public class IDCardUtils {

	// 加权因字数
	private static final int[] WI = new int[] { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	// 代码
	private static final char[] CODE = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

	/**
	 * 检查身份证是否合法
	 * 
	 * @param card
	 *            身份证号码
	 * @return 是/否
	 */
	public static boolean verifi(String card) {
		if (card.length() == 15 && Pattern.matches("^\\d{15}$", card)) {
			card = card15$18(card);
		}
		if (card.length() == 18 && isDate(card)) {
			card = card.toUpperCase();
			if (Pattern.matches("^\\d{17}[xX]|\\d{18}$", card)) {
				char[] chars = card.toCharArray();
				int si = 0;
				for (int i = 0; i < 17; i++) {
					si += (chars[i] - '0') * WI[i];
				}
				return chars[17] == CODE[si % 11];
			}
			return false;
		}
		return false;
	}

	private static boolean isDate(String card) {
		String y = card.substring(6, 10);
		String m = card.substring(10, 12);
		String d = card.substring(12, 14);
		String date = y + "-" + m + "-" + d;
		Pattern p = Pattern
				.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
		return p.matcher(date).matches();
	}

	/**
	 * 身份证15位转18位
	 * 
	 * @param $15
	 *            15位身份证号码
	 * @return 18位身份证号码
	 */
	public static String card15$18(String $15) {
		try {
			if ($15.length() == 15) {
				int si = 0;
				StringBuffer $18 = new StringBuffer();
				$18.append($15.substring(0, 6));
				$18.append("19");
				$18.append($15.substring(6, 15));
				for (int i = 0; i < 17; i++) {
					si += ($18.charAt(i) - '0') * WI[i];
				}
				$18.append(CODE[si % 11]);
				return $18.toString();
			}
		} catch (Exception ex) {
			return null;
		}
		return $15;
	}

	/**
	 * 根据身份编号获取年龄
	 *
	 * @param idCard
	 *            身份编号
	 * @return 年龄
	 */
	public static int getAgeByIdCard(String idCard) {
		int iAge = 0;
		Calendar cal = Calendar.getInstance();
		String year = idCard.substring(6, 10);
		int iCurrYear = cal.get(Calendar.YEAR);
		iAge = iCurrYear - Integer.valueOf(year);
		return iAge;
	}

	/**
	 * 根据身份编号获取生日
	 *
	 * @param idCard 身份编号
	 * @return 生日(yyyyMMdd)
	 */
	public static String getBirthByIdCard(String idCard) {
		return idCard.substring(6, 14);
	}

	/**
	 * 根据身份编号获取生日年
	 *
	 * @param idCard 身份编号
	 * @return 生日(yyyy)
	 */
	public static Short getYearByIdCard(String idCard) {
		return Short.valueOf(idCard.substring(6, 10));
	}

	/**
	 * 根据身份编号获取生日月
	 *
	 * @param idCard
	 *            身份编号
	 * @return 生日(MM)
	 */
	public static Short getMonthByIdCard(String idCard) {
		return Short.valueOf(idCard.substring(10, 12));
	}

	/**
	 * 根据身份编号获取生日天
	 *
	 * @param idCard
	 *            身份编号
	 * @return 生日(dd)
	 */
	public static Short getDateByIdCard(String idCard) {
		return Short.valueOf(idCard.substring(12, 14));
	}

	/**
	 * 根据身份编号获取性别
	 *
	 * @param idCard 身份编号
	 * @return 性别(M-男，F-女，N-未知)
	 */
	public static int getGenderByIdCard(String idCard) {
		int sGender = 0;
		String sCardNum = idCard.substring(16, 17);
		if (Integer.parseInt(sCardNum) % 2 != 0) {
			sGender = 1;//男
		} else {
			sGender = 2;//女
		}
		return sGender;
	}

	/*public static  void  main(String [] a){
		String idcard="130322198403148609";
		String sex= getGenderByIdCard(idcard);
		System.out.println("性别:" + sex);
		int age= getAgeByIdCard(idcard);
		System.out.println("年龄:" + age);
		Short nian=getYearByIdCard(idcard);
		Short yue=getMonthByIdCard(idcard);
		Short ri=getDateByIdCard(idcard);
		System.out.print(nian+"年"+yue+"月"+ri+"日");

		String sr=getBirthByIdCard(idcard);
		System.out.println("生日:" + sr);
	}*/
}
