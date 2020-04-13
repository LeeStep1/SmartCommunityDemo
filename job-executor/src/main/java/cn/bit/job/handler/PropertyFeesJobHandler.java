package cn.bit.job.handler;

import cn.bit.facade.data.property.PropertyDTO;
import cn.bit.facade.enums.*;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.Parameter;
import cn.bit.facade.model.fees.PropBillDetail;
import cn.bit.facade.model.fees.PropFeeItem;
import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.facade.model.fees.Rule;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.ParameterFacade;
import cn.bit.facade.service.fees.FeeRuleFacade;
import cn.bit.facade.service.fees.PropFeeItemFacade;
import cn.bit.facade.service.fees.PropertyFeesFacade;
import cn.bit.facade.service.property.PropertyFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
// 对应任务调度中心的 'JobHandler'
@JobHandler(value = "propertyFeesJobHandler")
public class PropertyFeesJobHandler extends IJobHandler {
    @Autowired
    private PropertyFeesFacade propertyFeesFacade;

    @Autowired
    private PropFeeItemFacade propFeeItemFacade;

    @Autowired
    private FeeRuleFacade feeRuleFacade;

    @Autowired
    private ParameterFacade parameterFacade;

    @Autowired
    private PropertyFacade propertyFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    private static final String PROP_BILL_LIST = "PROP_BILL_LIST";
    private static final String BILL_DETAIL_LIST = "BILL_DETAIL_LIST";
    private static final String BILL_DATE_FORMAT = "yyyy年M月";
    private static final String BILL_DESC = "物业管理费";
    private static final int SIZE = 100;

    @Override
    public ReturnT<String> execute(String str) throws Exception {

        XxlJobLogger.log("Property bill generation begin, str = " + str);

        // 需要将具体的实现逻辑迁移至此处，达到解耦
        this.generatePropBill();

        XxlJobLogger.log("Property bill generation end");

        return ReturnT.SUCCESS;
    }

