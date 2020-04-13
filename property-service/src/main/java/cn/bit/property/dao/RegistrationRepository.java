package cn.bit.property.dao;

import cn.bit.facade.model.property.Registration;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface RegistrationRepository extends MongoDao<Registration, ObjectId>,
		MongoRepository<Registration, ObjectId> {

	List<Registration> findByPartnerAndPhoneAndDataStatus(Integer partner, String phone, int dataStatus);

	void updateByIdIn(Registration toDelete, Set<ObjectId> ids);

	Registration upsertWithAddToSetRolesThenSetOnInsertCreateAtByCommunityIdAndPhoneAndDataStatus(
			Registration registration, ObjectId communityId, String phone, int dataStatus);
}
