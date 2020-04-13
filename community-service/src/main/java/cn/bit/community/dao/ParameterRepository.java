package cn.bit.community.dao;

import cn.bit.facade.model.community.Parameter;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface ParameterRepository
        extends MongoDao<Parameter, ObjectId>, MongoRepository<Parameter, ObjectId> {
    Page<Parameter> findByTypeAndKeyAndValueAndDataStatus(Integer type, String key, String value, int dataStatus,
                                                          Pageable pageable);

    Parameter findByTypeAndKeyAndCommunityIdAndDataStatus(Integer type, String key, ObjectId communityId, int dataStatus);

    List<Parameter> findByTypeAndCommunityIdAndDataStatus(Integer type, ObjectId communityId, int dataStatus);

    Parameter findByTypeAndKeyAndDataStatus(Integer type, String key, int dataStatus);

    Page<Parameter> findPageByCommunityIdAndTypeAndDataStatus(ObjectId communityId, Integer type, int dataStatus,
                                                              Pageable pageable);

    List<Parameter> findByTypeAndCommunityIdAndDataStatusAndKeyNotOrderByOrderNum(Integer type, ObjectId communityId,
                                                                                  int dataStatus, String key);

    Parameter upsertWithSetOnInsertCommunityIdAndTypeAndKeyAndNameAndCreateAtAndDataStatusByCommunityIdAndTypeAndKeyAndDataStatus(
            Parameter toAdd, ObjectId communityId, Integer type, String key, int dataStatus);

    Parameter updateWithSetValueAndUpdateAtById(Parameter toUpdate, ObjectId id);

    List<Parameter> findByCommunityIdInAndTypeAndKeyInAndDataStatus(Set<ObjectId> communityIds, Integer type,
                                                                    List<String> keys, int dataStatus);

    Parameter updateByCommunityIdAndTypeAndKeyAndDataStatus(Parameter toUpdate, ObjectId communityId, Integer type,
                                                            String key, int dataStatus);

	Parameter updateById(Parameter toUpdate, ObjectId id);
}
