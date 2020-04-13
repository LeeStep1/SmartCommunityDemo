package cn.bit.property.dao.Impl;

import cn.bit.facade.model.property.Fault;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by fxiao
 * on 2018/3/7
 */
@Slf4j
public class FaultRepositoryImpl extends AbstractMongoDao<Fault, ObjectId> implements MongoDao<Fault, ObjectId>{

    @Autowired
    private MongoTemplate template;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return template;
    }

}
