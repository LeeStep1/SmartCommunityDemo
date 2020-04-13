package cn.bit.communityIoT.mq.consumer;

import cn.bit.facade.model.user.Card;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.communityIoT.ElevatorRecordFacade;
import cn.bit.facade.service.user.CardFacade;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorDetailDTO;
import cn.bit.facade.vo.communityIoT.elevator.ElevatorDetailQO;
import cn.bit.facade.vo.mq.CreateRecordRequest;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : xiaoxi.lao
 * @Description :
 * @Date ： 2019/9/12 16:31
 */
@Component
@Slf4j
public class ElevatorRecordConsumer extends DeviceRecordConsumer {
    @Autowired
    private ElevatorFacade elevatorFacade;

    @Autowired
    private CardFacade cardFacade;

    @Autowired
    private ElevatorRecordFacade elevatorRecordFacade;

    @Override
    protected void writeRecord(CreateRecordRequest createRecordRequest) {
        // 手机蓝牙与点对点的记录可以先抛弃
        if (createRecordRequest.getType() == 1) {
            log.info("手机蓝牙记录暂不上传");
            return;
        }

        ElevatorDetailQO elevatorDetailQO = new ElevatorDetailQO(createRecordRequest.getTerminalCode(),
                                                                 createRecordRequest.getMacId());
        // 获取电梯信息
        ElevatorDetailDTO elevatorDetail = elevatorFacade.getElevatorDetail(elevatorDetailQO);

        if (null == elevatorDetail || StringUtil.isBlank(elevatorDetail.getMacAddress())) {
            log.info("ignore this elevator : {}:{}",
                     createRecordRequest.getTerminalCode(),
                     createRecordRequest.getMacId());
            return;
        }

        log.info("can not find this card no: {}", createRecordRequest.getKeyNo());
        // 获取卡信息
        Card card = cardFacade.findByKeyNoAndCmId(createRecordRequest.getKeyNo(), elevatorDetail.getCommunityId());

        // 手机蓝牙的记录暂时不需要上传
        if (null == card) {
            return;
        }

        // 将卡信息和电梯信息与设备发送过来的记录组装一起保存到数据库
        elevatorRecordFacade.addRecordBy(createRecordRequest,
                                         card,
                                         elevatorDetail);
    }
}
