package cn.bit.api.support.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信code2Session接口响应实体
 *
 * @author jianming.fan
 * @date 2018-10-31
 */
@Data
public class JsCode2SessionResponse implements Serializable {
    /**
     * openid
     */
    @JsonProperty("openid")
    private String openId;
    /**
     * session_key
     */
    @JsonProperty("session_key")
    private String sessionKey;
    /**
     * unionid
     */
    @JsonProperty("unionid")
    private String unionId;
    /**
     * errcode
     */
    @JsonProperty("errcode")
    private String errCode;
    /**
     * errMsg
     */
    @JsonProperty("errMsg")
    private String errMsg;
}
