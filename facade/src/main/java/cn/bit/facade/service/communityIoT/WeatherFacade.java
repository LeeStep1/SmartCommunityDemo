package cn.bit.facade.service.communityIoT;

import cn.bit.facade.vo.weather.WeatherRequest;

public interface WeatherFacade {

    Object getWeatherByCity(WeatherRequest request) throws Exception;

    Object getFutureWeatherByCity(WeatherRequest request) throws Exception;
}
