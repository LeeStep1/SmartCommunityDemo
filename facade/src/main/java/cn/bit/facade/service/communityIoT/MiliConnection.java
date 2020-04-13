package cn.bit.facade.service.communityIoT;

import java.util.Map;

/**
 * @Description : 米立封装库
 * @author : xiaoxi.lao
 * @Date ： 2018/9/17 10:13
 */
public interface MiliConnection {

    Map APIGet(String url);

    Map APIPost(String url, Object request) throws Exception;
}
