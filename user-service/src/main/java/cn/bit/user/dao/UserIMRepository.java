package cn.bit.user.dao;

import cn.bit.facade.model.user.IMUser;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface UserIMRepository extends UserIMRepositoryAdvice,  MongoDao<IMUser, ObjectId>, MongoRepository<IMUser, ObjectId> {

    IMUser findOneByUserIdAndRoleAndDataStatus(ObjectId userId, String role, Integer dataStatus);

    List<IMUser> findIMByUserIdInAndRoleAndDataStatus(Set<ObjectId> userIds, String postCode, int dataStatus);
}
