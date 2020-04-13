package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Message;
import cn.bit.facade.vo.moment.ShieldingVO;
import org.bson.types.ObjectId;

public interface ShieldingFacade {

    /**
     * 管理员屏蔽言论
     * @param shieldingVO
     * @param operatorId
     * @return
     */
    Message shieldingSpeechByManager(ShieldingVO shieldingVO, ObjectId operatorId);

    /**
     * 系统自动屏蔽言论
     * @param shieldingVO
     * @return
     */
    Message shieldingSpeechBySystem(ShieldingVO shieldingVO);
}