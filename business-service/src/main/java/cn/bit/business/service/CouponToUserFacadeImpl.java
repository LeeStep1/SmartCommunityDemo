package cn.bit.business.service;

import cn.bit.business.dao.CouponRepository;
import cn.bit.business.dao.CouponToUserRepository;
import cn.bit.business.dao.ShopRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.UseStatusType;
import cn.bit.facade.model.business.Coupon;
import cn.bit.facade.model.business.CouponToUser;
import cn.bit.facade.model.business.Shop;
import cn.bit.facade.service.business.CouponToUserFacade;
import cn.bit.facade.vo.business.CouponUseVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.support.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cn.bit.facade.exception.business.BusinessException.*;

/**
 * Created by fxiao
 * on 2018/4/3
 * 优惠券、用户关联表
 */
@Service("couponToUserFacade")
@Slf4j
public class CouponToUserFacadeImpl implements CouponToUserFacade {

    @Autowired
    private CouponToUserRepository couponToUserRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ShopRepository shopRepository;

    private static final String KEY = "COUPON_LOCK_";

    @Override
    public CouponToUser addCouponToUser(ObjectId couponId, ObjectId userId, String userName) throws BizException {
        // 获取优惠券信息
        Coupon coupon = couponRepository.findById(couponId);
        if (coupon == null || coupon.getDataStatus() == DataStatusType.INVALID.KEY) {
            throw BIZ_COUPON_NULL;
        }
        // 一 redis锁
        String nowTime = String.valueOf(System.currentTimeMillis());
        String key = String.format("%s%s", KEY, coupon.getId().toString());
        if (RedisService.lock(key, nowTime)) {
            Boolean exist = couponToUserRepository.existsByCouponIdAndUserId(coupon.getId(), userId);
            if (exist) {
                RedisService.unlock(key, nowTime);
                throw BIZ_COUPON_EXIST;
            }
            Coupon toUpdate = new Coupon();
            toUpdate.setReceiveNum(1);
            Coupon item = couponRepository.updateWithIncReceiveNumByIdAndValidStatusAndReceiveNumLessThanAndDataStatus(toUpdate, coupon.getId()
                    , DataStatusType.VALID.KEY, coupon.getAmount(), DataStatusType.VALID.KEY);
            if (item != null) {
                CouponToUser entity= createCouponToUser(coupon, userId, userName);
                entity = couponToUserRepository.insert(entity);
                if (entity != null) {
                    RedisService.unlock(key, nowTime);
                    return entity;
                }
            } else {
                throw BIZ_COUPON_FULL;
            }
        }
        return null;

        // 二 组合索引
        /*Coupon item = couponRepository.updateOne(new Query(Criteria.where("id").is(coupon.getId()).
                        and("validStatus").is(DataStatusType.VALID.KEY).
                        and("dataStatus").is(DataStatusType.VALID.KEY).
                        and("receiveNum").lt(coupon.getAmount())),
                new Update().inc("receiveNum", 1));
        if (item != null) {
            try {
                CouponToUser entity= createCouponToUser(coupon, userId, userName);
                return couponToUserRepository.insert(entity);
            } catch (Exception e){
                if (e.getMessage().indexOf("DuplicateKey") != -1) {
                    // 回滚优惠券的数量
                    couponRepository.updateOne(new Query(Criteria.where("id").is(coupon.getId())),
                            new Update().inc("receiveNum", -1));
                    throw BIZ_COUPON_EXIST;
                }
                e.printStackTrace();
            }
        }
        return null;*/
    }

    @Override
    public CouponToUser useCoupon(ObjectId id, String validCode) throws BizException {
        if (id == null) {
            throw BIZ_ID_NULL;
        }
        if (!StringUtil.isNotNull(validCode)) {
            throw BIZ_COUPON_VALID_CODE_NULL;
        }

        CouponToUser couponToUser = couponToUserRepository.findById(id);
        if (couponToUser == null) {
            throw BIZ_COUPON_NULL;
        }
        Date time = new Date();
        if (couponToUser.getValidityBeginAt().after(DateUtils.getStartTime(time)) || couponToUser.getValidityEndAt().before(DateUtils.getStartTime(time))) {
            throw BIZ_COUPON_DATE_INVALID;
        }
        if (!validCode.equals(couponToUser.getValidCode())){
            throw BIZ_COUPON_CODE_INVALID;
        }
        if (couponToUser.getUseStatus() == UseStatusType.USED.key){
            throw BIZ_COUPON_USED;
        }
        if (couponToUser.getUseStatus() == UseStatusType.EXPIRED.key){
            throw BIZ_COUPON_OUTDATED;
        }
        CouponToUser toUpdate = new CouponToUser();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setUseStatus(UseStatusType.USED.key);
        toUpdate = couponToUserRepository.updateByIdAndDataStatus(toUpdate, id, DataStatusType.VALID.KEY);
        // 热度自增
        CompletableFuture.runAsync(() -> {
            // 热度
            Shop shop = new Shop();
            shop.setPopularity(1L);
            shopRepository.updateWithIncPopularityById(shop, couponToUser.getShopId());
            // 销量
            Coupon coupon = new Coupon();
            coupon.setUseNum(1);
            couponRepository.updateWithIncUseNumByIdAndDataStatus(coupon, couponToUser.getCouponId(), DataStatusType.VALID.KEY);
        });
        return toUpdate;
    }

