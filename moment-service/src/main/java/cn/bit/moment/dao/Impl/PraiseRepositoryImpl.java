package cn.bit.moment.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.moment.Praise;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.moment.dao.PraiseRepositoryAdvice;
import com.mongodb.WriteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static cn.bit.facade.exception.moment.MomentException.DATA_SORT_IS_NULL;
import static cn.bit.facade.exception.moment.MomentException.MOMENT_ID_IS_NULL;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.user.UserBizException.USER_ID_NULL;

@Slf4j
public class PraiseRepositoryImpl
        extends AbstractMongoDao<Praise, ObjectId> implements MongoDao<Praise, ObjectId>, PraiseRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public List<Praise> incrementalPraiseList(IncrementalRequest incrementalRequest) {
        if (incrementalRequest.getMomentId() == null) {
            throw MOMENT_ID_IS_NULL;
        }
        if (incrementalRequest.getSort() == null) {
            throw DATA_SORT_IS_NULL;
        }
        Praise toQuery = new Praise();
        toQuery.setMomentId(incrementalRequest.getMomentId());
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        Criteria criteria = null;
        int page = 1;
        int size = 10;
        XSort xSort = null;
        if (incrementalRequest.getSort() == 1) {
            xSort = XSort.asc("createAt");
        } else {
            xSort = XSort.desc("createAt");
        }

        if (incrementalRequest.getSize() != 0) {
            size = incrementalRequest.getSize();
        }
        if (incrementalRequest.getStartAt() != null) {
            // 数据排序（1：升序（获取新的，时间从小到大），0：降序（获取旧的，时间从大到小））
            if (incrementalRequest.getSort() == 1) {
                criteria = Criteria.where("createAt").gt(incrementalRequest.getStartAt());
            } else {
                criteria = Criteria.where("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        Query query = buildExample(toQuery);
        if (criteria != null) {
            query.addCriteria(criteria);
        }
        Page<Praise> praisePage = this.findPage(query, page, size, xSort);
        if (praisePage == null) {
            return Collections.emptyList();
        }
        return praisePage.getRecords();
    }

    @Override
    public int updateMultiByMomentIdAndCreatorId(Praise toUpdate, ObjectId momentId, ObjectId creatorId) {
        return updateMultiBy(toUpdate, momentId, creatorId, DataStatusType.VALID.KEY);
    }

    @Override
    public Integer insertPraiseWithChecked(Praise praise) {
        Praise toQuery = new Praise();
        toQuery.setCreatorId(praise.getCreatorId());
        toQuery.setMomentId(praise.getMomentId());
        Query query = buildExample(toQuery);
        Praise toGet = findOne(query);
        // 已经点过赞了
        if (toGet != null && toGet.getDataStatus() == DataStatusType.VALID.KEY) {
            log.info("重复点赞 ...");
            return 0;
        }
        Date createAt = praise.getCreateAt();
        praise.setCreateAt(null);
        Update update = getUpdateObj(praise);
        // 插入的时候才会写入createAt,保证拿到的第一次点赞的时间
        update.setOnInsert("createAt", createAt);
        try {
            WriteResult writeResult = mongoTemplate.upsert(query, update, Praise.class);
            return writeResult.isUpdateOfExisting() ? 2 : 1;
        } catch (Exception e) {
            log.error("mongoTemplate.upsert Exception", e);
            return null;
        }
    }

    @Override
    public List<Praise> incrementalMyPraiseList(IncrementalRequest incrementalRequest, ObjectId communityId, ObjectId uid) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (uid == null) {
            throw USER_ID_NULL;
        }
        Praise toQuery = new Praise();
        toQuery.setCommunityId(communityId);
        toQuery.setCreatorId(uid);
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        Criteria criteria = null;
        int page = 1;
        int size = 10;
        XSort xSort = null;
        if (incrementalRequest.getSort() == 1) {
            xSort = XSort.asc("createAt");
        } else {
            xSort = XSort.desc("createAt");
        }

        if (incrementalRequest.getSize() != 0) {
            size = incrementalRequest.getSize();
        }
        if (incrementalRequest.getStartAt() != null) {
            // 数据排序（1：升序（获取新的，时间从小到大），0：降序（获取旧的，时间从大到小））
            if (incrementalRequest.getSort() == 1) {
                criteria = Criteria.where("createAt").gt(incrementalRequest.getStartAt());
            } else {
                criteria = Criteria.where("createAt").lt(incrementalRequest.getStartAt());
            }
        }
        Query query = buildExample(toQuery);
        if (criteria != null) {
            query.addCriteria(criteria);
        }
        Page<Praise> praisePage = this.findPage(query, page, size, xSort);
        if (praisePage == null) {
            return Collections.emptyList();
        }
        return praisePage.getRecords();
    }

    @Override
    public Long statisticsPraise(ObjectId communityId, ObjectId creatorId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        if (creatorId == null) {
            throw USER_ID_NULL;
        }
        Praise toQuery = new Praise();
        toQuery.setCommunityId(communityId);
        toQuery.setCreatorId(creatorId);
        toQuery.setDataStatus(DataStatusType.VALID.KEY);
        Query query = buildExample(toQuery);
        return mongoTemplate.count(query, Praise.class);
    }
}
