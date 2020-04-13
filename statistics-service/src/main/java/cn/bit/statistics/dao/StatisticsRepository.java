package cn.bit.statistics.dao;

import cn.bit.facade.model.statistics.Statistics;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by fxiao
 * on 2018/3/26
 */
public interface StatisticsRepository extends MongoDao<Statistics, ObjectId>, MongoRepository<Statistics, ObjectId> {
	boolean existsByCommunityIdAndDateTimeAndStatisticsType(ObjectId communityId, String dateTime, Integer statisticsType);
}
