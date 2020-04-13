package cn.bit.fees.service;

import cn.bit.facade.enums.*;
import cn.bit.facade.model.community.Parameter;
import cn.bit.facade.model.fees.PropBillDetail;
import cn.bit.facade.model.fees.PropFeeItem;
import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.facade.service.community.ParameterFacade;
import cn.bit.facade.service.fees.PropertyFeesFacade;
import cn.bit.facade.vo.fees.BillDetailVO;
import cn.bit.facade.vo.fees.BillRequest;
import cn.bit.facade.vo.fees.PublishBillRequest;
import cn.bit.facade.vo.statistics.ExpirePropertyBillResponse;
import cn.bit.facade.vo.statistics.PropertyBillSummaryRequest;
import cn.bit.facade.vo.statistics.PropertyBillSummaryResponse;
import cn.bit.fees.dao.PropBillDetailRepository;
import cn.bit.fees.dao.PropFeeItemRepository;
import cn.bit.fees.dao.PropertyBillRepository;
import cn.bit.framework.constant.CacheConstant;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.number.AmountUtil;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.property.PropertyBizException.*;
import static cn.bit.facade.exception.user.UserBizException.*;

@Component("propertyFeesFacade")
@Slf4j
public class PropertyFeesFacadeImpl implements PropertyFeesFacade {

    @Autowired
    private PropFeeItemRepository propFeeItemRepository;

    @Autowired
    private PropertyBillRepository propertyBillRepository;

    @Autowired
    private PropBillDetailRepository propBillDetailRepository;

    @Autowired
    private ParameterFacade parameterFacade;

    @Autowired
    private EsTemplate esTemplate;

    @Value("${trade.test}")
    private Boolean test;

    @Value("${trade.test.amount}")
    private Long testAmount;

    private static final String INDEX_NAME = "cm_bill";

    private static final String TYPE_NAME = "property_bill";

    @Override
    public Page<PropertyBill> findPropBillByEntity(PropertyBill propertyBill, Integer page, Integer size)
            throws BizException{
        propertyBill.setDataStatus(DataStatusType.VALID.KEY);
        return propertyBillRepository.findPage(propertyBill, page, size, XSort.desc("makeAt"));
    }

