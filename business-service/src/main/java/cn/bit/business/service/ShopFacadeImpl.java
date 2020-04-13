package cn.bit.business.service;

import cn.bit.business.dao.*;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.business.*;
import cn.bit.facade.service.business.ShopFacade;
import cn.bit.facade.vo.business.ShopItem;
import cn.bit.facade.vo.business.ShopRequest;
import cn.bit.facade.vo.business.ShopVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.business.BusinessException.BIZ_SHOP_ID_NULL;
import static cn.bit.facade.exception.business.BusinessException.BIZ_SHOP_NULL;

/**
 * Created by fxiao
 * on 2018/4/2
 */
@Service("shopFacade")
public class ShopFacadeImpl implements ShopFacade {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopTypeRepository shopTypeRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CouponToUserRepository couponToUserRepository;

    @Override
    public Shop addShop(Shop shop) throws BizException {
        shop.setPopularity(0L);
        shop.setCreateAt(new Date());
        shop.setDataStatus(DataStatusType.VALID.KEY);
        return shopRepository.insert(shop);
    }

    @Override
    public Shop editShop(Shop shop) throws BizException {
        ObjectId id = shop.getId();
        Shop toGet = shopRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw BIZ_SHOP_NULL;
        }
        if (id == null) {
            throw BIZ_SHOP_ID_NULL;
        }
        shop.setId(null);
        shop.setUpdateAt(new Date());

        if(shop.getType() != null){
            ShopType shopType = shopTypeRepository.getFirstByCodeAndDataStatus(shop.getType(), DataStatusType.VALID.KEY);
            if (shopType != null){
                shop.setTypeName(shopType.getName());
            }
        }

