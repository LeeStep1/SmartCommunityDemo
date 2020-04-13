package cn.bit.system.dao;

import cn.bit.facade.model.system.Client;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ClientRepository extends MongoDao<Client, ObjectId>, MongoRepository<Client, ObjectId> {

    Client findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    List<Client> findByTypeInAndPartnerAndDataStatus(Collection<Integer> types, Integer partner, Integer dataStatus);

	Page<Client> findByDataStatus(int dataStatus, Pageable pageable);
}
