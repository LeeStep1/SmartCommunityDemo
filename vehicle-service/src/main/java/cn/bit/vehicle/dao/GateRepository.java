package cn.bit.vehicle.dao;

import cn.bit.facade.model.vehicle.Gate;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface GateRepository extends MongoRepository<Gate, ObjectId> {
    List<Gate> findByCommunityIdAndDataStatus(ObjectId communityId, Integer dataStatus);

    Page<Gate> findByCommunityIdAndNameRegexIgnoreNullAndDataStatus(ObjectId communityId, String name,
                                                                    Integer dataStatus, Pageable pageable);

    List<Gate> findByCommunityIdAndNoInAndDataStatus(ObjectId communityId, Collection<String> gateNos, Integer dataStatus);
}