        return shopRepository.updateWithUnsetIfNullLogoByIdAndDataStatus(shop, id, DataStatusType.VALID.KEY);
    }

    @Override
    public Shop findOne(ObjectId id) throws BizException {
        return shopRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Shop deleteShop(ObjectId id) throws BizException {
        if (!StringUtil.isNotNull(id)) {
            throw BIZ_SHOP_ID_NULL;
        }
        Shop shop = shopRepository.findById(id);
        if (shop == null) {
            throw BIZ_SHOP_NULL;
        }
        shop.setDataStatus(DataStatusType.INVALID.KEY);
        // 修改优惠券
        Coupon coupon = new Coupon();
        coupon.setDataStatus(DataStatusType.INVALID.KEY);
        coupon.setValidStatus(DataStatusType.INVALID.KEY);
        couponRepository.updateWithValidStatusAndDataStatusByShopId(coupon, shop.getId());
        return shopRepository.updateById(shop, id);
    }

    @Override
    public Page<Shop> queryShopPage(Shop shop) throws BizException {
        Pageable pageable = new PageRequest(shop.getPage() - 1, shop.getSize(),
                new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Shop> resultPage =
                shopRepository.findByNameRegexAndProvinceAndCityAndDistrictAndCommunityIdsInAndDataStatusAllIgnoreNull(
                        StringUtil.makeQueryStringAllRegExp(shop.getName()), shop.getProvince(), shop.getCity(),
                        shop.getDistrict(), shop.getCommunityId(), DataStatusType.VALID.KEY, pageable);
        Page<Shop> shopPage = PageUtils.getPage(resultPage);
        List<Coupon> couponList = couponRepository.findByShopIdInAndValidStatusAndDataStatus(
                shopPage.getRecords().stream().map(Shop::getId).collect(Collectors.toSet()),
                DataStatusType.VALID.KEY, DataStatusType.VALID.KEY);
        shopPage.getRecords().forEach(
                item -> item.setCouponNum(
                        couponList.stream().filter(coupon -> item.getId().equals(coupon.getShopId())).count()));
        return shopPage;
    }

    @Override
    public List<ShopVO> getShopList(ShopRequest request) throws BizException {
        List<Shop> shops = shopRepository.findByCommunityIdsInAndTypeAndTagInAndDataStatusAllIgnoreNullOrderByCreateAtAsc(
        		request.getCommunityId(), request.getType(), request.getTag(), DataStatusType.VALID.KEY);

        return shops.stream().map(ShopVO::new).collect(Collectors.toList());
    }

    @Override
    public Page<ShopItem> queryByLocal(ShopRequest shopRequest) throws BizException {
        Pageable pageable = new PageRequest(shopRequest.getPage() - 1, shopRequest.getSize(), new Sort(Sort.Direction.DESC,"popularity"));
        org.springframework.data.domain.Page<ShopItem> shopItemPage = shopRepository.findByCommunityIdsAndTypeAndTagAndDataStatusAllIgnoreNull(
                shopRequest.getCommunityId(), shopRequest.getType(), shopRequest.getTag(), DataStatusType.VALID.KEY, pageable, ShopItem.class);

        Collection<ObjectId> shopIds = shopItemPage.getContent().parallelStream().map(ShopItem::getId).collect(Collectors.toList());
        List<Coupon> coupons = couponRepository.findByValidStatusAndDataStatusAndShopIdInAndValidityEndAtGreaterThanEqualOrderByCreateAtDesc(DataStatusType.VALID.KEY, DataStatusType.VALID.KEY, shopIds, DateUtils.getStartTime(new Date()));
        // 获取最新的两张优惠券
        shopItemPage.getContent().forEach(shopItem -> {
            List<String> couponList = new ArrayList<>();
            // 清理旧数据
            shopItem.setCouponNames(couponList);
            coupons.forEach(coupon -> {
                if (shopItem.getId().equals(coupon.getShopId())) {
                    if (couponList.size() < 2)
                        couponList.add(coupon.getName());
                }
            });
            if (couponList.size() > 0)
                shopItem.setCouponNames(couponList);
        });
        return PageUtils.getPage(shopItemPage);
    }

    @Override
    public ShopItem getVIP(ObjectId communityId) throws BizException {
        return shopRepository.findTop1ByCommunityIdsAndDataStatusOrderByPopularityDesc(communityId, DataStatusType.VALID.KEY, ShopItem.class);
    }

    @Override
    public void increment(ObjectId id) {
    	Shop toUpdate = new Shop();
    	toUpdate.setPopularity(1L);
        shopRepository.updateWithIncPopularityById(toUpdate, id);
    }

    @Override
    public ShopType addShopType(ShopType shopType) {
        shopType.setCreateAt(new Date());
        shopType.setDataStatus(DataStatusType.VALID.KEY);
        return shopTypeRepository.insert(shopType);
    }

    @Override
    public ShopType getShopTypeByCode(Integer code) {
        return shopTypeRepository.getFirstByCodeAndDataStatus(code,DataStatusType.VALID.KEY);
    }

    @Override
    public List<ShopType> getShopTypes(ShopType shopType) {
        return shopTypeRepository.find(shopType, XSort.asc("code"));
    }

    @Override
    public Shop updateCouponNamesForShop(ObjectId id, List<String> couponNames) {
        Shop shop = new Shop();
        shop.setCouponNames(couponNames);
        return shopRepository.updateWithUnsetIfNullCouponNamesByIdAndDataStatus(shop, id, DataStatusType.VALID.KEY);
    }

    @Override
    public Shop findOneWithDetail(ObjectId id, ObjectId userId) {
        if(id == null){
            throw BIZ_SHOP_ID_NULL;
        }
        Shop shop = shopRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
        if (shop == null) {
            throw BIZ_SHOP_NULL;
        }
        // 获取商家推荐
        List<Goods> goods = goodsRepository.findByShopIdAndDataStatusOrderByCreateAtAsc(id, DataStatusType.VALID.KEY);
        // 获取优惠券
        List<Coupon> coupons = couponRepository.findByShopIdAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByCreateAtAsc(id, DataStatusType.VALID.KEY, DateUtils.getStartTime(new Date()), DataStatusType.VALID.KEY);
        // 获取用户的优惠券列表集合
        if (userId != null) {
            List<CouponToUser> couponToUsers = couponToUserRepository.findByUserIdAndValidityEndAtGreaterThanEqual(userId, DateUtils.getStartTime(new Date()));
            couponToUsers.stream().forEach(couponToUser -> {
                coupons.forEach(coupon -> {
                    if (couponToUser.getCouponId().equals(coupon.getId())) {
                        coupon.setCouponToUserId(couponToUser.getId());
                        coupon.setUseStatus(couponToUser.getUseStatus());
                    }
                });
            });
        }
        shop.setGoods(goods);
        shop.setCoupons(coupons);
        return shop;
    }

}
