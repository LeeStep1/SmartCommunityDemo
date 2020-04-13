package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Report;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository
        extends MongoDao<Report, ObjectId>, MongoRepository<Report, ObjectId> {

    List<Report> findByCreatorIdAndCommunityIdAndType(ObjectId currUserId, ObjectId communityId, Integer type);

    Report findBySpeechIdAndCreatorIdAndType(ObjectId speechId, ObjectId uid, int type);

    Page<Report> findByCommunityIdAndTypeAndSpeechId(ObjectId communityId, Integer type,
                                                     ObjectId speechId, Pageable pageable);
}