    @Override
    public CouponToUser deleteCouponToUser(ObjectId id) throws BizException {
        if (id == null) {
            throw BIZ_ID_NULL;
        }
        CouponToUser toUpdate = new CouponToUser();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return couponToUserRepository.updateByIdAndDataStatus(toUpdate, id, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<CouponToUser> queryCouponToUserPage(CouponToUser couponToUser) throws BizException {
        Pageable pageable = new PageRequest(couponToUser.getPage() - 1, couponToUser.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<CouponToUser> resultPage = null;
        // 使用状态不为空
        if (couponToUser.getUseStatus() != null) {
            // app 分为未使用，已过期，已使用
            switch (UseStatusType.getByValue(couponToUser.getUseStatus())) {
                // 1、 未使用，条件是使用状态为未使用，时间为有效期
                case UNUSED:
                    resultPage = couponToUserRepository.findByUserIdAndUseStatusNotAndValidityEndAtGreaterThanEqualAndDataStatus(couponToUser.getUserId(),
                            UseStatusType.USED.key,
                            new Date(),
                            DataStatusType.VALID.KEY,
                            pageable);
                    break;
                // 2、已使用，条件是使用状态为已使用
                case USED:
                    resultPage = couponToUserRepository.findByUserIdAndUseStatusAndDataStatus(couponToUser.getUserId(),
                            couponToUser.getUseStatus(),
                            DataStatusType.VALID.KEY,
                            pageable);
                    break;
                // 3、已过期，条件是未使用，时间为已过期
                case EXPIRED:
                    resultPage = couponToUserRepository.findByUserIdAndUseStatusNotAndValidityEndAtLessThanAndDataStatus(couponToUser.getUserId(),
                            UseStatusType.USED.key,
                            new Date(),
                            DataStatusType.VALID.KEY,
                            pageable);
                    break;
                default:break;
            }
        } else {
            resultPage = couponToUserRepository.findByUserIdAndShopIdIgnoreNullAndDataStatus(couponToUser.getUserId(),
                    couponToUser.getShopId(),
                    DataStatusType.VALID.KEY,
                    pageable);
        }
        return PageUtils.getPage(resultPage);
    }

    @Override
    public List<CouponToUser> getCouponToUserList(CouponToUser couponToUser) throws BizException {
        couponToUser.setDataStatus(DataStatusType.VALID.KEY);
        return couponToUserRepository.find(couponToUser, XSort.asc("createAt"));
    }

    @Override
    public CouponToUser findOne(ObjectId id) throws BizException {
        return couponToUserRepository.findById(id);
    }

    @Override
    public long countByCouponId(ObjectId couponId) throws BizException {
        return couponToUserRepository.countByCouponIdAndDataStatus(couponId, DataStatusType.VALID.KEY);
    }

    @Override
    public long countByCouponAndUserId(ObjectId couponId, ObjectId userId) throws BizException {
        return couponToUserRepository.countByCouponIdAndUserId(couponId, userId);
    }

    @Override
    public CouponToUser findByCouponIdAndUserId(ObjectId couponId, ObjectId userId) throws BizException {
        return couponToUserRepository.findByCouponIdAndUserId(couponId, userId);
    }

    @Override
    public Map<ObjectId, CouponUseVO> getUsersCoupons(ObjectId userId) throws BizException {
        if (userId == null) {
            throw BIZ_USERID_NULL;
        }
        List<CouponToUser> couponToUsers = couponToUserRepository.findByUserIdAndValidityEndAtGreaterThanEqual(userId, DateUtils.getStartTime(new Date()));
        Map<ObjectId, CouponUseVO> map = new HashMap();
        couponToUsers.stream().forEach(couponToUser -> {
            CouponUseVO vo = new CouponUseVO();
            vo.setCouponToUserId(couponToUser.getId());
            vo.setUseStatus(couponToUser.getUseStatus());
            map.put(couponToUser.getCouponId(), vo);
        });
//        Map<String, CouponUseVO> map = couponToUserRepository.getUsersCoupons(userId);
        return map;
    }

    private static CouponToUser createCouponToUser(Coupon coupon, ObjectId userId, String userName) {
        CouponToUser entity= new CouponToUser();
        entity.setCouponId(coupon.getId());
        entity.setShopId(coupon.getShopId());
        entity.setShopName(coupon.getShopName());
        entity.setMaxPrice(coupon.getMaxPrice());

        entity.setValidCode(coupon.getValidCode());
        entity.setUserId(userId);
        entity.setUserName(userName);

        entity.setName(coupon.getName());
        entity.setCreateAt(new Date());
        entity.setUseStatus(UseStatusType.UNUSED.key);
        // 优惠券开始时间 00：00：00.000
        entity.setValidityBeginAt(DateUtils.getStartTime(coupon.getValidityBeginAt()));
        // 优惠券结束时间 23：59：59.999
        entity.setValidityEndAt(DateUtils.getEndTime(coupon.getValidityEndAt()));
        entity.setDataStatus(DataStatusType.VALID.KEY);

        entity.setAddress(coupon.getAddress());
        entity.setTelPhone(coupon.getTelPhone());
        entity.setCouponLimit(coupon.getCouponLimit());
        entity.setPrompt(coupon.getPrompt());
        return entity;
    }
}
