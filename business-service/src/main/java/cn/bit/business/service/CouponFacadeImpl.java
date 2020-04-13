package cn.bit.business.service;

import cn.bit.business.dao.CouponRepository;
import cn.bit.business.dao.CouponToUserRepository;
import cn.bit.business.dao.ShopRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.business.Coupon;
import cn.bit.facade.model.business.CouponToUser;
import cn.bit.facade.model.business.Shop;
import cn.bit.facade.service.business.CouponFacade;
import cn.bit.facade.vo.business.CouponItem;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.business.BusinessException.*;

/**
 * Created by fxiao
 * on 2018/4/3
 */
@Service("couponFacade")
public class CouponFacadeImpl implements CouponFacade {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CouponToUserRepository couponToUserRepository;

    @Override
    public Coupon addCoupon(Coupon coupon) throws BizException {
        if (coupon.getValidityEndAt().before(DateUtils.getStartTime(new Date()))) {
            throw BIZ_COUPON_INVALID_USE_DATE;
        }
        // 获取商家信息
        Shop toGet = shopRepository.findByIdAndDataStatus(coupon.getShopId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw BIZ_SHOP_NULL;
        }
        coupon.setIcon(coupon.getIcon() == null ? toGet.getLogo() : coupon.getIcon());
        coupon.setShopName(toGet.getName());
        coupon.setAmount(coupon.getAmount() == null ? 0: coupon.getAmount());
        coupon.setReceiveNum(0);
        // 随机六位数
        if (coupon.getValidCode() == null) {
            coupon.setValidCode((String.valueOf(System.currentTimeMillis())).substring(7));
        }
        // 默认发布状态
        coupon.setValidStatus(DataStatusType.VALID.KEY);
        // 经纬度
        coupon.setLocal(toGet.getLocal());
        coupon.setAddress(toGet.getAddress());
        coupon.setTelPhone(toGet.getTelPhone());
        coupon.setShopType(toGet.getType());
        // 优惠券使用量
        coupon.setUseNum(0);
        coupon.setMaxPrice(coupon.getMaxPrice() == null ? 0.0: coupon.getMaxPrice());
        // 关联社区ID
        coupon.setCommunityIds(toGet.getCommunityIds());

        coupon.setDataStatus(DataStatusType.VALID.KEY);
        coupon.setCreateAt(new Date());

        // 将最新的优惠券信息存放商家信息中
        /*CompletableFuture.runAsync(()->{
            if (toGet != null) {
                List<String> names = new ArrayList<>();
                names.add(coupon.getName());
                if (toGet.getCouponNames() != null && toGet.getCouponNames().size() > 0) {
                    names.add(toGet.getCouponNames().get(0));
                }
                Shop toUpdate = new Shop();
                toUpdate.setCouponNames(names);
                shopRepository.updateWithUnsetIfNullCouponNamesByIdAndDataStatus(toUpdate, coupon.getShopId(), DataStatusType.VALID.KEY);
            }
        });*/
        return couponRepository.insert(coupon);
    }

