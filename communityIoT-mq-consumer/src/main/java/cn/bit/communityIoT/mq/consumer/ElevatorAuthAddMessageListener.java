package cn.bit.communityIoT.mq.consumer;

import cn.bit.communityIoT.support.processor.ElevatorAuthProcessor;
import cn.bit.facade.vo.mq.ElevatorAuthVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ElevatorAuthAddMessageListener extends AbstractElevatorAuthMessageListener {

    @Autowired
    private List<ElevatorAuthProcessor> processors;

    @Override
    protected void execute(ElevatorAuthVO elevatorAuthVO) throws Exception {
        for (ElevatorAuthProcessor processor : processors) {
            log.info("电梯权限增加");
            processor.add(elevatorAuthVO);
        }
    }
}
