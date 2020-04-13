package cn.bit.facade.service.fees;

import cn.bit.facade.model.fees.Bill;
import cn.bit.facade.model.fees.Item;
import cn.bit.facade.model.fees.Template;
import cn.bit.facade.poi.entity.BillEntity;
import cn.bit.facade.vo.fees.BillVO;
import cn.bit.facade.vo.fees.ExportRequest;
import cn.bit.facade.vo.fees.FeesPageQuery;
import cn.bit.facade.vo.fees.TemplateVO;
import cn.bit.facade.vo.statistics.PropertyBillSummaryRequest;
import cn.bit.facade.vo.statistics.PropertyBillSummaryResponse;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface FeesFacade {

    /**
     * 新增收费项目
     *
     * @param item
     */
    void addItem(Item item);

    /**
     * 编辑收费项目
     *
     * @param item
     * @return
     */
    Item modifyItem(Item item);

    /**
     * 分页查询收费项目列表
     *
     * @param pageQuery
     * @return
     */
    Page<Item> pagingQueryItems(FeesPageQuery pageQuery);

    /**
     * 查询某社区收费项目列表
     *
     * @param communityId
     * @param name
     * @return
     */
    List<Item> listItemsByCommunityIdAndName(ObjectId communityId, String name);

    /**
     * 删除收费项目
     *
     * @param itemId
     */
    void deleteItemByItemId(ObjectId itemId);

    /**
     * 查看收费项目详情
     *
     * @param itemId
     * @return
     */
    Item findItemByItemId(ObjectId itemId);

    /**
     * 新建物业账单
     *
     * @param billVO
     */
    Bill addBill(BillVO billVO);

    /**
     * 编辑账单
     *
     * @param billVO
     */
    void modifyBill(BillVO billVO);

    /**
     * 账单分页
     *
     * @param pageQuery
     * @return
     */
    Page<BillVO> pagingQueryBills(FeesPageQuery pageQuery);

    /**
     * 删除账单
     *
     * @param id
     */
    void deleteBillById(ObjectId id);

    /**
     * 查看账单明细信息
     *
     * @param id
     * @return
     */
    BillVO findBillWithDetailById(ObjectId id);

    /**
     * 发布账单（通知业主缴费）
     *
     * @param id
     * @return
     */
    Bill publishBillById(ObjectId id);

    /**
     * 查询账单信息
     *
     * @param id
     * @return
     */
    Bill findBillById(ObjectId id);

    /**
     * 人工收费
     *
     * @param payVO
     */
    void payBillOffline(BillVO payVO);

    /**
     * 获取账单，并校验总金额
     *
     * @param id
     * @param totalPrice
     * @return
     */
    Bill getValidBill(ObjectId id, Long totalPrice);

    /**
     * 更新账单的交易订单号
     *
     * @param id
     * @param tradeId
     */
    void updateTradeIdById(ObjectId id, Long tradeId);

    /**
     * 完成支付，修改订单状态
     *
     * @param tradeId
     */
    void finishedPaymentByTradeId(Long tradeId);

    /**
     * 清理账单的交易订单流水号
     *
     * @param tradeId
     */
    void clearTradeIdByTradeId(Long tradeId);

    /**
     * 收费模板分页
     *
     * @param pageQuery
     * @return
     */
    Page<TemplateVO> pagingQueryTemplates(FeesPageQuery pageQuery);

    /**
     * 收费模板列表
     *
     * @param communityId
     * @param name
     * @return
     */
    List<Template> listTemplates(ObjectId communityId, String name);

    /**
     * 新建收费模板
     *
     * @param templateVO
     * @return
     */
    Template addTemplate(TemplateVO templateVO);

    /**
     * 编辑收费模板
     *
     * @param templateVO
     */
    void modifyTemplate(TemplateVO templateVO);

    /**
     * 删除收费模板
     *
     * @param id
     */
    void deleteTemplateById(ObjectId id);

    /**
     * 查询收费模板详情
     *
     * @param id
     * @return
     */
    TemplateVO findTemplateWithDetailById(ObjectId id);

    /**
     * 校验收费模板是否存在
     *
     * @param communityId
     * @param templateId
     */
    void checkTemplateExistByCommunityIdAndTemplateId(ObjectId communityId, ObjectId templateId);

    /**
     * 查询收费模板
     *
     * @param id
     * @return
     */
    Template findTemplateById(ObjectId id);

    /**
     * 查询待导出账单列表
     *
     * @param exportRequest
     * @return
     */
    List<BillEntity> listBillEntities(ExportRequest exportRequest);

    /**
     * 统计各个状态账单数量
     *
     * @param communityId
     * @return
     */
    Map<String, Long> countBills(ObjectId communityId);

    /**
     * 从es统计账单
     *
     * @param request
     * @return
     */
    PropertyBillSummaryResponse getBillSummary(PropertyBillSummaryRequest request);

    /**
     * 查询房间下某状态订单列表
     * @param id
     * @param key
     * @return
     */
    List<Bill> findBillByRoomIdAndStatus(ObjectId id, Integer key);
}
