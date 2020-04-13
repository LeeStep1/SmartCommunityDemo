package cn.bit.fees.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.fees.PropFeeItem;
import cn.bit.facade.model.fees.Rule;
import cn.bit.facade.service.fees.PropFeeItemFacade;
import cn.bit.fees.dao.PropFeeItemRepository;
import cn.bit.fees.dao.RuleRepository;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static cn.bit.facade.exception.fees.FeesBizException.*;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;

@Component("propFeeItemFacade")
@Slf4j
public class PropFeeItemFacadeImpl implements PropFeeItemFacade {

    @Autowired
    private PropFeeItemRepository propFeeItemRepository;

    @Autowired
    private RuleRepository ruleRepository;

    /**
     * 增加物业收费项目
     *
     * @param item
     * @return
     * @throws BizException
     */
    @Override
    public PropFeeItem addFeeItem(PropFeeItem item) throws BizException {
        item.setDataStatus(DataStatusType.VALID.KEY);
        item.setCreateAt(new Date());
        return propFeeItemRepository.insert(item);
    }

    /**
     * 更新收费项目
     *
     * @param propFeeItem
     * @return
     */
    @Override
    public PropFeeItem updateFeeItem(PropFeeItem propFeeItem) {
        if(propFeeItem.getItemName() != null && propFeeItem.getItemName().trim().length() == 0){
            throw FEES_ITEM_NAME_NULL;
        }
        PropFeeItem toGet = propFeeItemRepository.findById(propFeeItem.getId());
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw FEES_ITEM_NULL;
        }

        PropFeeItem toCheck = propFeeItemRepository.findByCommunityIdAndItemNameAndDataStatus(toGet.getCommunityId(), propFeeItem.getItemName(), DataStatusType.VALID.KEY);
        if(toCheck != null && !propFeeItem.getId().equals(toCheck.getId())){
            throw FEES_ITEM_NAME_EXISTS;
        }
        propFeeItem.setId(null);
        propFeeItem.setUpdateAt(new Date());
        return propFeeItemRepository.updateById(propFeeItem, toGet.getId());
    }

    /**
     * 查询详情
     *
     * @param id
     * @return
     */
    @Override
    public PropFeeItem findById(ObjectId id) {
        return propFeeItemRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    /**
     * 删除收费项目
     *
     *
     * @param modifierId
     * @param id
     * @return
     */
    @Override
    public boolean deleteById(ObjectId modifierId, ObjectId id) {
        PropFeeItem toGet = propFeeItemRepository.findById(id);
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw FEES_ITEM_NULL;
        }
        PropFeeItem toUpdate = new PropFeeItem();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setModifierId(modifierId);
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate = propFeeItemRepository.updateById(toUpdate, id);
        if(toUpdate == null){
            throw FEES_ITEM_DELETE_FAILURE;
        }
        // 删除收费规则
        log.info("删除相关收费规则 ...");
        Rule rule = new Rule();
        rule.setDataStatus(DataStatusType.INVALID.KEY);//数据失效
        rule.setUpdateAt(new Date());
        rule.setModifierId(modifierId);
        Long result = ruleRepository.updateByFeeItemId(rule, id);
        log.info(result + "个相关收费规则被删除");
        return  result >= 0;
    }

    /**
     * 分页查询
     *
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<PropFeeItem> queryPage(ObjectId communityId, Integer page, Integer size) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.ASC, "createAt"));
        org.springframework.data.domain.Page<PropFeeItem> pageList = propFeeItemRepository.findByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(pageList);
    }

    @Override
    public PropFeeItem findOne(PropFeeItem toCheck) {
        return propFeeItemRepository.findOne(toCheck);
    }

    @Override
    public PropFeeItem findByCommunityIdAndItemName(ObjectId communityId, String itemName) {
        return propFeeItemRepository.findByCommunityIdAndItemNameAndDataStatus(communityId, itemName, DataStatusType.VALID.KEY);
    }

    /**
     * 查询收费项目列表
     *
     * @param communityIds
     * @return
     */
    @Override
    public List<PropFeeItem> findByCommunityIdIn(Set<ObjectId> communityIds) {
        return propFeeItemRepository.findByCommunityIdInAndDataStatus(communityIds, DataStatusType.VALID.KEY);
    }
}
