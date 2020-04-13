package cn.bit.facade.service.communityIoT;

/**
 * @author : xiaoxi.lao
 * @Description :
 * @Date ： 2018/9/27 11:22
 */
public interface MiliTokenFacade {
    /**
     * token更新
     */
    String updateToken(String oldToken);
    /**
     * 获取token
     * @return 从redis获取token
     */
    String getToken();

    String getAuthString(String appId, String timestamp, String verify, String token, int type);

    String encoderByMd5(String str);

    String time2Date();
}