    private void generatePropBill() throws BizException {
        String today = DateUtils.formatDate(new Date(), DateUtils.DATE_FORMAT_DATEONLY);
        int page1 = 0;
        XxlJobLogger.log("today:" + today);
        Page<Parameter> parameterPage = null;
        do {
            page1++;
            // 获取所有今天需要生成账单的社区配置参数集合
            parameterPage = parameterFacade.findByTypeAndKeyAndValue(
                    ParamConfigType.BILL.getKey(), ParamKeyType.NEXTEFFECTDATE.name(), today + "", page1, SIZE);
            if (parameterPage == null || parameterPage.getTotal() == 0) {
                XxlJobLogger.log("today not need to generatePropBill end !!!");
                return;
            }
            // 需要生成账单的全部社区ID集合
            Set<ObjectId> allCommunityIds =
                    parameterPage.getRecords().stream().map(Parameter::getCommunityId).collect(Collectors.toSet());
            XxlJobLogger.log("today need to generatePropBill communityIds:" + allCommunityIds);
            List<Community> communityList = communityFacade.findByIds(allCommunityIds);
            if (communityList.isEmpty()) {
                XxlJobLogger.log("without any communities !!! generatePropBill end !!!");
                return;
            }
            Map<ObjectId, Community> communityMap = new HashMap<>();
            // 需要生成账单的有效社区ID集合
            Set<ObjectId> validCIds = communityList.stream().map(Community::getId).collect(Collectors.toSet());
            List<PropertyDTO> propertyDTOList = propertyFacade.findByCommunityIds(validCIds);
            communityList.forEach(community -> {
                propertyDTOList.stream().filter(propertyDTO -> propertyDTO.getCommunityId().equals(community.getId()))
                        .forEach(
                                propertyDTO -> {
                                    community.setPropertyId(propertyDTO.getCompanyId());
                                    community.setPropertyName(propertyDTO.getCompanyName());
                                }
                                );
                communityMap.put(community.getId(), community);
            });

            List<Parameter> parameterList = parameterFacade.findByCommunityIdInAndTypeAndKeyIn(
                    validCIds, ParamConfigType.BILL.getKey(), Arrays.asList(ParamKeyType.BILLCREATEDAY.name(),
                                                                            ParamKeyType.LASTEFFECTDATE.name(),
                                                                            ParamKeyType.CHARGINGSTANDARDS.name()));

            // 所有收费项目list
            List<PropFeeItem> feeItemList = propFeeItemFacade.findByCommunityIdIn(validCIds);
            if (CollectionUtils.isEmpty(feeItemList)) {
                XxlJobLogger.log("without any PropFeeItem !!! generatePropBill end !!!");
                return;
            }
            int page2 = 0;
            Page<UserToRoom> userToRoomPage = null;
            // 上月第一天
            Date lastMonthFirstDay = DateUtils.getPreviousMonthFirstDay();

            String lastMonth = DateUtils.formatDate(lastMonthFirstDay, BILL_DATE_FORMAT);
            String title = lastMonth + BILL_DESC;
            do {
                page2++;
                // 获取有效业主认证list
                userToRoomPage = userToRoomFacade.findValidProprietorsByCommunityIdIn(validCIds, page2, SIZE);
                if (userToRoomPage == null || userToRoomPage.getTotal() == 0) {
                    XxlJobLogger.log("without any valid proprietors !!! generatePropBill end !!!");
                    return;
                }
                // 社区对应的账单生产日
                Map<ObjectId, String> billCreateDayMap = new HashMap<>();
                // 社区对应的上期账单生成日
                Map<ObjectId, String> lastEffectDateMap = new HashMap<>();
                // 社区对应的收费标准
                Map<ObjectId, String> chargingStandardsMap = new HashMap<>();
                // 社区对应的收费项目集合
                Map<ObjectId, List<PropFeeItem>> feeItemListMap = new HashMap<>();
                // 社区对应的有效业主认证集合
                Map<ObjectId, List<UserToRoom>> userToRoomListMap = new HashMap<>();

                for (ObjectId communityId : validCIds) {

                    List<PropFeeItem> communityFeeItemList = feeItemList.stream().filter(
                            propFeeItem -> communityId.equals(propFeeItem.getCommunityId())).collect(Collectors.toList());
                    feeItemListMap.put(communityId, communityFeeItemList);

                    List<UserToRoom> communityUserToRoomList = userToRoomPage.getRecords().stream().filter(
                            userToRoom -> communityId.equals(userToRoom.getCommunityId())).collect(Collectors.toList());
                    userToRoomListMap.put(communityId, communityUserToRoomList);

                    billCreateDayMap.put(
                            communityId, getParamValueByKey(parameterList, communityId, ParamKeyType.BILLCREATEDAY.name()));
                    lastEffectDateMap.put(
                            communityId, getParamValueByKey(parameterList, communityId, ParamKeyType.LASTEFFECTDATE.name()));
                    chargingStandardsMap.put(
                            communityId, getParamValueByKey(parameterList, communityId, ParamKeyType.CHARGINGSTANDARDS.name()));
                }

                // 遍历得到需要产出账单的社区
                for (Parameter parameter : parameterPage.getRecords()) {
                    XxlJobLogger.log(parameter.getCommunityId() + "_community generatePropBill start...");

                    if (cn.bit.framework.utils.string.StringUtil.isBlank(billCreateDayMap.get(parameter.getCommunityId()))
                            || cn.bit.framework.utils.string.StringUtil.isBlank(lastEffectDateMap.get(parameter.getCommunityId()))
                            || cn.bit.framework.utils.string.StringUtil.isBlank(chargingStandardsMap.get(parameter.getCommunityId()))) {
                        XxlJobLogger.log("社区没有配置相关账单参数");
                        continue;
                    }

                    // 获取社区信息
                    Community community = communityMap.get(parameter.getCommunityId());
                    if (community == null) {
                        XxlJobLogger.log("社区不存在/未开放");
                        continue;
                    }
                    this.updateFeesParam(parameter, billCreateDayMap, chargingStandardsMap);
                    // 封装需要生成的账单实体集合
                    Map<String, List> billMap =
                            packageBillEntity(community, userToRoomListMap, feeItemListMap, title, lastMonthFirstDay);

                    if (billMap.isEmpty()) {
                        XxlJobLogger.log("当前社区没有生成任何账单");
                        continue;
                    }

                    // 需要写入数据库的总账单集合
                    propertyFeesFacade.insertAllBills(billMap.get(PROP_BILL_LIST));
                    // 需要写入数据库的子账单集合
                    propertyFeesFacade.insertAllBillDetails(billMap.get(BILL_DETAIL_LIST));
                    XxlJobLogger.log("community generatePropBill finish !!!");
                }
            } while (userToRoomPage != null && userToRoomPage.hasNextPage());
        } while (parameterPage != null && parameterPage.hasNextPage());
    }

    /**
     * 根据communityId，key 找出对应的value
     *
     * @param parameterList
     * @param communityId
     * @param key
     * @return
     */
    private String getParamValueByKey(List<Parameter> parameterList, ObjectId communityId, String key) {
        List<Parameter> list = parameterList.stream().filter(parameter -> communityId.equals(
                parameter.getCommunityId()) && parameter.getKey().equals(key)).collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(0).getValue();
    }

