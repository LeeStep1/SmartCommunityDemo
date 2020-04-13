package cn.bit.api.controller.v1;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.bit.api.support.*;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.api.support.annotation.SendPush;
import cn.bit.common.facade.community.model.Community;
import cn.bit.common.facade.community.service.CommunityFacade;
import cn.bit.facade.enums.*;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.community.CommunityTradeAccount;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.fees.*;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.poi.entity.BillEntity;
import cn.bit.facade.poi.styler.ExcelExportStylerImpl;
import cn.bit.facade.poi.utils.PoiMergeCellUtil;
import cn.bit.facade.service.community.CommunityTradeAccountFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.fees.FeeRuleFacade;
import cn.bit.facade.service.fees.FeesFacade;
import cn.bit.facade.service.fees.PropFeeItemFacade;
import cn.bit.facade.service.fees.PropertyFeesFacade;
import cn.bit.facade.service.trade.TradeFacade;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.vo.fees.*;
import cn.bit.facade.vo.trade.Order;
import cn.bit.facade.vo.trade.TradeOrder;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.fees.FeesBizException.*;
import static cn.bit.facade.exception.user.UserBizException.NO_PROPRIETOR;

@RestController
@RequestMapping(value = "/v1/fees", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class FeesController {
    @Autowired
    private PropertyFeesFacade propertyFeesFacade;

    @Autowired
    private FeeRuleFacade feeRuleFacade;

    @Autowired
    private PropFeeItemFacade propFeeItemFacade;

    @Autowired
    private CommunityTradeAccountFacade communityTradeAccountFacade;

    @Autowired
    private TradeFacade tradeFacade;

    @Autowired
    private FeesFacade feesFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    @Resource
    private CommunityFacade commonCommunityFacade;

    @Value("${trade.payment.notify.uri}")
    private String tradePaymentNotifyUri;

    private final String TODAY = DateUtils.getReqDate();

    private final String[] EXCEL_SUFFIX = {".xls", ".xlsx"};

    //=============================================property-bill-start==================================================

    /**
     * 物业根据社区获取账单,不包括详情(H5)
     * 分页
     *
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "社区账单分页(不包含详情)", path = "/property-bill/page")
    @Authorization
    public ApiResult getPropBillForPropertyByCommunityId(@RequestBody @Validated BillRequest billRequest,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        Page<PropertyBill> list = propertyFeesFacade.findPropBillByBillRequest(billRequest, page, size);
        return ApiResult.ok(list);
    }

    /**
     * 获取账单详情列表,包括详情
     *
     * @param billRequest
     * @param page
     * @param size
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "社区账单分页(包含详情)", path = "/property-bill/with-details/page")
    @Authorization
    public ApiResult billWithDetailsPage(@RequestBody @Validated BillRequest billRequest,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "5") Integer size) {
        try {
            return ApiResult.ok(propertyFeesFacade.queryList(billRequest, page, size));
        } catch (BizException e) {
            return ApiResult.error(e);
        } catch (Exception e) {
            return ApiResult.error(-1, "查询账单明细异常");
        }
    }

    /**
     * 根据账单ID获取账单,不包含详细
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "账单信息(不包含详细)", path = "/property-bill/{id}/detail")
    @Authorization
    public ApiResult getPropBillDetail(@PathVariable("id") ObjectId id) {
        PropertyBill propertyBill = propertyFeesFacade.findById(id);
        return propertyBill == null ? ApiResult.error(-1, "查询账单异常") : ApiResult.ok(propertyBill);
    }

    /**
     * 根据账单ID获取账单,包含详细
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "账单信息(包含详细)", path = "/property-bill/{id}/details/list")
    @Authorization
    public ApiResult getPropBillDetailsByBillId(@PathVariable("id") ObjectId id) {
        BillDetailVO billDetailVO = propertyFeesFacade.findByIdWithDetail(id);
        return ApiResult.ok(billDetailVO);
    }

    /**
     * 物业手动缴费
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "手动缴费", path = "/property-bill/{id}/payment")
    @Authorization
    public ApiResult paymentB(@PathVariable("id") ObjectId id) {
        boolean result = propertyFeesFacade.paymentBillByProperty(id, SessionUtil.getTokenSubject().getUid());
        return result ? ApiResult.ok() : ApiResult.error(-1, "缴费失败");
    }

    /**
     * 发布账单
     * 推送提醒缴费
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.REMINDER_PAY_BILL
    )
    @GetMapping(name = "发布账单", path = "/property-bill/{id}/publish")
    @Authorization
    public ApiResult publishOneBill(@PathVariable("id") ObjectId id) {
        boolean result = propertyFeesFacade.updateBillStatusById(id,
                SessionUtil.getTokenSubject().getUid(), BillStatusType.UNPAYMENT.getKey());
        if (result) {
            PropertyBill propertyBill = propertyFeesFacade.findById(id);
            PushTask pushTask = this.appendPushResult(propertyBill);
            return WrapResult.create(ApiResult.ok(), pushTask);
        }
        return ApiResult.error(-1, "账单发布失败");
    }

    /**
     * 一键发布账单
     * 一键推送提醒缴费
     *
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.REMINDER_PAY_BILL
    )
    @PostMapping(name = "一键发布账单", path = "/property-bill/publishAll")
    @Authorization
    public ApiResult publishAllBills(@RequestBody PublishBillRequest publishBillRequest) {
        // 先查询本次需要发布的账单集合
        List<PropertyBill> propertyBills = propertyFeesFacade.findByPublishBillRequest(
                publishBillRequest, SessionUtil.getCommunityId());
        if (propertyBills == null || propertyBills.size() == 0) {
            return ApiResult.error(-1, "没有需要发布的账单");
        }
        boolean result = propertyFeesFacade.publishAllBills(propertyBills,
                SessionUtil.getCommunityId(), SessionUtil.getTokenSubject().getUid());
        if (result) {
            // 推送缴费通知
            List<PushTask> pushTaskList = propertyBills.stream().map(this::appendPushResult).collect(Collectors.toList());
            return WrapResult.create(ApiResult.ok(), pushTaskList);
        }
        return ApiResult.error(-1, "一键发布账单失败");
    }

    /**
     * 单点推送催缴账单
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.ASK_FOR_PAY_BILL
    )
    @GetMapping(name = "单点催缴账单", path = "/property-bill/{id}/push-overdue-bill")
    @Authorization
    public ApiResult pushOverdueBill(@PathVariable("id") ObjectId id) {
        PropertyBill toGet = propertyFeesFacade.findById(id);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            return ApiResult.error(-1, "无效账单");
        }
        if (BillStatusType.OVERDUE.getKey() != toGet.getBillStatus()) {
            return ApiResult.error(-1, "此账单未达到催缴条件");
        }
        PushTask pushTask = this.appendPushResult(toGet);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }

    /**
     * 单点推送提醒缴费
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.REMINDER_PAY_BILL
    )
    @GetMapping(name = "单点提醒缴费", path = "/property-bill/{id}/push-unPay-bill")
    @Authorization
    public ApiResult pushUnPayBill(@PathVariable("id") ObjectId id) {
        PropertyBill toGet = propertyFeesFacade.findById(id);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY) {
            return ApiResult.error(-1, "无效账单");
        }
        if (BillStatusType.UNPAYMENT.getKey() != toGet.getBillStatus()) {
            return ApiResult.error(-1, "此账单未达到提醒缴费条件");
        }
        log.info("提醒业主缴费推送...");
        PushTask pushTask = this.appendPushResult(toGet);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }

    /**
     * 一键催缴，超期未缴费的业主
     *
     * @param communityId
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.ASK_FOR_PAY_BILL
    )
    @GetMapping(name = "一键催缴", path = "/property-bill/{communityId}/push-all-overdue-bill")
    @Authorization
    public ApiResult pushAllOverdueBill(@PathVariable("communityId") ObjectId communityId) {
        // 获取超期未缴费的账单列表
        List<PropertyBill> list = propertyFeesFacade.findOverdueBillsByCommunityId(communityId);
        if (CollectionUtils.isEmpty(list)) {
            return ApiResult.error(-1, "没有需要催缴的账单");
        }
        log.info("需要催缴的账单数量 list.size:" + list.size() + ", 开始推送 ...");
        List<PushTask> pushTaskList = list.stream().map(this::appendPushResult).collect(Collectors.toList());
        return WrapResult.create(ApiResult.ok(), pushTaskList);
    }

    /**
     * 封装推送订单实体
     *
     * @param propertyBill
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    private PushTask appendPushResult(PropertyBill propertyBill) {

        PushTarget pushTarget = new PushTarget();
        pushTarget.setUserIds(Collections.singleton(propertyBill.getProprietorId()));

        PushBillVO pushBillVO = new PushBillVO();
        pushBillVO.setId(propertyBill.getId());
        pushBillVO.setProprietorId(propertyBill.getProprietorId());
        pushBillVO.setProprietorName(propertyBill.getProprietorName());
        pushBillVO.setBillStatus(propertyBill.getBillStatus());
        pushBillVO.setRoomLocation(propertyBill.getRoomLocation() == null ? "" : propertyBill.getRoomLocation());
        pushBillVO.setTotalAmount(StringUtil.makePriceKeep2Decimal(propertyBill.getTotalAmount()));
        Date accountingDate = propertyBill.getAccountingDate();
        if (accountingDate != null) {
            String accountingDateStr = DateUtils.formatDate(accountingDate, "yyyy-MM");
            pushBillVO.setAccountingDate(accountingDateStr);
        }
        Date overdueDate = propertyBill.getOverdueDate();
        if (overdueDate != null) {
            String overdueDateDateStr = DateUtils.formatDate(overdueDate, "yyyy-MM-dd");
            pushBillVO.setOverdueDate(overdueDateDateStr);
        }
        pushBillVO.setCommunityId(propertyBill.getCommunityId());

        PushTask pushTask = new PushTask();
        pushTask.setPushTarget(pushTarget);
        pushTask.setDataObject(pushBillVO);
        return pushTask;
    }

    /**
     * @param orderRequest
     * @param request
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "支付账单", path = "/property-bill/order")
    @Authorization
    public ApiResult orderPropertyBill(@Validated @RequestBody OrderRequest orderRequest,
                                       HttpServletRequest request) {
        PropertyBill propertyBill = propertyFeesFacade.getValidPropertyBill(orderRequest.getId(),
                orderRequest.getTotalAmount());

        CommunityTradeAccount communityTradeAccount =
                communityTradeAccountFacade.getCommunityTradeAccountByCommunityIdAndClientAndPlatfrom(
                        propertyBill.getCommunityId(), SessionUtil.getAppSubject().getClient(),
                        orderRequest.getPlatform());

        String notifyUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 ? "" : ":" + request.getServerPort())
                + tradePaymentNotifyUri.replace("{platform}",
                PlatformType.fromValue(communityTradeAccount.getPlatform()).name().toLowerCase());
        log.info("回调路径：" + notifyUrl);

        TradeOrder tradeOrder = generateTradeOrder(propertyBill, SessionUtil.getTokenSubject().getUid(),
                communityTradeAccount.getTradeType(), communityTradeAccount.getTradeAccountId(),
                "120.196.55.41", notifyUrl);
        Order order = tradeFacade.createTrade(tradeOrder);

        propertyFeesFacade.updateTradeIdById(orderRequest.getId(), order.getTradeId());

        return ApiResult.ok(order);
    }

    /**
     * 封装支付订单实体
     *
     * @param propertyBill
     * @param operatorId
     * @param tradeType
     * @param tradeAccountId
     * @param userIp
     * @param notifyUrl
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    private TradeOrder generateTradeOrder(PropertyBill propertyBill, ObjectId operatorId, Integer tradeType,
                                          ObjectId tradeAccountId, String userIp, String notifyUrl) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setTradeId(propertyBill.getTradeId());
        tradeOrder.setTradeType(tradeType);
        tradeOrder.setBizType(TradeBizType.PROPERTY_BILL.value());
        tradeOrder.setUserId(operatorId);
        tradeOrder.setTitle(propertyBill.getRoomLocation() + propertyBill.getTitle());
        tradeOrder.setTradeAccountId(tradeAccountId);
        tradeOrder.setGoodsType(GoodsType.VIRTUAL.value());
        tradeOrder.setTotalAmount(propertyBill.getTotalAmount());
        tradeOrder.setUserIp(userIp);
        tradeOrder.setNotifyUrl(notifyUrl);
        return tradeOrder;
    }

    //================================================property-bill-end=================================================

    //================================================bill-detail-start=================================================

    /**
     * 更新多个子账单详细
     *
     * @param billDetailVO
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "更新多个子账单详细", path = "/bill-detail/{billId}/editAll")
    @Authorization
    public ApiResult editOneBillDetail(@PathVariable("billId") ObjectId billId,
                                       @RequestBody BillDetailVO billDetailVO) {
        List<PropBillDetail> propBillDetails = billDetailVO.getBillDetailList();
        propertyFeesFacade.updateBillDetailsByBillId(SessionUtil.getTokenSubject().getUid(), billId, propBillDetails);
        return ApiResult.ok();
    }
    //================================================bill-detail-end===================================================

    //================================================fees-item-start===================================================

    /**
     * 更新收费项目（旧）
     *
     * @param id
     * @param propFeeItem
     * @return
     * @since 2018-08-02
     */
    @Deprecated
    @PostMapping(name = "编辑收费项目(旧)", path = "/item/edit/{id}")
    @Authorization
    public ApiResult editPropFeeItem(@PathVariable("id") ObjectId id,
                                     @RequestBody PropFeeItem propFeeItem) {
        propFeeItem.setId(id);
        propFeeItem.setModifierId(SessionUtil.getTokenSubject().getUid());
        propFeeItem = propFeeItemFacade.updateFeeItem(propFeeItem);
        return propFeeItem == null ? ApiResult.error(-1, "更新收费项目失败") : ApiResult.ok(propFeeItem);
    }

    /**
     * 更新收费项目（新）
     *
     * @param propFeeItem
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "编辑收费项目", path = "/item/edit")
    @Authorization
    public ApiResult editPropFeeItem(@Validated(PropFeeItem.Update.class) @RequestBody PropFeeItem propFeeItem) {
        propFeeItem.setModifierId(SessionUtil.getTokenSubject().getUid());
        propFeeItem = propFeeItemFacade.updateFeeItem(propFeeItem);
        return propFeeItem == null ? ApiResult.error(-1, "更新收费项目失败") : ApiResult.ok(propFeeItem);
    }

    /**
     * 删除收费项目,包括规则
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "删除收费项目(包括规则)", path = "/item/delete/{id}")
    @Authorization
    public ApiResult deletePropFeeItem(@PathVariable("id") ObjectId id) {
        propFeeItemFacade.deleteById(SessionUtil.getTokenSubject().getUid(), id);
        return ApiResult.ok();
    }

    /**
     * 分页查询收费项目
     *
     * @param page
     * @param size
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "收费项目分页", path = "/item/page")
    @Authorization
    public ApiResult getPropFeeItemPage(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return ApiResult.ok(propFeeItemFacade.queryPage(SessionUtil.getCommunityId(), page, size));
    }

    /**
     * 收费项目详情
     *
     * @param id
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "收费项目详情", path = "/item/{id}/detail")
    @Authorization
    public ApiResult propFeeItemDetail(@PathVariable("id") ObjectId id) {
        PropFeeItem propFeeItem = propFeeItemFacade.findById(id);
        if (propFeeItem == null) {
            throw DATA_INVALID;
        }
        return ApiResult.ok(propFeeItem);
    }

    /**
     * 验证项目是否已经存在
     *
     * @param propFeeItem
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    private boolean checkItemExistForAdd(PropFeeItem propFeeItem) {
        PropFeeItem toCheck = propFeeItemFacade.findByCommunityIdAndItemName(
                propFeeItem.getCommunityId(), propFeeItem.getItemName());
        return toCheck != null;
    }
    //===============================================fees-item-end======================================================

    //===============================================fees-rule-start====================================================

    /**
     * 获取收费规则计费类型集合
     *
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @GetMapping(name = "收费规则计费类型列表", path = "/rule-types")
    public ApiResult getFeesRuleTypes() {
        Map result = new HashMap();
        List<Map> list = new ArrayList<>();
        for (FeesRuleType feesRuleType : FeesRuleType.values()) {
            Map map = new HashMap();
            map.put("key", feesRuleType.getKey());
            map.put("value", feesRuleType.getValue());
            list.add(map);
        }
        result.put("ruleTypes", list);
        return ApiResult.ok(result);
    }

    /**
     * 新增收费项目及规则，统一设置楼栋
     *
     * @param itemRuleRequest
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "新增收费项目及规则", path = "/item-rule/addAll")
    @Authorization
    public ApiResult addFeeRuleForAllBuilding(@RequestBody @Validated ItemRuleRequest itemRuleRequest) {

        PropFeeItem propFeeItem = new PropFeeItem();
        propFeeItem.setCommunityId(itemRuleRequest.getCommunityId());
        propFeeItem.setPropertyId(itemRuleRequest.getPropertyId());
        propFeeItem.setItemName(itemRuleRequest.getItemName());
        if (this.checkItemExistForAdd(propFeeItem)) {
            return ApiResult.error(-1, "收费项目已存在");
        }
        propFeeItem.setIsAutoBill(itemRuleRequest.getIsAutoBill());
        if (itemRuleRequest.getIsAutoBill() == null) {
            propFeeItem.setIsAutoBill(Boolean.TRUE);
        }
        propFeeItem.setType(itemRuleRequest.getType());
        propFeeItem.setCreatorId(SessionUtil.getTokenSubject().getUid());
        propFeeItem = propFeeItemFacade.addFeeItem(propFeeItem);
        if (propFeeItem == null) {
            return ApiResult.error(-1, "新增失败");
        }
        Rule rule = new Rule();
        BeanUtils.copyProperties(itemRuleRequest, rule);
        rule.setFeeItemId(propFeeItem.getId());
        rule.setFeeItemName(propFeeItem.getItemName());
        rule.setCreatorId(SessionUtil.getTokenSubject().getUid());
        boolean result = feeRuleFacade.addRuleForAllBuilding(rule);
        return !result ? ApiResult.error(-1, "新增失败") : ApiResult.ok();
    }

    /**
     * 修改规则（旧）
     *
     * @param id
     * @param rule
     * @return
     * @since 20180718
     */
    @GetMapping(name = "编辑收费规则(旧)", path = "/rule/{id}/edit")
    @Deprecated
    @Authorization
    public ApiResult editFeeRule(@PathVariable("id") ObjectId id, Rule rule) {
        rule.setModifierId(SessionUtil.getTokenSubject().getUid());
        rule = feeRuleFacade.updateRule(rule);
        return rule == null ? ApiResult.error(-1, "更新失败") : ApiResult.ok(rule);
    }

    /**
     * 修改规则（新）
     *
     * @param rule
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "编辑收费规则", path = "/rule/edit")
    @Authorization
    public ApiResult editFeeRule(@RequestBody Rule rule) {
        rule.setModifierId(SessionUtil.getTokenSubject().getUid());
        rule = feeRuleFacade.updateRule(rule);
        return rule == null ? ApiResult.error(-1, "更新失败") : ApiResult.ok(rule);
    }

    /**
     * 分页查询
     *
     * @param toGet
     * @param page
     * @param size
     * @return
     * @since 2019-10-30
     */
    @Deprecated
    @PostMapping(name = "收费规则分页", path = "/rule/page")
    @Authorization
    public ApiResult pageFeeRule(@RequestBody Rule toGet,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        toGet.setCommunityId(SessionUtil.getCommunityId());
        Page<Rule> rulePage = feeRuleFacade.queryPage(toGet, page, size);
        return ApiResult.ok(rulePage);
    }
    //================================================fees-rule-end=====================================================


    // ============================== 物业费模块新接口 ===== 20191030 bills start ========================================
    // ====================================== 收费项目 ==================================================================
    @GetMapping(name = "收费项目分页", path = "/items")
    @Authorization
    public ApiResult pagingQueryItems(String name,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        FeesPageQuery pageQuery = new FeesPageQuery();
        pageQuery.setCommunityId(SessionUtil.getCommunityId());
        pageQuery.setName(name);
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return ApiResult.ok(feesFacade.pagingQueryItems(pageQuery));
    }

    @GetMapping(name = "收费项目列表", path = "/items/list")
    @Authorization
    public ApiResult listItems(String name) {
        return ApiResult.ok(feesFacade.listItemsByCommunityIdAndName(SessionUtil.getCommunityId(), name));
    }

    @PostMapping(name = "新增收费项目", path = "/items/add")
    @Authorization
    public ApiResult addItems(@RequestBody @Validated(Item.Add.class) Item item) {
        item.setCommunityId(SessionUtil.getCommunityId());
        item.setCompanyId(SessionUtil.getCompanyId());
        item.setCreator(SessionUtil.getTokenSubject().getUid());
        feesFacade.addItem(item);
        return ApiResult.ok();
    }

    @PostMapping(name = "编辑收费项目", path = "/items/modify")
    @Authorization
    public ApiResult modifyItems(@RequestBody @Validated(Item.Modify.class) Item item) {
        return ApiResult.ok(feesFacade.modifyItem(item));
    }

    @GetMapping(name = "收费项目详情", path = "/items/{id}/detail")
    @Authorization
    public ApiResult getItemById(@PathVariable ObjectId id) {
        return ApiResult.ok(feesFacade.findItemByItemId(id));
    }

    @PostMapping(name = "删除收费项目", path = "/items/{id}/delete")
    @Authorization
    public ApiResult deleteItem(@PathVariable ObjectId id) {
        feesFacade.deleteItemByItemId(id);
        return ApiResult.ok();
    }

    // ====================================== 物业账单 ==================================================================

    @GetMapping(name = "账单分页", path = "/bills")
    @Authorization
    public ApiResult pagingQueryBills(@RequestParam(required = false) ObjectId roomId,
                                      @RequestParam(required = false) ObjectId proprietorId,
                                      Integer status, Integer year,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        FeesPageQuery pageQuery = new FeesPageQuery();
        pageQuery.setCommunityId(SessionUtil.getCommunityId());
        pageQuery.setRoomId(roomId);
        pageQuery.setProprietorId(proprietorId);
        pageQuery.setStatus(status);
        if (year != null) {
            if (year < 1970 || year > 9999) {
                return ApiResult.ok(new Page<>());
            }
            pageQuery.setStartAt(DateUtils.getOneYearStart(year));
            pageQuery.setEndAt(DateUtils.getOneYearEnd(year));
        }
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return ApiResult.ok(feesFacade.pagingQueryBills(pageQuery));
    }

    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.REMINDER_PAY_BILL
    )
    @PostMapping(name = "新建账单", path = "/bills/add")
    @Authorization
    public ApiResult addBill(@RequestBody @Validated(BillVO.Add.class) BillVO billVO) {
        Household proprietor = householdFacade.findAuthOwnerByRoom(billVO.getRoomId());
        if (proprietor == null || !SessionUtil.getCommunityId().equals(proprietor.getCommunityId())) {
            throw NO_PROPRIETOR;
        }
        billVO.setProprietorId(proprietor.getUserId());
        billVO.setProprietorName(proprietor.getUserName());
        billVO.setRoomLocation(proprietor.getRoomLocation());
        billVO.setCompanyId(SessionUtil.getCompanyId());
        billVO.setCommunityId(proprietor.getCommunityId());
        billVO.setBuildingId(proprietor.getBuildingId());
        billVO.setCreator(SessionUtil.getTokenSubject().getUid());
        billVO.setModifier(billVO.getCreator());
        Bill bill = feesFacade.addBill(billVO);
        if (billVO.getSaveTemplate()) {
            // 保存套餐
            TemplateVO templateVO = new TemplateVO();
            BeanUtils.copyProperties(billVO, templateVO);
            feesFacade.addTemplate(templateVO);
        }
        if (billVO.getNotify()) {
            // 发布账单（通知业主缴费）
            Bill toPublish = feesFacade.publishBillById(bill.getId());
            if (toPublish == null) {
                throw FEES_BILL_ALREADY_NOTIFICATION;
            }
            PushTask pushTask = appendBillPushEntity(toPublish);
            return WrapResult.create(ApiResult.ok(), pushTask);
        }
        return ApiResult.ok();
    }

    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.REMINDER_PAY_BILL
    )
    @PostMapping(name = "编辑账单", path = "/bills/modify")
    @Authorization
    public ApiResult modifyBill(@RequestBody @Validated(BillVO.Modify.class) BillVO billVO) {
        billVO.setModifier(SessionUtil.getTokenSubject().getUid());
        feesFacade.modifyBill(billVO);
        if (billVO.getNotify()) {
            // 发布账单（通知业主缴费）
            Bill toPublish = feesFacade.publishBillById(billVO.getId());
            if (toPublish == null) {
                throw FEES_BILL_ALREADY_NOTIFICATION;
            }
            PushTask pushTask = appendBillPushEntity(toPublish);
            return WrapResult.create(ApiResult.ok(), pushTask);
        }
        return ApiResult.ok();
    }

    @PostMapping(name = "删除账单", path = "/bills/{id}/delete")
    @Authorization
    public ApiResult deleteBill(@PathVariable ObjectId id) {
        feesFacade.deleteBillById(id);
        return ApiResult.ok();
    }

    @GetMapping(name = "查看账单明细", path = "/bills/{id}/detail")
    @Authorization
    public ApiResult getBillById(@PathVariable ObjectId id) {
        BillVO billVO = feesFacade.findBillWithDetailById(id);
        return ApiResult.ok(billVO);
    }

    /**
     * 通知业主缴费
     * 推送提醒缴费
     *
     * @param id
     * @return
     */
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.REMINDER_PAY_BILL
    )
    @PostMapping(name = "通知业主缴费", path = "/bills/{id}/notify")
    @Authorization
    public ApiResult notifyBill(@PathVariable("id") ObjectId id) {
        // 发布账单
        Bill bill = feesFacade.publishBillById(id);
        if (bill == null) {
            throw FEES_BILL_ALREADY_NOTIFICATION;
        }
        PushTask pushTask = appendBillPushEntity(bill);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }

    /**
     * 推送催缴账单
     *
     * @param id
     * @return
     */
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = {ClientType.HOUSEHOLD},
            point = PushPointEnum.ASK_FOR_PAY_BILL
    )
    @GetMapping(name = "催缴物业费", path = "/bills/{id}/expedite")
    @Authorization
    public ApiResult expediteBill(@PathVariable("id") ObjectId id) {
        Bill toGet = feesFacade.findBillById(id);
        if (toGet == null) {
            throw FEES_BILL_NOT_EXISTS;
        }
        if (BillStatusType.PAYMENT.getKey() == toGet.getStatus()) {
            throw FEES_BILL_ALREADY_PAID;
        }
        if (BillStatusType.UNPAYMENT.getKey() != toGet.getStatus()) {
            throw FEES_BILL_NOT_ALLOW_EXPEDITING;
        }
        PushTask pushTask = appendBillPushEntity(toGet);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }

    @PostMapping(name = "人工收费", path = "/bills/pay-offline")
    @Authorization
    public ApiResult payBillOffline(@RequestBody @Validated(BillVO.Pay.class) BillVO payVO) {
        payVO.setModifier(SessionUtil.getTokenSubject().getUid());
        feesFacade.payBillOffline(payVO);
        return ApiResult.ok();
    }

    @PostMapping(name = "线上支付账单", path = "/bills/pay-online")
    @Authorization
    public ApiResult payBillOnline(@Validated @RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        // 获取账单，并校验总金额
        Bill bill = feesFacade.getValidBill(orderRequest.getId(), orderRequest.getTotalAmount());

        CommunityTradeAccount tradeAccount =
                communityTradeAccountFacade.getCommunityTradeAccountByCommunityIdAndClientAndPlatfrom(
                        bill.getCommunityId(), SessionUtil.getAppSubject().getClient(), orderRequest.getPlatform());

        if (tradeAccount == null) {
            log.info("获取社区物业收款账号异常...communityTradeAccount: {}", tradeAccount);
            throw OPERATION_FAILURE;
        }
        String notifyUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 ? "" : ":" + request.getServerPort())
                + tradePaymentNotifyUri.replace("{platform}",
                PlatformType.fromValue(tradeAccount.getPlatform()).name().toLowerCase());
        log.info("线上支付账单回调路径：{}", notifyUrl);

        TradeOrder tradeOrder = generateTradeOrder(bill, SessionUtil.getTokenSubject().getUid(),
                tradeAccount.getTradeType(), tradeAccount.getTradeAccountId(), "120.196.55.41", notifyUrl);
        Order order = tradeFacade.createTrade(tradeOrder);

        // 更新账单的交易订单号
        feesFacade.updateTradeIdById(orderRequest.getId(), order.getTradeId());
        return ApiResult.ok(order);
    }

    // ====================================== 收费模板 ==================================================================

    @GetMapping(name = "收费模板分页", path = "/templates")
    @Authorization
    public ApiResult pagingQueryTemplates(String name,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        FeesPageQuery pageQuery = new FeesPageQuery();
        pageQuery.setCommunityId(SessionUtil.getCommunityId());
        pageQuery.setName(name);
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return ApiResult.ok(feesFacade.pagingQueryTemplates(pageQuery));
    }

    @GetMapping(name = "收费模板列表", path = "/templates/list")
    @Authorization
    public ApiResult listTemplates(String name) {
        return ApiResult.ok(feesFacade.listTemplates(SessionUtil.getCommunityId(), name));
    }

    @PostMapping(name = "新建收费模板", path = "/templates/add")
    @Authorization
    public ApiResult addTemplate(@RequestBody @Validated(TemplateVO.Add.class) TemplateVO templateVO) {
        templateVO.setCompanyId(SessionUtil.getCompanyId());
        templateVO.setCommunityId(SessionUtil.getCommunityId());
        templateVO.setCreator(SessionUtil.getTokenSubject().getUid());

        feesFacade.addTemplate(templateVO);
        return ApiResult.ok();
    }

    @PostMapping(name = "编辑收费模板", path = "/templates/modify")
    @Authorization
    public ApiResult modifyTemplate(@RequestBody @Validated(TemplateVO.Modify.class) TemplateVO templateVO) {
        feesFacade.modifyTemplate(templateVO);
        return ApiResult.ok();
    }

    @PostMapping(name = "删除收费模板", path = "/templates/{id}/delete")
    @Authorization
    public ApiResult deleteTemplate(@PathVariable ObjectId id) {
        feesFacade.deleteTemplateById(id);
        return ApiResult.ok();
    }

    @GetMapping(name = "查看收费模板明细", path = "/templates/{id}/detail")
    @Authorization
    public ApiResult getTemplateById(@PathVariable ObjectId id) {
        return ApiResult.ok(feesFacade.findTemplateWithDetailById(id));
    }

    /**
     * 房间绑定物业费套餐
     *
     * @param room
     * @return
     */
    @PostMapping(name = "房间绑定物业费套餐", path = "/templates/room/bind")
    @Authorization
    public ApiResult bindFeesTemplate(@Validated(Room.BindFeesTemplate.class) @RequestBody Room room) {
        roomFacade.checkExistByCommunityIdAndRoomId(SessionUtil.getCommunityId(), room.getId());
        feesFacade.checkTemplateExistByCommunityIdAndTemplateId(SessionUtil.getCommunityId(), room.getFeesTemplateId());
        roomFacade.bindFeesTemplate(room.getId(), room.getFeesTemplateId());
        return ApiResult.ok();
    }

    /**
     * 房间解绑物业费套餐
     *
     * @param id
     * @return
     */
    @PostMapping(name = "房间解绑物业费套餐", path = "/templates/room/{id}/unbind")
    @Authorization
    public ApiResult unbindFeesTemplate(@PathVariable("id") ObjectId id) {
        roomFacade.checkExistByCommunityIdAndRoomId(SessionUtil.getCommunityId(), id);
        roomFacade.bindFeesTemplate(id, null);
        return ApiResult.ok();
    }

    @GetMapping(name = "获取房间及账单信息", path = "/templates/room/{id}/bills")
    @Authorization
    public ApiResult getRoomInfoAndBillsByRoomId(@PathVariable ObjectId id) {
        RoomTemplateBillVO resultVO = new RoomTemplateBillVO();
        Household household = householdFacade.findAuthOwnerByRoom(id);
        if (household == null) {
            return ApiResult.ok(resultVO);
        }
        resultVO.setRoomId(id);
        resultVO.setProprietor(household.getUserName());
        ObjectId templateId = roomFacade.findTemplateIdById(id);
        if (templateId != null) {
            Template template = feesFacade.findTemplateById(templateId);
            if (template != null) {
                resultVO.setTemplateId(templateId);
                resultVO.setTemplateName(template.getName());
            }
        }
        FeesPageQuery pageQuery = new FeesPageQuery();
        pageQuery.setCommunityId(SessionUtil.getCommunityId());
        pageQuery.setRoomId(id);
        pageQuery.setPage(1);
        pageQuery.setSize(10);
        Page<BillVO> bills = feesFacade.pagingQueryBills(pageQuery);
        if (bills.getTotal() == 0) {
            return ApiResult.ok(resultVO);
        }
        resultVO.setBills(bills);
        List<Bill> unpaymentBills = feesFacade.findBillByRoomIdAndStatus(id, BillStatusType.UNPAYMENT.getKey());
        resultVO.setUnpaymentNum(unpaymentBills.size());
        unpaymentBills.forEach(b -> {
            if (b.getTotalPrice() != null) {
                resultVO.setUnpaymentMoney(resultVO.getUnpaymentMoney() + b.getTotalPrice());
            }
        });
        return ApiResult.ok(resultVO);
    }

    /**
     * 住房档案模板导出(只导出没有档案的房间)
     *
     * @return
     */
    @PostMapping(name = "导出账单", path = "/bills/export")
    @Authorization
    public void exportBills(HttpServletResponse response, @RequestBody ExportRequest exportRequest) {
        exportRequest.setCommunityId(SessionUtil.getCommunityId());
        Community community = commonCommunityFacade.getCommunityByCommunityId(exportRequest.getCommunityId());
        List<BillEntity> bills = feesFacade.listBillEntities(exportRequest);
        String sheetName = community.getName() + "物业账单";
        ExportParams params = new ExportParams();
        params.setSheetName(sheetName);
        params.setStyle(ExcelExportStylerImpl.class);
        Workbook workbook = ExcelExportUtil.exportExcel(params, BillEntity.class, bills);
        ExcelExportStylerImpl styler = new ExcelExportStylerImpl(workbook);
        CellStyle headerStyle = styler.getDefaultStyle(Font.COLOR_NORMAL);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        setBorder(headerStyle);
        Sheet sheet = workbook.getSheetAt(0);

        // 设置密码保护
        sheet.protectSheet("");
        // 设置表头样式
        Row header = sheet.getRow(0);
        header.getCell(0).setCellStyle(headerStyle);
        header.getCell(1).setCellStyle(headerStyle);
        header.getCell(2).setCellStyle(headerStyle);
        header.getCell(3).setCellStyle(headerStyle);
        header.getCell(4).setCellStyle(headerStyle);
        header.getCell(5).setCellStyle(headerStyle);
        header.getCell(6).setCellStyle(headerStyle);
        header.getCell(7).setCellStyle(headerStyle);
        header.getCell(8).setCellStyle(headerStyle);
//        header.getCell(9).setCellStyle(headerStyle);
//        header.getCell(10).setCellStyle(headerStyle);
//        header.getCell(11).setCellStyle(headerStyle);
        // key : 需要合并的列, value : 合并列的依赖列
        Map<Integer, int[]> mergeMap = new HashMap<>();
        int[] dependentColumn = {0};
        mergeMap.put(0, dependentColumn);
        mergeMap.put(1, dependentColumn);
        mergeMap.put(2, dependentColumn);
//        mergeMap.put(3, dependentColumn);
//        mergeMap.put(4, dependentColumn);
        // 如果启用column 3，4，6 则这里要变成11
        mergeMap.put(8, dependentColumn);
        PoiMergeCellUtil.mergeCells(sheet, mergeMap);
        try {
            response.setContentType("application/x-download;charset=iso8859-1");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(sheetName + "_" + TODAY + EXCEL_SUFFIX[0], "utf8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("exportBills IOException:", e);
        }
    }
    // ====================================== 私有方法 ==================================================================

    /**
     * 拼接账单消息推送实体
     *
     * @param bill
     * @return
     */
    private PushTask appendBillPushEntity(Bill bill) {

        PushBillVO pushBillVO = new PushBillVO();
        pushBillVO.setCommunityId(bill.getCommunityId());
        pushBillVO.setId(bill.getId());
        pushBillVO.setProprietorId(bill.getProprietorId());
        pushBillVO.setProprietorName(bill.getProprietorName());
        pushBillVO.setBillStatus(bill.getStatus());
        pushBillVO.setRoomLocation(bill.getRoomLocation() == null ? "" : bill.getRoomLocation());
        // 移位操作，保留两位小数
        pushBillVO.setTotalAmount(StringUtil.makePriceKeep2Decimal(bill.getTotalPrice()));
        PushTask pushTask = new PushTask();
        PushTarget pushTarget = new PushTarget();
        pushTarget.setUserIds(Collections.singleton(bill.getProprietorId()));
        pushTask.setPushTarget(pushTarget);
        pushTask.setDataObject(pushBillVO);
        return pushTask;
    }

    /**
     * 封装交易订单实体
     *
     * @param bill
     * @param operatorId
     * @param tradeType
     * @param tradeAccountId
     * @param userIp
     * @param notifyUrl
     * @return
     */
    private TradeOrder generateTradeOrder(Bill bill, ObjectId operatorId, Integer tradeType,
                                          ObjectId tradeAccountId, String userIp, String notifyUrl) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setTradeId(bill.getTradeId());
        tradeOrder.setTradeType(tradeType);
        tradeOrder.setBizType(TradeBizType.PROPERTY_BILL.value());
        tradeOrder.setUserId(operatorId);
        tradeOrder.setTitle(bill.getName());
        tradeOrder.setTradeAccountId(tradeAccountId);
        tradeOrder.setGoodsType(GoodsType.VIRTUAL.value());
        tradeOrder.setTotalAmount(bill.getTotalPrice());
        tradeOrder.setUserIp(userIp);
        tradeOrder.setNotifyUrl(notifyUrl);
        return tradeOrder;
    }

    /**
     * 设置单元格边框
     *
     * @param style
     */
    public static void setBorder(CellStyle style) {
        // 下边框
//        style.setBorderBottom(BorderStyle.THIN);
        // 左边框
//        style.setBorderLeft(BorderStyle.THIN);
        // 上边框
//        style.setBorderTop(BorderStyle.THIN);
        // 右边框
        style.setBorderRight(BorderStyle.THIN);
    }

    // ================================ 物业费模块新接口 === 20191030 bills end ==========================================
}
