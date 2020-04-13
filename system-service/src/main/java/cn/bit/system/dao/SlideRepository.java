package cn.bit.system.dao;

import cn.bit.facade.model.system.Slide;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SlideRepository extends MongoDao<Slide, ObjectId>, MongoRepository<Slide, ObjectId> {
	Slide updateById(Slide slide, ObjectId id);
}
