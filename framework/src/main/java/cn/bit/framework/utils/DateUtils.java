package cn.bit.framework.utils;

import cn.bit.framework.utils.validate.ValidateUtils;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 日期工具类
 * 
 * @author healy
 * 
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils {

	public static final String TIME_WITH_MINUTE_PATTERN = "HH:mm";

	public static final long DAY_MILLI = 24 * 60 * 60 * 1000; // 一天的MilliSecond

	public final static int LEFT_OPEN_RIGHT_OPEN = 1;
	public final static int LEFT_CLOSE_RIGHT_OPEN = 2;
	public final static int LEFT_OPEN_RIGHT_CLOSE = 3;
	public final static int LEFT_CLOSE_RIGHT_CLOSE = 4;
	/**
	 * 比较日期的模式 --只比较日期，不比较时间
	 */
	public final static int COMP_MODEL_DATE = 1;
	/**
	 * 比较日期的模式 --只比较时间，不比较日期
	 */
	public final static int COMP_MODEL_TIME = 2;
	/**
	 * 比较日期的模式 --比较日期，也比较时间
	 */
	public final static int COMP_MODEL_DATETIME = 3;

	private static Logger logger = Logger.getLogger(DateUtils.class);

	/**
	 * 要用到的DATE Format的定义
	 */
	public static String DATE_FORMAT_DATEONLY = "yyyy-MM-dd"; // 年/月/日
	public static String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss"; // 年/月/日
	public static SimpleDateFormat sdfDateTime = new SimpleDateFormat(DateUtils.DATE_FORMAT_DATETIME);
	// Global SimpleDateFormat object
	public static SimpleDateFormat sdfDateOnly = new SimpleDateFormat(DateUtils.DATE_FORMAT_DATEONLY);
	public static final SimpleDateFormat SHORTDATEFORMAT = new SimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat HMS_FORMAT = new SimpleDateFormat("HH:mm:ss");
	public static final SimpleDateFormat formatTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 根据日期格式字符串解析日期字符串
	 * 
	 * @param str
	 *            日期字符串
	 * @param parsePatterns
	 *            日期格式字符串
	 * @return 解析后日期
	 * @throws ParseException
	 */
	public static Date parseDate(String str, String parsePatterns) throws ParseException {
		return parseDate(str, new String[] { parsePatterns });
	}

	/**
	 * 根据单位字段比较两个日期
	 * 
	 * @param date
	 *            日期1
	 * @param otherDate
	 *            日期2
	 * @param withUnit
	 *            单位字段，从Calendar field取值
	 * @return 等于返回0值, 大于返回大于0的值 小于返回小于0的值
	 */
	public static int compareDate(Date date, Date otherDate, int withUnit) {
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);
		Calendar otherDateCal = Calendar.getInstance();
		otherDateCal.setTime(otherDate);

		switch (withUnit) {
		case Calendar.YEAR:
			dateCal.clear(Calendar.MONTH);
			otherDateCal.clear(Calendar.MONTH);
		case Calendar.MONTH:
			dateCal.set(Calendar.DATE, 1);
			otherDateCal.set(Calendar.DATE, 1);
		case Calendar.DATE:
			dateCal.set(Calendar.HOUR_OF_DAY, 0);
			otherDateCal.set(Calendar.HOUR_OF_DAY, 0);
		case Calendar.HOUR:
			dateCal.clear(Calendar.MINUTE);
			otherDateCal.clear(Calendar.MINUTE);
		case Calendar.MINUTE:
			dateCal.clear(Calendar.SECOND);
			otherDateCal.clear(Calendar.SECOND);
		case Calendar.SECOND:
			dateCal.clear(Calendar.MILLISECOND);
			otherDateCal.clear(Calendar.MILLISECOND);
		case Calendar.MILLISECOND:
			break;
		default:
			throw new IllegalArgumentException("withUnit 单位字段 " + withUnit + " 不合法！！");
		}
		return dateCal.compareTo(otherDateCal);
	}

	/**
	 * 根据单位字段比较两个时间
	 * 
	 * @param date
	 *            时间1
	 * @param otherDate
	 *            时间2
	 * @param withUnit
	 *            单位字段，从Calendar field取值
	 * @return 等于返回0值, 大于返回大于0的值 小于返回小于0的值
	 */
	public static int compareTime(Date date, Date otherDate, int withUnit) {
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);
		Calendar otherDateCal = Calendar.getInstance();
		otherDateCal.setTime(otherDate);

		dateCal.clear(Calendar.YEAR);
		dateCal.clear(Calendar.MONTH);
		dateCal.set(Calendar.DATE, 1);
		otherDateCal.clear(Calendar.YEAR);
		otherDateCal.clear(Calendar.MONTH);
		otherDateCal.set(Calendar.DATE, 1);
		switch (withUnit) {
		case Calendar.HOUR:
			dateCal.clear(Calendar.MINUTE);
			otherDateCal.clear(Calendar.MINUTE);
		case Calendar.MINUTE:
			dateCal.clear(Calendar.SECOND);
			otherDateCal.clear(Calendar.SECOND);
		case Calendar.SECOND:
			dateCal.clear(Calendar.MILLISECOND);
			otherDateCal.clear(Calendar.MILLISECOND);
		case Calendar.MILLISECOND:
			break;
		default:
			throw new IllegalArgumentException("withUnit 单位字段 " + withUnit + " 不合法！！");
		}
		return dateCal.compareTo(otherDateCal);
	}

	/**
	 * 获得当前的日期毫秒
	 * 
	 * @return
	 */
	public static long nowTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 获得当前的时间戳
	 * 
	 * @return
	 */
	public static Timestamp nowTimeStamp() {
		return new Timestamp(nowTimeMillis());
	}

	/**
	 * yyyy-MM-dd 当前日期
	 * 
	 */
	public static String getReqDate() {
		return SHORTDATEFORMAT.format(new Date());
	}

	/**
	 * yyyy-MM-dd 传入日期
	 * 
	 * @param date
	 * @return
	 */
	public static String getReqDate(Date date) {
		return SHORT_DATE_FORMAT.format(date);
	}

	/**
	 * yyyyMMdd 传入日期
	 * 
	 * @param date
	 * @return
	 */
	public static String getReqDateyyyyMMdd(Date date) {
		return SHORTDATEFORMAT.format(date);
	}

	/**
	 * yyyy-MM-dd 传入的时间戳
	 * 
	 * @param tmp
	 * @return
	 */
	public static String TimestampToDateStr(Timestamp tmp) {
		return SHORT_DATE_FORMAT.format(tmp);
	}

	/**
	 * HH:mm:ss 当前时间
	 * 
	 * @return
	 */
	public static String getReqTime() {
		return HMS_FORMAT.format(new Date());
	}

	/**
	 * 得到时间戳格式字串
	 * 
	 * @param date
	 * @return
	 */
	public static String getTimeStampStr(Date date) {
		return LONG_DATE_FORMAT.format(date);
	}

	/**
	 * 得到长日期格式字串
	 * 
	 * @return
	 */
	public static String getLongDateStr() {
		return LONG_DATE_FORMAT.format(new Date());
	}

	public static String getLongDateStr(Timestamp time) {
		return LONG_DATE_FORMAT.format(time);
	}

	/**
	 * 得到短日期格式字串
	 * 
	 * @param date
	 * @return
	 */
	public static String getShortDateStr(Date date) {
		if (date == null) {
			return "";
		}
		return SHORT_DATE_FORMAT.format(date);
	}

	public static String getShortDateStr() {
		return SHORT_DATE_FORMAT.format(new Date());
	}

	/**
	 * 计算 millisecond 毫秒后的时间
	 * 
	 * @param date
	 * @param millisecond
	 * @return
	 */
	public static Date addMillisecond(Date date, int millisecond) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MILLISECOND, millisecond);
		return calendar.getTime();
	}

	/**
	 * 计算 second 秒后的时间
	 *
	 * @param date
	 * @param second
	 * @return
	 */
	public static Date addSecond(Date date, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, second);
		return calendar.getTime();
	}

	/**
	 * 计算 minute 分钟后的时间
	 * 
	 * @param date
	 * @param minute
	 * @return
	 */
	public static Date addMinute(Date date, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minute);
		return calendar.getTime();
	}

	/**
	 * 计算 hour 小时后的时间
	 * 
	 * @param date
	 * @param hour
	 * @return
	 */
	public static Date addHour(Date date, int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR, hour);
		return calendar.getTime();
	}

	/**
	 * 得到day的起始时间点。
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDayStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 得到day的终止时间点.
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDayEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MILLISECOND, -1);
		return calendar.getTime();
	}

	/**
	 * 计算 day 天后的时间
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date addDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		return calendar.getTime();
	}

	/**
	 * 得到month的终止时间点.
	 * 
	 * @param date
	 * @return
	 */
	public static Date getMonthEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.MILLISECOND, -1);
		return calendar.getTime();
	}

	public static Date addMonth(Date date, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, month);
		return calendar.getTime();
	}

	public static Date addYear(Date date, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, year);
		return calendar.getTime();
	}

	public static Timestamp strToTimestamp(String dateStr) {
		return Timestamp.valueOf(dateStr);
	}

	public static Timestamp strToTimestamp(Date date) {
		return Timestamp.valueOf(formatTimestamp.format(date));
	}

	public static Timestamp getCurTimestamp() {
		return Timestamp.valueOf(formatTimestamp.format(new Date()));
	}

	/**
	 * 取得两个日期之间的日数
	 * 
	 * @return t1到t2间的日数，如果t2 在 t1之后，返回正数，否则返回负数
	 */
	public static long daysBetween(Timestamp t1, Timestamp t2) {
		return (t2.getTime() - t1.getTime()) / DAY_MILLI;
	}

	/**
	 * 取得两个日期之间的秒数
	 * @return t1到t2间的秒数，如果d2 在 d1之后，返回正数，否则返回负数
	 */
	public static long secondsBetween(Date d1, Date d2) {
		return (d2.getTime() - d1.getTime()) / 1000;
	}

	/**
	 * 返回java.sql.Timestamp型的SYSDATE
	 * 
	 * @return java.sql.Timestamp型的SYSDATE
	 * @since 1.0
	 * @history
	 */
	public static Timestamp getSysDateTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * 利用缺省的Date格式(YYYY/MM/DD)转换String到java.sql.Timestamp
	 * 
	 * @param sDate
	 *            Date string
	 * @return
	 * @since 1.0
	 * @history
	 */
	public static Timestamp toSqlTimestamp(String sDate) {
		if (sDate == null) {
			return null;
		}
		if (sDate.length() != DateUtils.DATE_FORMAT_DATEONLY.length()
				&&sDate.length() != DateUtils.DATE_FORMAT_DATETIME.length()) {
			return null;
		}
		return toSqlTimestamp(sDate, 
				sDate.length() == DateUtils.DATE_FORMAT_DATEONLY.length()
				?DateUtils.DATE_FORMAT_DATEONLY
				:DateUtils.DATE_FORMAT_DATETIME);

	}

	/**
	 * 利用缺省的Date格式(YYYY/MM/DD hh:mm:ss)转化String到java.sql.Timestamp
	 * 
	 * @param sDate
	 *            Date string
	 * @param sFmt
	 *            Date format DATE_FORMAT_DATEONLY/DATE_FORMAT_DATETIME
	 * @return
	 * @since 1.0
	 * @history
	 */
	public static Timestamp toSqlTimestamp(String sDate, String sFmt) {
		String temp = null;
		if (sDate == null || sFmt == null) {
			return null;
		}
		if (sDate.length() != sFmt.length()) {
			return null;
		}
		if (sFmt.equals(DateUtils.DATE_FORMAT_DATETIME)) {
			temp = sDate.replace('/', '-');
			temp = temp + ".000000000";
		} else if (sFmt.equals(DateUtils.DATE_FORMAT_DATEONLY)) {
			temp = sDate.replace('/', '-');
			temp = temp + " 00:00:00.000000000";
			// }else if( sFmt.equals (DateUtils.DATE_FORMAT_SESSION )){
			// //Format: 200009301230
			// temp =
			// sDate.substring(0,4)+"-"+sDate.substring(4,6)+"-"+sDate.substring(6,8);
			// temp += " " + sDate.substring(8,10) + ":" +
			// sDate.substring(10,12) + ":00.000000000";
		} else {
			return null;
		}
		// java.sql.Timestamp.value() 要求的格式必须为yyyy-mm-dd hh:mm:ss.fffffffff
		return Timestamp.valueOf(temp);
	}

	/**
	 * 以YYYY/MM/DD HH24:MI:SS格式返回系统日期时间
	 * 
	 * @return 系统日期时间
	 * @since 1.0
	 * @history
	 */
	public static String getSysDateTimeString() {
		return toString(new Date(System.currentTimeMillis()), DateUtils.sdfDateTime);
	}

	/**
	 * 根据指定的Format转化java.util.Date到String
	 * 
	 * @param dt
	 *            java.util.Date instance
	 * @param sFmt
	 *            Date format , DATE_FORMAT_DATEONLY or DATE_FORMAT_DATETIME
	 * @return
	 * @since 1.0
	 * @history
	 */
	public static String toString(Date dt, String sFmt) {
		if (dt == null || sFmt == null || "".equals(sFmt)) {
			return "";
		}
		return toString(dt, new SimpleDateFormat(sFmt));
	}

	/**
	 * 利用指定SimpleDateFormat instance转换java.util.Date到String
	 * 
	 * @param dt
	 *            java.util.Date instance
	 * @param formatter
	 *            SimpleDateFormat Instance
	 * @return
	 * @since 1.0
	 * @history
	 */
	private static String toString(Date dt, SimpleDateFormat formatter) {
		String sRet = null;

		try {
			sRet = formatter.format(dt).toString();
		} catch (Exception e) {
			logger.error(e);
			sRet = null;
		}

		return sRet;
	}

	/**
	 * 转换java.sql.Timestamp到String，格式为YYYY/MM/DD HH24:MI
	 * 
	 * @param dt
	 *            java.sql.Timestamp instance
	 * @return
	 * @since 1.0
	 * @history
	 */
	public static String toSqlTimestampString2(Timestamp dt) {
		if (dt == null) {
			return null;
		}
		String temp = toSqlTimestampString(dt, DateUtils.DATE_FORMAT_DATETIME);
		return temp.substring(0, 16);
	}

	public static String toString(Timestamp dt) {
		return dt == null ? "" : toSqlTimestampString2(dt);
	}

	/**
	 * 根据指定的格式转换java.sql.Timestamp到String
	 * 
	 * @param dt
	 *            java.sql.Timestamp instance
	 * @param sFmt
	 *            Date 格式，DATE_FORMAT_DATEONLY/DATE_FORMAT_DATETIME/
	 *            DATE_FORMAT_SESSION
	 * @return
	 * @since 1.0
	 * @history
	 */
	public static String toSqlTimestampString(Timestamp dt, String sFmt) {
		String temp = null;
		String out = null;
		if (dt == null || sFmt == null) {
			return null;
		}
		temp = dt.toString();
		if (sFmt.equals(DateUtils.DATE_FORMAT_DATETIME) || // "YYYY/MM/DD
				// HH24:MI:SS"
				sFmt.equals(DateUtils.DATE_FORMAT_DATEONLY)) { // YYYY/MM/DD
			temp = temp.substring(0, sFmt.length());
			out = temp.replace('/', '-');
			// }else if( sFmt.equals (DateUtils.DATE_FORMAT_SESSION ) ){
			// //Session
			// out =
			// temp.substring(0,4)+temp.substring(5,7)+temp.substring(8,10);
			// out += temp.substring(12,14) + temp.substring(15,17);
		}
		return out;
	}

	// 得到当前日期的星期
	public static int getWeek() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int w = cal.get(Calendar.DAY_OF_WEEK);
		return w;
	}

	/**
	 * Timestamp 格式转换成yyyy-MM-dd timestampToSql(Timestamp 格式转换成yyyy-MM-dd)
	 * 
	 * @param timestamp
	 *            时间
	 * @return createTimeStr yyyy-MM-dd 时间
	 * @Exception 异常对象
	 * @since V1.0
	 */
	public static String timestampToStringYMD(Timestamp timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.DATE_FORMAT_DATEONLY);
		String createTimeStr = sdf.format(timestamp);
		return createTimeStr;
	}

	/**
	 * 判断一个时间是否在某个时间区间内
	 * 
	 * @param now
	 *            目标时间
	 * @param start
	 *            时间区间开始
	 * @param end
	 *            时间区间结束
	 * @param model
	 *            区间模式
	 * @return 是否在区间内
	 */
	public static boolean isBetween(Date now, Date start, Date end, int model) {
		return isBetween(now, start, end, model, LEFT_OPEN_RIGHT_OPEN);
	}

	/**
	 * 判断时间是否在制定的时间段之类
	 * 
	 * @param date
	 *            需要判断的时间
	 * @param start
	 *            时间段的起始时间
	 * @param end
	 *            时间段的截止时间
	 * @param interModel
	 *            区间的模式
	 * 
	 *            <pre>
	 * 		取值：
	 * 			LEFT_OPEN_RIGHT_OPEN
	 * 			LEFT_CLOSE_RIGHT_OPEN
	 * 			LEFT_OPEN_RIGHT_CLOSE
	 * 			LEFT_CLOSE_RIGHT_CLOSE
	 * </pre>
	 * @param compModel
	 *            比较的模式
	 * 
	 *            <pre>
	 * 		取值：
	 * 			COMP_MODEL_DATE		只比较日期，不比较时间
	 * 			COMP_MODEL_TIME		只比较时间，不比较日期
	 * 			COMP_MODEL_DATETIME 比较日期，也比较时间
	 * </pre>
	 * @return
	 */
	public static boolean isBetween(Date date, Date start, Date end, int interModel, int compModel) {
		if (date == null || start == null || end == null) {
			throw new IllegalArgumentException("日期不能为空");
		}
		SimpleDateFormat format = null;
		switch (compModel) {
		case COMP_MODEL_DATE: {
			format = new SimpleDateFormat("yyyyMMdd");
			break;
		}
		case COMP_MODEL_TIME: {
			format = new SimpleDateFormat("HHmmss");
			break;
		}
		case COMP_MODEL_DATETIME: {
			format = new SimpleDateFormat("yyyyMMddHHmmss");
			break;
		}
		default: {
			throw new IllegalArgumentException(String.format("日期的比较模式[%d]有误", compModel));
		}
		}
		long dateNumber = Long.parseLong(format.format(date));
		long startNumber = Long.parseLong(format.format(start));
		long endNumber = Long.parseLong(format.format(end));
		switch (interModel) {
		case LEFT_OPEN_RIGHT_OPEN: {
			if (dateNumber <= startNumber || dateNumber >= endNumber) {
				return false;
			} else {
				return true;
			}
		}
		case LEFT_CLOSE_RIGHT_OPEN: {
			if (dateNumber < startNumber || dateNumber >= endNumber) {
				return false;
			} else {
				return true;
			}
		}
		case LEFT_OPEN_RIGHT_CLOSE: {
			if (dateNumber <= startNumber || dateNumber > endNumber) {
				return false;
			} else {
				return true;
			}
		}
		case LEFT_CLOSE_RIGHT_CLOSE: {
			if (dateNumber < startNumber || dateNumber > endNumber) {
				return false;
			} else {
				return true;
			}
		}
		default: {
			throw new IllegalArgumentException(String.format("日期的区间模式[%d]有误", interModel));
		}
		}
	}

	/**
	 * 得到当前周起始时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getWeekStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.get(Calendar.WEEK_OF_YEAR);
		int firstDay = calendar.getFirstDayOfWeek();
		calendar.set(Calendar.DAY_OF_WEEK, firstDay);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 得到当前周截止时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getWeekEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.get(Calendar.WEEK_OF_YEAR);
		int firstDay = calendar.getFirstDayOfWeek();
		calendar.set(Calendar.DAY_OF_WEEK, 8 - firstDay);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 得到当月起始时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getMonthStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 得到当前年起始时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getYearStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 得到当前年最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getYearEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * 得到某年起始时间
	 *
	 * @param year
	 * @return
	 */
	public static Date getOneYearStart(Integer year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 得到某年最后一天
	 *
	 * @param year
	 * @return
	 */
	public static Date getOneYearEnd(Integer year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * 取得月天数
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 取得月第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getFirstDateOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		return getStartTime(c.getTime());
	}

	/**
	 * 取得月最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getLastDateOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		return getEndTime(c.getTime());
	}

	/**
	 * 取得季度第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getSeasonStart(Date date) {
		return getDayStart(getFirstDateOfMonth(getSeasonDate(date)[0]));
	}

	/**
	 * 取得季度最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getSeasonEnd(Date date) {
		return getDayEnd(getLastDateOfMonth(getSeasonDate(date)[2]));
	}

	/**
	 * 取得季度月
	 * 
	 * @param date
	 * @return
	 */
	public static Date[] getSeasonDate(Date date) {
		Date[] season = new Date[3];

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		int nSeason = getSeason(date);
		if (nSeason == 1) {// 第一季度
			c.set(Calendar.MONTH, Calendar.JANUARY);
			season[0] = c.getTime();
			c.set(Calendar.MONTH, Calendar.FEBRUARY);
			season[1] = c.getTime();
			c.set(Calendar.MONTH, Calendar.MARCH);
			season[2] = c.getTime();
		} else if (nSeason == 2) {// 第二季度
			c.set(Calendar.MONTH, Calendar.APRIL);
			season[0] = c.getTime();
			c.set(Calendar.MONTH, Calendar.MAY);
			season[1] = c.getTime();
			c.set(Calendar.MONTH, Calendar.JUNE);
			season[2] = c.getTime();
		} else if (nSeason == 3) {// 第三季度
			c.set(Calendar.MONTH, Calendar.JULY);
			season[0] = c.getTime();
			c.set(Calendar.MONTH, Calendar.AUGUST);
			season[1] = c.getTime();
			c.set(Calendar.MONTH, Calendar.SEPTEMBER);
			season[2] = c.getTime();
		} else if (nSeason == 4) {// 第四季度
			c.set(Calendar.MONTH, Calendar.OCTOBER);
			season[0] = c.getTime();
			c.set(Calendar.MONTH, Calendar.NOVEMBER);
			season[1] = c.getTime();
			c.set(Calendar.MONTH, Calendar.DECEMBER);
			season[2] = c.getTime();
		}
		return season;
	}

	/**
	 * 
	 * 1 第一季度 2 第二季度 3 第三季度 4 第四季度
	 * 
	 * @param date
	 * @return
	 */
	public static int getSeason(Date date) {

		int season = 0;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int month = c.get(Calendar.MONTH);
		switch (month) {
		case Calendar.JANUARY:
		case Calendar.FEBRUARY:
		case Calendar.MARCH:
			season = 1;
			break;
		case Calendar.APRIL:
		case Calendar.MAY:
		case Calendar.JUNE:
			season = 2;
			break;
		case Calendar.JULY:
		case Calendar.AUGUST:
		case Calendar.SEPTEMBER:
			season = 3;
			break;
		case Calendar.OCTOBER:
		case Calendar.NOVEMBER:
		case Calendar.DECEMBER:
			season = 4;
			break;
		default:
			break;
		}
		return season;
	}

	/**
	 * 字符串转date
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date StringToDate(String dateString) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date date = null;
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			logger.error(e);
		}
		return date;
	}

	/**
	 * 判断输入日期是一个星期中的第几天(星期天为一个星期第一天)
	 * 
	 * @param date
	 * @return
	 */
	public static int getWeekIndex(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 当前时间的前几天，并且以例如2013/12/09 00:00:00 形式输出
	 */
	public static Date subDays(int days) {
		Date date = addDay(new Date(), -days);
		String dateStr = getReqDate(date);
		Date date1 = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date1 = sdf.parse(dateStr);
		} catch (ParseException e) {
			logger.error(e);
		}
		return date1;
	}

	/**
	 * 判断开始时间和结束时间，是否超出了当前时间的一定的间隔数限制 如：开始时间和结束时间，不能超出距离当前时间90天
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间按
	 * @param interval
	 *            间隔数
	 * @param dateUnit
	 *            单位(如：月，日),参照Calendar的时间单位
	 * @return
	 */
	public static boolean isOverIntervalLimit(Date startDate, Date endDate, int interval, int dateUnit) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(dateUnit, interval * (-1));
		Date curDate = getDayStart(cal.getTime());
		if (getDayStart(startDate).compareTo(curDate) < 0 || getDayStart(endDate).compareTo(curDate) < 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断开始时间和结束时间，是否超出了当前时间的一定的间隔数限制, 时间单位默认为天数 如：开始时间和结束时间，不能超出距离当前时间90天
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间按
	 * @param interval
	 *            间隔数
	 * @return
	 */
	public static boolean isOverIntervalLimit(Date startDate, Date endDate, int interval) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, interval * (-1));
		Date curDate = getDayStart(cal.getTime());
		if (getDayStart(startDate).compareTo(curDate) < 0 || getDayStart(endDate).compareTo(curDate) < 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断开始时间和结束时间，是否超出了当前时间的一定的间隔数限制, 时间单位默认为天数 如：开始时间和结束时间，不能超出距离当前时间90天
	 * 
	 * @param startDateStr
	 *            开始时间
	 * @param endDateStr
	 *            结束时间按
	 * @param interval
	 *            间隔数
	 * @return
	 */
	public static boolean isOverIntervalLimit(String startDateStr, String endDateStr, int interval) {
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = DateUtils.parseDate(startDateStr, DateUtils.DATE_FORMAT_DATEONLY);
			endDate = DateUtils.parseDate(endDateStr, DateUtils.DATE_FORMAT_DATEONLY);
		} catch (ParseException e) {
			logger.error(e);
			return false;
		}
		return isOverIntervalLimit(startDate, endDate, interval);
	}

	/**
	 * 传入时间字符串及时间格式，返回对应的Date对象
	 * 
	 * @param src
	 *            时间字符串
	 * @param pattern
	 *            时间格式
	 * @return Date
	 */
	public static Date getDateFromString(String src, String pattern) {
		SimpleDateFormat f = new SimpleDateFormat(pattern);
		try {
			return f.parse(src);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 取季度
	 * 
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int getQuarter(Date date) {
		if (date.getMonth() == 0 || date.getMonth() == 1 || date.getMonth() == 2) {
			return 1;
		} else if (date.getMonth() == 3 || date.getMonth() == 4 || date.getMonth() == 5) {
			return 2;
		} else if (date.getMonth() == 6 || date.getMonth() == 7 || date.getMonth() == 8) {
			return 3;
		} else if (date.getMonth() == 9 || date.getMonth() == 10 || date.getMonth() == 11) {
			return 4;
		} else {
			return 0;

		}
	}

	/**
	 * 取得通用日期时间格式字符串
	 * 
	 * @param date
	 * @return String
	 */
	public static String formatDate(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/**
	 * 获取当日的日期格式串
	 * 
	 * @param
	 * @return String
	 */
	public static String today() {
		return formatDate(new Date(), "yyyy-MM-dd");
	}

	/**
	 * 获取当前时间格式串
	 * 
	 * @param
	 * @return String
	 */
	public static String currentTime() {
		return formatDate(new Date(), "yyyyMMddhhmmssSSS");
	}

	/**
	 * 取得指定日期格式的字符串
	 * 
	 * @param date
	 * @param format
	 * @return String
	 */
	public static String formatDate(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	/**
	 * 获取昨日的日期格式串
	 * 
	 * @return Date
	 */
	public static String getYesterday() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		return formatDate(calendar.getTime(), "yyyy-MM-dd");
	}

	/**
	 * 判断当前时间是否在一定的时间范围内
	 * 
	 * @param startTime
	 * @param endTime
	 * @return boolean
	 */
	public static boolean isInBetweenTimes(String startTime, String endTime) {
		Date nowTime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String time = sdf.format(nowTime);
		if (time.compareTo(startTime) >= 0 && time.compareTo(endTime) <= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 字符转日期
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date getDateByStr(String dateStr) {
		SimpleDateFormat formatter = null;
		if (dateStr == null) {
			return null;
		} else if (dateStr.length() == 10) {
			formatter = new SimpleDateFormat("yyyy-MM-dd");
		} else if (dateStr.length() == 16) {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		} else if (dateStr.length() == 19) {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		} else if (dateStr.length() > 19) {
			dateStr = dateStr.substring(0, 19);
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		} else {
			return null;
		}
		try {
			return formatter.parse(dateStr);
		} catch (ParseException e) {
			logger.error(e);
			return null;
		}
	}

	/**
	 * 根据传入的数字，输出相比现在days天的数据
	 * 
	 * @param days
	 * @return Date
	 */
	public static Date getDate(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}

	/**
	 * 日期最大时间
	 * 
	 * @param dt
	 * @return
	 */
	public static Date getMaxTime(Date dt) {

		Date dt1 = null;
		Calendar ca = Calendar.getInstance();
		ca.setTime(dt);
		ca.add(Calendar.DAY_OF_MONTH, 1);
		dt1 = ca.getTime();
		dt1 = DateUtils.getMinTime(dt1);
		ca.setTime(dt1);
		ca.add(Calendar.SECOND, -1);
		dt1 = ca.getTime();
		return dt1;
	}

	/**
	 * 日期最小时间
	 * 
	 * @param dt
	 * @return
	 */
	public static Date getMinTime(Date dt) {
		Date dt1 = null;
		dt1 = DateUtils.getDateByStr(DateUtils.formatDate(dt, "yyyy-MM-dd"));
		return dt1;
	}

	/**
	 * 月的最后一天
	 * 
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Date getLastDayOfMonth(Date date) {
		Calendar cDay1 = Calendar.getInstance();
		cDay1.setTime(date);
		int lastDay = cDay1.getActualMaximum(Calendar.DAY_OF_MONTH);
		Date lastDate = cDay1.getTime();
		lastDate.setDate(lastDay);
		return lastDate;
	}

	/**
	 * 月的第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
		return calendar.getTime();
	}

	/**
	 * 上月第一天
	 * 
	 * @return
	 */
	public static Date getPreviousMonthFirstDay() {
		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
		return getMinTime(lastDate.getTime());
	}

	/**
	 * 上月最后一天
	 * 
	 * @return
	 */
	public static Date getPreviousMonthLastDay() {
		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.DATE, -1);
		return getMinTime(lastDate.getTime());
	}

	/**
	 * 两个日期相关天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long getDateDiff(String startDate, String endDate) {
		long diff = 0;
		try {
			Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);

			diff = date1.getTime() > date2.getTime() ? (date1.getTime() - date2.getTime())
					/ (24 * 60 * 60 * 1000)
					: (date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
		} catch (ParseException e) {
		}
		return diff;
	}

	public static long getDateDiff(Date date1, Date date2) {
		if (ValidateUtils.isEmpty(date1) || ValidateUtils.isEmpty(date1)) {
			return 0L;
		}
		long diff = date1.getTime() > date2.getTime() ? (date1.getTime() - date2
				.getTime()) / (24 * 60 * 60 * 1000) : (date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
		return diff;
	}

	/*
	 * 判断两个时间是不是在一个周中
	 */
	public static boolean isSameWeekWithToday(Date date) {

		if (date == null) {
			return false;
		}

		// 0.先把Date类型的对象转换Calendar类型的对象
		Calendar todayCal = Calendar.getInstance();
		Calendar dateCal = Calendar.getInstance();

		todayCal.setTime(new Date());
		dateCal.setTime(date);
		int subYear = todayCal.get(Calendar.YEAR) - dateCal.get(Calendar.YEAR);
		// subYear==0,说明是同一年
		if (subYear == 0) {
			if (todayCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR))
				return true;
		} else if (subYear == 1 && dateCal.get(Calendar.MONTH) == 11 && todayCal.get(Calendar.MONTH) == 0) {
			if (todayCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR))
				return true;
		} else if (subYear == -1 && todayCal.get(Calendar.MONTH) == 11 && dateCal.get(Calendar.MONTH) == 0) {
			if (todayCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR))
				return true;
		}
		return false;
	}

	/**
	 * getStrFormTime: <br/>
	 * 
	 * @param form
	 *            格式时间
	 * @param date
	 *            时间
	 * @return
	 */
	public static String getStrFormTime(String form, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(form);
		return sdf.format(date);
	}

	/**
	 * 获取几天内日期 return 2014-5-4、2014-5-3
	 */
	public static List<String> getLastDays(int countDay) {
		List<String> listDate = new ArrayList<String>();
		for (int i = 0; i < countDay; i++) {
			listDate.add(DateUtils.getReqDateyyyyMMdd(DateUtils.getDate(-i)));
		}
		return listDate;
	}

	/**
	 * 对时间进行格式化
	 * 
	 * @param date
	 * @return
	 */
	public static Date dateFormat(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date value = new Date();

		try {
			value = sdf.parse(sdf.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return value;

	}

	/**
	 * 获取时间各个单位的值
	 * @param date
	 * @param field
	 * @return
	 */
	public static int getValueByField(Date date, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.get(field);
		return calendar.get(field);
	}

	public static Date addByString(Date original, String src, String pattern ) {
		Date date = getDateFromString(src, pattern);
		int hour = getValueByField(date, Calendar.HOUR_OF_DAY);
		int minute = getValueByField(date, Calendar.MINUTE);
		int second = getValueByField(date, Calendar.SECOND);
		original = addHour(original, hour);
		original = addMinute(original, minute);
		original = addSecond(original, second);
		return original;
	}

	/**
	 * 获取该时间的初始时间
	 * @return
	 */
	public static Date getStartTime(Date time) {
		if (time == null) {
			return null;
		}
		Calendar todayStart = Calendar.getInstance();
		todayStart.setTime(time);
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime();
	}

	/**
	 * 获取该日期的最后时间
	 * @return
	 */
	public static Date getEndTime(Date time) {
		if (time == null) {
			return null;
		}
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.setTime(time);
		todayEnd.set(Calendar.HOUR_OF_DAY, 23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		todayEnd.set(Calendar.MILLISECOND, 999);
		return todayEnd.getTime();
	}

	/**
	 * 获取当天剩余的秒数
	 * 明天0点减去当前时间
	 * @return
	 */
	public static long getTomorrowZeroSeconds() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long tomorrowZeroSeconds = (calendar.getTimeInMillis()- System.currentTimeMillis()) / 1000;
		return tomorrowZeroSeconds;
	}

	/**
	 * 比较两个时间相差的分钟数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDateDiffForMinutes(Date date1, Date date2) {
		if (ValidateUtils.isEmpty(date1) || ValidateUtils.isEmpty(date1)) {
			return 0L;
		}
		return (date1.getTime() - date2.getTime()) / (60 * 1000);
	}

	public static void main(String[] args) throws ParseException {
		Date date3 = DateUtils.addMinutes(new Date(), 5);
		System.out.println(new Date().after(DateUtils.addMinute(new Date(), -10)));
		Date date1 = DateUtils.addMinutes(new Date(), -1);
		Date date2 = DateUtils.addMinutes(date1, 5);
		System.out.println(date1);
		System.out.println(date2);
		System.out.println(DateUtils.isBetween(new Date(), date1, date2, DateUtils.COMP_MODEL_TIME));
		System.out.println("seconds:" + getTomorrowZeroSeconds());

		Integer a = 1;
		int b = 1;
		System.out.println(getDateDiffForMinutes(getDateByStr("2018-04-21 17:14"),new Date()));

		System.out.println(getNextDateByMonthsWithDay(getDateByStr("2018-04-21 17:14"),2, 1));
		System.out.println(formatDate(new Date(), "yyyy年M月"));
	}

	/**
	 * 根据传入的数字转变为对应的中文月份
	 * @param month
	 * @return
	 */
	public static String getStringByMonth(int month) {
		int num = Math.abs(month % 12);
		String result = "";
		switch (num){
			case 1:
				result = "一";
				break;
			case 2:
				result = "二";
				break;
			case 3:
				result = "三";
				break;
			case 4:
				result = "四";
				break;
			case 5:
				result = "五";
				break;
			case 6:
				result = "六";
				break;
			case 7:
				result = "七";
				break;
			case 8:
				result = "八";
				break;
			case 9:
				result = "九";
				break;
			case 10:
				result = "十";
				break;
			case 11:
				result = "十一";
				break;
			case 0:
				result = "十二";
				break;
		}
		return result;
	}

	/**
	 * 获取指定日期（未来或者过去的某个日期）
	 * @param months
	 * @param day
	 * @return
	 */
    public static String getNextDateByMonthsWithDay(Date date, int months, int day) {
		Calendar today = Calendar.getInstance();
		if(date != null){
			today.setTime(date);
		}
		today.set(Calendar.DATE, day);// 设为当前月的 day 号
		today.add(Calendar.MONTH, months);//未来/过去的months月
		return DateUtils.formatDate(today.getTime(), DATE_FORMAT_DATEONLY);
    }
}
