package cn.bit.facade.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信模板
 */
public enum SmsTempletType {
    SMS100003("sms_100003", "手机号+验证码+设置密码注册新用户", "【掌居宝】验证码123456，用于新用户注册，切勿泄漏。如非本人操作，请忽略此短信。"),
    SMS100004("sms_100004", "APP重置密码", "【掌居宝】验证码123456，用于重置登录密码，切勿泄漏。如非本人操作，请忽略此短信。"),
    SMS100005("sms_100005", "手机号+验证码登录APP", "【掌居宝】验证码123456，用于重置登录密码，切勿泄漏。如非本人操作，请忽略此短信。"),
    SMS100006("sms_100006", "更换手机号", "【掌居宝】验证码123456，用于验证新手机号，切勿泄漏。如非本人操作，请忽略此短信。"),
    SMS100016("sms_100016", "物业人员注册邀请", "<企业名称>已将你加入<社区名称>管理团队，请登录掌居宝物业端查看详情。"),
    SMS100018("sms_100018", "物业添加业主档案后通知业主", "【掌居宝】{物业名}已帮您通过{社区名称}{房屋名称}的业主认证，下载掌居宝APP开启智慧生活。");

    /**
     * 模版id
     */
    private String key;
    /**
     * 说明
     */
    private String value;
    /**
     * 示例
     */
    private String remark;

    private static Map<Integer, String> map = new HashMap<>();

    SmsTempletType(String key, String value, String remark) {
        this.key = key;
        this.value = value;
        this.remark = remark;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String getRemark() {
        return this.remark;
    }
}
