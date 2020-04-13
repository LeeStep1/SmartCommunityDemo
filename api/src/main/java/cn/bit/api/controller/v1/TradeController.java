package cn.bit.api.controller.v1;

import cn.bit.facade.service.trade.TradeFacade;
import cn.bit.facade.vo.trade.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/v1/trade", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class TradeController {

    @Autowired
    private TradeFacade tradeFacade;

    @PostMapping(name = "平台支付完成后回调", path = "/{platform}/payment/notify")
    public void paymentNotify(@PathVariable("platform") String platform, @RequestBody String body,
                              HttpServletRequest request, HttpServletResponse response) throws Exception {
        Notification notification = new Notification();
        notification.setPlatform(platform);
        notification.setNotifyData(body);
        String responseStr = tradeFacade.paymentNotify(notification);
        response.getWriter().write(responseStr);
        response.getWriter().close();
    }

}
