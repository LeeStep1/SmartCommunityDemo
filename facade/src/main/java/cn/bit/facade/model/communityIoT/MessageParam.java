package cn.bit.facade.model.communityIoT;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author jianjun.cheng
 * @description 短信发送请求参数
 * @create 2018-04-28
 **/
@Data
public class MessageParam implements Serializable {

    /**
     * 手机号码
     */
    private String number;
    /**
     * 签名
     */
    private String signName;
    /**
     * 模板id
     */
    private String tplId;

    /**
     * 对应模板中的参数
     */
    private Params params;
    /**
     * 扩展字段
     */
    private String ext;
    /**
     * 扩展码字段
     */
    private String extend;

    public static class Params{
        /**
         * 验证码
         */
        @Setter
        @Getter
        private String code;

        /**
         * 手机号
         */
        @Setter
        @Getter
        private String phone;

        /**
         * 企业名字
         */
        @Setter
        @Getter
        private String company;

        /**
         * 社区名字
         */
        @Setter
        @Getter
        private String community;

        /**
         * 房屋名字
         */
        @Setter
        @Getter
        private String house;

        /**
         * 应用名称
         */
        @Setter
        @Getter
        private String appName;
    }
}

