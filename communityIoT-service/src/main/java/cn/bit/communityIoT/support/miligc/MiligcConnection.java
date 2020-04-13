package cn.bit.communityIoT.support.miligc;

import cn.bit.facade.service.communityIoT.MiliConnection;
import cn.bit.facade.service.communityIoT.MiliTokenFacade;
import cn.bit.framework.utils.rsa.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class MiligcConnection implements MiliConnection {

    @Value("${mili.url}")
    private String basePath;
    @Value("${mili.appId}")
    private String appId;
    @Value("${mili.appSecret}")
    private String appSecret;
    private int type = 3;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MiliTokenFacade miliTokenFacade;

    /**
     * 获取token
     */
    private String getToken() {
        return miliTokenFacade.getToken();
    }

    /**
     * get请求
     *
     * @param url
     * @return
     * @throws Exception
     */
    public Map APIGet(String url) {
        String token = getToken();
        URI uri = URI.create(basePath + url);
        String timestamp = time2Date();
        String verify = encoderByMd5(timestamp + "|" + type + "|" + appSecret);
        String authString = getAuthString(appId, timestamp, verify, token, type);
        String code = Base64.encode(authString.getBytes());
        RequestEntity<Void> miliRequestByGetMethod = RequestEntity.get(uri).header("Authorization", "Basic " + code)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString()).build();

        try {
            ResponseEntity<Map> getMethodResponse = restTemplate.exchange(miliRequestByGetMethod, Map.class);
            return getMethodResponse.getBody();
        } catch (HttpClientErrorException e) {
            if (Objects.equals(e.getStatusCode(), HttpStatus.UNAUTHORIZED)) {
                miliTokenFacade.updateToken(token);
                return APIGet(url);
            }
            log.error("mili get method error");
        }
        return Collections.emptyMap();
    }

    /**
     * post请求
     *
     * @param url
     * @param request
     * @return
     * @throws Exception
     */
    public Map APIPost(String url, Object request) throws Exception {
        StringBuffer str = new StringBuffer();
        String token = getToken();
        if (!StringUtils.isEmpty(request)) {
            Class clazz = request.getClass();
            Field[] fs = clazz.getDeclaredFields();

            if (fs.length > 0) {
                str.append("?");
            }

            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                f.setAccessible(true);
                Object val = f.get(request);
                if (!"serialVersionUID".equals(f.getName())) {
                    if (!StringUtils.isEmpty(val)) {
                        str.append(f.getName());
                        str.append("=");
                        str.append(val.toString());
                        if ((i + 1) < fs.length) {
                            str.append("&&");
                        }
                    }
                }
            }

            if (url.endsWith("&&")) {
                url = org.apache.commons.lang.StringUtils.substringBeforeLast(url, "&&");
            }
        }

        URI uri = URI.create(basePath + url + (StringUtils.isEmpty(str) ? null : str.toString()));
        String timestamp = time2Date();
        String verify = encoderByMd5(timestamp + "|" + type + "|" + appSecret);
        String authString = getAuthString(appId, timestamp, verify, token, type);
        String code = Base64.encode(authString.getBytes());
        RequestEntity<Void> miliRequestByPostMethod = RequestEntity.post(uri).header("Authorization", "Basic " + code)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString()).build();

        try {
            ResponseEntity<Map> postMethodResponse = restTemplate.exchange(miliRequestByPostMethod, Map.class);
            return postMethodResponse.getBody();
        } catch (HttpClientErrorException e) {
            if (Objects.equals(e.getStatusCode(), HttpStatus.UNAUTHORIZED)) {
                miliTokenFacade.updateToken(token);
                return APIPost(url, request);
            }
            log.error("mili post method error");
        }
        return Collections.emptyMap();
    }

    private String getAuthString(String appId, String timestamp, String verify, String token, int type) {
        return miliTokenFacade.getAuthString(appId, timestamp, verify, token, type);
    }

    private String encoderByMd5(String str) {
        return miliTokenFacade.encoderByMd5(str);
    }

    private String time2Date() {
        return miliTokenFacade.time2Date();
    }
}
