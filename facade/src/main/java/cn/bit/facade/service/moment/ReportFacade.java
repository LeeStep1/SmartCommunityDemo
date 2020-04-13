package cn.bit.facade.service.moment;

import cn.bit.facade.model.moment.Report;
import cn.bit.facade.vo.moment.ReportVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReportFacade {

    /**
     * 根据用户、社区、举报对象类型查询举报集合
     * @param currUserId
     * @param communityId
     * @param type
     * @return
     */
    List<Report> findByCreatorIdAndCommunityIdAndType(ObjectId currUserId, ObjectId communityId, Integer type);

    /**
     * 新增举报记录
     * @param reportVO
     * @param uid
     * @param maxReportNum
     * @return
     */
    Report addReport(ReportVO reportVO, ObjectId uid, int maxReportNum);

    /**
     * 被举报的言论查询举报列表
     *
     * @param communityId
     * @param reportVO
     * @param page
     * @param size
     * @return
     */
    Page<Report> queryPageByCommunityIdAndSpeechIdAndType(ObjectId communityId, ReportVO reportVO, int page, int size);
}