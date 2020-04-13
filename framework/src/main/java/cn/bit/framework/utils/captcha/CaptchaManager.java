package cn.bit.framework.utils.captcha;


import cn.bit.framework.enums.CaptchaTextType;
import cn.bit.framework.exceptions.BizException;

/**
 * 验证码管理类
 *
 */
public interface CaptchaManager {

    /**
     * 生成图片验证码
     * @param type 验证码类型
     * @param imgWidth 图片宽度
     * @param imgHeight 图片高度
     * @param noise 干扰线条数
     * @param textLength 文本长度
     * @param expires 过期时间(单位：秒)
     * @return
     * @throws BizException
     */
    Captcha generateImgCaptcha(CaptchaTextType type, int imgWidth,
                               int imgHeight, int noise, int textLength, int expires) throws BizException;

    /**
     * 生成手机验证码(等接入短信网关后实现)
     * @param type 验证码类型
     * @param mobile 手机号
     * @param textLength 文本长度
     * @param expires 过期时间(单位：秒)
     * @return
     * @throws BizException
     */
    Captcha generateMobileCaptcha(CaptchaTextType type, String mobile, int textLength, int expires) throws BizException;

    /**
     * 删除验证码
     * @param id 验证码id
     * @throws BizException
     */
    void removeCaptcha(String id) throws BizException;

    /**
     * 获取验证码
     * @param id 验证码id
     * @return
     * @throws BizException
     */
    Captcha getCaptcha(String id) throws BizException;
}
