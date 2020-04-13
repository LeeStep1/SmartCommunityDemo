package cn.bit.fees.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.fees.Rule;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.fees.FeeRuleFacade;
import cn.bit.fees.dao.RuleRepository;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.fees.FeesBizException.FEES_RULE_NULL;

@Component("feeRuleFacade")
@Slf4j
public class FeeRuleFacadeImpl implements FeeRuleFacade
{
    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private BuildingFacade buildingFacade;

    @Override
    public Rule addRule(Rule rule) throws BizException {
        rule.setDataStatus(DataStatusType.VALID.KEY);
        rule.setCreateAt(new Date());
        return ruleRepository.insert(rule);
    }

    /**
     * 根据itemId获取收费规则列表
     *
     * @param feeItemId
     * @return
     */
    @Override
    public List<Rule> findByItemId(ObjectId feeItemId) {
        return ruleRepository.findAllByFeeItemIdAndDataStatus(feeItemId,DataStatusType.VALID.KEY);
    }

    @Override
    public Rule updateRule(Rule rule) {
        Rule toGet = ruleRepository.findById(rule.getId());
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw FEES_RULE_NULL;
        }
        rule.setId(null);
        rule.setUpdateAt(new Date());
        return ruleRepository.updateById(rule, toGet.getId());
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @Override
    public Rule findById(ObjectId id) {
        return ruleRepository.findById(id);
    }

    /**
     * 分页查询
     *
     * @param rule
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Rule> queryPage(Rule rule, Integer page, Integer size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.ASC, "buildingId"));
        org.springframework.data.domain.Page<Rule> pageList = ruleRepository.findByCommunityIdAndFeeItemIdIgnoreNullAndDataStatus(
                rule.getCommunityId(), rule.getFeeItemId(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(pageList);
    }

    /**
     * 按社区统一设置楼栋收费规则
     *
     * @param rule
     * @return
     */
    @Override
    public boolean addRuleForAllBuilding(Rule rule) {
        log.info("addRuleForAllBuilding start... rule:" + rule);
        Building toGetBuilding = new Building();
        toGetBuilding.setCommunityId(rule.getCommunityId());
//        toGetBuilding.setOpen(Boolean.TRUE);//暂时全部楼栋都设置规则
        List<Building> buildingList = buildingFacade.queryList(toGetBuilding);
        if(buildingList == null || buildingList.size() == 0){
            throw new BizException("没有对应的楼栋");
        }
        rule.setDataStatus(DataStatusType.VALID.KEY);
        rule.setCreateAt(new Date());
        Set<ObjectId> buildingIds = buildingList.stream().map(Building::getId).collect(Collectors.toSet());
        List<Rule> buildingRuleList = ruleRepository.findByFeeItemIdAndBuildingIdInAndDataStatus(rule.getFeeItemId(), buildingIds, DataStatusType.VALID.KEY);
        Map<ObjectId, Rule> buildingRuleMap = new HashMap<>();
        if(buildingRuleList != null && buildingRuleList.size() > 0){
            for(Rule toCheck : buildingRuleList){
                buildingRuleMap.put(toCheck.getBuildingId(), toCheck);
            }
        }
        List<Rule> toSaveRuleList = new ArrayList<>();
        for (Building building : buildingList){
            Rule toSaveRule = buildingRuleMap.get(building.getId());
            if(toSaveRule != null){
                log.info("楼栋(" + building.getName() + ")已经存在此收费项目的计费规则...continue >>>>>");
                continue;
            }
            toSaveRule = new Rule();
            BeanUtils.copyProperties(rule, toSaveRule);
            toSaveRule.setBuildingId(building.getId());
            toSaveRule.setBuildingName(building.getName());
            toSaveRuleList.add(toSaveRule);
        }
        if(toSaveRuleList.size() > 0){
            ruleRepository.insertAll(toSaveRuleList);
            log.info("insertAll rules finish !!!");
            return true;
        }
        log.info("not need insert rules return false!!!");
        return false;
    }

    /**
     * 根据项目ID 删除规则
     *
     * @param modifierId
     * @param feeItemId
     * @return
     */
    @Override
    public Long deleteByFeeItemId(ObjectId modifierId, ObjectId feeItemId) {
        Rule toUpdate = new Rule();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);//数据失效
        toUpdate.setUpdateAt(new Date());
        toUpdate.setModifierId(modifierId);
        return ruleRepository.updateByFeeItemId(toUpdate, feeItemId);
    }

    /**
     * 查询规则列表
     *
     * @param communityId
     * @param feeItemIds
     * @return
     */
    @Override
    public List<Rule> findByCommunityIdAndFeeItemIdIn(ObjectId communityId, Set<ObjectId> feeItemIds) {
        return ruleRepository.findByCommunityIdAndFeeItemIdInAndDataStatus(communityId, feeItemIds, DataStatusType.VALID.KEY);
    }
}
