package cn.bit.property.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.UseStatusType;
import cn.bit.facade.model.property.ReleasePass;
import cn.bit.facade.vo.property.ReleasePassRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.property.dao.ReleasePassRepositoryAdvice;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;

public class ReleasePassRepositoryImpl extends AbstractMongoDao<ReleasePass, ObjectId>
        implements MongoDao<ReleasePass, ObjectId>, ReleasePassRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Page getReleasePassPage(ReleasePassRequest request, int page, int size, XSort createAt) {
        Query query = getQueryByReleasePassRequest(request);
        Page _page = findPage(query, page, size, createAt);
        return _page;
    }

    public Query getQueryByReleasePassRequest(ReleasePassRequest request) {
        Integer releaseStatus = request.getReleaseStatus();
        Criteria criteria = Criteria.where("dataStatus").is(DataStatusType.VALID.KEY);
        criteria.and("communityId").is(request.getCommunityId());
        if(StringUtil.isNotBlank(request.getUserName())){
            criteria.and("userName").regex(request.getUserName());
            request.setUserName(null);
        }
        if (request.getUserId() == null) {
            if(releaseStatus == null) {
                return buildExample(request).addCriteria(criteria);
            }
            if (releaseStatus == UseStatusType.UNUSED.key) {
                criteria.and("endAt").gt(new Date());
            }
            if (releaseStatus == UseStatusType.EXPIRED.key) {
                criteria.and("endAt").lt(new Date());
                request.setReleaseStatus(UseStatusType.UNUSED.key);
            }
        } else {
            criteria.and("userId").is(request.getUserId());
            switch (UseStatusType.getByValue(request.getReleaseStatus())) {
                case USED:
                case EXPIRED:
                    Criteria criteria1 = new Criteria();
                    criteria1.and("releaseStatus").is(UseStatusType.USED.key);
                    Criteria criteria2 = new Criteria();
                    criteria2.and("releaseStatus").is(UseStatusType.UNUSED.key).and("endAt").lte(new Date());
                    criteria.orOperator(criteria1, criteria2);
                    break;
                case UNUSED:
                    criteria.and("releaseStatus").is(UseStatusType.UNUSED.key).and("endAt").gt(new Date());
                    break;
                default:break;
            }
            return new Query(criteria);
        }
        return buildExample(request).addCriteria(criteria);
    }
}
