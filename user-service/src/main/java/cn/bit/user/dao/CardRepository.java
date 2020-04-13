package cn.bit.user.dao;

import cn.bit.facade.model.user.Card;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/8
 **/
public interface CardRepository extends MongoDao<Card, ObjectId>, MongoRepository<Card, ObjectId> {

    Card findByUserIdAndCommunityIdAndKeyTypeAndProcessTimeAfterAndDataStatus(
            ObjectId userId, ObjectId communityId, Integer keyType, Date now, Integer dataStatus);

    Card findByKeyNoAndKeyTypeAndDataStatus(String keyNo, Integer keyType, Integer dataStatus);

    Card findByKeyNoAndKeyIdAndDataStatus(String keyNo, String keyId, Integer dataStatus);

    int updateById(Card card, ObjectId id);

    Card findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    Card findByIdAndUserIdAndDataStatus(ObjectId id, ObjectId userId, Integer dataStatus);

    Card findByKeyNoAndDataStatus(String keyNo, Integer dataStatus);

    Card findByKeyNoAndCommunityIdAndDataStatus(String keyNo,
                                                ObjectId communityId,
                                                Integer dataStatus);

    Card findByUserIdAndCommunityIdAndKeyTypeAndRoomNameInAndProcessTimeGreaterThanAndDataStatus(
            ObjectId userId, ObjectId communityId, Integer keyType, Set<String> roomName,
            Date processTime, Integer dataStatus);

    Card findByUserIdAndCommunityIdAndKeyTypeAndDataStatus(
            ObjectId userId, ObjectId communityId, Integer keyType, Integer dataStatus);
    

    List<Card> findByUserIdAndCommunityIdAndKeyTypeInAndDataStatus(
            ObjectId userId, ObjectId communityId, Collection<Integer> keyType, Integer dataStatus);
    

    Boolean existsByCommunityIdAndKeyNoAndKeyTypeAndDataStatus(
            ObjectId communityId, String keyNo, Integer keyType, Integer dataStatus);

	Card updateByIdAndDataStatus(Card toUpdate, ObjectId id, int dataStatus);
	

    Page<Card> findByCommunityIdAndKeyNoIgnoreNullAndKeyTypeInAndNameIgnoreNullAndDataStatus(
            ObjectId communityId, String keyNo, List<Integer> keyTypes,
            String name, Integer dataStatus, Pageable pageable);

    Card updateByKeyIdAndDataStatus(Card toUpdate, String keyId, int dataStatus);

    List<Card> findByCommunityIdAndKeyNoAndKeyTypeAndDataStatusOrderByCreateAtDesc(
            ObjectId communityId, String keyNo, Integer keyType, int dataStatus);

    Card upsertWithUnsetIfNullRoomNameAndRoomIdByCommunityIdAndKeyNoAndKeyTypeAndDataStatus(Card card, ObjectId communityId,
                                                                                            String keyNo, Integer keyType, int dataStatus);

    List<Card> findByUserIdAndKeyTypeAndRoomNameInAndDataStatus(ObjectId userId, Integer keyType,
                                                                Collection<String> roomLocations, int dataStatus);

    Page<Card> findByCommunityIdAndUserIdAndKeyIdAndKeyNoAndKeyTypeAndDataStatusAllIgnoreNull(ObjectId communityId,
                                                                                              ObjectId userId,
                                                                                              String keyId,
                                                                                              String keyNo,
                                                                                              Integer keyType,
                                                                                              int dataStatus,
                                                                                              Pageable pageable);

    int updateWithPullAllRoomNameByKeyIdAndKeyNoAndDataStatus(Card card, String keyId, String keyNo, int dataStatus);

    List<Card> findByUserIdAndCommunityIdAndEndDateGreaterThanAndDataStatus(ObjectId userId,
                                                                            ObjectId communityId,
                                                                            Date endDate,
                                                                            int dataStatus);
}
