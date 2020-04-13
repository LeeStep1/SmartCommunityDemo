package cn.bit.system.dao;

import cn.bit.facade.model.system.App;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppRepository extends MongoDao<App, ObjectId>, MongoRepository<App, ObjectId> {

	Page<App> findByDataStatus(int dataStatus, Pageable pageable);

	App updateById(App app, ObjectId id);
}
