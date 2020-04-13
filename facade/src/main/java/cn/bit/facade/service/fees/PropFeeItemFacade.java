package cn.bit.facade.service.fees;

import cn.bit.facade.model.fees.PropFeeItem;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface PropFeeItemFacade
{
    /**
     * 增加物业收费项目
     * @param item
     * @return
     * @throws BizException
     */
    PropFeeItem addFeeItem(PropFeeItem item) throws BizException;

    /**
     * 更新收费项目
     * @param propFeeItem
     * @return
     */
    PropFeeItem updateFeeItem(PropFeeItem propFeeItem);

    /**
     * 查询详情
     * @param id
     * @return
     */
    PropFeeItem findById(ObjectId id);

    /**
     * 删除收费项目
     *
     * @param modifierId
     * @param id
     * @return
     */
    boolean deleteById(ObjectId modifierId, ObjectId id);

    /**
     * 分页查询
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    Page<PropFeeItem> queryPage(ObjectId communityId, Integer page, Integer size);

    PropFeeItem findOne(PropFeeItem toCheck);

    PropFeeItem findByCommunityIdAndItemName(ObjectId communityId, String itemName);

    /**
     * 查询收费项目列表
     * @param communityIds
     * @return
     */
    List<PropFeeItem> findByCommunityIdIn(Set<ObjectId> communityIds);
}