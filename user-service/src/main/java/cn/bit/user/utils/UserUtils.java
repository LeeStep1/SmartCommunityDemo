package cn.bit.user.utils;

import cn.bit.common.facade.exception.UnknownException;
import cn.bit.common.facade.user.enums.CodeEnum;
import cn.bit.facade.enums.TimeUnitEnum;
import cn.bit.framework.utils.DateUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 用户服务工具类
 *
 * @author decai.liu
 * @date 2018-09-06 17:34
 */
public class UserUtils {

	public static <S, T> T convert(S source, Class<T> clazz) {
		if (source == null) {
			return null;
		}
		try {
			T target = clazz.newInstance();
			BeanUtils.copyProperties(source, target);
			return target;
		} catch (Exception e) {
			throw new UnknownException(CodeEnum.UNKNOWN_ERROR, e);
		}
	}

	/**
	 * 根据有效时长及度量单位计算过期时间
	 * @param processTime
	 * @param timeUnit {@link TimeUnitEnum}
	 * @return
	 */
	public static Date getExpireAt(int processTime, int timeUnit){

		if(TimeUnitEnum.MILLISECOND.value().equals(timeUnit)){
			return DateUtils.addMillisecond(new Date(), processTime);
		}
		if(TimeUnitEnum.SECOND.value().equals(timeUnit)){
			return DateUtils.addSecond(new Date(), processTime);
		}
		if(TimeUnitEnum.MINUTE.value().equals(timeUnit)){
			return DateUtils.addMinute(new Date(), processTime);
		}
		if(TimeUnitEnum.HOUR.value().equals(timeUnit)){
			return DateUtils.addHour(new Date(), processTime);
		}
		if(TimeUnitEnum.DAY.value().equals(timeUnit)){
			return DateUtils.addDay(new Date(), processTime);
		}
		if(TimeUnitEnum.MONTH.value().equals(timeUnit)){
			return DateUtils.addMonth(new Date(), processTime);
		}
		if(TimeUnitEnum.YEAR.value().equals(timeUnit)){
			return DateUtils.addYear(new Date(), processTime);
		}
		return null;
	}
}

