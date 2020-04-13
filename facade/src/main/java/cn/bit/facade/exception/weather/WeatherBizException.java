package cn.bit.facade.exception.weather;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by terry on 2018/1/14.
 */
@Slf4j
public class WeatherBizException extends BizException {

    public static final WeatherBizException CITY_NAME_NOT_NULL = new WeatherBizException(9010001, "查询城市名不能为空");

    public WeatherBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public WeatherBizException(int code, String msg) {
        super(code, msg);
    }

    public WeatherBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public WeatherBizException newInstance(String msgFormat, Object... args) {
        return new WeatherBizException(this.code, msgFormat, args);
    }

    public WeatherBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new WeatherBizException(this.code, this.msg);
    }
}