    /**
     * 获取需要插入的账单实体集合
     *
     * @param community
     * @param userToRoomListMap
     * @param feeItemListMap
     * @param title
     * @param lastMonthFirstDay
     * @return
     */
    private Map<String, List> packageBillEntity(Community community,
                                                Map<ObjectId, List<UserToRoom>> userToRoomListMap,
                                                Map<ObjectId, List<PropFeeItem>> feeItemListMap,
                                                String title,
                                                Date lastMonthFirstDay) {
        ObjectId communityId = community.getId();
        // 获取社区下所有收费项目
        List<PropFeeItem> communityFeeItemList = feeItemListMap.get(communityId);
        if (CollectionUtils.isEmpty(communityFeeItemList)) {
            XxlJobLogger.log("community without any propFeeItems, not need generatePropBill!!!");
            return Collections.EMPTY_MAP;
        }
        Set<ObjectId> feeItemIds = new HashSet<>();
        Map<ObjectId, PropFeeItem> feeItemMap = new HashMap<>();
        for (PropFeeItem propFeeItem : communityFeeItemList) {
            feeItemMap.put(propFeeItem.getId(), propFeeItem);
            feeItemIds.add(propFeeItem.getId());
        }
        // 根据社区及收费项目ID集合获取所有收费规则
        List<Rule> communityRules = feeRuleFacade.findByCommunityIdAndFeeItemIdIn(communityId, feeItemIds);
        if (CollectionUtils.isEmpty(communityRules)) {
            XxlJobLogger.log("community without any rules, not need generatePropBill!!!");
            return Collections.EMPTY_MAP;
        }
        // 获取社区下所有有效业主
        List<UserToRoom> userToRooms = userToRoomListMap.get(communityId);
        if (CollectionUtils.isEmpty(userToRooms)) {
            XxlJobLogger.log("community without any proprietors, not need generatePropBill!!!");
            return Collections.EMPTY_MAP;
        }

        Map<String, List> map = new HashMap<>();

        // 需要写入数据库的总账单集合
        List<PropertyBill> insertPropertyBills = new ArrayList<>();
        // 需要写入数据库的子账单集合
        List<PropBillDetail> insertBillDetails = new ArrayList<>();

        // 遍历有效业主，生成账单
        for (UserToRoom userToRoom : userToRooms) {
            // 用合同上的房屋面积，具备法律效应
            Integer area = userToRoom.getArea();
            if (area == null || area <= 0) {
                XxlJobLogger.log(userToRoom.getRoomLocation() +
                        " >>>>>>>>> room area is null, not need generate property bill !!!");
                continue;
            }
            // 创建总账单实体
            PropertyBill propertyBill =
                    createPropertyBill(userToRoom, title, lastMonthFirstDay, community.getPropertyId(), community.getName());
            // 根据规则添加子账单
            for (Rule rule : communityRules) {
                PropFeeItem feeItem = feeItemMap.get(rule.getFeeItemId());
                if (rule.getBuildingId().equals(userToRoom.getBuildingId()) && feeItem.getIsAutoBill()) {
                    // 创建子账单实体
                    PropBillDetail propBillDetail = createPropBillDetail(propertyBill, area, rule, feeItem.getType());
                    // 将子账单放入待插入数据库的集合里
                    insertBillDetails.add(propBillDetail);
                    // 合计各个子账单的金额
                    propertyBill.setTotalPrice(propertyBill.getTotalPrice() + propBillDetail.getSubtotal());
                    propertyBill.setTotalAmount(propertyBill.getTotalAmount() + propBillDetail.getTotalAmount());
                }
            }
            // 将总账单放入待插入数据库的集合里
            insertPropertyBills.add(propertyBill);
        }

        map.put(PROP_BILL_LIST, insertPropertyBills);
        map.put(BILL_DETAIL_LIST, insertBillDetails);
        return map;
    }


    /**
     * 初始化账单实体
     *
     * @param userToRoom
     * @param title
     * @param lastMonthFirstDay
     * @param propertyId
     * @param propertyName
     * @return
     */
    private PropertyBill createPropertyBill(UserToRoom userToRoom,
                                            String title,
                                            Date lastMonthFirstDay,
                                            ObjectId propertyId,
                                            String propertyName) {
        PropertyBill propertyBill = new PropertyBill();
        // 设定账单信息
        propertyBill.setId(new ObjectId());
        propertyBill.setTitle(title);
        // 未发布
        propertyBill.setBillStatus(BillStatusType.UNPUBLISHED.getKey());
        propertyBill.setTotalPrice(0L);
        propertyBill.setTotalAmount(0L);
        propertyBill.setAccountingDate(lastMonthFirstDay);
        propertyBill.setMakeAt(new Date());
        propertyBill.setCreateAt(new Date());
        propertyBill.setDataStatus(DataStatusType.VALID.KEY);

        // 设定业主信息
        propertyBill.setProprietorId(userToRoom.getProprietorId());
        propertyBill.setProprietorName(userToRoom.getName());

        // 房屋信息
        propertyBill.setCommunityId(userToRoom.getCommunityId());
        propertyBill.setBuildingId(userToRoom.getBuildingId());
        propertyBill.setRoomId(userToRoom.getRoomId());
        propertyBill.setRoomLocation(userToRoom.getRoomLocation());

        // 获取物业公司信息
        propertyBill.setPropertyId(propertyId);
        propertyBill.setPropertyName(propertyName);
        return propertyBill;
    }

