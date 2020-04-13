package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Message;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface MessageRepository
        extends MessageRepositoryAdvice, MongoDao<Message, ObjectId>, MongoRepository<Message, ObjectId> {

    Message findByMomentIdAndCreatorIdAndCreateAtAndDataStatus(ObjectId momentId, ObjectId creatorId,
                                                               Date createAt, int dataStatus);
}
