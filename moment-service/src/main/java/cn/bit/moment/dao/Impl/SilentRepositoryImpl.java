package cn.bit.moment.dao.Impl;

import cn.bit.facade.enums.SilentStatusType;
import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Silent;
import cn.bit.facade.vo.moment.SilentRequest;
import cn.bit.facade.vo.moment.SilentVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.framework.utils.DateUtils;
import cn.bit.moment.dao.SilentRepositoryAdvice;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.moment.MomentException.*;

@Slf4j
public class SilentRepositoryImpl
        extends AbstractMongoDao<Silent, ObjectId> implements MongoDao<Silent, ObjectId>, SilentRepositoryAdvice {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Silent upsertSilent(SilentVO silentVO, ObjectId communityId, ObjectId operatorId) {
        if (silentVO.getSilentUserId() == null) {
            throw SILENT_USER_ID_IS_NULL;
        }
        if (silentVO.getSilentMinutes() == null || silentVO.getSilentMinutes() < 1) {
            throw SILENT_MINUTES_IS_NULL;
        }
        Silent toQuery = new Silent();
        toQuery.setCommunityId(communityId);
        toQuery.setSilentUserId(silentVO.getSilentUserId());
        Query query = buildExample(toQuery);
        Silent toGet = mongoTemplate.findOne(query, Silent.class);
        Date currDate = new Date();
        Date currSilentEndAt = currDate;
        if (toGet != null && toGet.getSilentEndAt() != null && toGet.getSilentEndAt().after(currDate)) {
            log.info("历史禁言的次数：" + toGet.getSilentTimes());
            currSilentEndAt = toGet.getSilentEndAt();
        }
        Silent silent = new Silent();
        BeanUtils.copyProperties(silentVO, silent);
        Date silentEndAt = DateUtils.addSecond(currSilentEndAt, silent.getSilentMinutes().intValue() * 60);
        // 禁言失效时间
        silent.setSilentEndAt(silentEndAt);
        silent.setCommunityId(communityId);
        silent.setCreatorId(operatorId);
        silent.setUpdateAt(currDate);

        Update update = getUpdateObj(silent);
        // 插入的时候才会更新
        update.setOnInsert("createAt", currDate);
        // 禁言次数加1
        update.inc("silentTimes", 1);
        // 禁言累计时间
        update.inc("totalSilentMinutes", silent.getSilentMinutes());
        // 未处理的累加至已处理
        if (toGet != null
                && toGet.getNewShieldingMomentIds() != null
                && !toGet.getNewShieldingMomentIds().isEmpty()) {

            update.addToSet("shieldingMomentIds").each(toGet.getNewShieldingMomentIds().toArray());
            // 清空当前的屏蔽动态id集合
            update.pullAll("newShieldingMomentIds", toGet.getNewShieldingMomentIds().toArray());
        }
        if (toGet != null
                && toGet.getNewShieldingCommentIds() != null
                && !toGet.getNewShieldingCommentIds().isEmpty()) {

            update.addToSet("shieldingCommentIds").each(toGet.getNewShieldingCommentIds().toArray());
            // 清空当前的屏蔽评论id集合
            update.pullAll("newShieldingCommentIds", toGet.getNewShieldingCommentIds().toArray());
        }

        return mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().upsert(Boolean.TRUE).returnNew(Boolean.TRUE), Silent.class);
    }

    @Override
    public Silent relieveSilentUser(ObjectId id) {
        if (id == null) {
            throw SILENT_ID_IS_NULL;
        }
        Silent silent = findById(id);
        if (silent == null) {
            throw DATA_NOT_EXIST;
        }
        // 已经解除禁言
        if (silent.getSilentEndAt().before(new Date())) {
            log.info("该禁言已经失效了，无须重复解除禁言");
            return silent;
        }
        Silent toQuery = new Silent();
        toQuery.setId(id);
        Query query = buildExample(toQuery);
        Silent toUpdate = new Silent();
        toUpdate.setUpdateAt(new Date());
        Update update = getUpdateObj(toUpdate);
        Long diff = DateUtils.getDateDiffForMinutes(silent.getSilentEndAt(), new Date());
        // 提前解除禁言,计算实际禁言时长
        if (diff > 0) {
            log.info("管理员提前了 " + diff + " 分钟为用户解除禁言");
            update.set("silentEndAt", new Date());
            update.inc("totalSilentMinutes", -diff);
        }
        return mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(Boolean.TRUE), Silent.class);
    }

    @Override
    public Page<Silent> findPageBySilentRequest(SilentRequest silentRequest, int page, int size) {
        if (silentRequest == null || silentRequest.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }
        Criteria criteria = null;
        if (silentRequest.getStatus() != null) {
            // 查询被禁言列表
            if (silentRequest.getStatus() == SilentStatusType.SILENT.getKey()) {
                criteria = Criteria.where("silentEndAt").gt(new Date());
            }
            // 查询已解除禁言列表
            if (silentRequest.getStatus() == SilentStatusType.RELIEVE.getKey()) {
                criteria = new Criteria().orOperator(
                        Criteria.where("silentEndAt").exists(Boolean.FALSE), Criteria.where("silentEndAt").lte(new Date()));
            }
            silentRequest.setStatus(null);
        }
        Query query = buildExample(silentRequest);
        if (criteria != null) {
            query.addCriteria(criteria);
        }
        return findPage(query, page, size, XSort.desc("createAt"));
    }

    @Override
    public Silent upsertSilentByMomentAndOperatorId(Moment moment, ObjectId operatorId) {
        Silent toQuery = new Silent();
        toQuery.setCommunityId(moment.getCommunityId());
        toQuery.setSilentUserId(moment.getCreatorId());
        Query query = buildExample(toQuery);
        Date currDate = new Date();
        Silent toUpdate = new Silent();
        toUpdate.setUpdateAt(currDate);
        toUpdate.setCreatorId(operatorId);
        Update update = getUpdateObj(toUpdate);
        update.setOnInsert("communityId", moment.getCommunityId());
        update.setOnInsert("silentUserId", moment.getCreatorId());
        update.setOnInsert("createAt", currDate);
        update.addToSet("newShieldingMomentIds").each(moment.getId());
        return mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().upsert(Boolean.TRUE).returnNew(Boolean.TRUE), Silent.class);
    }

    @Override
    public Silent upsertSilentByCommentAndOperatorId(Comment comment, ObjectId operatorId) {
        Silent toQuery = new Silent();
        toQuery.setCommunityId(comment.getCommunityId());
        toQuery.setSilentUserId(comment.getCreatorId());
        Query query = buildExample(toQuery);
        Date currDate = new Date();
        Silent toUpdate = new Silent();
        toUpdate.setUpdateAt(currDate);
        toUpdate.setCreatorId(operatorId);
        Update update = getUpdateObj(toUpdate);
        update.setOnInsert("communityId", comment.getCommunityId());
        update.setOnInsert("silentUserId", comment.getCreatorId());
        update.setOnInsert("createAt", currDate);
        update.addToSet("newShieldingCommentIds").each(comment.getId());
        return mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().upsert(Boolean.TRUE).returnNew(Boolean.TRUE), Silent.class);
    }
}
