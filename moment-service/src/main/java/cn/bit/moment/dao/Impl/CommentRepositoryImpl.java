package cn.bit.moment.dao.Impl;

import cn.bit.facade.enums.CommentStatusType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.moment.dao.CommentRepositoryAdvice;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.List;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.moment.MomentException.DATA_SORT_IS_NULL;
import static cn.bit.facade.exception.moment.MomentException.MOMENT_ID_IS_NULL;
import static cn.bit.facade.exception.user.UserBizException.USER_ID_NULL;

@Slf4j
public class CommentRepositoryImpl
        extends AbstractMongoDao<Comment, ObjectId> implements MongoDao<Comment, ObjectId>, CommentRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Comment updateNumByIdAndFieldName(ObjectId commentId, String fieldName, int num) {
        Comment toQuery = new Comment();
        toQuery.setId(commentId);
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        Query query = buildExample(toQuery);
        // 增加或减少 fieldName 的值
        Update update = new Update().inc(fieldName, num);
        return updateOne(query, update);
    }

    @Override
    public List<Comment> incrementalCommentList(IncrementalRequest incrementalRequest) {
        if (incrementalRequest.getMomentId() == null) {
            throw MOMENT_ID_IS_NULL;
        }
        if (incrementalRequest.getSort() == null) {
            throw DATA_SORT_IS_NULL;
        }
        Criteria criteria = Criteria.where("momentId").is(incrementalRequest.getMomentId())
                .and("dataStatus").is(DataStatusType.VALID.KEY);
        criteria.and("status").is(CommentStatusType.NORMAL.getKey());
        int page = 1;
        int size = 10;
        Query query = new Query();
        query.addCriteria(criteria);
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
            // 数据排序（1：升序（时间从小到大），0：降序（时间从大到小））
            if (incrementalRequest.getSort() == 1) {
                criteria.and("createAt").gt(incrementalRequest.getStartAt());
            } else {
                criteria.and("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        Page<Comment> commentPage = this.findPage(query, page, size, xSort);
        if (commentPage == null) {
            return Collections.emptyList();
        }
        return commentPage.getRecords();
    }

    @Override
    public List<Comment> incrementalMyCommentList(IncrementalRequest incrementalRequest,
                                                  ObjectId communityId, ObjectId uid) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (uid == null) {
            throw USER_ID_NULL;
        }
        Criteria criteria = Criteria.where("communityId").is(communityId);
        criteria.and("creatorId").is(uid).and("dataStatus").is(DataStatusType.VALID.KEY);
        criteria.and("status").is(CommentStatusType.NORMAL.getKey());
        int page = 1;
        int size = 10;
        Query query = new Query();
        query.addCriteria(criteria);
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
            // 数据排序（1：升序（时间从小到大），0：降序（时间从大到小））
            if (incrementalRequest.getSort() == 1) {
                criteria.and("createAt").gt(incrementalRequest.getStartAt());
            } else {
                criteria.and("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        Page<Comment> commentPage = this.findPage(query, page, size, xSort);
        if (commentPage == null) {
            return Collections.emptyList();
        }
        return commentPage.getRecords();
    }

    @Override
    public Long statisticsComment(ObjectId communityId, ObjectId creatorId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (creatorId == null) {
            throw USER_ID_NULL;
        }
        Comment toQuery = new Comment();
        toQuery.setCommunityId(communityId);
        toQuery.setCreatorId(creatorId);
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        toQuery.setStatus(CommentStatusType.NORMAL.getKey());
        Query query = buildExample(toQuery);
        return mongoTemplate.count(query, Comment.class);
    }
}
