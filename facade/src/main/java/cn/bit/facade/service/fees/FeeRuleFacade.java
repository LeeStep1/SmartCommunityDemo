package cn.bit.facade.service.fees;

import cn.bit.facade.model.fees.Rule;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface FeeRuleFacade {

    /**
     * 增加费用收费规则
     * @param rule
     * @return
     * @throws BizException
     */
    Rule addRule(Rule rule) throws BizException;

    /**
     * 根据itemId获取收费规则列表
     * @param id
     * @return
     */
    List<Rule> findByItemId(ObjectId id);

    /**
     * 更新
     * @param rule
     * @return
     */
    Rule updateRule(Rule rule);

    /**
     * 查询
     * @param id
     * @return
     */
    Rule findById(ObjectId id);

    /**
     * 分页查询
     * @param rule
     * @param page
     * @param size
     * @return
     */
    Page<Rule> queryPage(Rule rule, Integer page, Integer size);

    /**
     * 按社区统一设置楼栋收费规则
     * @param rule
     * @return
     */
    boolean addRuleForAllBuilding(Rule rule);

    /**
     * 根据项目ID 删除规则
     * @param modifierId
     * @param feeItemId
     * @return
     */
    Long deleteByFeeItemId(ObjectId modifierId, ObjectId feeItemId);

    /**
     * 查询规则列表
     * @param communityId
     * @param feeItemIds
     * @return
     */
    List<Rule> findByCommunityIdAndFeeItemIdIn(ObjectId communityId, Set<ObjectId> feeItemIds);
}