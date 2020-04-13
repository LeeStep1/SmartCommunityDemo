package cn.bit.trade.manager;

import cn.bit.facade.enums.PlatformType;
import cn.bit.facade.enums.TradeStatusType;
import cn.bit.facade.enums.TradeType;
import cn.bit.facade.exception.trade.TradeException;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.facade.vo.trade.Order;
import cn.bit.facade.vo.trade.TradeOrder;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static cn.bit.facade.exception.trade.TradeException.*;

/**
 * 微信交易管理器
 *
 * @author jianming.fan
 * @date 2018-10-09
 */
@Component
@Slf4j
public class WechatTradeManager implements TradeManager {

    private static final SimpleDateFormat WX_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Map<String, WXPay> WX_PAY_MAP = new HashMap<>();

    @Override
    public Order createOrder(TradeAccount tradeAccount, TradeOrder tradeOrder) throws TradeException {
        String trade_type;
        TradeType tradeType = TradeType.fromValue(tradeOrder.getTradeType());
        switch (tradeType) {
            case APP:
                trade_type = "APP";
                break;
            case WEB:
                trade_type = "MWEB";
                break;
            default:
                throw UNSUPPORTED_TRADE_TYPE;
        }

        Map<String, String> reqData = new HashMap<>();
        reqData.put("body", tradeOrder.getTitle());
        reqData.put("detail", StringUtil.defaultIfNull(tradeOrder.getDetail()));
        reqData.put("attach", StringUtil.defaultIfNull(tradeOrder.getAttach()));
        reqData.put("out_trade_no", tradeOrder.getTradeId().toString());
        reqData.put("total_fee", tradeOrder.getTotalAmount().toString());
        reqData.put("spbill_create_ip", tradeOrder.getUserIp());
        if (tradeOrder.getStartAt() != null) {
            reqData.put("time_start", WX_DATE_FORMAT.format(tradeOrder.getStartAt()));
        }
        if (tradeOrder.getExpireAt() != null) {
            reqData.put("time_expire", WX_DATE_FORMAT.format(tradeOrder.getExpireAt()));
        }
        reqData.put("notify_url", tradeOrder.getNotifyUrl());
        reqData.put("trade_type", trade_type);
        if (tradeType == TradeType.WEB) {
            reqData.put("scene_info", "{\"h5_info\": {\"type\":\"Wap\",\"wap_url\": \"https://smcm.bitiot.com.cn\",\"wap_name\": \"掌居宝\"}}");
        }

        try {
            WXPay wxPay = getWXPay(tradeAccount);
            Map<String, String> rspData = wxPay.unifiedOrder(reqData);

            log.info("order platform: {}, response data: {}", PlatformType.WECHAT, rspData);

            if (!"SUCCESS".equals(rspData.get("return_code"))) {
                throw new TradeException(1019999, rspData.get("return_msg"));
            }

            if (!"SUCCESS".equals(rspData.get("result_code"))) {
                throw new TradeException(1019999, rspData.get("err_code_des"));
            }

            Order order = new Order();
            order.setTradeId(tradeOrder.getTradeId());
            order.setTradeType(tradeOrder.getTradeType());
            order.setPlatform(PlatformType.WECHAT.value());
            if (tradeType == TradeType.APP) {
                order.setAppId(tradeAccount.getAppId());
                order.setPartnerId(tradeAccount.getPartnerId());
                order.setPrepayId(rspData.get("prepay_id"));
                order.setPackageValue("Sign=WXPay");
                order.setNoncestr(WXPayUtil.generateNonceStr());
                order.setTimestamp(System.currentTimeMillis() / 1000 + "");
                order.setSign(generateWechatSign(order, tradeAccount.getKey()));
            } else if (tradeType == TradeType.WEB) {
                order.setPaymentUrl(rspData.get("mweb_url"));
            }

            return order;
        } catch (Exception e) {
            throw new TradeException(1019999, e.getMessage());
        }
    }

