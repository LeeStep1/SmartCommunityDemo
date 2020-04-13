package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.enums.push.PushPointEnum;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/push", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PushController {

    @GetMapping(name = "待配置功能节点列表", path = "/func-points")
    @Authorization
    public ApiResult listPushPoints() {
        return ApiResult.ok(PushPointEnum.functionPoints());
    }

}
