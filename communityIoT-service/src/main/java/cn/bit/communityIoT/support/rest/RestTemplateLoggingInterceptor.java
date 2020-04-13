package cn.bit.communityIoT.support.rest;

import cn.bit.framework.utils.json.JSONException;
import cn.bit.framework.utils.json.JSONObject;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author xiaoxi.lao
 * @Description :
 * @Date ： 2018/10/22 15:49
 */
@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        requestLog(request, body);
        return execution.execute(request, body);
    }

    private void requestLog(HttpRequest request, byte[] body) {
        if (HttpMethod.GET.equals(request.getMethod()) || HttpMethod.DELETE.equals(request.getMethod())) {
            String requestBody;
            try {
                requestBody = convertGetMethodURLParam2JsonString(request.getURI());
            } catch (JSONException e) {
                log.error("uri转换异常");
                requestBody = "";
            }
            log.info("请求URL : {}, HTTPMethod : {}, 请求参数 : {}, Headers : {}",
                    request.getURI(), request.getMethod(), requestBody, request.getHeaders());
        } else {
            log.info("请求URL : {}, HTTPMethod : {}, 请求参数 : {}, Headers : {}", request.getURI(), request.getMethod(),
                    new String(body, Charset.forName("UTF-8")), request.getHeaders());
        }
    }

    private String convertGetMethodURLParam2JsonString(URI uri) throws JSONException {
        String query = uri.getQuery();
        if (StringUtil.isEmpty(query)) {
            return "";
        }
        String[] URLParams = query.split("&");
        JSONObject queryParam = new JSONObject();
        for (String urlParam : URLParams) {
            if (StringUtil.isEmpty(urlParam)) {
                continue;
            }
            String[] keyAndValue = urlParam.split("=");
            queryParam.put(keyAndValue[0], Optional.ofNullable(keyAndValue[1]).orElse(""));
        }
        return queryParam.toString();
    }
}