package cn.bit.communityIoT.service;

import cn.bit.communityIoT.support.token.AbstractTokenManager;
import cn.bit.facade.service.communityIoT.MiliTokenFacade;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.rsa.Base64;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.MessageDigest;
import java.util.Date;

import static cn.bit.framework.constant.CacheConstant.MILI_TOKEN;

/**
 * @author : xiaoxi.lao
 * @Description :
 * @Date ： 2018/9/20 11:42
 */
@Slf4j
@Service("miliTokenFacade")
public class MiliTokenFacadeImpl extends AbstractTokenManager implements MiliTokenFacade {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mili.url}")
    private String basePath;
    @Value("${mili.appId}")
    private String appId;
    @Value("${mili.appSecret}")
    private String appSecret;
    private int type = 3;

    private static final String MILI_TOKEN_LAST_TOKEN = MILI_TOKEN + "_last_token";

    private static final String MILI_TOKEN_REFRESHING = MILI_TOKEN + "_refreshing";

    protected String doRefreshToken() {
        String timestamp = time2Date();
        String verify = encoderByMd5(timestamp + "|" + type + "|" + appSecret);
        String authString = getAuthString(appId, timestamp, verify, null, type);
        String code = Base64.encode(authString.getBytes());

        RequestEntity<Void> getTokenRequest = RequestEntity.get(URI.create(basePath + "wuye/config"))
                .header("Authorization", "Basic " + code)
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString()).build();
        log.info("刷新米立token");
        ResponseEntity<JSONObject> getTokenResponse = restTemplate.exchange(getTokenRequest, JSONObject.class);
        String token = getTokenResponse.getBody().getJSONObject("body").getString("token");
        log.info("米立token = " + token);
        RedisTemplateUtil.setStr(MILI_TOKEN, token);
        return token;
    }

    @Override
    protected String getLastToken() {
        return MILI_TOKEN_LAST_TOKEN;
    }

    @Override
    protected String getRefreshingKey() {
        return MILI_TOKEN_REFRESHING;
    }

    @Override
    protected String getTokenFromRedis() {
        return RedisTemplateUtil.getStr(MILI_TOKEN);
    }

    @Override
    public String getAuthString(String appId, String timestamp, String verify, String token, int type) {
        StringBuffer str = new StringBuffer();
        if (appId != null) {
            str.append(appId);
        }
        str.append(":");
        if (timestamp != null) {
            str.append(timestamp);
        }
        str.append(":");
        if (verify != null) {
            str.append(verify);
        }
        str.append(":");
        if (token != null) {
            str.append(token);
        }
        str.append(":");
        str.append(type);
        return str.toString();
    }

    @Override
    public String encoderByMd5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("GBK"));
            StringBuffer buf = new StringBuffer();
            for (byte b : md.digest()) {
                buf.append(String.format("%02x", b & 0xff));
            }
            return buf.toString();
        } catch (Exception e) {
            log.error("Exception:", e);
            return null;
        }
    }

    @Override
    public String time2Date() {
        Long date = new Date().getTime() / 1000;
        return String.valueOf(date);
    }
}
