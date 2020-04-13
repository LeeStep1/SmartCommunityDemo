package cn.bit.system.dao;

import cn.bit.facade.model.system.Version;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VersionRepository extends MongoDao<Version, ObjectId>, MongoRepository<Version, ObjectId> {


    List<Version> findByAppIdAndDataStatusOrderBySequenceDesc(ObjectId appId, Integer dataStatus);

    List<Version> findAllByAppIdAndPublishedOrderBySequenceDesc(ObjectId appId, Boolean published);

    Version findTop1ByAppIdAndPublishedAndHasErrorAndSequenceGreaterThanOrderByCreateAtDesc(ObjectId appId, Boolean published,
                                                                                         Boolean hasError, String sequence);

    Version findTop1ByAppIdAndPublishedAndHasErrorAndSequenceGreaterThanEqualOrderByCreateAtDesc(ObjectId appId, Boolean published,
                                                                                         Boolean hasError, String sequence);

	Page<Version> findByAppIdAndDataStatus(ObjectId appId, int dataStatus, Pageable pageable);

    Page<Version> findByAppIdAndPublished(ObjectId appId, boolean b, Pageable pageable);

	Version updateById(Version version, ObjectId id);
}
