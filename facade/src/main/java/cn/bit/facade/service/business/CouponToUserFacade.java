package cn.bit.facade.service.business;

import cn.bit.facade.model.business.CouponToUser;
import cn.bit.facade.vo.business.CouponUseVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Created by fxiao
 * on 2018/4/3
 * 优惠券、用户接口
 */
public interface CouponToUserFacade {

    /**
     * 新增关联信息
     * @param couponId
     * @return
     */
    CouponToUser addCouponToUser(ObjectId couponId, ObjectId userId, String userName) throws BizException;

    /**
     * 使用优惠券
     * @param id
     * @param validCode
     * @return
     */
    CouponToUser useCoupon(ObjectId id, String validCode) throws BizException;

    /**
     * 删除关联信息
     * @param id
     * @return
     */
    CouponToUser deleteCouponToUser(ObjectId id) throws BizException;

    /**
     * 分页
     * @param couponToUser
     * @return
     */
    Page<CouponToUser> queryCouponToUserPage(CouponToUser couponToUser) throws BizException;

    /**
     * 获取列表
     * @param couponToUser
     * @return
     */
    List<CouponToUser> getCouponToUserList(CouponToUser couponToUser) throws BizException;

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    CouponToUser findOne(ObjectId id) throws BizException;

    /**
     * 统计已领取的数量
     * @param couponId
     * @return
     */
    long countByCouponId(ObjectId couponId) throws BizException;

    /**
     * 根据优惠券ID和用户ID判断是否已领取
     * @param couponId
     * @param userId
     * @return
     */
    long countByCouponAndUserId(ObjectId couponId, ObjectId userId) throws BizException;

    /**
     * 根据优惠券ID和用户ID查询
     * @param couponId
     * @param userId
     * @return
     * @throws BizException
     */
    CouponToUser findByCouponIdAndUserId(ObjectId couponId, ObjectId userId) throws BizException;

    /**
     * 获取用户未使用的有效的优惠券
     * @param userId
     * @return
     */
    Map<ObjectId, CouponUseVO> getUsersCoupons(ObjectId userId) throws BizException;
}
