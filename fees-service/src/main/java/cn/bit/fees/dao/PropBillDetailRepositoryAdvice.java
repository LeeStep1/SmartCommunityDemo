package cn.bit.fees.dao;

import org.bson.types.ObjectId;

import java.util.Map;

/**
 * Created by decai.liu
 *
 * @description
 * @create: 2018/3/19
 **/
public interface PropBillDetailRepositoryAdvice {
    Map<String, Long> countTotalPriceById(ObjectId id);
}