    @Override
    public Coupon editCoupon(Coupon coupon) throws BizException {
        ObjectId id = coupon.getId();
        Coupon toGet = couponRepository.findById(coupon.getId());
        if (toGet != null && toGet.getValidStatus().equals(DataStatusType.INVALID.KEY)) {
            throw BIZ_COUPON_OUTDATED;
        }
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw BIZ_COUPON_NULL;
        }
        if (coupon.getAmount() != null && coupon.getAmount() < toGet.getAmount()) {
            throw BIZ_COUPON_NUM_LESS_THAN_BEFORE;
        }
        if (coupon.getValidityEndAt() != null && coupon.getValidityEndAt().before(toGet.getValidityEndAt())) {
            throw BIZ_COUPON_DATE_LESS_THAN_BEFORE;
        }
        coupon.setUpdateAt(new Date());
        coupon.setId(null);
        return couponRepository.updateById(coupon, id);
    }

    @Override
    public long updateCouponsByShopId(Coupon coupon, ObjectId shopId) throws BizException {
        return couponRepository.updateByShopIdAndDataStatus(coupon, shopId, DataStatusType.VALID.KEY);
    }

    @Override
    public Coupon deleteCoupon(ObjectId id) throws BizException {
        if (id == null) {
            throw BIZ_COUPON_ID_NULL;
        }
        Coupon coupon = couponRepository.findById(id);
        if (coupon == null) {
            throw BIZ_COUPON_NULL;
        }
        coupon.setDataStatus(DataStatusType.INVALID.KEY);
        return couponRepository.updateOne(coupon);
    }

    @Override
    public Page<Coupon> queryCouponPage(Coupon coupon) throws BizException {
        if (coupon.getShopId() == null) {
            throw BIZ_SHOP_ID_NULL;
        }
        coupon.setDataStatus(DataStatusType.VALID.KEY);
        Page<Coupon> pages = couponRepository.findPage(coupon, coupon.getPage(), coupon.getSize(), XSort.desc("createAt"));
        // 优惠券失效
        Date nowDate = DateUtils.getStartTime(new Date());
        pages.getRecords().parallelStream().forEach(coupon1 -> {
            if (nowDate.after(coupon1.getValidityEndAt()))
                coupon1.setValidStatus(DataStatusType.INVALID.KEY);
        });
        return pages;
    }

    @Override
    public List<Coupon> getCouponList(Coupon coupon) throws BizException {
        coupon.setDataStatus(DataStatusType.VALID.KEY);
        List<Coupon> target = couponRepository.find(coupon, XSort.asc("createAt"));
        return target;
    }

    @Override
    public Coupon findOne(ObjectId id) throws BizException {
        return couponRepository.findById(id);
    }

    @Override
    public List<Coupon> getValidCoupon(ObjectId shopId) throws BizException {
        if (shopId == null) {
            throw BIZ_SHOP_ID_NULL;
        }
        return couponRepository.findByShopIdAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByCreateAtAsc(shopId, DataStatusType.VALID.KEY, DateUtils.getStartTime(new Date()), DataStatusType.VALID.KEY);
    }

    @Override
    public CouponItem getVipCoupon(ObjectId communityId) throws BizException {
        if (communityId == null) {
            throw BIZ_COUPON_ID_NULL;
        }
//        return couponRepository.getVipCoupon(communityId);
        return couponRepository.findTop1ByCommunityIdsAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByMaxPriceDesc(
                communityId, DataStatusType.VALID.KEY, DateUtils.getStartTime(new Date()), DataStatusType.VALID.KEY, CouponItem.class);
    }

    @Override
    public Page<CouponItem> getVipCoupons(Coupon coupon) throws BizException {
        if (coupon.getCommunityId() == null) {
            throw BIZ_COMMUNITYID_NULL;
        }
        Pageable pageable = new PageRequest(coupon.getPage() - 1, coupon.getSize(), new Sort(Sort.Direction.DESC, "maxPrice"));
        org.springframework.data.domain.Page<CouponItem> couponPage = couponRepository.findByCommunityIdsAndShopTypeIgnoreNullAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatus(
                coupon.getCommunityId(), coupon.getShopType(), DataStatusType.VALID.KEY, DateUtils.getStartTime(new Date()), DataStatusType.VALID.KEY, pageable, CouponItem.class);
        return PageUtils.getPage(couponPage);
    }

    @Override
    public List<String> getTop2Coupons(ObjectId shopId) throws BizException {
        if (shopId == null) {
            throw BIZ_SHOP_ID_NULL;
        }
        List<Coupon> couponList = couponRepository.findTop2ByShopIdAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByCreateAtAsc(
                shopId, DataStatusType.VALID.KEY, DateUtils.getStartTime(new Date()), DataStatusType.VALID.KEY);
        return couponList.stream().map(Coupon::getName).collect(Collectors.toList());
    }

    @Override
    public void incrementUseNum(ObjectId id) {
        Coupon toUpdate = new Coupon();
        toUpdate.setUseNum(1);
        couponRepository.updateWithIncUseNumByIdAndDataStatus(toUpdate, id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Coupon> findByParams() {
        return couponRepository.findByValidStatusAndDataStatusAndValidityEndAtLessThan(DataStatusType.VALID.KEY,
                DataStatusType.VALID.KEY, DateUtils.getStartTime(new Date()));
    }

    @Override
    public Coupon updateValidStatus(ObjectId id) {
        Coupon coupon = new Coupon();
        coupon.setValidStatus(DataStatusType.INVALID.KEY);
        return couponRepository.updateById(coupon, id);
    }

    @Override
    public long updateByIdIn(Set<ObjectId> ids) {
        if(ids == null || ids.isEmpty()){
            return 0;
        }
        Coupon coupon = new Coupon();
        coupon.setValidStatus(DataStatusType.INVALID.KEY);
        return couponRepository.updateByIdIn(coupon, ids);
    }

    @Override
    public Coupon findDetail(ObjectId id, ObjectId uid) {
        Coupon toGet = couponRepository.findById(id);
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw BIZ_COUPON_NULL;
        }
        CouponToUser couponToUser = couponToUserRepository.findByCouponIdAndUserId(id, uid);
        if (couponToUser != null) {
            toGet.setCouponToUserId(couponToUser.getId());
            toGet.setUseStatus(couponToUser.getUseStatus() == null ? 0 : couponToUser.getUseStatus());
        }
        return toGet;
    }

    @Override
    public Coupon changeCouponPublish(Coupon coupon) {
        if(coupon.getId() == null){
            throw BIZ_COUPON_ID_NULL;
        }
        Coupon toUpdate = new Coupon();
        toUpdate.setValidStatus(coupon.getValidStatus());
        return couponRepository.updateById(toUpdate, coupon.getId());
    }
}
