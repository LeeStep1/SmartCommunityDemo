package cn.bit.communityIoT.service;

import cn.bit.communityIoT.support.token.AbstractTokenManager;
import cn.bit.facade.service.communityIoT.FreeViewTokenFacade;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.rsa.Base64;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static cn.bit.framework.constant.CacheConstant.FREEVIEW_TOKEN;

/**
 * @author : xiaoxi.lao
 * @Description :
 * @Date ： 2018/9/20 11:59
 */

@Slf4j
@Service("freeViewTokenFacade")
public class FreeViewTokenFacadeImpl extends AbstractTokenManager implements FreeViewTokenFacade {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${freeView.url}")
    private String freeViewUrl;
    @Value("${freeView.basic.uuid}")
    private String freeViewUUID;
    @Value("${freeView.username}")
    private String freeViewUserName;
    @Value("${freeView.password}")
    private String freeViewPassword;

    /**
     * 全视通获取token
     */
    private static final String FREEVIEW_GET_TOKEN = ":9700/api/accesstoken?userName={userName}&password={password}";

    private static final String FREEVIEW_TOKEN_LAST_TOKEN = FREEVIEW_TOKEN + "_last_token";

    private static final String FREEVIEW_TOKEN_REFRESHING = FREEVIEW_TOKEN + "_refreshing";

    protected String doRefreshToken() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Basic " + Base64.encode(freeViewUUID.getBytes()));
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        Map<String, String> urlVarMap = new HashMap<>();
        urlVarMap.put("userName", freeViewUserName);
        urlVarMap.put("password", freeViewPassword);

        HttpEntity<Map> entity = new HttpEntity<>(httpHeaders);
        String url = freeViewUrl + FREEVIEW_GET_TOKEN;
        log.info("刷新全视通token");
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class, urlVarMap);

        String jsonResult = JSON.toJSONString(response.getBody());
        JSONObject jsonObject = JSON.parseObject(jsonResult);
        String token = "Bearer " + jsonObject.getString("access_token");
        log.info("全视通token = " + token);
        RedisTemplateUtil.setStr(FREEVIEW_TOKEN, token);
        return token;
    }

    @Override
    protected String getLastToken() {
        return FREEVIEW_TOKEN_LAST_TOKEN;
    }

    @Override
    protected String getRefreshingKey() {
        return FREEVIEW_TOKEN_REFRESHING;
    }

    @Override
    protected String getTokenFromRedis() {
        return RedisTemplateUtil.getStr(FREEVIEW_TOKEN);
    }

}
