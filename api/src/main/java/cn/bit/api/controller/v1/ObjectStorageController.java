package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class ObjectStorageController {

    @Value("${oss.sts.token.url}")
    private String ossStsTokenEndpoint;

    @Value("${oss.readonly.sts.token.url}")
    private String ossReadonlyStsTokenEndpoint;

    @Value("${oss.policy.url}")
    private String ossPolicyEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping(name = "获取OSS-Token", path = "/oss/sts-token")
    public ApiResult getOSSSTSToken() {
        try {
            return restTemplate.getForObject(ossStsTokenEndpoint, ApiResult.class);
        } catch (Exception e) {
            log.error("getOSSSTSToken error:", e);
            return ApiResult.error(-1, "请求异常");
        }
    }

    @GetMapping(name = "获取OSS-token(只读)", path = "/oss/sts-token/read-only")
    public ApiResult getOSSReadOnlyStsToken() {
        try {
            return restTemplate.getForObject(ossReadonlyStsTokenEndpoint, ApiResult.class);
        } catch (Exception e) {
            log.error("getOSSReadOnlyStsToken error:", e);
            return ApiResult.error(-1, "请求异常");
        }
    }

    @GetMapping(name = "获取OSS桶政策", path = "/oss/{bucket}/policy")
    public ApiResult getOSSPolicy(@PathVariable("bucket") String bucket) {
        try {
            return restTemplate.getForObject(ossPolicyEndpoint + "/" + bucket, ApiResult.class);
        } catch (Exception e) {
            log.error("getOSSPolicy error:", e);
            return ApiResult.error(-1, "请求异常");
        }
    }

}
