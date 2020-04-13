package cn.bit.statistics.service;

import cn.bit.facade.model.statistics.Statistics;
import cn.bit.facade.service.statistics.StatisticsFacade;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.statistics.dao.StatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;

/**
 * Created by fxiao
 * on 2018/3/26
 */
@Service("statisticsFacade")
@Slf4j
public class StatisticsFacadeImpl implements StatisticsFacade {

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Override
    public Statistics addStatistics(Statistics statistics) throws BizException {
        return statisticsRepository.insert(statistics);
    }

    @Override
    public void addStatisticsS(List<Statistics> statistics) throws BizException {
        statisticsRepository.insertAll(statistics);
    }

    @Override
    public Statistics updateStatistics(Statistics statistics) throws BizException {
        return statisticsRepository.updateOne(statistics, null);
    }

    @Override
    public List<Statistics> getList(Statistics statistics) throws BizException {
        return statisticsRepository.find(statistics, XSort.asc("createAt"));
    }

    @Override
    public Statistics findOne(Statistics statistics) throws BizException {
        if (statistics.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }
        return statisticsRepository.findOne(statistics);
    }

    @Override
    public boolean exist(ObjectId communityId, String dateTime, Integer statisticsType) throws BizException {
        return statisticsRepository.existsByCommunityIdAndDateTimeAndStatisticsType(communityId, dateTime, statisticsType);
    }
}
