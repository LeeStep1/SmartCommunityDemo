package cn.bit.facade.service.property;

import cn.bit.facade.model.property.Fault;
import cn.bit.facade.vo.property.FaultPageQuery;
import cn.bit.facade.vo.statistics.FaultResponse;
import cn.bit.facade.vo.statistics.StatisticsRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Created by fxiao
 * on 2018/3/7
 * 故障接口
 */
public interface FaultFacade {

    /**
     * 添加故障
     *
     * @param entity
     * @return
     */
    Fault addFault(Fault entity) throws BizException;

    /**
     * 修改故障信息（待接收状态才能修改）
     *
     * @param entity
     * @return
     */
    Fault editFault(Fault entity) throws BizException;

    /**
     * 删除故障信息（逻辑删除）
     *
     * @param id
     * @return
     */
    Fault hiddenById(ObjectId id) throws BizException;

    /**
     * 根据ID获取故障详细
     *
     * @param id
     * @return
     */
    Fault findOne(ObjectId id) throws BizException;

    /**
     * 故障分页
     *
     * @param entity
     * @param client
     * @return
     */
    @Deprecated
    Page<Fault> queryFaultPage(Fault entity, Integer client, int page, int size) throws BizException;

    /**
     * 获取故障列表
     *
     * @param entity
     * @return
     */
    List<Fault> getFaultList(Fault entity) throws BizException;

    // ============================================[数据统计]==================================================

    /**
     * 根据社区ID
     * 获取总数量
     *
     * @param communityId
     * @return
     */
    Long countFaultByCommunityId(ObjectId communityId) throws BizException;

    /**
     * 根据类型查询统计
     *
     * @param communityId
     * @param faultItem
     * @return
     */
    Long countFaultByItem(ObjectId communityId, Integer faultItem);

    /**
     * 根据星星查询
     *
     * @param communityId
     * @param star
     * @return
     */
    Long countFaultByEvaluationGrade(ObjectId communityId, Integer star);

    /**
     * 根据时间查询
     *
     * @param communityId
     * @return
     * @throws BizException
     */
    Long countFaultByTime(ObjectId communityId) throws BizException;

    /**
     * 获取社区下待处理的故障数量
     *
     * @param communityId
     * @param faultStatus
     * @return
     */
    Map<String, Long> countUnRepairedFault(ObjectId communityId, Integer faultStatus);

    /**
     * 维修工:获取指派给自己的待检修故障数量
     *
     * @param communityId
     * @return
     */
    Map<String, Long> queryFaultCountByCommunityIdAndRepairId(ObjectId communityId, ObjectId repairId, Integer faultStatus);

    /**
     * 故障统计
     *
     * @param statisticsRequest
     * @return
     */
    FaultResponse getFaultStatistics(StatisticsRequest statisticsRequest);

    /**
     * 更新故障单
     *
     * @param entity
     * @param userId
     * @return
     */
    Fault updateFault(Fault entity, ObjectId userId);

    /**
     * 处理故障单
     *
     * @param entity
     * @param userVO
     * @return
     */
    Fault auditFault(Fault entity, UserVO userVO);

    /**
     * 评价故障单
     *
     * @param entity
     * @param uid
     * @return
     */
    Fault faultComment(Fault entity, ObjectId uid);

    /**
     * 统计当前故障工单
     *
     * @param request
     * @return
     */
    FaultResponse getFaultStatisticsForBigScreen(StatisticsRequest request);

    /**
     * 故障工单分页
     *
     * @param query
     * @return
     */
    Page<Fault> listFaults(FaultPageQuery query);
}
