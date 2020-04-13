package cn.bit.system.dao;

import cn.bit.facade.model.system.Resource;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ResourceRepository extends MongoDao<Resource, ObjectId>, MongoRepository<Resource, ObjectId> {

    Resource findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    Resource findByUriSignAndTypeAndDataStatus(String uriSign, Integer type, Integer dataStatus);

    List<Resource> findByIdInAndClientsAndVisibilityNotAndDataStatus(Collection<ObjectId> ids, Integer client,
                                                                     Integer visibility, Integer dataStatus);

    List<Resource> findByGroupIdExistsAndTypeAndDataStatus(boolean exists, Integer type, Integer dataStatus);

    List<Resource> findByGroupIdExistsAndTypeAndClientsInAndDataStatus(boolean exists, Integer type,
                                                                       Collection<Integer> client, Integer dataStatus);

    Long upsertWithSetOnInsertCreateAtByUriSignAndTypeAndDataStatus(Resource resource, String uriSign, Integer type, int dataStatus);
}
