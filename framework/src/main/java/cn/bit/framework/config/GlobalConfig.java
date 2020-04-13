package cn.bit.framework.config;


import cn.bit.framework.utils.ResourceUtils;

import java.util.Map;

/**
 * Created by terry on 2016/6/28.
 */
public class GlobalConfig {


    /*public static Map<String, String> OAUTH_CONFIG = ResourceUtils.getResource("oauth").getMap();*/

    public static Map<String, String> SYS_CONFIG = ResourceUtils.getResource("system").getMap();

    public static final String SYS_HOST = SYS_CONFIG.get("onyx.host");


    // ############################## OAUTH ##############################
    /*public static final String OAUTH_CALLBACK_URL = OAUTH_CONFIG.get("oAuth.callbackUrl");

    public static final String OAUTH_GITHUB_AUTHORIZATION_URL = OAUTH_CONFIG.get("oAuth.github.authorizationUrl");
    public static final String OAUTH_GITHUB_ACCESSTOKEN_URL = OAUTH_CONFIG.get("oAuth.github.accessTokenUrl");
    public static final String OAUTH_GITHUB_ACCOUNT_URL = OAUTH_CONFIG.get("oAuth.github.accountUrl");
    public static final String OAUTH_GITHUB_STATE = OAUTH_CONFIG.get("oAuth.github.state");
    public static final String OAUTH_GITHUB_CLIENTID = OAUTH_CONFIG.get("oAuth.github.clientId");
    public static final String OAUTH_GITHUB_CLIENTSECRET = OAUTH_CONFIG.get("oAuth.github.clientSecret");

    public static final String OAUTH_WEIBO_CLIENTID = OAUTH_CONFIG.get("oAuth.weibo.clientId");
    public static final String OAUTH_WEIBO_CLIENTSECRET = OAUTH_CONFIG.get("oAuth.weibo.clientSecret");
    public static final String OAUTH_WEIBO_TOKENINFO_URL = OAUTH_CONFIG.get("oAuth.weibo.tokenInfoUrl");
    public static final String OAUTH_WEIBO_USERINFO_URL = OAUTH_CONFIG.get("oAuth.weibo.userInfoUrl");

    public static final String OAUTH_WECHAT_AUTHORIZATION_URL = OAUTH_CONFIG.get("oAuth.wechat.authorizationUrl");
    public static final String OAUTH_WECHAT_ACCESSTOKEN_URL = OAUTH_CONFIG.get("oAuth.wechat.accessTokenUrl");
    public static final String OAUTH_WECHAT_STATE = OAUTH_CONFIG.get("oAuth.wechat.state");
    public static final String OAUTH_WECHAT_APPID = OAUTH_CONFIG.get("oAuth.wechat.appId");
    public static final String OAUTH_WECHAT_APPSECRET = OAUTH_CONFIG.get("oAuth.wechat.appSecret");
    public static final String OAUTH_WECHAT_USERINFO_URL = OAUTH_CONFIG.get("oAuth.wechat.userInfoUrl");

    public static final String OAUTH_QQ_AUTHORIZATION_URL = OAUTH_CONFIG.get("oAuth.qq.authorizationUrl");
    public static final String OAUTH_QQ_ACCESSTOKEN_URL = OAUTH_CONFIG.get("oAuth.qq.accessTokenUrl");
    public static final String OAUTH_QQ_STATE = OAUTH_CONFIG.get("oAuth.github.state");
    public static final String OAUTH_QQ_SCOPE = OAUTH_CONFIG.get("oAuth.github.scope");
    public static final String OAUTH_QQ_OPENID_URL = OAUTH_CONFIG.get("oAuth.qq.openIdUrl");
    public static final String OAUTH_QQ_USERINFO_URL = OAUTH_CONFIG.get("oAuth.qq.userInfoUrl");
    public static final String OAUTH_QQ_APPID = OAUTH_CONFIG.get("oAuth.qq.appId");
    public static final String OAUTH_QQ_APPKEY = OAUTH_CONFIG.get("oAuth.qq.appKey");*/