    /**
     * 更新账单的生成日（上期，下期）
     *
     * @param parameter
     * @param billCreateDayMap
     * @param chargingStandardsMap
     */
    private void updateFeesParam(Parameter parameter,
                                 Map<ObjectId, String> billCreateDayMap,
                                 Map<ObjectId, String> chargingStandardsMap) {
        // 更新上期生成日
        Date date = new Date();
        String lastDateStr = DateUtils.formatDate(date, DateUtils.DATE_FORMAT_DATEONLY);
        Parameter toUpdate = new Parameter();
        toUpdate.setValue(lastDateStr);
        toUpdate.setUpdateAt(date);
        parameterFacade.updateWithSetValueAndUpdateAtByCommunityIdAndTypeAndKey(
                toUpdate, parameter.getCommunityId(), ParamConfigType.BILL.getKey(), ParamKeyType.LASTEFFECTDATE.name());
        XxlJobLogger.log("更新上期账单生成日完成 !!! lastDate:" + lastDateStr);
        // 下期生成日 根据收费标准来计算，现阶段按每月缴费计算
        int billCreateDay = Integer.parseInt(billCreateDayMap.get(parameter.getCommunityId()));
        int months = Integer.parseInt(chargingStandardsMap.get(parameter.getCommunityId()));
        String nextDate = DateUtils.getNextDateByMonthsWithDay(date, months, billCreateDay);
        toUpdate = new Parameter();
        toUpdate.setValue(nextDate);
        toUpdate.setUpdateAt(date);
        parameterFacade.updateWithSetValueAndUpdateAtById(toUpdate, parameter.getId());
        XxlJobLogger.log("更新下期账单生成日完成 !!! nextDate:" + nextDate);
    }

    /**
     * 物业管理费算法
     * 目前支持固定单价（单价*面积）及固定收费
     *
     * @param propertyBill
     * @param area
     * @param rule
     * @param type
     * @return
     * @throws BizException
     */
    private PropBillDetail createPropBillDetail(PropertyBill propertyBill,
                                                Integer area,
                                                Rule rule,
                                                Integer type) throws BizException {

        PropBillDetail propBillDetail = new PropBillDetail();

        propBillDetail.setBillId(propertyBill.getId());
        propBillDetail.setFeeItemId(rule.getFeeItemId());
        propBillDetail.setFeeItemName(rule.getFeeItemName());
        // 计费类型，前端用于控制是否可以修改此账单
        propBillDetail.setType(type);
        // 物业读数先不做
        //propBillDetail.setPreviousRead();
        //propBillDetail.setCurrentRead();
        //propBillDetail.setUsed();
        propBillDetail.setUnitPrice(rule.getUnitPrice());
        Long currentFee = 0L;

        // 计算当前费用
        if (type == FeesRuleType.CONSTANTUNITPRICE.getKey()) {
            // 面积*单价 除以100是因为面积存储的时候乘了100
            currentFee = propBillDetail.getUnitPrice().longValue() * area / 100;
        } else if (type == FeesRuleType.CONSTANTFEES.getKey()) {
            // 固定收费
            currentFee = new Long(propBillDetail.getUnitPrice());
        } else if (type == FeesRuleType.CUSTOM.getKey()) {
            // 自定义规则就是生成一个0元子账单让物业人员自己填写实际收费金额
            currentFee = new Long(0);
        }

        // 放大10000倍存入数据库，传到前端，避免失精度 总价按*10000计算
        propBillDetail.setCurrentFee(currentFee);
        // 往期收费暂时不做
        propBillDetail.setPreviousFee(0L);
        propBillDetail.setSubtotal(propBillDetail.getCurrentFee() + propBillDetail.getPreviousFee());
        // 放大100倍存入数据库，传到前端，避免失精度，此金额用于支付
        propBillDetail.setTotalAmount(StringUtil.calculateTotalAmount(propBillDetail.getSubtotal()));

        propBillDetail.setCreateAt(new Date());
        propBillDetail.setDataStatus(DataStatusType.VALID.KEY);
        return propBillDetail;
    }
}
