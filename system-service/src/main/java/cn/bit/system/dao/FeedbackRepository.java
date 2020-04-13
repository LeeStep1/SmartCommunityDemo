package cn.bit.system.dao;

import cn.bit.facade.model.system.Feedback;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedbackRepository extends MongoDao<Feedback, ObjectId>, MongoRepository<Feedback, ObjectId> {

    List<Feedback> findByAppIdOrderByCreateAtDesc(ObjectId appId);

	Page<Feedback> findByAppIdIgnoreNullAndDataStatus(ObjectId appId, int dataStatus, Pageable pageable);

	Feedback updateById(Feedback feedback, ObjectId id);
}
