package cn.bit.api.support.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信code2Session接口请求实体类
 *
 * @author jianming.fan
 * @date 2018-10-31
 */
@Data
public class JsCode2SessionRequest implements Serializable {
    private String appId;

    private String secret;

    private String authCode;

    private String grantType = "authorization_code";
}
