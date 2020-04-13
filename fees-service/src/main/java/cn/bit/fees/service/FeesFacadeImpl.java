package cn.bit.fees.service;

import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.facade.enums.BillStatusType;
import cn.bit.facade.enums.ReceiveWayType;
import cn.bit.facade.enums.fees.BillSourceEnum;
import cn.bit.facade.model.fees.Bill;
import cn.bit.facade.model.fees.BillDetail;
import cn.bit.facade.model.fees.Item;
import cn.bit.facade.model.fees.Template;
import cn.bit.facade.poi.entity.BillEntity;
import cn.bit.facade.service.fees.FeesFacade;
import cn.bit.facade.vo.fees.BillVO;
import cn.bit.facade.vo.fees.ExportRequest;
import cn.bit.facade.vo.fees.FeesPageQuery;
import cn.bit.facade.vo.fees.TemplateVO;
import cn.bit.facade.vo.statistics.PropertyBillSummaryRequest;
import cn.bit.facade.vo.statistics.PropertyBillSummaryResponse;
import cn.bit.fees.dao.BillDetailRepository;
import cn.bit.fees.dao.BillRepository;
import cn.bit.fees.dao.ItemRepository;
import cn.bit.fees.dao.TemplateRepository;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.fees.FeesBizException.*;
import static cn.bit.framework.exceptions.BizException.OPERATION_FAILURE;

@Component("feesFacade")
@Slf4j
public class FeesFacadeImpl implements FeesFacade {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillDetailRepository billDetailRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private EsTemplate esTemplate;

    /**
     * 测试支付
     */
    @Value("${trade.test}")
    private Boolean test;

    /**
     * 测试金额固定为 1分
     */
    @Value("${trade.test.amount}")
    private Long testAmount;

    private static final String INDEX_NAME = "cm_bill";

    private static final String TYPE_NAME = "property_bill";

    /**
     * 新增收费项目
     *
     * @param item
     */
    @Override
    public void addItem(Item item) {
        boolean nameExist = itemRepository.existsByCommunityIdAndNameAndDataStatus(
                item.getCommunityId(), item.getName(), DataStatusEnum.VALID.value());
        if (nameExist) {
            throw FEES_ITEM_NAME_EXISTS;
        }
        item.setDataStatus(DataStatusEnum.VALID.value());
        item.setCreateAt(new Date());
        item.setUpdateAt(item.getCreateAt());
        item = itemRepository.save(item);
        if (item == null) {
            log.error("新增收费项目插入数据库失败...");
            throw OPERATION_FAILURE;
        }
    }

    /**
     * 编辑收费项目
     *
     * @param item
     * @return
     */
    @Override
    public Item modifyItem(Item item) {
        item.setUpdateAt(new Date());
        item = itemRepository.updateByIdAndDataStatus(item, item.getId(), DataStatusEnum.VALID.value());
        if (item == null) {
            throw FEES_ITEM_NULL;
        }
        return item;
    }

