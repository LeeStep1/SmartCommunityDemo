package cn.bit.moment.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.moment.dao.MessageRepositoryAdvice;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

import static cn.bit.facade.exception.moment.MomentException.DATA_SORT_IS_NULL;

@Slf4j
public class MessageRepositoryImpl
        extends AbstractMongoDao<Message, ObjectId> implements MongoDao<Message, ObjectId>, MessageRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public List<Message> findByIncrementalRequestAndCommunityIdAndUserId(IncrementalRequest incrementalRequest,
                                                                         ObjectId communityId, ObjectId uid) {
        if (incrementalRequest.getSort() == null) {
            throw DATA_SORT_IS_NULL;
        }
        Criteria criteria = Criteria.where("communityId").is(communityId)
                .and("dataStatus").is(DataStatusType.VALID.KEY).and("noticeTo").is(uid);
        Query query = new Query();
        query.addCriteria(criteria);
        int page = 1;
        int size = 10;
        if (incrementalRequest.getSize() != 0) {
            size = incrementalRequest.getSize();
        }
        XSort xSort = null;
        if (incrementalRequest.getSort() == 1) {
            xSort = XSort.asc("createAt");
        } else {
            xSort = XSort.desc("createAt");
        }
        if (incrementalRequest.getStartAt() != null) {
            // 排序方式（1：升序，0：降序）
            if (incrementalRequest.getSort() == 1) {
                criteria.and("createAt").gt(incrementalRequest.getStartAt());
            } else {
                criteria.and("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        Page<Message> messagePage = this.findPage(query, page, size, xSort);
        if (messagePage == null) {
            return Collections.emptyList();
        }
        return messagePage.getRecords();
    }
}
