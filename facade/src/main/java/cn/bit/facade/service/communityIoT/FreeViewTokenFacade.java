package cn.bit.facade.service.communityIoT;

/**
 * @author : xiaoxi.lao
 * @Description :
 * @Date ： 2018/9/27 11:21
 */
public interface FreeViewTokenFacade {
    /**
     * token更新
     */
    String updateToken(String oldToken);
    /**
     * 获取token
     * @return 从redis获取token
     */
    String getToken();
}