    /**
     * 查询物业账单，不包含详细
     *
     * @param billRequest
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<PropertyBill> findPropBillByBillRequest(BillRequest billRequest, Integer page, Integer size) {
    	Set<Integer> billStatus = billRequest.getBillStatusSet();
    	Date overdueDate = null;
    	// 查询已超期
    	if(billStatus != null && billStatus.size() == 1 && billStatus.contains(BillStatusType.OVERDUE.getKey())){
    	    billStatus.remove(BillStatusType.OVERDUE.getKey());
    	    billStatus.add(BillStatusType.UNPAYMENT.getKey());
    	    overdueDate = new Date();
	    }
	    Date startDate = null;
	    Date endDate = null;
	    if(billRequest.getMakeBillAt() != null){
		    startDate = DateUtils.getFirstDateOfMonth(billRequest.getMakeBillAt());
		    endDate = DateUtils.getLastDateOfMonth(billRequest.getMakeBillAt());
	    }

	    Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "makeAt"));
	    org.springframework.data.domain.Page<PropertyBill> propertyBillPage = propertyBillRepository
			    .findByCommunityIdAndBuildingIdAndProprietorIdAndRoomLocationContainsAndBillStatusInAndOverdueDateLessThanAndMakeAtGreaterThanEqualAndMakeAtLessThanEqualAndDataStatusAllIgnoreNull(
			    		billRequest.getCommunityId(), billRequest.getBuildingId(), billRequest.getProprietorId(),
                        billRequest.getRoomNo(), billStatus, overdueDate, startDate, endDate, DataStatusType.VALID.KEY,
                        pageable);
	    propertyBillPage.getContent().forEach(this::checkOverdueBill);
        return PageUtils.getPage(propertyBillPage);
    }

    @Override
    public List<PropBillDetail> findPropBillDetailByBillId(ObjectId billId){
        if (billId == null) {
            throw BILL_ID_IS_NULL;
        }
        return propBillDetailRepository.findByBillIdAndDataStatus(billId, DataStatusType.VALID.KEY);
    }

    /**
     * 查询物业账单，包含详细
     *
     * @param billRequest
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<BillDetailVO> queryList(BillRequest billRequest, Integer page, Integer size) throws BizException {
        Page<PropertyBill> bill = this.findPropBillByBillRequest(billRequest, page, size);
        List<BillDetailVO> billDetailVOList = new ArrayList<>();
        List<PropBillDetail> details = propBillDetailRepository.findByBillIdInAndDataStatus(
                bill.getRecords().stream().map(PropertyBill::getId).collect(Collectors.toSet()), DataStatusType.VALID.KEY);
        bill.getRecords().forEach(propertyBill -> {
            BillDetailVO billDetailVO = new BillDetailVO();
            billDetailVO.setPropertyBill(propertyBill);
            billDetailVO.setBillDetailList(details.stream().filter(propBillDetail ->
                    propBillDetail.getBillId().equals(propertyBill.getId())).collect(Collectors.toList()));
            billDetailVOList.add(billDetailVO);
        });
        return new Page<>(bill.getCurrentPage(), bill.getTotal(), size, billDetailVOList);
    }

    /**
     * 更新账单状态
     *
     * @param id
     * @param operatorId
     * @return
     */
    @Override
    public boolean updateBillStatusById(ObjectId id, ObjectId operatorId, Integer billStatus) {
        if (id == null) {
            throw BILL_ID_IS_NULL;
        }
        if(!BillStatusType.checkBillStatus(billStatus)){
            throw STATUS_INVALID;
        }
        PropertyBill propertyBill = propertyBillRepository.findById(id);
        if(propertyBill == null || propertyBill.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }
        if(billStatus == propertyBill.getBillStatus()){
            throw SAME_DATA;
        }
        PropertyBill toUpdate = new PropertyBill();
        // 未发布的账单
        if(billStatus == BillStatusType.UNPAYMENT.getKey()){
            // 设置超期时间
            Parameter parameter = parameterFacade.findByTypeAndKeyAndCommunityId(
                    ParamConfigType.BILL.getKey(), ParamKeyType.BILLOVERDUE.getValue(), propertyBill.getCommunityId());
            toUpdate.setOverdueDate(this.getOverdueDay(parameter));
        }
        toUpdate.setBillStatus(billStatus);
        toUpdate.setUpdateAt(new Date());
        toUpdate.setModifierId(operatorId);
        toUpdate = propertyBillRepository.updateById(toUpdate, id);
        upsertPropertyBill(propertyBill);
        return toUpdate != null;
    }

    /**
     * 物业手动缴费
     *
     * @param id
     * @param operatorId
     * @return
     */
    @Override
    public boolean paymentBillByProperty(ObjectId id, ObjectId operatorId) {
        if (id == null) {
            throw BILL_ID_IS_NULL;
        }
        PropertyBill propertyBill = propertyBillRepository.findById(id);
        if(propertyBill == null || propertyBill.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }
        // 已经缴费了
        if(BillStatusType.PAYMENT.getKey() == propertyBill.getBillStatus()){
            throw SAME_DATA;
        }
        PropertyBill toUpdate = new PropertyBill();
        toUpdate.setBillStatus(BillStatusType.PAYMENT.getKey());
        toUpdate.setPayAt(new Date());
        toUpdate.setUpdateAt(toUpdate.getPayAt());
        toUpdate.setModifierId(operatorId);
        toUpdate.setReceiveWay(ReceiveWayType.CASH.getKey());
        toUpdate = propertyBillRepository.updateById(toUpdate, id);
        upsertPropertyBill(toUpdate);
        return toUpdate != null;
    }

    /**
     * 统计物业费的账单数量
     *
     * @param communityId
     * @param buildingId
     * @return
     */
    @Override
    public Map<String, Long> countOverdueBills(ObjectId communityId, ObjectId buildingId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }

