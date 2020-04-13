package cn.bit.user.dao;

import cn.bit.facade.model.user.IMUser;
import org.bson.types.ObjectId;

public interface UserIMRepositoryAdvice {

    int pullAllByUserIdAndRole(IMUser imUser, ObjectId userId, String role);

    IMUser updateOneByUserIdAndRole(IMUser updateBean);
}
