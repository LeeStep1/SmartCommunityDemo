package cn.bit.fees.dao;

import cn.bit.facade.model.fees.Item;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemRepository extends MongoDao<Item, ObjectId>, MongoRepository<Item, ObjectId> {

    boolean existsByCommunityIdAndNameAndDataStatus(ObjectId communityId, String name, int dataStatus);

    Item updateByIdAndDataStatus(Item item, ObjectId id, int dataStatus);

    Page<Item> findByCommunityIdAndNameRegexIgnoreNullAndDataStatus(ObjectId communityId,
                                                                    String name, int dataStatus, Pageable pageable);

    List<Item> findByCommunityIdAndNameRegexIgnoreNullAndDataStatus(ObjectId communityId, String name, int dataStatus);

    Item findByIdAndDataStatus(ObjectId id, int dataStatus);
}
