package cn.bit.api.controller.v1;


import cn.bit.api.support.ApiResult;
import cn.bit.facade.service.communityIoT.WeatherFacade;
import cn.bit.facade.vo.weather.WeatherRequest;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.string.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 天气相关的接口
 */
@RestController
@RequestMapping(value = "/v1/communityIoT/weather", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class WeatherController {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(WeatherController.class);
    @Autowired
    private WeatherFacade weatherFacade;

    /**
     * 通过城市名查询天气
     *
     * @return
     */
    @PostMapping(name = "通过城市名查询天气", path = "/city/view")
    public Object getWeatherByCity(@RequestBody @Validated WeatherRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResult.error(1, bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            Object result = weatherFacade.getWeatherByCity(request);
            if (!StringUtil.isNotNull(result)) {
                return ApiResult.error(-1, "暂时获取不到最新的天气信息");
            }
            return result;
        } catch (BizException e) {
            return ApiResult.error(e);
        } catch (Exception e) {
            log.info("查询天气异常:\n{}", e.getMessage());
            return ApiResult.error(1, "查询天气异常");
        }
    }

    /**
     * 通过城市名查询未来天气
     *
     * @return
     */
    @PostMapping(name = "通过城市名查询未来天气", path = "/city/view/future")
    public Object getFutureWeatherByCity(@RequestBody @Validated WeatherRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResult.error(1, bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            Object result = weatherFacade.getFutureWeatherByCity(request);
            if (!StringUtil.isNotNull(result)) {
                return ApiResult.error(-1, "暂时获取不到未来的天气信息");
            }
            return result;
        } catch (BizException e) {
            return ApiResult.error(e);
        } catch (Exception e) {
            log.info("查询未来天气异常:\n{}", e.getMessage());
            return ApiResult.error(1, "查询未来天气异常");
        }

    }

}
