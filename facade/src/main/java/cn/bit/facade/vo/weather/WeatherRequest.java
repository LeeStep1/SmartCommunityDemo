package cn.bit.facade.vo.weather;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by lixinkui on 2018/2/6.
 */
@Data
public class WeatherRequest implements Serializable {

    @NotBlank(message = "查询城市名不能为空")
    private String city;

}
