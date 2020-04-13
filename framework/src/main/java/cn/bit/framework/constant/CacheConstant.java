/**
 * wusc.edu.pay.common.constant.CacheConstant.java
 */
package cn.bit.framework.constant;

/**
 * <ul>
 * <li>Title:缓存常量类</li>
 * <li>Description: 定义缓存KEY值</li>
 * <li>Copyright: www.gzzyzz.com</li>
 * <li>Company:</li>
 * </ul>
 *
 * @author Hill
 * @version 2014-4-22
 */
public class CacheConstant {

    public static final String SPLITTER = "_";

    /**
     * 验证码
     */
    public static final String CAPTCHA_KEY_PREFIX = "CAPTCHA_";
    public static final String CAPTCHA_SEQUENCE = "CAPTCHA_SEQ";

    /**
     * 邮件
     */
    public static final String MAIL_KEY_PREFIX = "MAIL_";
    public static final String MAIL_SEQUENCE = "MAIL_SEQ";

    /**
     * token
     */
    public static final String TOKEN_UID_PREFIX = "TOKEN_UID_";
    public static final String UID_TOKEN_PREFIX = "UID_TOKEN_";
    public static final String MILI_TOKEN = "MILI_TOKEN";
    public static final String FREEVIEW_TOKEN = "FREEVIEW_TOKEN";
    public static final String EZVIZ_TOKEN = "EZVIZ_TOKEN";

    /**
     * counter
     */
    public static final String COUNTER_PREFIX = "COUNTER_";

    public static final String ACL_PREFIX = "ACL_";

    /**
     * 社区列表
     */
    public static final String COMMUNITY_MAP = "COMMUNITYMAP";

    /**
     * 社区物业
     */
    public static final String USER_TO_PROPERTY_PREFIX = "USER_TO_PROPERTY_";

    /**
     * 物业账单
     */
    public static final String PROPERTY_BILL = "PROPERTY_BILL";

    /**
     * 举报上限次数
     */
    public static final String REPORT_EXPECT_COUNT = "REPORT_EXPECT_COUNT_";

    /**
     * 敏感词
     */
    public static final String SENSITIVE_WORDS = "MOMENT_SENSITIVE_WORDS";
}
