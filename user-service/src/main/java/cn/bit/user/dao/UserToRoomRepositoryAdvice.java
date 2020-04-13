package cn.bit.user.dao;

import cn.bit.facade.model.user.UserToRoom;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

public interface UserToRoomRepositoryAdvice {

    int updateMultiByUserId(UserToRoom userToRoom, ObjectId userId);

    int updateMiliUIdById(UserToRoom userToRoom, Long newMiliUId);

    Page<UserToRoom> findPageByUserToRoom(UserToRoom toGet, Integer page, Integer size);
}
