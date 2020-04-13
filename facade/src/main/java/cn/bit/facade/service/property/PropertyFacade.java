package cn.bit.facade.service.property;

import cn.bit.facade.data.property.PropertyDTO;
import cn.bit.facade.vo.property.Property;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;

public interface PropertyFacade {
    /**
     * 按Id查询
     * @param id
     * @return
     */
    Property findOne(ObjectId id) throws BizException;

	Property findByCommunityId(ObjectId communityId);

	List<PropertyDTO> findByCommunityIds(Collection<ObjectId> communityIds);
}
