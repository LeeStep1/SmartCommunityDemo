package cn.bit.support;

import cn.bit.facade.enums.UseStatusType;
import cn.bit.facade.model.business.CouponToUser;
import cn.bit.framework.utils.DateUtils;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CouponToUserAfterConvertListener extends AbstractMongoEventListener<CouponToUser> {

    @Override
    public void onAfterConvert(AfterConvertEvent<CouponToUser> event) {
        super.onAfterConvert(event);
        CouponToUser couponToUser = event.getSource();
        addUseStatus(couponToUser);
    }

    public void addUseStatus(CouponToUser couponToUser){
        if (couponToUser.getUseStatus() != UseStatusType.USED.key && couponToUser.getValidityEndAt().before(DateUtils.getStartTime(new Date()))) {
            couponToUser.setUseStatus(UseStatusType.EXPIRED.key);
        }
    }
}
