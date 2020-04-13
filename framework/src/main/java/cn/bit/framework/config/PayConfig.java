package cn.bit.framework.config;


import cn.bit.framework.utils.ResourceUtils;

import java.util.Map;

/**
 * 支付配置
 * 
 * @author zhengj
 * @since 1.0 2016年10月24日
 */
public class PayConfig {
    
    public final static Map<String, String> PAY_CONFIG = ResourceUtils.getResource("pay").getMap();
    
    
    public final static String WXPAY_APP_ID = PAY_CONFIG.get("wxpay.appId");
    public final static String WXPAY_MCH_ID = PAY_CONFIG.get("wxpay.mchId");
    public final static String WXPAY_MCH_KEY = PAY_CONFIG.get("wxpay.mchKey");
    public final static String WXPAY_BODY = PAY_CONFIG.get("wxpay.body");
    public final static String WXPAY_NOTIFY_URL = PAY_CONFIG.get("wxpay.notifyUrl");
    public final static String WXPAY_UNIFIED_ORDER_URL= PAY_CONFIG.get("wxpay.unifiedOrderUrl");
    public final static String WXPAY_ORDER_QUERY_URL= PAY_CONFIG.get("wxpay.orderQueryUrl");
    public final static String WXPAY_DOWNLOAD_BILL_URL= PAY_CONFIG.get("wxpay.downloadBillUrl");
    public final static String WXPAY_DOWNLOAD_BILL_PATH= PAY_CONFIG.get("wxpay.downloadBillPath");
    
    
    private PayConfig() {
    }
    
}
