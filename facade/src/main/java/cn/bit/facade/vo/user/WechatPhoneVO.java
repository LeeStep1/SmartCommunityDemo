package cn.bit.facade.vo.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信手机号展示实体类
 *
 * @author jianming.fan
 * @date 2018-10-31
 */
@Data
public class WechatPhoneVO implements Serializable {
    /**
     * iv
     */
    private String iv;
    /**
     * encryptedData
     */
    private String encryptedData;
    /**
     * 第三方平台的用户授权码
     */
    private String authCode;
}
