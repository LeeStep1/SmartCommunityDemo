package cn.bit.job.handler;

import cn.bit.facade.service.communityIoT.MiliTokenFacade;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : xiaoxi.lao
 * @Description : 定时刷新米立token
 * @Date ： 2018/9/15 11:36
 */
@Component
@Slf4j
@JobHandler(value = "miliTokenJobHandler")
public class MiliTokenJobHandler extends IJobHandler {

    @Autowired
    private MiliTokenFacade miliTokenFacade;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("定时刷新米立token");
        miliTokenFacade.getToken();
        return ReturnT.SUCCESS;
    }
}
