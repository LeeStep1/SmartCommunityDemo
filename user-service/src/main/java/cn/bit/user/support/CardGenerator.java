package cn.bit.user.support;

import cn.bit.facade.enums.CardStatusType;
import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.TimeUnitEnum;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.vo.user.card.CardVO;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.user.dao.CardRepository;
import cn.bit.user.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.TIME_UNIT_INVALID;

/**
 * @author : xiaoxi.lao
 * @Description : 卡片生成器
 * @Date ： 2018/9/17 10:17
 */
@Component
@Slf4j
public class CardGenerator {

    @Autowired
    private CardRepository cardRepository;

    /**
     * 生成临时通行二维码
     * @param entity
     * @return
     */
    public Card applyHouseholdQRCard(CardVO entity) {
        Card beforeCardCreation = new Card();
        beforeCardCreation.setCommunityId(entity.getCommunityId());
        beforeCardCreation.setUserId(entity.getUserId());
        beforeCardCreation.setName(entity.getName());
        beforeCardCreation.setKeyNo(this.randomHexString(12));
        beforeCardCreation.setKeyType(entity.getKeyType());
        beforeCardCreation.setKeyId(new ObjectId().toHexString());
        // 二维码需要标明申请状态
        beforeCardCreation.setValidState(CardStatusType.APPLYING.KEY);
        Date startTime = new Date();
        return insertCard(beforeCardCreation, entity.getRoomName(), startTime, DateUtils.addSecond(startTime, entity.getProcessTime()));
    }

    /**
     * 生成虚拟卡
     * @param userId
     * @param communityId
     * @param name
     * @return
     */
    public Card applyUserCard(ObjectId userId, ObjectId communityId, String name) {
        Card card = cardRepository.findByUserIdAndCommunityIdAndKeyTypeAndProcessTimeAfterAndDataStatus(
                userId, communityId, CertificateType.PHONE_MAC.KEY, new Date(),
                        DataStatusType.VALID.KEY);
        Card beforeCardCreation = new Card();
        beforeCardCreation.setUserId(userId);
        beforeCardCreation.setCommunityId(communityId);
        beforeCardCreation.setName(name);
        beforeCardCreation.setKeyNo(this.randomHexString(12));
        beforeCardCreation.setKeyId(new ObjectId().toHexString());
        beforeCardCreation.setKeyType(CertificateType.PHONE_MAC.KEY);
        beforeCardCreation.setValidState(CardStatusType.VALID.KEY);
        Date startTime = new Date();
        // 虚拟卡都给50年
        return card == null ? insertCard(beforeCardCreation, null, startTime, DateUtils.addYear(startTime, 50)) : card;
    }

    /**
     * 生成实体卡
     * @param cardVO
     * @return
     */
    public Card applyPhysicalCard(CardVO cardVO) {
        Date expireAt = cardVO.getExpireAt();
        Integer processTime = cardVO.getProcessTime();
        Integer timeUnit = cardVO.getTimeUnit() == null ? TimeUnitEnum.SECOND.value() : cardVO.getTimeUnit();
        if (TimeUnitEnum.fromValue(timeUnit) == null) {
            throw TIME_UNIT_INVALID;
        }
        // 生成实体卡前置处理
        Card beforeCardCreation = new Card();
        beforeCardCreation.setCommunityId(cardVO.getCommunityId());
        beforeCardCreation.setPhone(cardVO.getPhone());
        beforeCardCreation.setUserId(cardVO.getUserId());
        beforeCardCreation.setName(cardVO.getName());
        beforeCardCreation.setKeyNo(cardVO.getKeyNo());

        List<Card> oldCards = cardRepository.findByCommunityIdAndKeyNoAndKeyTypeAndDataStatusOrderByCreateAtDesc(
                cardVO.getCommunityId(),
                cardVO.getKeyNo(),
                cardVO.getKeyType(),
                DataStatusType.INVALID.KEY);
        beforeCardCreation.setKeyId(null != oldCards && oldCards.size() > 0
                                    ? oldCards.iterator().next().getKeyId()
                                    : new ObjectId().toHexString());

        beforeCardCreation.setKeyType(cardVO.getKeyType());
        beforeCardCreation.setValidState(CardStatusType.VALID.KEY);
        if (cardVO.getRooms() != null && !cardVO.getRooms().isEmpty()) {
            beforeCardCreation.setRoomId(cardVO.getRooms().iterator().next());
        }

        if (expireAt == null && processTime == null) {
            // 有效期默认50年
            expireAt = DateUtils.addYear(new Date(), 50);
        } else if (expireAt == null) {
            expireAt = UserUtils.getExpireAt(processTime, timeUnit);
        }
        return insertCard(beforeCardCreation, cardVO.getRoomName(), new Date(), DateUtils.getEndTime(expireAt));
    }

    /**
     * 生成卡片核心代码
     * @param source
     * @param roomName
     * @param startTime
     * @param processTime
     * @return
     */
    private Card insertCard(Card source, Set<String> roomName, Date startTime, Date processTime) {
        // 判断该用户在当前社区虚拟卡是否存在（只有虚拟卡具有唯一性）
        Card card = new Card();
        BeanUtils.copyProperties(source, card);
        card.setRoomName(roomName);
        /*card.setKeyId(new ObjectId().toHexString());*/
        // 使用次数目前先给0
        card.setUseTimes(0);
        card.setStartDate(startTime);
        card.setProcessTime(processTime);
        card.setEndDate(card.getProcessTime());
        card.setControlType(8);
        card.setCreateAt(new Date());
        card.setCreateId(card.getUserId());
        card.setDataStatus(DataStatusType.VALID.KEY);
        return cardRepository.insert(card);
    }

    /**
     * 生成keyNo
     *
     * @param len
     * @return
     */
    private String randomHexString(int len) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(Integer.toHexString(new Random().nextInt(16)));
        }
        return result.toString().toUpperCase();
    }
}
