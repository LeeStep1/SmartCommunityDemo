package cn.bit.fees.dao;

import cn.bit.facade.model.fees.Template;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TemplateRepository extends MongoDao<Template, ObjectId>, MongoRepository<Template, ObjectId> {

    <T> Page<T> findByCommunityIdAndNameRegexIgnoreNull(ObjectId communityId, String name, Pageable pageable, Class<T> tClass);

    void updateById(Template toUpdate, ObjectId id);

    void deleteById(ObjectId id);

    List<Template> findByCommunityIdAndNameRegexIgnoreNull(ObjectId communityId, String name);

    boolean existsByCommunityIdAndId(ObjectId communityId, ObjectId id);
}
