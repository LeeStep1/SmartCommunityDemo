package cn.bit.facade.service.communityIoT;

/**
 * @Description : 第三方token管理接口
 * @author : xiaoxi.lao
 * @Date ： 2018/9/17 10:13
 */
public interface TokenManager {
    /**
     * 刷新token
     */
    String refreshToken();
}
