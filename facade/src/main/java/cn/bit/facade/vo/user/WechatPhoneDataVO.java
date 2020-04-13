package cn.bit.facade.vo.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信手机信息
 *
 * @author jianming.fan
 * @date 2018-11-13
 */
@Data
public class WechatPhoneDataVO implements Serializable {
    /**
     * 手机号（国外手机会带区号）
     */
    private String phoneNumber;
    /**
     * 手机号（不带区号）
     */
    private String purePhoneNumber;
    /**
     * 区号
     */
    private String countryCode;
}