    /**
     * 分页查询收费列表
     *
     * @param pageQuery
     * @return
     */
    @Override
    public Page<Item> pagingQueryItems(FeesPageQuery pageQuery) {
        Pageable pageable = new PageRequest(pageQuery.getPage() - 1, pageQuery.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Item> items = itemRepository.findByCommunityIdAndNameRegexIgnoreNullAndDataStatus(
                pageQuery.getCommunityId(), StringUtil.makeQueryStringAllRegExp(pageQuery.getName()), DataStatusEnum.VALID.value(), pageable);
        return PageUtils.getPage(items);
    }

    /**
     * 查询某社区收费项目列表
     *
     * @param communityId
     * @param name
     * @return
     */
    @Override
    public List<Item> listItemsByCommunityIdAndName(ObjectId communityId, String name) {
        return itemRepository.findByCommunityIdAndNameRegexIgnoreNullAndDataStatus(
                communityId, StringUtil.makeQueryStringAllRegExp(name), DataStatusEnum.VALID.value());
    }

    /**
     * 删除收费项目
     *
     * @param id
     */
    @Override
    public void deleteItemByItemId(ObjectId id) {
        Item item = new Item();
        item.setDataStatus(DataStatusEnum.INVALID.value());
        item.setUpdateAt(new Date());
        itemRepository.updateByIdAndDataStatus(item, id, DataStatusEnum.VALID.value());
    }

    /**
     * 查看收费项目详情
     *
     * @param itemId
     * @return
     */
    @Override
    public Item findItemByItemId(ObjectId itemId) {
        return itemRepository.findByIdAndDataStatus(itemId, DataStatusEnum.VALID.value());
    }

    /**
     * 新建物业账单
     *
     * @param billVO
     */
    @Override
    public Bill addBill(BillVO billVO) {
        List<BillDetail> charges = billVO.getCharges();
        if (CollectionUtils.isEmpty(charges)) {
            throw FEES_BILL_ITEM_NULL;
        }
        Bill toAdd = new Bill();
        BeanUtils.copyProperties(billVO, toAdd);
        toAdd.setStatus(BillStatusType.UNPUBLISHED.getKey());
        toAdd.setCreateAt(new Date());
        toAdd.setUpdateAt(toAdd.getCreateAt());
        Long totalPrice = 0L;
        for (BillDetail billDetail : charges) {
            billDetail.setSource(BillSourceEnum.BILL.value());
            billDetail.setCreateAt(toAdd.getCreateAt());
            billDetail.setCreator(toAdd.getCreator());
            billDetail.setUpdateAt(toAdd.getUpdateAt());
            if (billDetail.getTotalPrice() != null) {
                totalPrice += billDetail.getTotalPrice();
            }
        }
        toAdd.setTotalPrice(totalPrice);
        toAdd = billRepository.save(toAdd);
        if (toAdd == null) {
            log.error("新建账单插入数据库失败...");
            throw OPERATION_FAILURE;
        }

        ObjectId billId = toAdd.getId();
        charges.stream().forEach(billDetail -> billDetail.setRelateId(billId));
        charges = billDetailRepository.save(charges);
        log.debug("billDetails: {}", charges);
        return toAdd;
    }

    /**
     * 编辑账单
     *
     * @param billVO
     */
    @Override
    public void modifyBill(BillVO billVO) {
        Bill toGet = billRepository.findById(billVO.getId());
        if (toGet == null) {
            throw FEES_BILL_NOT_EXISTS;
        }
        if (toGet.getStatus() != BillStatusType.UNPUBLISHED.getKey()) {
            throw FEES_BILL_STATUS_NOT_ALLOW_MODIFY;
        }
        Bill toUpdate = new Bill();
        toUpdate.setName(billVO.getName());
        toUpdate.setRemark(billVO.getRemark());
        toUpdate.setUpdateAt(new Date());
        toUpdate.setModifier(billVO.getModifier());

        List<BillDetail> charges = billVO.getCharges();
        if (!CollectionUtils.isEmpty(charges)) {
            Long totalPrice = 0L;
            for (BillDetail billDetail : charges) {
                billDetail.setRelateId(toGet.getId());
                billDetail.setSource(BillSourceEnum.BILL.value());
                billDetail.setCreateAt(toGet.getCreateAt());
                billDetail.setCreator(toGet.getCreator());
                billDetail.setUpdateAt(toUpdate.getUpdateAt());
                if (billDetail.getTotalPrice() != null) {
                    totalPrice += billDetail.getTotalPrice();
                }
            }
            toUpdate.setTotalPrice(totalPrice);
            // 先删除旧的收费项目
            billDetailRepository.deleteByRelateId(toGet.getId());
            // 再插入新的收费项目
            billDetailRepository.save(charges);
        }
        // 最后更新账单信息
        billRepository.updateById(toUpdate, toGet.getId());
    }

    /**
     * 账单分页
     *
     * @param pageQuery
     * @return
     */
    @Override
    public Page<BillVO> pagingQueryBills(FeesPageQuery pageQuery) {
        Pageable pageable = new PageRequest(pageQuery.getPage() - 1, pageQuery.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Bill> bills =
                billRepository.findByCommunityIdAndRoomIdAndProprietorIdAndStatusAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAllIgnoreNull(
                        pageQuery.getCommunityId(), pageQuery.getRoomId(), pageQuery.getProprietorId(), pageQuery.getStatus(),
                        DateUtils.getStartTime(pageQuery.getStartAt()), DateUtils.getEndTime(pageQuery.getEndAt()),
                        pageable, Bill.class);
        if (bills.getTotalElements() == 0) {
            return new Page<>();
        }

        List<BillDetail> billDetails =
                billDetailRepository.findByRelateIdInOrderByCreateAtAsc(bills.getContent().stream().map(Bill::getId).collect(Collectors.toSet()));
        List<BillVO> billVOList = new ArrayList<>(bills.getContent().size());
        bills.getContent().forEach(bill -> {
            BillVO vo = new BillVO();
            BeanUtils.copyProperties(bill, vo);
            vo.setCharges(billDetails.stream().filter(billDetail -> billDetail.getRelateId().equals(bill.getId())).collect(Collectors.toList()));
            billVOList.add(vo);
        });
        return new Page<>(pageQuery.getPage(), bills.getTotalElements(), pageQuery.getSize(), billVOList);
    }

    /**
     * 删除账单
     *
     * @param id
     */
    @Override
    public void deleteBillById(ObjectId id) {
        Bill toGet = billRepository.findById(id);
        if (toGet == null) {
            throw FEES_BILL_NOT_EXISTS;
        }
        if (toGet.getStatus() != BillStatusType.UNPUBLISHED.getKey()) {
            throw FEES_BILL_STATUS_NOT_ALLOW_DELETE;
        }
        // 先删除主账单
        billRepository.deleteById(id);
        // 再删除相关收费项目明细
        billDetailRepository.deleteByRelateId(id);
    }

    /**
     * 查看账单明细
     *
     * @param id
     * @return
     */
    @Override
    public BillVO findBillWithDetailById(ObjectId id) {
        Bill bill = billRepository.findById(id);
        if (bill == null) {
            log.info("账单({})不存在...", id);
            return null;
        }
        List<BillDetail> charges = billDetailRepository.findByRelateIdInOrderByCreateAtAsc(Collections.singleton(id));
        BillVO billVO = new BillVO();
        BeanUtils.copyProperties(bill, billVO);
        billVO.setCharges(charges);
        return billVO;
    }

    /**
     * 发布账单（通知业主缴费）
     *
     * @param id
     * @return
     */
    @Override
    public Bill publishBillById(ObjectId id) {
        Bill toPublish = new Bill();
        toPublish.setStatus(BillStatusType.UNPAYMENT.getKey());
        toPublish.setUpdateAt(new Date());
        toPublish = billRepository.updateByIdAndStatus(toPublish, id, BillStatusType.UNPUBLISHED.getKey());
        if (toPublish == null) {
            throw FEES_BILL_NOT_EXISTS;
        }
        // 写入es
        upsertBill(toPublish);
        return toPublish;
    }

    /**
     * 查询账单信息
     *
     * @param id
     * @return
     */
    @Override
    public Bill findBillById(ObjectId id) {
        return billRepository.findById(id);
    }

    /**
     * 人工收费
     *
     * @param payVO
     */
    @Override
    public void payBillOffline(BillVO payVO) {
        Bill toPay = new Bill();
        toPay.setPayUser(payVO.getPayUser());
        toPay.setReceiveWay(payVO.getReceiveWay());
        toPay.setStatus(BillStatusType.PAYMENT.getKey());
        toPay.setPayAt(new Date());
        toPay.setPayRemark(payVO.getPayRemark());
        toPay.setModifier(payVO.getModifier());
        toPay.setUpdateAt(toPay.getPayAt());
        int result = billRepository.updateByIdInAndStatus(toPay, payVO.getBillIds(), BillStatusType.UNPAYMENT.getKey());
        if (result == 0) {
            log.info("本次缴费的订单（{}）列表中，不包含未缴费的订单...", payVO.getBillIds());
            return;
        }
        List<Bill> payBills = billRepository.findByIdIn(payVO.getBillIds());
        // 更新 es
        payBills.forEach(bill -> upsertBill(bill));
    }

    /**
     * 获取账单，并校验总金额
     *
     * @param id
     * @param totalPrice
     * @return
     */
    @Override
    public Bill getValidBill(ObjectId id, Long totalPrice) {

        Bill toGet = billRepository.findById(id);
        if (toGet == null || BillStatusType.UNPUBLISHED.getKey() == toGet.getStatus()) {
            throw FEES_BILL_NOT_EXISTS;
        }

        if (BillStatusType.PAYMENT.getKey() == toGet.getStatus()) {
            throw FEES_BILL_ALREADY_PAID;
        }

        if (test) {
            toGet.setTotalPrice(testAmount);
            return toGet;
        }

        if (!totalPrice.equals(toGet.getTotalPrice())) {
            throw FEES_BILL_PRICE_NOT_MATCH;
        }
        return toGet;
    }

    /**
     * 更新账单的交易订单号
     *
     * @param id
     * @param tradeId
     */
    @Override
    public void updateTradeIdById(ObjectId id, Long tradeId) {
        Bill toUpdate = new Bill();
        toUpdate.setTradeId(tradeId);
        toUpdate.setUpdateAt(new Date());
        billRepository.updateById(toUpdate, id);
    }

    /**
     * 完成支付，修改订单状态
     *
     * @param tradeId
     */
    @Override
    public void finishedPaymentByTradeId(Long tradeId) {
        Bill bill = new Bill();
        bill.setReceiveWay(ReceiveWayType.ONLINE.getKey());
        bill.setStatus(BillStatusType.PAYMENT.getKey());
        bill.setPayAt(new Date());
        bill.setPayUser("业主");
        bill.setUpdateAt(bill.getPayAt());
        bill = billRepository.updateByTradeIdAndStatus(bill, tradeId, BillStatusType.UNPAYMENT.getKey());
        if (bill == null) {
            log.info("交易流水号({})没有对应未缴费的账单!", tradeId);
            return;
        }
        upsertBill(bill);
    }

    /**
     * 清除账单的交易订单流水号
     *
     * @param tradeId
     */
    @Override
    public void clearTradeIdByTradeId(Long tradeId) {
        Bill bill = new Bill();
        bill.setUpdateAt(new Date());
        bill = billRepository.updateWithUnsetIfNullTradeIdByTradeIdAndStatus(bill, tradeId, BillStatusType.UNPAYMENT.getKey());
        if (bill == null) {
            log.info("交易流水号({})没有对应未缴费的账单, 无需清除!", tradeId);
            return;
        }
    }

    /**
     * 收费模板分页
     *
     * @param pageQuery
     * @return
     */
    @Override
    public Page<TemplateVO> pagingQueryTemplates(FeesPageQuery pageQuery) {
        Pageable pageable = new PageRequest(pageQuery.getPage() - 1, pageQuery.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Template> templates =
                templateRepository.findByCommunityIdAndNameRegexIgnoreNull(
                        pageQuery.getCommunityId(), StringUtil.makeQueryStringAllRegExp(pageQuery.getName()),
                        pageable, Template.class);

        if (templates.getTotalElements() == 0) {
            return new Page<>();
        }
        List<TemplateVO> vos = new ArrayList<>(templates.getContent().size());
        List<BillDetail> billDetails =
                billDetailRepository.findByRelateIdInOrderByCreateAtAsc(templates.getContent().stream().map(Template::getId).collect(Collectors.toSet()));

        templates.getContent().forEach(template -> {
            TemplateVO vo = new TemplateVO();
            BeanUtils.copyProperties(template, vo);
            vo.setCharges(billDetails.stream().filter(billDetail -> billDetail.getRelateId().equals(template.getId())).collect(Collectors.toList()));
            vos.add(vo);
        });
        return new Page<>(pageQuery.getPage(), templates.getTotalElements(), pageQuery.getSize(), vos);
    }

    /**
     * 收费模板列表
     *
     * @param communityId
     * @param name
     * @return
     */
    @Override
    public List<Template> listTemplates(ObjectId communityId, String name) {
        return templateRepository.findByCommunityIdAndNameRegexIgnoreNull(communityId, StringUtil.makeQueryStringAllRegExp(name));
    }

    /**
     * 新建收费模板
     *
     * @param templateVO
     * @return
     */
    @Override
    public Template addTemplate(TemplateVO templateVO) {
        List<BillDetail> charges = templateVO.getCharges();
        if (CollectionUtils.isEmpty(charges)) {
            throw FEES_BILL_ITEM_NULL;
        }
        Template toAdd = new Template();
        BeanUtils.copyProperties(templateVO, toAdd);
        toAdd.setCreateAt(new Date());
        toAdd.setUpdateAt(toAdd.getCreateAt());
        for (BillDetail billDetail : charges) {
            billDetail.setSource(BillSourceEnum.TEMPLATE.value());
            billDetail.setCreateAt(toAdd.getCreateAt());
            billDetail.setCreator(toAdd.getCreator());
            billDetail.setUpdateAt(toAdd.getUpdateAt());
        }
        toAdd = templateRepository.save(toAdd);
        if (toAdd == null) {
            log.error("新建收费模板插入数据库失败...");
            throw OPERATION_FAILURE;
        }

        ObjectId templateId = toAdd.getId();
        charges.stream().forEach(billDetail -> billDetail.setRelateId(templateId));
        charges = billDetailRepository.save(charges);
        log.debug("billDetails: {}", charges);
        return toAdd;
    }

    /**
     * 编辑收费模板
     *
     * @param templateVO
     */
    @Override
    public void modifyTemplate(TemplateVO templateVO) {
        Template toGet = templateRepository.findById(templateVO.getId());
        if (toGet == null) {
            throw FEES_TEMPLATE_NOT_EXISTS;
        }
        Template toUpdate = new Template();
        toUpdate.setName(templateVO.getName());
        toUpdate.setRemark(templateVO.getRemark());
        toUpdate.setUpdateAt(new Date());

        List<BillDetail> charges = templateVO.getCharges();
        if (!CollectionUtils.isEmpty(charges)) {
            for (BillDetail billDetail : charges) {
                billDetail.setRelateId(toGet.getId());
                billDetail.setSource(BillSourceEnum.TEMPLATE.value());
                billDetail.setCreateAt(toGet.getCreateAt());
                billDetail.setCreator(toGet.getCreator());
                billDetail.setUpdateAt(toUpdate.getUpdateAt());
            }
            // 先删除旧的收费项目
            billDetailRepository.deleteByRelateId(toGet.getId());
            // 再插入新的收费项目
            billDetailRepository.save(charges);
        }
        // 最后更新收费模板信息
        templateRepository.updateById(toUpdate, toGet.getId());
    }

    /**
     * 删除收费模板
     *
     * @param id
     */
    @Override
    public void deleteTemplateById(ObjectId id) {
        Template toGet = templateRepository.findById(id);
        if (toGet == null) {
            throw FEES_TEMPLATE_NOT_EXISTS;
        }
        // 先删除收费模板
        templateRepository.deleteById(id);
        // 再删除相关收费项目明细
        billDetailRepository.deleteByRelateId(id);
    }

    /**
     * 查询收费模板详情
     *
     * @param id
     * @return
     */
    @Override
    public TemplateVO findTemplateWithDetailById(ObjectId id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            log.info("收费模板({})不存在...", id);
            return null;
        }
        List<BillDetail> charges = billDetailRepository.findByRelateIdInOrderByCreateAtAsc(Collections.singleton(id));
        TemplateVO templateVO = new TemplateVO();
        BeanUtils.copyProperties(template, templateVO);
        templateVO.setCharges(charges);
        return templateVO;
    }

    /**
     * 校验收费模板是否存在
     *
     * @param communityId
     * @param templateId
     */
    @Override
    public void checkTemplateExistByCommunityIdAndTemplateId(ObjectId communityId, ObjectId templateId) {
        boolean exist = templateRepository.existsByCommunityIdAndId(communityId, templateId);
        if (!exist) {
            throw FEES_TEMPLATE_NOT_EXISTS;
        }
    }

    /**
     * 查询收费模板
     *
     * @param id
     * @return
     */
    @Override
    public Template findTemplateById(ObjectId id) {
        return templateRepository.findById(id);
    }

    /**
     * 查询待导出账单列表
     *
     * @param request
     * @return
     */
    @Override
    public List<BillEntity> listBillEntities(ExportRequest request) {
        List<Bill> billList = billRepository.findByCommunityIdAndBuildingIdInAndStatusAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAllIgnoreNullOrderByBuildingIdAscRoomIdAsc(
                request.getCommunityId(), request.getBuildingIds(), request.getStatus(),
                DateUtils.getStartTime(request.getStartAt()), DateUtils.getEndTime(request.getEndAt()));
        if (CollectionUtils.isEmpty(billList)) {
            return Collections.EMPTY_LIST;
        }
        Set<ObjectId> billIds = billList.stream().map(Bill::getId).collect(Collectors.toSet());
        List<BillDetail> detailList = billDetailRepository.findByRelateIdInOrderByCreateAtAsc(billIds);
        List<BillEntity> entityList = new ArrayList<>(detailList.size());
        Map<ObjectId, Bill> billMap = billList.stream().collect(Collectors.toMap(Bill::getId, bill -> bill));
        for (BillDetail detail : detailList) {
            Bill bill = billMap.get(detail.getRelateId());
            BillEntity entity = new BillEntity();
            entity.setId(bill.getId().toString());
            entity.setRoomLocation(bill.getRoomLocation());
            entity.setProprietorName(bill.getProprietorName());
            entity.setBillName(bill.getName());
            entity.setStatus(bill.getStatus() + "");
            entity.setTotal(StringUtil.makePriceKeep2Decimal(bill.getTotalPrice()));
            entity.setItemName(detail.getItemName());
            entity.setItemType(detail.getItemType() + "");
            if (detail.getUnitPrice() == null) {
                entity.setUnitPrice("/");
            } else {
                entity.setUnitPrice(StringUtil.makePriceKeep2Decimal(detail.getUnitPrice().longValue()));
            }
            entity.setUnits(StringUtil.isBlank(detail.getUnits()) ? "/" : detail.getUnits());
            if (detail.getQuantity() == null) {
                entity.setQuantity("/");
            } else {
                entity.setQuantity(StringUtil.makePriceKeep2Decimal(detail.getQuantity().longValue()));
            }
            if (detail.getTotalPrice() == null) {
                entity.setPrice("/");
            } else {
                entity.setPrice(StringUtil.makePriceKeep2Decimal(detail.getTotalPrice().longValue()));
            }
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * 统计各个状态账单数量
     *
     * @param communityId
     * @return
     */
    @Override
    public Map<String, Long> countBills(ObjectId communityId) {
        long unPublishNum = billRepository.countByCommunityIdAndStatus(communityId, BillStatusType.UNPUBLISHED.getKey());
        long payNum = billRepository.countByCommunityIdAndStatus(communityId, BillStatusType.PAYMENT.getKey());
        long unPayNum = billRepository.countByCommunityIdAndStatus(communityId, BillStatusType.UNPAYMENT.getKey());
        Map<String, Long> map = new HashMap<>();
        map.put("unPublishNum", unPublishNum);
        map.put("paymentNum", payNum);
        map.put("unPaymentNum", unPayNum);
        return map;
    }

    /**
     * 从es统计账单
     *
     * @param request
     * @return
     */
    @Override
    public PropertyBillSummaryResponse getBillSummary(PropertyBillSummaryRequest request) {
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

    /**
     * 查询房间下某状态订单列表
     *
     * @param roomId
     * @param key
     * @return
     */
    @Override
    public List<Bill> findBillByRoomIdAndStatus(ObjectId roomId, Integer key) {
        return billRepository.findByRoomIdAndStatus(roomId, key);
    }

    // ======================================= 私有方法 =================================================================

    /**
     * 账单数据写入es
     *
     * @param bill
     */
    private void upsertBill(Bill bill) {
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, bill.getId().toString(), generateBill(bill));
    }

    /**
     * 从es移除账单数据
     *
     * @param roomIds
     */
    private void removeBill(Collection<ObjectId> roomIds) {
        roomIds.forEach(roomId -> esTemplate.deleteAsync(INDEX_NAME, TYPE_NAME, roomId.toString()));
    }

    /**
     * 封装 es 物业账单实体
     *
     * @param bill
     * @return
     */
    private cn.bit.facade.data.fees.PropertyBill generateBill(Bill bill) {
        cn.bit.facade.data.fees.PropertyBill toUpsert = new cn.bit.facade.data.fees.PropertyBill();
        toUpsert.setCommunityId(bill.getCommunityId());
        toUpsert.setRoomId(bill.getRoomId());
        toUpsert.setTotalAmount(bill.getTotalPrice());
        toUpsert.setStatus(bill.getStatus());
        toUpsert.setCreateAt(bill.getCreateAt());
        return toUpsert;
    }
}