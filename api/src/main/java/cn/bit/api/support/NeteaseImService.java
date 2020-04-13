package cn.bit.api.support;

import cn.bit.framework.utils.httpclient.HttpUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class NeteaseImService {

    private String registerUrl;

    private String updateUrl;

    private String appKey;

    private String appSecret;

    public NeteaseImService(String registerUrl, String updateUrl, String appKey, String appSecret) {
        this.registerUrl = registerUrl;
        this.updateUrl = updateUrl;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public Integer registerIM(String accid, String token, String name) throws Exception {
        return requestIM(registerUrl, accid, token, name);
    }

    public Integer updateIM(String accid, String token) throws Exception {
        return requestIM(updateUrl, accid, token,null);
    }

    /*public static Integer updateIMUseRestTemplate(RestTemplate restTemplate,String accid, String token){
        return requestIMUseSpringRestTemplate(restTemplate,GlobalConfig.IM_UPDATEURL, accid, token);
    }

    private static Integer requestIMUseSpringRestTemplate(RestTemplate restTemplate,String url, String accid, String token) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        headers.setContentType(type);

        String curTime = String.valueOf((new Date()).getTime() / 1000L);

        headers.add("AppKey", GlobalConfig.IM_APPKEY);
        headers.add("Nonce", GlobalConfig.IM_NONCE);
        headers.add("CurTime", curTime);
        headers.add("CheckSum", getCheckSum(GlobalConfig.IM_APPSECRET, GlobalConfig.IM_NONCE, curTime));
        //headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");


        Map<String, String> bodys = new HashMap<>();
        bodys.put("accid", accid);
        bodys.put("token", token);

        HttpEntity<String> formEntity = new HttpEntity<String>(JSONObject.toJSONString(bodys), headers);

        String result = restTemplate.postForObject(url, formEntity, String.class);
        JSONObject resultJson = JSONObject.parseObject(result);

        return resultJson.getInteger("code");
    }*/

    private Integer requestIM(String url, String accid, String token, String name) throws Exception {
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        String nonce = generateNonce();
        String checkSum = getCheckSum(appSecret, nonce, curTime);

        Map<String, String> headers = new HashMap<>();
        headers.put("AppKey", appKey);
        headers.put("Nonce", nonce);
        headers.put("CurTime", curTime);
        headers.put("CheckSum", checkSum);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        Map<String, String> bodys = new HashMap<>();
        bodys.put("accid", accid);
        bodys.put("token", token);
        if(StringUtils.isNotBlank(name)){
            bodys.put("name",name);
        }
        HttpResponse response = HttpUtils.doPost(url, null, null, headers, null, bodys);
        InputStream inputStream = response.getEntity().getContent();
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "/n");
        }
        inputStream.close();
        log.info("注册网易云信：" + sb);
        JSONObject jsonObject = JSONObject.parseObject(sb.toString().replace("/n",""));
        return  jsonObject.getInteger("code");
    }

    // 计算并获取CheckSum
    private String getCheckSum(String appSecret, String nonce, String curTime) {
        return DigestUtils.sha1Hex(appSecret + nonce + curTime);
    }

    private String generateNonce() {
        return UUID.randomUUID().toString();
    }
}
