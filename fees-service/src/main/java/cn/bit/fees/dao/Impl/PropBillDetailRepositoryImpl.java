package cn.bit.fees.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.fees.PropBillDetail;
import cn.bit.fees.dao.PropBillDetailRepositoryAdvice;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Slf4j
public class PropBillDetailRepositoryImpl extends AbstractMongoDao<PropBillDetail, String>
        implements MongoDao<PropBillDetail, String>, PropBillDetailRepositoryAdvice {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Map<String, Long> countTotalPriceById(ObjectId id) {
        Aggregation agg = newAggregation(
                Aggregation.match(Criteria.where("billId").is(id).and("dataStatus").is(DataStatusType.VALID.KEY)),
                Aggregation.group("billId").sum("subtotal").as("total").sum("totalAmount").as("totalAmount")
        );
        AggregationResults<MyResult> list = mongoTemplate.aggregate(agg,"FEES_PROP_BILL_DETAIL",MyResult.class);
        Map<String, Long> map = new HashMap();
        if(list == null || list.getUniqueMappedResult() == null){
            map.put("total", 0L);
            map.put("totalAmount", 0L);
            log.error("查询账单明细异常，没有找到相关子账单，账单总金额设置为0");
            return map;
//            throw new RuntimeException("查询账单明细异常");
        }
        map.put("total", list.getUniqueMappedResult().total);
        map.put("totalAmount", list.getUniqueMappedResult().totalAmount);
        return map;
    }

    private class MyResult{
        Long total;
        Long totalAmount;
    }

}
