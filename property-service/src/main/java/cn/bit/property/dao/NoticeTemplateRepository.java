package cn.bit.property.dao;

import cn.bit.facade.model.property.NoticeTemplate;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoticeTemplateRepository
		extends MongoDao<NoticeTemplate, ObjectId>, MongoRepository<NoticeTemplate, ObjectId> {

	NoticeTemplate updateByIdAndDataStatus(NoticeTemplate toUpdate, ObjectId id, int dataStatus);

	Page<NoticeTemplate> findByCommunityIdAndNameRegexAndTitleRegexAndDataStatusAllIgnoreNull(
            ObjectId communityId, String name, String title, Integer dataStatus, Pageable pageable);

	NoticeTemplate findByIdAndDataStatus(ObjectId id, int dataStatus);

	<T> List<T> findByCommunityIdAndDataStatus(ObjectId communityId, int dataStatus, Class<T> tClass);

    NoticeTemplate updateWithUnsetIfNullThumbnailUrlByIdAndDataStatus(NoticeTemplate toModify, ObjectId id, int dtaStatus);
}
