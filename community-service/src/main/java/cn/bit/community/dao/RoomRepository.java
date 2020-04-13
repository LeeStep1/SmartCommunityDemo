package cn.bit.community.dao;

import cn.bit.facade.model.community.Room;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface RoomRepository extends MongoDao<Room, ObjectId>, MongoRepository<Room, ObjectId> {

    List<Room> findAllByIdInAndDataStatus(Collection<ObjectId> roomIds, Integer dataStatus);

    Room findByOutIdAndDataStatus(String outId, Integer dataStatus);

	List<Room> findByNameAndCodeAndBuildingIdAndDataStatusAllIgnoreNullOrderByCreateAtAsc(String name, String code, ObjectId buildingId, int dataStatus);

	Room upsertById(Room toUpdate, ObjectId id);

	Boolean existsByBuildingIdAndNameAndDataStatus(ObjectId buildingId, String name, int dataStatus);

	List<Room> findByBuildingIdInAndOutIdIsNotNull(List<ObjectId> collect);

	Room findByBuildingIdAndNameAndDataStatus(ObjectId buildingId, String name, int dataStatus);

	Room upsertWithUnsetIfNullFeesTemplateIdThenSetOnInsertCreateAtById(Room room, ObjectId id);
}
