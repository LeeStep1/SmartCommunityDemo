package cn.bit.business.dao.Impl;

import cn.bit.facade.model.business.Coupon;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by fxiao
 * on 2018/4/3
 */
@Slf4j
public class CouponRepositoryImpl extends AbstractMongoDao<Coupon, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