    @Override
    public Trade paymentNotify(String notifyData, TradeAccountSelector selector) throws TradeException {
        Map<String, String> dataMap;
        try {
            dataMap = WXPayUtil.xmlToMap(notifyData);
        } catch (Exception e) {
            throw INVALID_WECHAT_PAY_NOTIFICATION;
        }

        if (!"SUCCESS".equals(dataMap.get("return_code"))) {
            throw INVALID_TRADE;
        }

        String appId = dataMap.get("appid");
        TradeAccount tradeAccount = selector.select(appId);
        try {
            String signType = dataMap.get(WXPayConstants.FIELD_SIGN_TYPE);
            if (!WXPayUtil.isSignatureValid(dataMap, tradeAccount.getKey(), getSignType(signType))) {
                throw INVALID_SIGN;
            }
        } catch (Exception e) {
            throw INVALID_WECHAT_PAY_NOTIFICATION;
        }

        Date payAt;
        try {
            payAt = DateUtils.parseDate(dataMap.get("time_end"), "yyyyMMddHHmmss");
        } catch (Exception e) {
            throw INVALID_WECHAT_PAY_NOTIFICATION;
        }

        Trade trade = new Trade();
        trade.setId(Long.valueOf(dataMap.get("out_trade_no")));
        trade.setAppId(appId);
        trade.setPartnerId(dataMap.get("mch_id"));
        trade.setTotalAmount(Long.valueOf(dataMap.get("total_fee")));
        trade.setAgtTradeNo(dataMap.get("transaction_id"));
        trade.setPayAt(payAt);
        trade.setStatus("SUCCESS".equals(dataMap.get("result_code"))
                ? TradeStatusType.PAID.value() : TradeStatusType.CLOSED.value());
        return trade;
    }

    @Override
    public String notifyResponse() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    @Override
    public String getName() {
        return PlatformType.WECHAT.name().toLowerCase();
    }

    private static WXPay getWXPay(TradeAccount tradeAccount) {
        return new WXPay(new WXPayConfig() {
            @Override
            public String getAppID() {
                return tradeAccount.getAppId();
            }

            @Override
            public String getMchID() {
                return tradeAccount.getPartnerId();
            }

            @Override
            public String getKey() {
                return tradeAccount.getKey();
            }

            @Override
            public InputStream getCertStream() {
                return null;
            }

            @Override
            public int getHttpConnectTimeoutMs() {
                return 0;
            }

            @Override
            public int getHttpReadTimeoutMs() {
                return 0;
            }
        }, getSignType(tradeAccount.getSignType()));

//        WXPay wxPay = WX_PAY_MAP.get(tradeAccount.getId().toString());
//        if (wxPay != null) {
//            return wxPay;
//        }
//
//        synchronized (WX_PAY_MAP) {
//            if ((wxPay = WX_PAY_MAP.get(tradeAccount.getId().toString())) != null) {
//                return wxPay;
//            }
//
//            wxPay = putWXPay(tradeAccount);
//        }
//
//        return wxPay;
    }

    private WXPay putWXPay(TradeAccount tradeAccount) {
        synchronized (WX_PAY_MAP) {
            WXPay wxPay = new WXPay(new WXPayConfig() {
                @Override
                public String getAppID() {
                    return tradeAccount.getAppId();
                }

                @Override
                public String getMchID() {
                    return tradeAccount.getPartnerId();
                }

                @Override
                public String getKey() {
                    return tradeAccount.getKey();
                }

                @Override
                public InputStream getCertStream() {
                    return null;
                }

                @Override
                public int getHttpConnectTimeoutMs() {
                    return 0;
                }

                @Override
                public int getHttpReadTimeoutMs() {
                    return 0;
                }
            });

            WX_PAY_MAP.put(tradeAccount.getId().toString(), wxPay);
            return wxPay;
        }
    }

    private static WXPayConstants.SignType getSignType(String signType) {
        try {
            return WXPayConstants.SignType.fromValue(signType);
        } catch (Exception e) {
            throw new RuntimeException("Sign Type is Not Support : signType=" + signType);
        }
    }

    private static String generateWechatSign(Order order, String key) throws Exception {
        Map<String, String> signData = new HashMap<>();
        signData.put("appid", order.getAppId());
        signData.put("partnerid", order.getPartnerId());
        signData.put("prepayid", order.getPrepayId());
        signData.put("package", order.getPackageValue());
        signData.put("noncestr", order.getNoncestr());
        signData.put("timestamp", order.getTimestamp());

        return WXPayUtil.generateSignature(signData, key);
    }

}
