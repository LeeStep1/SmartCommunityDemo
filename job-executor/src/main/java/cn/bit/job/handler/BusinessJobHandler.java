package cn.bit.job.handler;

import cn.bit.facade.model.business.Coupon;
import cn.bit.facade.model.business.Shop;
import cn.bit.facade.service.business.CouponFacade;
import cn.bit.facade.service.business.ShopFacade;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaoyu.fang
 * @date 2018/6/22 15:14
 */
@Component
@Slf4j
@JobHandler(value = "businessJobHandler")
public class BusinessJobHandler extends IJobHandler {

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private ShopFacade shopFacade;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            // 获取已过期的优惠券
            /*List<Coupon> couponList = couponFacade.findByParams();
            if (couponList != null && couponList.size() > 0) {
                Set<ObjectId> couponIds = couponList.stream().map(Coupon::getId).collect(Collectors.toSet());
                // 改为撤下状态
                if(!couponIds.isEmpty()){
                    couponFacade.updateByIdIn(couponIds);
                }

                couponList.forEach(coupon -> {
                    List couponNames = couponFacade.getTop2Coupons(coupon.getShopId());
                    Shop shop = shopFacade.updateCouponNamesForShop(coupon.getShopId(), couponNames);
                    XxlJobLogger.log("shop <<<<<<<<<<<<<<< " + shop + ">>>>>>>>>>>>>");
                });

            }*/
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        return ReturnT.SUCCESS;
    }
}
