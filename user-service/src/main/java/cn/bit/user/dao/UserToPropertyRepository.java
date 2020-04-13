package cn.bit.user.dao;

import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserToPropertyRepository extends MongoDao<UserToProperty, ObjectId>,
        MongoRepository<UserToProperty, ObjectId> {
}