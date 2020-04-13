package cn.bit.facade.service.fees;

import cn.bit.facade.model.fees.PropBillDetail;
import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.facade.vo.fees.BillDetailVO;
import cn.bit.facade.vo.fees.BillRequest;
import cn.bit.facade.vo.fees.PublishBillRequest;
import cn.bit.facade.vo.statistics.ExpirePropertyBillResponse;
import cn.bit.facade.vo.statistics.PropertyBillSummaryRequest;
import cn.bit.facade.vo.statistics.PropertyBillSummaryResponse;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PropertyFeesFacade
{
    /**
     * 生成物业账单 定时每月1日运行
     * @return
     * @throws BizException
     */
//    void generatePropBill() throws BizException;

    /**
     * 查询物业账单
     * @param propertyBill
     * @return
     * @throws BizException
     */
    Page<PropertyBill> findPropBillByEntity(PropertyBill propertyBill, Integer page, Integer size) throws BizException;

    /**
     * 根据账单查询物业账单明细
     * @param billId
     * @return
     */
    List<PropBillDetail> findPropBillDetailByBillId(ObjectId billId);

    /**
     * 多条件查物业账单
     * @param entity
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    Page<BillDetailVO> queryList(BillRequest entity, Integer page, Integer size) throws BizException;

    /**
     * 更新账单状态
     * @param id
     * @param operatorId
     * @return
     */
    boolean updateBillStatusById(ObjectId id, ObjectId operatorId, Integer billStatus);

    /**
     * 获取有效可交易的物业账单
     *
     * @param id
     * @param totalAmount
     * @return
     */
    PropertyBill getValidPropertyBill(ObjectId id, Long totalAmount);

    /**
     * 根据账单id更新交易流水id
     * @param id
     * @param tradeId
     */
    void updateTradeIdById(ObjectId id, Long tradeId);

    /**
     * 完成支付
     * @param tradeId
     * @return
     */
    void finishedPaymentByTradeId(Long tradeId);

    /**
     * 根据tradeId查找订单清除tradeId
     * @param tradeId
     */
    void clearTradeIdByTradeId(Long tradeId);

    /**
     * 统计
     * @param id
     */
    void updateTotalPriceById(ObjectId id);

    PropertyBill findById(ObjectId id);

    /**
     * 创建一个详细子账单
     * @param propBillDetail
     * @return
     */
    PropBillDetail addBillDetail(PropBillDetail propBillDetail);

    /**
     * 一键发布
     *
     * @param propertyBills
     * @param communityId
     * @param uid
     * @return
     */
    boolean publishAllBills(List<PropertyBill> propertyBills, ObjectId communityId, ObjectId uid);

    /**
     * 查询物业账单，不包含详细
     * @param billRequest
     * @param page
     * @param size
     * @return
     */
    Page<PropertyBill> findPropBillByBillRequest(BillRequest billRequest, Integer page, Integer size);

    /**
     * 物业手动缴费
     * @param id
     * @param operatorId
     * @return
     */
    boolean paymentBillByProperty(ObjectId id, ObjectId operatorId);

    /**
     * 统计物业费的账单数量
     * @param communityId
     * @param buildingId
     * @return
     */
    Map<String,Long> countOverdueBills(ObjectId communityId, ObjectId buildingId);
    /**
     * 根据社区查询超期未缴费的账单
     * @param communityId
     * @return
     */
    List<PropertyBill> findOverdueBillsByCommunityId(ObjectId communityId);

    /**
     * 根据楼栋ID统计物业费的账单数量
     * @param buildingId
     * @param billStatus
     * @param billDate
     * @return
     */
    Long countBillNumByBuildingIdAndBillStatusAndDate(ObjectId buildingId, Integer billStatus, Date billDate);

    /**
     * 查询未发布账单
     * @param publishBillRequest
     * @param communityId
     * @return
     */
    List<PropertyBill> findByPublishBillRequest(PublishBillRequest publishBillRequest, ObjectId communityId);

    /**
     * 根据子账单id获取详细
     * @param id
     * @return
     */
    PropBillDetail findPropBillDetailById(ObjectId id);

    /**
     * 修改子账单详细
     * @param propBillDetail
     * @return
     */
    PropBillDetail updateOnePropBillDetail(PropBillDetail propBillDetail);

    /**
     * 更新账单详细
     *
     * @param uid
     * @param billId
     * @param propBillDetails
     * @return
     */
    void updateBillDetailsByBillId(ObjectId uid, ObjectId billId, List<PropBillDetail> propBillDetails);

    /**
     * 物业账单汇总
     *
     * @param propertyBillSummaryRequest
     * @return
     */
    PropertyBillSummaryResponse getPropertyBillSummary(PropertyBillSummaryRequest propertyBillSummaryRequest);

    /**
     * 超期物业账单统计
     *
     * @param communityId
     * @return
     */
    ExpirePropertyBillResponse getExpirePropertyBillStatistics(ObjectId communityId);

    /**
     * 根据楼栋id查询账单列表
     * @param buildingIds
     * @param billStatus
     * @param billDate
     * @return
     */
    List<PropertyBill> findByBuildingIdInAndBillStatusAndDate(Set<ObjectId> buildingIds, Integer billStatus, Date billDate);

    /**
     * 批量插入物业账单
     * @param insertPropertyBills
     */
    void insertAllBills(List<PropertyBill> insertPropertyBills);

    /**
     * 批量插入子账单
     * @param insertBillDetails
     */
    void insertAllBillDetails(List<PropBillDetail> insertBillDetails);

    /**
     * 查询账单详情
     * @param id
     * @return
     */
	BillDetailVO findByIdWithDetail(ObjectId id);
}
