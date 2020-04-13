package cn.bit.trade.manager;

import cn.bit.facade.enums.PlatformType;
import cn.bit.facade.enums.TradeStatusType;
import cn.bit.facade.enums.TradeType;
import cn.bit.facade.exception.trade.TradeException;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.facade.vo.trade.Order;
import cn.bit.facade.vo.trade.TradeOrder;
import cn.bit.framework.config.GlobalConfig;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.bit.facade.exception.trade.TradeException.*;

/**
 * 支付宝交易管理器
 *
 * @author jianming.fan
 * @date 2018-10-09
 */
@Component
@Slf4j
public class AlipayTradeManager implements TradeManager{

    private static String ALIPAY_GATEWAY;

    private static final Map<String, AlipayClient> ALIPAY_CLIENT_MAP = new HashMap<>();

    @Override
    public Order createOrder(TradeAccount tradeAccount, TradeOrder tradeOrder) throws TradeException {
        AlipayClient alipayClient = getAlipayClient(tradeAccount);

        AlipayTradeAppPayModel bizModel = new AlipayTradeAppPayModel();
        bizModel.setBody(tradeOrder.getDetail());
        bizModel.setSubject(tradeOrder.getTitle());
        bizModel.setOutTradeNo(tradeOrder.getTradeId().toString());
        bizModel.setTimeoutExpress(calculateMinuteInterval(tradeOrder.getStartAt(),
                tradeOrder.getExpireAt()));
        bizModel.setTotalAmount(cent2Yuan(tradeOrder.getTotalAmount()));
        bizModel.setProductCode("QUICK_MSECURITY_PAY");
        bizModel.setGoodsType(tradeOrder.getGoodsType().toString());
        bizModel.setPassbackParams(tradeOrder.getAttach());

        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(tradeOrder.getNotifyUrl());
        request.setBizModel(bizModel);

        try {
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);

            log.info("order platform: {}, order info: {}", PlatformType.ALIPAY, response.getBody());

            Order order = new Order();
            order.setTradeId(tradeOrder.getTradeId());
            order.setTradeType(TradeType.APP.value());
            order.setPlatform(PlatformType.ALIPAY.value());
            order.setOrderInfo(response.getBody());
            return order;
        } catch (AlipayApiException e) {
            throw new TradeException(Integer.valueOf(e.getErrCode()), e.getErrMsg());
        }
    }

    @Override
    public Trade paymentNotify(String notifyData, TradeAccountSelector selector) throws TradeException {
        Map<String, String> dataMap;
        try {
            dataMap = formStrToMap(notifyData);
        } catch (Exception e) {
            throw INVALID_ALIPAY_PAY_NOTIFICATION;
        }

        String appId = dataMap.get("app_id");
        TradeAccount tradeAccount = selector.select(appId);
        try {
            String charset = dataMap.get("charset");
            String signType = dataMap.get("sign_type");
            if (!AlipaySignature.rsaCheckV1(dataMap, tradeAccount.getAlipayPubKey(), charset, signType)) {
                throw INVALID_SIGN;
            }
        } catch (AlipayApiException e) {
            throw INVALID_ALIPAY_PUBLIC_KEY;
        }

        Trade trade = new Trade();
        trade.setId(Long.valueOf(dataMap.get("out_trade_no")));
        trade.setAppId(appId);
        trade.setPartnerId(dataMap.get("seller_id"));
        trade.setTotalAmount(yuan2Cent(dataMap.get("total_amount")));
        trade.setAgtTradeNo(dataMap.get("trade_no"));
        trade.setPayAt(DateUtils.getDateByStr(dataMap.get("gmt_payment")));
        trade.setStatus(transAlipayTradeStatus(dataMap.get("trade_status"), trade.getPayAt() != null));
        return trade;
    }

    @Override
    public String notifyResponse() {
        return "success";
    }

    @Override
    public String getName() {
        return PlatformType.ALIPAY.name().toLowerCase();
    }

    private static AlipayClient getAlipayClient(TradeAccount tradeAccount) {
        return DefaultAlipayClient.builder(ALIPAY_GATEWAY, tradeAccount.getAppId(), tradeAccount.getPvtKey())
                .signType(tradeAccount.getSignType()).build();
//        AlipayClient client = ALIPAY_CLIENT_MAP.get(tradeAccount.getId().toString());
//        if (client != null) {
//            return client;
//        }
//
//        synchronized (ALIPAY_CLIENT_MAP) {
//            if ((client = ALIPAY_CLIENT_MAP.get(tradeAccount.getId().toString())) != null) {
//                return client;
//            }
//
//            client = putAlipayClient(tradeAccount);
//        }
//
//        return client;
    }

    private AlipayClient putAlipayClient(TradeAccount tradeAccount) {
        synchronized (ALIPAY_CLIENT_MAP) {
            AlipayClient client = new DefaultAlipayClient(ALIPAY_GATEWAY, tradeAccount.getAppId(), tradeAccount.getPvtKey());
            ALIPAY_CLIENT_MAP.put(tradeAccount.getId().toString(), client);
            return client;
        }
    }

    private static String cent2Yuan(Long cent) {
        if (cent == null) {
            return null;
        }

        if (cent < 10) {
            return "0.0" + cent;
        }

        if (cent < 100) {
            return "0." + cent;
        }

        String centStr = cent.toString();
        StringBuilder sb = new StringBuilder(centStr.substring(0, centStr.length() - 2));
        sb.append(".");
        sb.append(centStr.substring(centStr.length() - 2));
        return sb.toString();
    }

    private static Long yuan2Cent(String yuan) {
        if (StringUtil.isBlank(yuan)) {
            return 0L;
        }

        return Long.valueOf(yuan.replace(".", ""));
    }

    private static String calculateMinuteInterval(Date startAt, Date expireAt) {
        if (startAt == null || expireAt == null) {
            return null;
        }

        Long minuteInterval = DateUtils.getDateDiff(startAt, expireAt) / 1000 / 60;
        return minuteInterval + "m";
    }

    private static Map<String, String> formStrToMap(String formStr) throws UnsupportedEncodingException {
        List<String> pairs = new LinkedList<>();
        StringTokenizer st = new StringTokenizer(formStr, "&");
        while (st.hasMoreTokens()) {
            pairs.add(st.nextToken().trim());
        }

        Map<String, String> map = new HashMap<>(pairs.size());
        for (String pair : pairs) {
            int idx = pair.indexOf(61);
            if (idx == -1) {
                map.putIfAbsent(URLDecoder.decode(pair, StandardCharsets.UTF_8.name()), null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                map.putIfAbsent(name, value);
            }
        }

        return map;
    }

    private static Integer transAlipayTradeStatus(String alipayTradeStatus, boolean paid) {
        return "WAIT_BUYER_PAY".equals(alipayTradeStatus) ? TradeStatusType.NOT_PAY.value()
                : "TRADE_CLOSED".equals(alipayTradeStatus) ? (paid ? TradeStatusType.REFUNDED.value() : TradeStatusType.CLOSED.value())
                : ("TRADE_SUCCESS".equals(alipayTradeStatus) || "TRADE_FINISHED".equals(alipayTradeStatus))
                ? TradeStatusType.PAID.value() : TradeStatusType.UNKNOWN.value();
    }

    @Value("${alipay.gateway}")
    public void setAlipayGateway(String alipayGateway) {
        ALIPAY_GATEWAY = alipayGateway;
    }
}
