package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Comment;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface CommentRepository
        extends MongoDao<Comment, ObjectId>, MongoRepository<Comment, ObjectId>, CommentRepositoryAdvice {

    Page<Comment> findByIdIn(Set<ObjectId> ids, Pageable pageable);

    Page<Comment> findByMomentIdAndStatusAndDataStatusAllIgnoreNull(ObjectId momentId, int status,
                                                                    int dataStatus, Pageable pageable);

    Page<Comment> findByCommunityIdAndStatusAndReportNumGreaterThanEqualAndDataStatusAllIgnoreNull(
            ObjectId communityId, Integer status, Integer reportNum, int dataStatus, Pageable pageable);
}
