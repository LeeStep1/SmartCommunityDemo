package cn.bit.facade.service.business;

import cn.bit.facade.model.business.Coupon;
import cn.bit.facade.vo.business.CouponItem;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

/**
 * Created by fxiao
 * on 2018/4/3
 * 优惠券
 */
public interface CouponFacade {

    /**
     * 新增优惠券
     * @param coupon
     * @return
     */
    Coupon addCoupon(Coupon coupon) throws BizException;

    /**
     * 修改优惠券（只针对修改使用状态）
     * @param coupon
     * @return
     */
    Coupon editCoupon(Coupon coupon) throws BizException;

    /**
     *
     * @throws BizException
     */
    long updateCouponsByShopId(Coupon coupon, ObjectId shopId) throws BizException;

    /**
     * 删除优惠券
     * @param id
     * @return
     */
    Coupon deleteCoupon(ObjectId id) throws BizException;

    /**
     * 优惠券分页（H5）
     * @param coupon
     * @return
     */
    Page<Coupon> queryCouponPage(Coupon coupon) throws BizException;

    /**
     * 获取优惠券列表
     * @param coupon
     * @return
     */
    List<Coupon> getCouponList(Coupon coupon) throws BizException;

    /**
     * 查询
     * @param id
     * @return
     */
    Coupon findOne(ObjectId id) throws BizException;

    /**
     * 获取有效的优惠券
     * @param shopId
     * @return
     * @throws BizException
     */
    List<Coupon> getValidCoupon(ObjectId shopId) throws BizException;

    /**
     * 获取VIP首个
     * @param communityId
     * @return
     */
    CouponItem getVipCoupon(ObjectId communityId) throws BizException;

    /**
     * 获取vip用户的优惠券
     * @param coupon
     * @return
     */
    Page<CouponItem> getVipCoupons(Coupon coupon) throws BizException;

    /**
     * 获取商家的头两个优惠券
     * @param shopId
     * @return
     */
    List getTop2Coupons(ObjectId shopId) throws BizException;

    /**
     * 使用量
     * @param id
     */
    void incrementUseNum(ObjectId id) throws BizException;

    /**
     * 根据条件获取过期的优惠券
     * @param
     * @return
     */
    List<Coupon> findByParams();

    /**
     * 更新过期的优惠券的状态
     * @param id
     * @return
     */
    Coupon updateValidStatus(ObjectId id);

    /**
     *
     * @param ids
     */
    long updateByIdIn(Set<ObjectId> ids);

	Coupon findDetail(ObjectId id, ObjectId uid);

    Coupon changeCouponPublish(Coupon coupon);
}
