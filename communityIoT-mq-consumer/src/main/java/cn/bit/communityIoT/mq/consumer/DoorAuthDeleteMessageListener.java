package cn.bit.communityIoT.mq.consumer;

import cn.bit.communityIoT.support.processor.DoorAuthProcessor;
import cn.bit.facade.vo.mq.DoorAuthVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DoorAuthDeleteMessageListener extends AbstractDoorAuthMessageListener {
    @Autowired
    private List<DoorAuthProcessor> processors;

    @Override
    protected void execute(DoorAuthVO doorAuthVO) throws Exception {
        for (DoorAuthProcessor processor : processors) {
            log.info("门禁权限删除");
            processor.delete(doorAuthVO);
        }
    }
}
