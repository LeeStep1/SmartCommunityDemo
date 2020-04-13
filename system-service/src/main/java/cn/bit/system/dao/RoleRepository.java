package cn.bit.system.dao;

import cn.bit.facade.model.system.Role;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoDao<Role, ObjectId>, MongoRepository<Role, ObjectId> {
}
