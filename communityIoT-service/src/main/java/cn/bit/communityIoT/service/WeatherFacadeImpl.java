package cn.bit.communityIoT.service;


import cn.bit.facade.service.communityIoT.WeatherFacade;
import cn.bit.facade.vo.weather.WeatherRequest;
import cn.bit.framework.utils.httpclient.HttpUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static cn.bit.facade.exception.weather.WeatherBizException.CITY_NAME_NOT_NULL;

/**
 * 天气
 */
public class WeatherFacadeImpl implements WeatherFacade {

    /**
     * 请求路径
     */
    @Getter
    @Setter
    private String url;

    public static final String WEATHER_URL = "/weather/city/view";

    public static final String FUTURE_WEATHER_URL = "/weather/city/view/future";

    @Override
    public Object getWeatherByCity(WeatherRequest request) throws Exception {
        if (request==null || StringUtils.isBlank(request.getCity())) {
            throw CITY_NAME_NOT_NULL;
        }

        // TODO 头部预留，后期用于接入权限各和其它的
        Map<String, String> headers = new HashMap<>();
        String result = HttpUtils.doPost(url + WEATHER_URL, headers, JSONObject.toJSONString(request));
        return JSONObject.parse(result);
    }

    @Override
    public Object getFutureWeatherByCity(WeatherRequest request) throws Exception {
        if (request==null || StringUtils.isBlank(request.getCity())) {
            throw CITY_NAME_NOT_NULL;
        }

        // TODO 头部预留，后期用于接入权限各和其它的
        Map<String, String> headers = new HashMap<>();
        String result = HttpUtils.doPost(url + FUTURE_WEATHER_URL, headers, JSONObject.toJSONString(request));
        return JSONObject.parse(result);
    }
}