        long unPublishNum = propertyBillRepository
                .countByCommunityIdAndBuildingIdIgnoreNullAndBillStatusAndDataStatus(
                        communityId, buildingId, BillStatusType.UNPUBLISHED.getKey(), DataStatusType.VALID.KEY);
        long payNum = propertyBillRepository
                .countByCommunityIdAndBuildingIdIgnoreNullAndBillStatusAndDataStatus(
                        communityId, buildingId, BillStatusType.PAYMENT.getKey(), DataStatusType.VALID.KEY);
        long unPayNum = propertyBillRepository
                .countByCommunityIdAndBuildingIdIgnoreNullAndBillStatusAndDataStatus(
                        communityId, buildingId, BillStatusType.UNPAYMENT.getKey(), DataStatusType.VALID.KEY);
        long overdueNum = propertyBillRepository
                .countByCommunityIdAndBuildingIdIgnoreNullAndBillStatusAndOverdueDateLessThanAndDataStatus(
                        communityId, buildingId, BillStatusType.UNPAYMENT.getKey(), new Date(), DataStatusType.VALID.KEY);
        Map<String, Long> map = new HashMap<>();
        map.put("unPublishNum", unPublishNum);
        map.put("paymentNum", payNum);
        map.put("unPaymentNum", unPayNum);
        map.put("overdueNum", overdueNum);
        return map;
    }

    /**
     * 根据社区查询未缴费的账单
     *
     * @param communityId
     * @return
     */
    @Override
    public List<PropertyBill> findOverdueBillsByCommunityId(ObjectId communityId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        return propertyBillRepository.findByCommunityIdAndBillStatusAndOverdueDateLessThanAndDataStatus(
                communityId, BillStatusType.UNPAYMENT.getKey(), new Date(), DataStatusType.VALID.KEY);
    }

    @Override
    public PropertyBill getValidPropertyBill(ObjectId id, Long totalAmount) {
        if (id == null) {
            throw BILL_ID_IS_NULL;
        }
        PropertyBill propertyBill = findById(id);
        if (propertyBill == null
                || propertyBill.getBillStatus().equals(BillStatusType.UNPUBLISHED.getKey())) {
            throw BILL_NOT_EXISTS;
        }

        if (propertyBill.getBillStatus().equals(BillStatusType.PAYMENT.getKey())) {
            throw BILL_ALREADY_PAID;
        }

        if (test) {
            propertyBill.setTotalAmount(testAmount);
        } else if (!propertyBill.getTotalAmount().equals(totalAmount)) {
            throw BILL_PRICE_NOT_MATCH;
        }

        return propertyBill;
    }

    @Override
    public void updateTradeIdById(ObjectId id, Long tradeId) {
        PropertyBill propertyBill = new PropertyBill();
        propertyBill.setId(id);
        propertyBill.setTradeId(tradeId);
        propertyBill.setUpdateAt(new Date());
        propertyBillRepository.updateOne(propertyBill);
    }

    @Override
    public void finishedPaymentByTradeId(Long tradeId) {
        PropertyBill propertyBill = new PropertyBill();
        propertyBill.setReceiveWay(ReceiveWayType.ONLINE.getKey());
        propertyBill.setBillStatus(BillStatusType.PAYMENT.getKey());
        propertyBill.setPayAt(new Date());
        propertyBill.setUpdateAt(propertyBill.getPayAt());
        propertyBill = propertyBillRepository.updateByTradeIdAndBillStatusAndDataStatus(
                propertyBill, tradeId, BillStatusType.UNPAYMENT.getKey(), DataStatusType.VALID.KEY);
        if (propertyBill == null) {
            throw DATA_INVALID;
        }
        upsertPropertyBill(propertyBill);
    }

    @Override
    public void clearTradeIdByTradeId(Long tradeId) {
        PropertyBill propertyBill = new PropertyBill();
        propertyBill.setUpdateAt(new Date());
        propertyBill = propertyBillRepository.updateByTradeIdAndBillStatusAndDataStatus(
                propertyBill, tradeId, BillStatusType.UNPAYMENT.getKey(), DataStatusType.VALID.KEY);
        if (propertyBill == null) {
            throw DATA_INVALID;
        }
    }

    /**
     * 统计各账单总价
     *
     * @param id
     */
    @Override
    public void updateTotalPriceById(ObjectId id) {
        if (id == null) {
            throw BILL_ID_IS_NULL;
        }
        Map<String, Long> map = propBillDetailRepository.countTotalPriceById(id);
        log.info("billId = {}, and totalPrice = {}, and totalAmount = {}", id, map.get("total"), map.get("totalAmount"));
        PropertyBill propertyBill = new PropertyBill();
        propertyBill.setTotalPrice(map.get("total"));
        propertyBill.setTotalAmount(map.get("totalAmount"));
        propertyBill.setUpdateAt(new Date());
        propertyBill = propertyBillRepository.updateById(propertyBill, id);
        upsertPropertyBill(propertyBill);
    }

    @Override
    public PropertyBill findById(ObjectId id) {
        if (id == null) {
            throw BILL_ID_IS_NULL;
        }
        PropertyBill propertyBill = propertyBillRepository.findById(id);
        if(propertyBill == null || propertyBill.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }
        // 判断是否超期
        this.checkOverdueBill(propertyBill);
        return propertyBill;
    }


    private void checkOverdueBill(PropertyBill propertyBill) {
        // 判断是否超期
        if(propertyBill.getBillStatus() == BillStatusType.UNPAYMENT.getKey()
                && propertyBill.getOverdueDate() != null
                && new Date().after(propertyBill.getOverdueDate())){
            log.info("this bill is overdue, overdueDate:{}", propertyBill.getOverdueDate());
            // 已超期
            propertyBill.setBillStatus(BillStatusType.OVERDUE.getKey());
        }
    }

    /**
     * 创建一个详细子账单
     *
     * @param propBillDetail
     * @return
     */
    @Override
    public PropBillDetail addBillDetail(PropBillDetail propBillDetail) {
        PropFeeItem propFeeItem = propFeeItemRepository.findById(propBillDetail.getFeeItemId());
        propBillDetail.setFeeItemName(propFeeItem.getItemName());
        propBillDetail.setCreateAt(new Date());
        propBillDetail.setDataStatus(DataStatusType.VALID.KEY);

        // 放大100倍存入数据库，传到前端，避免失精度
        // 往期收费暂时不做
        propBillDetail.setPreviousFee(0L);
        propBillDetail.setSubtotal(propBillDetail.getCurrentFee() + propBillDetail.getPreviousFee());

        propBillDetail.setTotalAmount(StringUtil.calculateTotalAmount(propBillDetail.getSubtotal()));

        propBillDetail = propBillDetailRepository.insert(propBillDetail);
        log.info("addBillDetail end >>>> propBillDetail:{}", propBillDetail);
        if(propBillDetail != null){
            log.info("创建子帐单完成，调用统计各账单总价...");
            this.updateTotalPriceById(propBillDetail.getBillId());
        }
        return propBillDetail;
    }

    /**
     * 一键发布
     * @param communityId
     * @param uid
     * @return
     */
    @Override
    public boolean publishAllBills(List<PropertyBill> propertyBills, ObjectId communityId, ObjectId uid) {
        log.info("publishAllBills start ... communityId:{}", communityId);
        Set<ObjectId> billIds = propertyBills.stream().map(PropertyBill::getId).collect(Collectors.toSet());
        log.info("findByPublishBillRequest finish >>> billIds.size() = {}", billIds.size());
        PropertyBill toUpdate = new PropertyBill();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setModifierId(uid);
        toUpdate.setBillStatus(BillStatusType.UNPAYMENT.getKey());
        // 设置超期时间
        Parameter parameter = parameterFacade.findByTypeAndKeyAndCommunityId(
                ParamConfigType.BILL.getKey(), ParamKeyType.BILLOVERDUE.getValue(), communityId);
        toUpdate.setOverdueDate(this.getOverdueDay(parameter));

        int result = propertyBillRepository.updateByIdInAndDataStatusAndBillStatus(
                toUpdate, billIds, DataStatusType.VALID.KEY, BillStatusType.UNPUBLISHED.getKey());
        if (result > 0) {
            billIds.forEach(id -> {
                toUpdate.setId(id);
                upsertPropertyBill(toUpdate);
            });
        }
        log.info("publishAllBills finish >>> resultSize = {}", result);
        return result == billIds.size();
    }

    /**
     * 查询未发布账单
     *
     * @param publishBillRequest
     * @param communityId
     * @return
     */
    @Override
    public List<PropertyBill> findByPublishBillRequest(PublishBillRequest publishBillRequest, ObjectId communityId) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }
        List<PropertyBill> propertyBillList = propertyBillRepository.findByCommunityIdAndBillStatusAndDataStatusAndBuildingIdIgnoreNull(
                communityId, BillStatusType.UNPUBLISHED.getKey(), DataStatusType.VALID.KEY, publishBillRequest.getBuildingId());
        return propertyBillList;
    }

    /**
     * 根据楼栋ID统计物业费的账单数量
     *
     * @param buildingId
     * @param billStatus
     * @param billDate
     * @return
     */
    @Override
    public Long countBillNumByBuildingIdAndBillStatusAndDate(ObjectId buildingId, Integer billStatus, Date billDate) {
        if (buildingId == null) {
            throw BUILDING_ID_NULL;
        }
        Date overdueDate = null;
        Date startAt = null;
        Date endAt = null;
        Integer bStatus = billStatus;
        if(bStatus == BillStatusType.OVERDUE.getKey()){
            bStatus = BillStatusType.UNPAYMENT.getKey();
            overdueDate = new Date();
        }
        if(billDate != null){
            startAt = DateUtils.getFirstDateOfMonth(billDate);
            endAt = DateUtils.getLastDateOfMonth(billDate);
        }
        return propertyBillRepository.countByBuildingIdAndBillStatusAndOverdueDateLessThanAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
                buildingId, bStatus, overdueDate, startAt, endAt, DataStatusType.VALID.KEY);
    }

    /**
     * 根据子账单id获取详细
     *
     * @param id
     * @return
     */
    @Override
    public PropBillDetail findPropBillDetailById(ObjectId id) {
        if (id == null) {
            throw BILL_DETAIL_ID_IS_NULL;
        }
        return propBillDetailRepository.findById(id);
    }

    /**
     * 修改子账单详细
     *
     * @param propBillDetail
     * @return
     */
    @Override
    public PropBillDetail updateOnePropBillDetail(PropBillDetail propBillDetail) {
        if (propBillDetail.getId() == null) {
            throw BILL_DETAIL_ID_IS_NULL;
        }
        propBillDetail.setUpdateAt(new Date());
        // 往期收费暂时不做
        propBillDetail.setPreviousFee(0L);
        propBillDetail.setSubtotal(propBillDetail.getCurrentFee() + propBillDetail.getPreviousFee());

        propBillDetail.setTotalAmount(StringUtil.calculateTotalAmount(propBillDetail.getSubtotal()));

        propBillDetail = propBillDetailRepository.updateOne(propBillDetail);
        log.info("updateOnePropBillDetail end >>>> propBillDetail:{}", propBillDetail);
        if(propBillDetail != null){
            log.info("更新子帐单完成，调用统计各账单总价...");
            this.updateTotalPriceById(propBillDetail.getBillId());
        }
        return propBillDetail;
    }

    /**
     * 更新账单详细
     *
     *
     * @param uid
     * @param billId
     * @param propBillDetails
     * @return
     */
    @Override
    public void updateBillDetailsByBillId(ObjectId uid, ObjectId billId, List<PropBillDetail> propBillDetails) {
        // 设置redis校验，锁定当前数据
        boolean exist = RedisTemplateUtil.setIfAbsent(CacheConstant.PROPERTY_BILL, billId.toString());
        if (!exist) {//数据已被他人修改
            log.error("数据已被他人锁定");
            throw DATA_INVALID;
        }
        try {
            if (billId == null || propBillDetails == null || propBillDetails.size() == 0) {
                throw NO_BILL_NEED_TO_UPDATE;
            }
            // 校验用户输入的值是否为空
            for(PropBillDetail toCheck : propBillDetails){
                if(toCheck.getId() == null){
                    throw BILL_DETAIL_ID_IS_NULL;
                }
                if(toCheck.getCurrentFee() == null){
                    throw BILL_DETAIL_FEES_IS_NULL;
                }
            }
            PropertyBill propertyBill = propertyBillRepository.findById(billId);
            if (propertyBill == null || propertyBill.getDataStatus() == DataStatusType.INVALID.KEY) {
                throw BILL_NOT_EXISTS;
            }
            // 账单当前状态不能编辑
            if (propertyBill.getBillStatus() != BillStatusType.UNPUBLISHED.getKey()) {
                throw BILL_STATUS_INVALID;
            }

            List<PropBillDetail> toGetList = propBillDetailRepository.findByBillIdAndTypeAndDataStatus(
                    billId, FeesRuleType.CUSTOM.getKey(), DataStatusType.VALID.KEY);
            // 没有需要修改的账单
            if (toGetList == null || toGetList.size() == 0) {
                log.error("错误数据，错误操作，或者是恶意的操作 !!!");
                // 错误数据，错误操作，或者是恶意的操作
                throw DATA_INVALID;
            }

            // 非法新增不明子账单
            if (toGetList.size() < propBillDetails.size()) {
                log.error("非法新增不明子账单，或者是恶意的操作 !!!");
                // 错误数据，错误操作，或者是恶意的操作
                throw DATA_INVALID;
            }
            Set<ObjectId> billDetailIds = toGetList.stream().map(PropBillDetail::getId).collect(Collectors.toSet());
            log.info("billDetailIds:" + billDetailIds);
            for (PropBillDetail toUpdate : propBillDetails) {
                // 不存在的子账单数据
                if (!billDetailIds.contains(toUpdate.getId())) {
                    log.error("非法新增不明子账单，或者是恶意的操作 !!!");
                    throw DATA_INVALID;
                }
                if (!(toUpdate.getCurrentFee() == 0 || toUpdate.getCurrentFee() > 99)) {
                    log.error("非法金额 currentFee:{} ，或者是恶意的操作 !!!", toUpdate.getCurrentFee());
                    throw DATA_INVALID;
                }
            }
            //to update detail
            for (PropBillDetail propBillDetail : propBillDetails) {
                PropBillDetail toUpdate = new PropBillDetail();
                toUpdate.setUpdateAt(new Date());
                toUpdate.setModifierId(uid);
                toUpdate.setCurrentFee(propBillDetail.getCurrentFee());
                toUpdate.setSubtotal(toUpdate.getCurrentFee());
                toUpdate.setTotalAmount(StringUtil.calculateTotalAmount(toUpdate.getSubtotal()));
                toUpdate = propBillDetailRepository.updateById(toUpdate, propBillDetail.getId());
                log.info("updateResult toUpdate:" + toUpdate);
            }
            log.info("更新子账单完成，调用统计各账单总价...");
            this.updateTotalPriceById(billId);
        } finally {
            // 解锁
            RedisTemplateUtil.del(CacheConstant.PROPERTY_BILL);
        }

    }

    @Override
    public PropertyBillSummaryResponse getPropertyBillSummary(PropertyBillSummaryRequest request) {
        if (request.getCommunityId() == null) {
            throw COMMUNITY_ID_NULL;
        }

        if (request.getStartAt() == null) {
            request.setStartAt(DateUtils.getFirstDateOfMonth(new Date()));
        }

        if (request.getEndAt() == null) {
            request.setEndAt(DateUtils.getLastDateOfMonth(new Date()));
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.matchQuery("communityId", request.getCommunityId().toString())))
                .addAggregation(AggregationBuilders.filter("summary",
                        QueryBuilders.boolQuery()
                                .mustNot(QueryBuilders.matchQuery("status", BillStatusType.UNPUBLISHED.getKey()))
                                .filter(QueryBuilders.rangeQuery("createAt")
                                        .from(DateUtils.getShortDateStr(request.getStartAt()))
                                        .to(DateUtils.getShortDateStr(request.getEndAt()))))
                        .subAggregation(AggregationBuilders.sum("valid_amount")
                                .field("totalAmount")
                                .format("0"))
                        .subAggregation(AggregationBuilders.filter("unpaid_count",
                                QueryBuilders.matchQuery("status", BillStatusType.UNPAYMENT.getKey()))
                                .subAggregation(AggregationBuilders.sum("unpaid_amount")
                                        .field("totalAmount")
                                        .format("0")))
                        .subAggregation(AggregationBuilders.filter("paid_count",
                                QueryBuilders.matchQuery("status", BillStatusType.PAYMENT.getKey()))
                                .subAggregation(AggregationBuilders.sum("paid_amount")
                                        .field("totalAmount")
                                        .format("0"))));

        SearchResponse searchResponse = searchRequestBuilder.get();

        PropertyBillSummaryResponse response = new PropertyBillSummaryResponse();
        Filter summary = searchResponse.getAggregations().get("summary");
        response.setValidCount(summary.getDocCount());

        Sum validAmount = summary.getAggregations().get("valid_amount");
        response.setValidAmount(Long.valueOf(validAmount.getValueAsString()));

        Filter paidCount = summary.getAggregations().get("paid_count");
        response.setPaidCount(paidCount.getDocCount());
        Sum paidAmount = paidCount.getAggregations().get("paid_amount");
        response.setPaidAmount(Long.valueOf(paidAmount.getValueAsString()));

        Filter unpaidCount = summary.getAggregations().get("unpaid_count");
        response.setUnpaidCount(unpaidCount.getDocCount());
        Sum unpaidAmount = unpaidCount.getAggregations().get("unpaid_amount");
        response.setUnpaidAmount(Long.valueOf(unpaidAmount.getValueAsString()));

        return response;
    }

    @Override
    public ExpirePropertyBillResponse getExpirePropertyBillStatistics(ObjectId communityId) {
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }

        SearchRequestBuilder searchRequestBuilder = esTemplate.getClient().prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSize(0)
                .setQuery(QueryBuilders.constantScoreQuery(
                        QueryBuilders.matchQuery("communityId", communityId.toString())))
                .addAggregation(AggregationBuilders.terms("proprietor").field("roomId")
                        .size(20)
                        .order(Terms.Order.aggregation("expire>expire_count", false))
                        .subAggregation(AggregationBuilders.filter("expire",
                                QueryBuilders.boolQuery()
                                        .filter(QueryBuilders.rangeQuery("expireAt")
                                                .lte("now"))
                                        .filter(QueryBuilders.matchQuery("status", BillStatusType.UNPAYMENT.getKey())))
                                .subAggregation(AggregationBuilders.count("expire_count").field("createAt"))));

        SearchResponse searchResponse = searchRequestBuilder.get();

        ExpirePropertyBillResponse response = new ExpirePropertyBillResponse();
        List<ExpirePropertyBillResponse.Section> sections = new LinkedList<>();
        Terms proprietor = searchResponse.getAggregations().get("proprietor");
        for (Terms.Bucket bucket : proprietor.getBuckets()) {
            Filter expire = bucket.getAggregations().get("expire");
            if (expire.getDocCount() == 0) {
                continue;
            }

            ExpirePropertyBillResponse.Section section = new ExpirePropertyBillResponse.Section();
            section.setName(bucket.getKeyAsString());
            section.setTotal(bucket.getDocCount());
            section.setCount(expire.getDocCount());
            section.setProportion(AmountUtil.roundDownStr(section.getCount() * 100.0D / section.getTotal()) + "%");
            sections.add(section);
        }
        response.setProprietorSections(sections);

        return response;
    }

    /**
     * 根据楼栋id查询账单列表
     *
     * @param buildingIds
     * @param billStatus
     * @param billDate
     * @return
     */
    @Override
    public List<PropertyBill> findByBuildingIdInAndBillStatusAndDate(Set<ObjectId> buildingIds,
                                                                     Integer billStatus, Date billDate) {
        Date overdueDate = null;
        Date startAt = null;
        Date endAt = null;
        Integer bStatus = billStatus;
        if(bStatus == BillStatusType.OVERDUE.getKey()){
            bStatus = BillStatusType.UNPAYMENT.getKey();
            overdueDate = new Date();
        }
        if(billDate != null){
            startAt = DateUtils.getFirstDateOfMonth(billDate);
            endAt = DateUtils.getLastDateOfMonth(billDate);
        }
        return propertyBillRepository.findByBuildingIdInAndBillStatusAndOverdueDateLessThanAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
                buildingIds, bStatus, overdueDate, startAt, endAt, DataStatusType.VALID.KEY);
    }

    /**
     * 批量插入物业账单
     *
     * @param insertPropertyBills
     */
    @Override
    public void insertAllBills(List<PropertyBill> insertPropertyBills) {
        propertyBillRepository.insertAll(insertPropertyBills);
        // 遍历插入到es
        insertPropertyBills.forEach(this::upsertPropertyBill);
    }

    /**
     * 批量插入子账单
     *
     * @param insertBillDetails
     */
    @Override
    public void insertAllBillDetails(List<PropBillDetail> insertBillDetails) {
        propBillDetailRepository.insertAll(insertBillDetails);
    }

    /**
     * 查询账单详情
     *
     * @param id
     * @return
     */
    @Override
    public BillDetailVO findByIdWithDetail(ObjectId id) {
        if(id == null){
            throw BILL_ID_IS_NULL;
        }
        PropertyBill propertyBill = propertyBillRepository.findById(id);
        checkOverdueBill(propertyBill);
        List<PropBillDetail> billDetailList = propBillDetailRepository.findByBillIdAndDataStatus(id, DataStatusType.VALID.KEY);
        BillDetailVO billDetailVO = new BillDetailVO();
        billDetailVO.setPropertyBill(propertyBill);
        billDetailVO.setBillDetailList(billDetailList);
        return billDetailVO;
    }

    /**
     * 转换overdueDay
     * @param parameter
     * @return
     */
    private Date getOverdueDay(Parameter parameter){
        int day = 30;
        if(parameter != null){
            try {
                day = Integer.parseInt(parameter.getValue());
            }catch (Exception e){
                log.error("getOverdueDay exception:{}", e);
                day = 30;
            }
        }
        log.info("set bill overdueDay:{}", day);
        return DateUtils.addDay(new Date(), day);
    }

    /**
     * 账单数据写入es
     * @param propertyBill
     */
    private void upsertPropertyBill(PropertyBill propertyBill) {
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, propertyBill.getId().toString(),
                generatePropertyBill(propertyBill));
    }

    /**
     * 从es移除账单数据
     * @param roomIds
     */
    private void removePropertyBill(Collection<ObjectId> roomIds) {
        roomIds.forEach(roomId -> esTemplate.deleteAsync(INDEX_NAME, TYPE_NAME, roomId.toString()));
    }

    private cn.bit.facade.data.fees.PropertyBill generatePropertyBill(PropertyBill propertyBill) {
        cn.bit.facade.data.fees.PropertyBill toUpsert = new cn.bit.facade.data.fees.PropertyBill();
        toUpsert.setCommunityId(propertyBill.getCommunityId());
        toUpsert.setRoomId(propertyBill.getRoomId());
        toUpsert.setTotalAmount(propertyBill.getTotalAmount());
        toUpsert.setStatus(propertyBill.getBillStatus());
        toUpsert.setCreateAt(propertyBill.getCreateAt());
        toUpsert.setExpireAt(propertyBill.getOverdueDate());
        return toUpsert;
    }
}