    // ############################## OSS ##############################
    public static final String OSS_ENDPOINT = SYS_CONFIG.get("oss.endpoint");
    public static final String OSS_ACCESS_KEI_ID = SYS_CONFIG.get("oss.accessKeyId");
    public static final String OSS_ACCESS_KEI_SECRET = SYS_CONFIG.get("oss.accessKeySecret");
    public static final String OSS_BUCKET_CAPTCHA = SYS_CONFIG.get("oss.bucket.captcha");
    public static final String OSS_BUCKET_CAPTCHA_DOMAIN = SYS_CONFIG.get("oss.bucket.captcha.domain");
    public static final String OSS_BUCKET_AVATAR = SYS_CONFIG.get("oss.bucket.avatar");
    public static final String OSS_BUCKET_AVATAR_DOMAIN = SYS_CONFIG.get("oss.bucket.avatar.domain");

    // ############################## MQ ##############################
    /*public static final String MQ_EXCHANGE_ONYX = SYS_CONFIG.get("mq.exchange.onyx");
    public static final String MQ_QUEUE_MAIL = SYS_CONFIG.get("mq.queue.mail");
    public static final String MQ_ROUTEKEY_MAIL = SYS_CONFIG.get("mq.routekey.mail");
    public static final String MQ_QUEUE_STATISTICS = SYS_CONFIG.get("mq.queue.statistics");
    public static final String MQ_ROUTEKEY_STATISTICS = SYS_CONFIG.get("mq.routekey.statistics");*/

    // ############################## others ##############################
    /*public static final String ACTIVATION_FRONT_URL = SYS_CONFIG.get("activation.front.url");
    public static final int ALIVE_TIME_CAPTCHA = Integer.valueOf(SYS_CONFIG.get("alive.time.captcha"));
    public static final int ALIVE_TIME_ACTIVATION_TOKEN = Integer.valueOf(SYS_CONFIG.get("alive.time.activation" +
            ".token"));
    public static final int ALIVE_TIME_ACCESS_TOKEN = Integer.valueOf(SYS_CONFIG.get("alive.time.access.token"));
    public static final int ALIVE_TIME_RETRIEVE_PASSWD_TOKEN = Integer.valueOf(SYS_CONFIG.get("alive.time.retrieve" +
            ".password.token"));

    public static final int MAIL_TASK_RETRIES = Integer.valueOf(SYS_CONFIG.get("mail.task.retries"));
    public static final int MAIL_TASK_FIRST_DELAY = Integer.valueOf(SYS_CONFIG.get("mail.task.first.delay"));

    public static final long ROOT_PRODUCT_CATEGORY_ID = 0L;
    public static final int REDIS_DB_MYBATIS = Integer.valueOf(SYS_CONFIG.get("redis.db.mybatis"));

    public static final String PUSH_LEAN_CLOUD_APP_ID = SYS_CONFIG.get("push.leanCloud.appId");
    public static final String PUSH_LEAN_CLOUD_APP_KEY = SYS_CONFIG.get("push.leanCloud.appKey");*/

    // ######################### mili device ############################
    /*public static final String MILI_URL = SYS_CONFIG.get("mili.url");
    public static final String MILI_APPID = SYS_CONFIG.get("mili.appId");
    public static final String MILI_APPSECRET = SYS_CONFIG.get("mili.appSecret");*/

    // ############################## SMS ##############################
    /*public static final String SMS_URL = SYS_CONFIG.get("sms.url");
    public static final String SMS_APPID = SYS_CONFIG.get("sms.appid");

    public static final String EZVIZ_URL = SYS_CONFIG.get("ezviz.url");*/

    public static long[] getMailTaskRetryDelays() {
        String[] ss = SYS_CONFIG.get("mail.task.retry.delays").split(",");
        long[] delays = new long[ss.length];
        for (int i = 0; i < ss.length; i++) {
            delays[i] = Long.valueOf(ss[i]);
        }
        return delays;
    }

    public static final String[] SYS_LOCALES = {"en_US", "zh_CN"};

    // ############################## IM config ##############################

    public static final String IM_REGISTERURL = SYS_CONFIG.get("im.registerUrl");
    public static final String IM_UPDATEURL = SYS_CONFIG.get("im.updateUrl");
    public static final String IM_UPDATEUINFO= SYS_CONFIG.get("im.updateUinfo");
    public static final String IM_APPKEY = SYS_CONFIG.get("im.appKey");
    public static final String IM_APPSECRET = SYS_CONFIG.get("im.appSecret");
    public static final String IM_NONCE = SYS_CONFIG.get("im.nonce");


}
