package cn.bit.property.dao;

import cn.bit.facade.vo.property.Property;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PropertyRepository extends MongoDao<Property, ObjectId>, MongoRepository<Property, ObjectId> {
}
