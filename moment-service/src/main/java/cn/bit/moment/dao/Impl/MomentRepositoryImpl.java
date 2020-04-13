package cn.bit.moment.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.MomentStatusType;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.moment.dao.MomentRepositoryAdvice;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.bit.facade.exception.moment.MomentException.DATA_SORT_IS_NULL;

@Slf4j
public class MomentRepositoryImpl
        extends AbstractMongoDao<Moment, ObjectId> implements MongoDao<Moment, ObjectId>, MomentRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Moment updateNumByIdAndFieldName(ObjectId momentId, String fieldName, int num) {
        Moment toQuery = new Moment();
        toQuery.setId(momentId);
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        Query query = buildExample(toQuery);
        // 增加或减少 fieldName 的值
        Update update = new Update().inc(fieldName, num);
        return updateOne(query, update);
    }

    @Override
    public List<Moment> findByIncrementalRequestAndCommunityId(IncrementalRequest incrementalRequest,
                                                               ObjectId communityId) {
        Criteria criteria = Criteria.where("communityId").is(communityId).and("dataStatus").is(DataStatusType.VALID.KEY);
        criteria.and("status").in(
                Arrays.asList(MomentStatusType.REVIEWED.getKey(), MomentStatusType.AUTOREVIEWED.getKey()));
        int page = 1;
        int size = 10;
        if (incrementalRequest.getSize() != 0) {
            size = incrementalRequest.getSize();
        }
        XSort xSort = XSort.desc("createAt");
        if (incrementalRequest.getStartAt() != null) {
            // 数据排序（1：升序（获取新的，时间从小到大），0：降序（获取旧的，时间从大到小））
            if (incrementalRequest.getSort() == 1) {
                criteria.and("createAt").gt(incrementalRequest.getStartAt());
                xSort = XSort.asc("createAt");
            } else {
                criteria.and("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        if (incrementalRequest.getMomentType() != null) {
            criteria.and("type").is(incrementalRequest.getMomentType());
        }
        Query query = new Query();
        query.addCriteria(criteria);
        log.info("findByIncrementalRequestAndCommunityId query:" + query);
        // 用审核通过时间排序
        Page<Moment> momentPage = this.findPage(query, page, size, xSort);
        if (momentPage == null) {
            return Collections.emptyList();
        }
        return momentPage.getRecords();
    }

    @Override
    public List<Moment> findByIncrementalRequestAndCommunityIdAndCreatorId(IncrementalRequest incrementalRequest,
                                                                           ObjectId communityId, ObjectId currUserId) {
        if (incrementalRequest.getSort() == null) {
            throw DATA_SORT_IS_NULL;
        }
        Criteria criteria = Criteria.where("communityId").is(communityId).and("dataStatus").is(DataStatusType.VALID.KEY);
        criteria.and("creatorId").is(currUserId);
        // 用户不能查看已被屏蔽的动态
        criteria.and("status").nin(
                Arrays.asList(MomentStatusType.AUDOSHIELDING.getKey(), MomentStatusType.HANDSHIELDING.getKey()));
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
            if (incrementalRequest.getSort() == 1) {
                // 数据排序（1：升序（获取新的，时间从小到大），0：降序（获取旧的，时间从大到小））
                criteria.and("createAt").gt(incrementalRequest.getStartAt());
            } else {
                criteria.and("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        if (incrementalRequest.getMomentType() != null) {
            criteria.and("type").is(incrementalRequest.getMomentType());
        }
        Query query = new Query();
        query.addCriteria(criteria);
        Page<Moment> momentPage = this.findPage(query, page, size, xSort);
        if (momentPage == null) {
            return Collections.emptyList();
        }
        return momentPage.getRecords();
    }
}
