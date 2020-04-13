package cn.bit.job.handler;

import cn.bit.facade.service.communityIoT.FreeViewTokenFacade;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : xiaoxi.lao
 * @Description : 定时刷新全视通token
 * @Date ： 2018/9/15 11:36
 */
@Component
@Slf4j
@JobHandler(value = "freeViewTokenJobHandler")
public class FreeViewTokenJobHandler extends IJobHandler {

    @Autowired
    private FreeViewTokenFacade freeViewTokenFacade;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("定时刷新全视通token");
        freeViewTokenFacade.getToken();
        return ReturnT.SUCCESS;
    }
}
