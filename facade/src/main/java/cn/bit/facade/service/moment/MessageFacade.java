package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Message;
import cn.bit.facade.vo.IncrementalRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface MessageFacade {

    /**
     * 增量查询消息列表
     * @param incrementalRequest
     * @param communityId
     * @param uid
     * @return
     */
    List<Message> findByIncrementalRequest(IncrementalRequest incrementalRequest, Integer partner, ObjectId communityId, ObjectId uid);
}