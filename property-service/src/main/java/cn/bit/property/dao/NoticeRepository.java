package cn.bit.property.dao;

import cn.bit.facade.model.property.Notice;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface NoticeRepository extends MongoDao<Notice, ObjectId>, MongoRepository<Notice, ObjectId> {

	Notice updateById(Notice toUpdate, ObjectId id);

	Page<Notice> findByCommunityIdAndCreateAtGreaterThanEqualIgnoreNullAndCreateAtLessThanIgnoreNullAndDataStatus(
			ObjectId communityId, Date startAt, Date endAt, int dataStatus, Pageable pageable);

    Notice findByIdAndDataStatus(ObjectId id, int dataStatus);
}
