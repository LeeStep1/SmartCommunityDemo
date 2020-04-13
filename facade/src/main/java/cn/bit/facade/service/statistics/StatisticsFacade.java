package cn.bit.facade.service.statistics;

import cn.bit.facade.model.statistics.Statistics;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/3/26
 */
public interface StatisticsFacade {

    /**
     * 新增统计
     * @param statistics
     * @return
     */
    Statistics addStatistics(Statistics statistics) throws BizException;

    /**
     * 批量添加
     * @param statistics
     * @return
     */
    void addStatisticsS(List<Statistics> statistics) throws BizException;

    /**
     * 修改统计
     * @param statistics
     * @return
     */
    Statistics updateStatistics(Statistics statistics) throws BizException;

    /**
     * 获取列表
     * @param statistics
     * @return
     */
    List<Statistics> getList(Statistics statistics) throws BizException;

    /**
     * 根据社区条件查询
     * @param statistics
     * @return
     */
    Statistics findOne(Statistics statistics) throws BizException;

    /**
     * 判断是否存在数据
     * @param
     * @return
     */
    boolean exist(ObjectId communityId, String dataTime, Integer statisticsType) throws BizException;
}
