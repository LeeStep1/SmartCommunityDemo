package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.business.dto.*;
import cn.bit.common.facade.business.enums.ActivityStatusEnum;
import cn.bit.common.facade.business.enums.TermTypeEnum;
import cn.bit.common.facade.business.model.ShopWorker;
import cn.bit.common.facade.business.query.*;
import cn.bit.common.facade.business.service.BusinessFacade;
import cn.bit.common.facade.data.Location;
import cn.bit.common.facade.user.model.User;
import cn.bit.facade.enums.PublishStatusType;
import cn.bit.facade.enums.ShopPopularType;
import cn.bit.facade.model.business.*;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.service.business.*;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.vo.business.*;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author xiaoyu.fang
 * @date 2018-05-05
 * 商圈API
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1/biz", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class BusinessController {

    /**
     * 轮播图
     */
    @Autowired
    private BizSlideFacade bizSlideFacade;

    /**
     * 商家
     */
    @Autowired
    private ShopFacade shopFacade;

    /**
     * 商品
     */
    @Autowired
    private GoodsFacade goodsFacade;

    /**
     * 优惠券
     */
    @Autowired
    private CouponFacade couponFacade;

    /**
     * 优惠券、用户关联表
     */
    @Autowired
    private CouponToUserFacade couponToUserFacade;

    /**
     * 便民服务
     */
    @Autowired
    private ConvenienceFacade convenienceFacade;

    /**
     * 社区
     */
    @Autowired
    private CommunityFacade communityFacade;

    /**
     * 商圈公共接口
     */
    @Autowired
    private BusinessFacade businessFacade;

    @Value("${common.business.companyId}")
    private ObjectId companyId;

    /**
     * 公共用户服务
     */
    @Resource
    private cn.bit.common.facade.user.service.UserFacade commonUserFacade;

    // ################################## 【轮播图】###################################

    /**
     * 新增商圈轮播图（H5）
     *
     * @param bizSlide
     * @return
     */
    @PostMapping(name = "新增商圈轮播图", path = "/slide/add")
    @Authorization
    public ApiResult addBizSlide(@Validated(BizSlide.Add.class) @RequestBody BizSlide bizSlide) {
        // 已分配社区
        if (bizSlide.getCommunityName() == null) {
            Community community = communityFacade.findOne(bizSlide.getCommunityId());
            bizSlide.setCommunityName(community == null ? null : community.getName());
        }
        if (bizSlide.getGotoType() == 1 && bizSlide.getShopId() != null) {
            ShopDTO shopAppDTO = businessFacade.getShopByShopId(bizSlide.getShopId());
            bizSlide.setShopName(shopAppDTO.getName());
        }
        bizSlide.setCreateId(SessionUtil.getCurrentUser().getId());
        return ApiResult.ok(bizSlideFacade.addSlide(bizSlide));
    }

    /**
     * 更新商圈轮播图（H5）
     *
     * @param bizSlide
     * @return
     */
    @PostMapping(name = "编辑商圈轮播图", path = "/slide/edit")
    @Authorization
    public ApiResult editBizSlide(@Validated(BizSlide.Update.class) @RequestBody BizSlide bizSlide) {
        // 社区信息
        if (bizSlide.getCommunityName() == null) {
            Community community = communityFacade.findOne(bizSlide.getCommunityId());
            bizSlide.setCommunityName(community.getName());
        }
        if (bizSlide.getGotoType() == 1 && bizSlide.getShopId() != null) {
            ShopDTO shopAppDTO = businessFacade.getShopByShopId(bizSlide.getShopId());
            bizSlide.setShopName(shopAppDTO.getName());
        }
        return ApiResult.ok(bizSlideFacade.editSlide(bizSlide));
    }

    /**
     * 发布
     *
     * @param id
     * @return
     */
    @GetMapping(name = "发布商圈轮播图", path = "/slide/{id}/publish")
    @Authorization
    public ApiResult publishSlide(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(bizSlideFacade.publishSlide(id));
    }

    /**
     * 撤销发布
     *
     * @param id
     * @return
     */
    @GetMapping(name = "撤销商圈轮播图", path = "/slide/{id}/revoke")
    @Authorization
    public ApiResult unPublishSlide(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(bizSlideFacade.revokeSlide(id));
    }

    /**
     * 根据ID获取轮播图信息（H5）
     *
     * @param id
     * @return
     */
    @GetMapping(name = "获取轮播图详情", path = "/slide/{id}/detail")
    public ApiResult findBizSlide(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(bizSlideFacade.findOne(id));
    }

    /**
     * 轮播图分页（H5）
     *
     * @param bizSlide
     * @return
     */
    @PostMapping(name = "轮播图分页", path = "/slide/page")
    public ApiResult queryBizSlidePage(@RequestBody BizSlide bizSlide,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        bizSlide.setPage(bizSlide.getPage() == null ? page : bizSlide.getPage());
        bizSlide.setSize(bizSlide.getSize() == null ? size : bizSlide.getSize());
        return ApiResult.ok(bizSlideFacade.querySlidePage(bizSlide));
    }

    /**
     * 根据社区ID获取已发布列表
     *
     * @param bizSlide
     * @return
     */
    @PostMapping(name = "获取已发布轮播图列表", path = "/slide/list")
    public ApiResult getBizSlideList(@RequestBody BizSlide bizSlide, @RequestHeader("BIT-CID") ObjectId communityId) {
        // 已发布状态
        bizSlide.setPublished(PublishStatusType.PUBLISHED.key);
        bizSlide.setCommunityId(bizSlide.getCommunityId() == null ? communityId : bizSlide.getCommunityId());
        return ApiResult.ok(bizSlideFacade.getSlideList(bizSlide));
    }

    /**
     * 删除（H5）
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除轮播图", path = "/slide/{id}/delete")
    @Authorization
    public ApiResult deleteSlide(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(bizSlideFacade.deleteSlide(id));
    }

    // =========================【便民服务】===========================

    /**
     * 新增服务
     *
     * @param convenience
     * @return
     */
    @PostMapping(name = "新增便民服务", path = "/convenience/add")
    @Authorization
    public ApiResult addConvenience(@Validated(Convenience.Add.class) @RequestBody Convenience convenience) {
        Community community = communityFacade.findOne(convenience.getCommunityId());
        // 创建人ID
        convenience.setCreateId(SessionUtil.getCurrentUser().getId());
        convenience.setCommunityName(community.getName());
        return ApiResult.ok(convenienceFacade.addConvenience(convenience));
    }

    /**
     * 修改服务
     *
     * @param convenience
     * @return
     */
    @PostMapping(name = "编辑便民服务", path = "/convenience/edit")
    @Authorization
    public ApiResult updateConvenience(@Validated(Convenience.Update.class) @RequestBody Convenience convenience) {
        Convenience entity = convenienceFacade.findOne(convenience.getId());
        if (entity == null) {
            return ApiResult.error(-1, "服务不存在");
        }
        if (!convenience.getCommunityId().equals(entity.getCommunityId())) {
            Community community = communityFacade.findOne(convenience.getCommunityId());
            if (community != null) {
                convenience.setCommunityName(community.getName());
            }
        }
        return ApiResult.ok(convenienceFacade.editConvenience(convenience));
    }

    /**
     * 获取服务详细
     *
     * @param id
     * @return
     */
    @GetMapping(name = "便民服务详情", path = "/convenience/{id}/detail")
    public ApiResult getConvenience(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(convenienceFacade.findOne(id));
    }

    /**
     * 服务分页
     *
     * @param convenience
     * @return
     */
    @PostMapping(name = "便民服务分页", path = "/convenience/page")
    public ApiResult queryConveniencePage(@RequestBody Convenience convenience,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        convenience.setPage(convenience.getPage() == null ? page : convenience.getPage());
        convenience.setSize(convenience.getSize() == null ? size : convenience.getSize());
        return ApiResult.ok(convenienceFacade.queryConveniencePage(convenience));
    }

    /**
     * 根据社区ID获取服务列表
     *
     * @param convenience
     * @return
     */
    @PostMapping(name = "社区便民服务列表", path = "/convenience/list")
    public ApiResult getConvenienceList(@Validated(Convenience.Search.class) @RequestBody Convenience convenience) {
        return ApiResult.ok(convenienceFacade.getConvenienceList(convenience));
    }

    /**
     * 删除服务
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除便民服务", path = "/convenience/{id}/delete")
    @Authorization
    public ApiResult deleteConvenience(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(convenienceFacade.deleteConvenience(id));
    }

    /**
     * app获取顶部轮播图和服务
     *
     * @param communityId
     * @return
     */
    @GetMapping(name = "获取顶部轮播图和便民服务", path = "/{communityId}/getTopSlideAndService")
    public ApiResult getTopSlideAndService(@PathVariable("communityId") ObjectId communityId, Integer size) {
        List<SlideVO> bizSlides = bizSlideFacade.getByCommunityId(communityId);
        List<ConvenienceVO> conveniences = convenienceFacade.getByCommunityId(communityId, size);
        SlideAndServiceVO vo = new SlideAndServiceVO();
        vo.setConvenienceVOS(conveniences);
        vo.setSlideVOS(bizSlides);
        return ApiResult.ok(vo);
    }

    // ################################## 【商家】###################################

    /**
     * 新增商家（H5）
     *
     * @param shop
     * @return
     */
    @PostMapping(name = "新增商家", path = "/shop/add")
    @Authorization
    public ApiResult addBizShop(@Validated(Shop.Add.class) @RequestBody Shop shop) {
        shop.setCreateId(SessionUtil.getTokenSubject().getUid());
        ShopType shopType = shopFacade.getShopTypeByCode(shop.getType());
        if (shopType != null) {
            shop.setTypeName(shopType.getName());
        }
        return ApiResult.ok(shopFacade.addShop(shop));
    }

    /**
     * @param shop
     * @return
     */
    @PostMapping(name = "编辑商家", path = "/shop/edit")
    @Authorization
    public ApiResult editBizShop(@Validated(Shop.Update.class) @RequestBody Shop shop) {
        Shop toUpdate = shopFacade.editShop(shop);
        if (toUpdate != null) {
            // 修改已发布的优惠券
            CompletableFuture.runAsync(() -> {
                Coupon coupon = new Coupon();
                coupon.setCommunityIds(shop.getCommunityIds());
                coupon.setShopName(shop.getName());
                coupon.setAddress(shop.getAddress());
                coupon.setTelPhone(shop.getTelPhone());
                couponFacade.updateCouponsByShopId(coupon, shop.getId());
            });
        }
        return ApiResult.ok(toUpdate);
    }

    /**
     * 获取商家详细
     *
     * @param id
     * @return
     */
    @GetMapping(name = "商家信息", path = "/shop/{id}/detail")
    public ApiResult getBizShopById(@PathVariable("id") ObjectId id) {
        Shop shops = shopFacade.findOne(id);
        return ApiResult.ok(shops);
    }

    /**
     * 获取商家详细（APP）
     *
     * @param id
     * @return
     */
    @GetMapping(name = "商家详细(包括优惠券信息)", path = "/shop/{id}/getInfo")
    public ApiResult getBizShopInfo(@PathVariable("id") ObjectId id, @RequestHeader("BIT-UID") Object userId) {
        Shop shop = shopFacade.findOneWithDetail(id, userId == null ? null : new ObjectId(userId.toString()));
        return ApiResult.ok(shop);
    }

    /**
     * 商家分页（H5）
     *
     * @param entity
     * @return
     */
    @PostMapping(name = "商家分页", path = "/shop/page")
    public ApiResult queryBizShopPage(@RequestBody Shop entity,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        entity.setPage(entity.getPage() == null ? page : entity.getPage());
        entity.setSize(entity.getSize() == null ? size : entity.getSize());
        Page<Shop> pages = shopFacade.queryShopPage(entity);
        return ApiResult.ok(pages);
    }

    /**
     * 商家列表
     *
     * @param request
     * @return
     */
    @PostMapping(name = "商家列表", path = "/shop/list")
    public ApiResult queryBizShopList(@RequestBody ShopRequest request) {
        return ApiResult.ok(shopFacade.getShopList(request));
    }

    /**
     * 根据商家ID获取绑定的社区列表
     *
     * @param id
     * @return
     */
    @GetMapping(name = "根据商家ID获取绑定的社区列表", path = "/shop/{id}/getCommunity")
    public ApiResult getCommunityByShopId(@PathVariable("id") ObjectId id) {
        Shop shop = shopFacade.findOne(id);
        Set<ObjectId> communityIds = shop.getCommunityIds();
        List<Community> communities = communityFacade.findByIds(communityIds);
        return ApiResult.ok(communities);
    }

    /**
     * 周边商家
     * 条件：
     * 1.社区ID
     * 2.经纬度
     *
     * @param shop
     * @return
     */
    @PostMapping(name = "周边商家列表", path = "/shop/getLocalShops")
    public ApiResult getLocalShops(@Validated @RequestBody ShopRequest shop,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        shop.setPage(shop.getPage() == null ? page : shop.getPage());
        shop.setSize(shop.getSize() == null ? size : shop.getSize());
        Page shops = shopFacade.queryByLocal(shop);
        return ApiResult.ok(shops);
    }

    /**
     * 条件：社区ID或经纬度
     * 顶部商家和优惠券
     *
     * @return
     */
    @PostMapping(name = "顶部商家和优惠券", path = "/shop/getVipShops")
    public ApiResult getTop2Shop(@RequestBody Map<String, Object> params) {
        Object communityId = params.get("communityId");
        if (communityId == null) {
            return ApiResult.error(-1, "社区ID不能为空");
        }
        VipVO vo = new VipVO();
        // 商家
        ShopItem shop = shopFacade.getVIP(new ObjectId(communityId.toString()));
        vo.setShop(shop);
        // 优惠券
        CouponItem coupon = couponFacade.getVipCoupon(new ObjectId(communityId.toString()));
        vo.setCoupon(coupon);
        return ApiResult.ok(vo);
    }

    /**
     * 热门商家（APP）
     *
     * @param shop
     * @return
     */
    @PostMapping(name = "热门商家分页", path = "/shop/popular")
    public ApiResult getNearShop(@Validated @RequestBody ShopRequest shop,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        shop.setPage(shop.getPage() == null ? page : shop.getPage());
        shop.setSize(shop.getSize() == null ? size : shop.getSize());
        if (shop.getType() == null || ShopPopularType.Boutique.key.equals(shop.getType())) {
            shop.setType(null);
        }
        Page<ShopItem> shopPage = shopFacade.queryByLocal(shop);
        return ApiResult.ok(shopPage);
    }

    /**
     * 获取周边优惠
     *
     * @param coupon
     * @return
     */
    @PostMapping(name = "周边优惠分页", path = "/shop/coupons")
    public ApiResult getMaxPriceCoupons(@RequestBody Coupon coupon,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestHeader("BIT-UID") String userId) {
        coupon.setPage(coupon.getPage() == null ? page : coupon.getPage());
        coupon.setSize(coupon.getSize() == null ? size : coupon.getSize());
        if (coupon.getCommunityId() == null) {
            return ApiResult.error(-1, "社区ID不能为空");
        }
        if (coupon.getShopType() == null || ShopPopularType.Boutique.key.equals(coupon.getShopType())) {
            coupon.setShopType(null);
        }
        Page<CouponItem> couponPage = couponFacade.getVipCoupons(coupon);
        // 获取用户所有未使用的优惠券
        if (userId != null) {
            Map<ObjectId, CouponUseVO> map = couponToUserFacade.getUsersCoupons(new ObjectId(userId));
            if (!map.isEmpty()) {
                List<CouponItem> couponList = couponPage.getRecords();
                couponList.parallelStream().forEach(couponItem -> {
                    couponItem.setCouponToUserId(map.get(couponItem.getId()) == null ? null : map.get(couponItem.getId()).getCouponToUserId());
                    couponItem.setUseStatus(map.get(couponItem.getId()) == null ? null : map.get(couponItem.getId()).getUseStatus());
                });
            }
        }
        return ApiResult.ok(couponPage);
    }

    /**
     * 删除商家
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除商家", path = "/shop/{id}/delete")
    public ApiResult deleteShop(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(shopFacade.deleteShop(id));
    }

    // =============================【商品表】==============================

    /**
     * 新增商品
     *
     * @param goods
     * @return
     */
    @PostMapping(name = "新增商品", path = "/goods/add")
    @Authorization
    public ApiResult addMerchandise(@Validated(Goods.Add.class) @RequestBody Goods goods) {
        return ApiResult.ok(goodsFacade.addGoods(goods, SessionUtil.getTokenSubject().getUid()));
    }

    /**
     * 更新商品
     *
     * @param goods
     * @return
     */
    @PostMapping(name = "编辑商品", path = "/goods/edit")
    @Authorization
    public ApiResult updateMerchandise(@Validated(Goods.Update.class) @RequestBody Goods goods) {
        return ApiResult.ok(goodsFacade.editGoods(goods));
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping(name = "商品详情", path = "/goods/{id}/detail")
    public ApiResult getMerchandise(@PathVariable("id") ObjectId id) {
        Goods item = goodsFacade.findOne(id);
        return ApiResult.ok(item);
    }

    /**
     * 商品分页
     *
     * @param goods
     * @return
     */
    @PostMapping(name = "商品分页", path = "/goods/page")
    public ApiResult queryMerchandisePage(@RequestBody Goods goods,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        goods.setPage(goods.getPage() == null ? page : goods.getPage());
        goods.setSize(goods.getSize() == null ? size : goods.getSize());
        return ApiResult.ok(goodsFacade.queryGoodsPage(goods));
    }

    /**
     * 商品删除
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除商品", path = "/goods/{id}/delete")
    public ApiResult deleteMerchandise(@PathVariable("id") ObjectId id) {
        return goodsFacade.deleteGoods(id) == null ? ApiResult.error(-1, "删除失败") : ApiResult.ok();
    }

    // =============================【优惠券】==============================

    /**
     * 新增优惠券（H5）
     *
     * @param coupon
     * @return
     */
    @PostMapping(name = "新增优惠券", path = "/coupon/add")
    @Authorization
    public ApiResult addCoupon(@Validated(Coupon.Add.class) @RequestBody Coupon coupon) {
        coupon.setCreateId(SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(couponFacade.addCoupon(coupon));
    }

    /**
     * 更新优惠券（H5）
     *
     * @param coupon
     * @return
     */
    @PostMapping(name = "编辑优惠券", path = "/coupon/edit")
    @Authorization
    public ApiResult editCoupon(@Validated(Coupon.Update.class) @RequestBody Coupon coupon) {
        return ApiResult.ok(couponFacade.editCoupon(coupon));
    }

    /**
     * 根据ID查询优惠券信息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "优惠券详情", path = "/coupon/{id}/detail")
    public ApiResult getCoupon(@PathVariable("id") ObjectId id) {
        Coupon coupon = couponFacade.findDetail(id, SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(coupon);
    }

    /**
     * 删除优惠券信息（H5）
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除优惠券", path = "/coupon/{id}/delete")
    @Authorization
    public ApiResult deleteCoupon(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(couponFacade.deleteCoupon(id));
    }

    /**
     * 优惠券分页（H5）
     *
     * @param coupon
     * @return
     */
    @PostMapping(name = "优惠券分页", path = "/coupon/page")
    public ApiResult queryCouponPage(@RequestBody Coupon coupon,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        coupon.setPage(coupon.getPage() == null ? page : coupon.getPage());
        coupon.setSize(coupon.getSize() == null ? size : coupon.getSize());
        return ApiResult.ok(couponFacade.queryCouponPage(coupon));
    }

    /**
     * 修改数据状态
     *
     * @param coupon
     * @return
     */
    @PostMapping(name = "更换优惠券有效状态", path = "/coupon/valid")
    @Authorization
    public ApiResult changeCouponPublish(@Validated(Coupon.Search.class) @RequestBody Coupon coupon) {
        return ApiResult.ok(couponFacade.changeCouponPublish(coupon));
    }

    // ==========================================【商家类型及标签】=======================================

    /**
     * 新增商家类型
     *
     * @param shopType
     * @return
     */
    @PostMapping(name = "新增商家类型", path = "/shop/type/add")
    @Authorization
    public ApiResult addBizType(@Validated @RequestBody ShopType shopType) {
        shopType.setCreateId(SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(shopFacade.addShopType(shopType));
    }

    /**
     * 获取商家类型列表(APP不需分页)
     *
     * @return
     */
    @GetMapping(name = "商家类型列表", path = "/shop/type/list")
    public ApiResult getShopTypes() {
        return ApiResult.ok(shopFacade.getShopTypes(new ShopType()));
    }

    // ==========================================【获取优惠券】===========================================

    /**
     * 领取优惠券 （APP）
     *
     * @param couponToUser
     * @return
     */
    @PostMapping(name = "领取优惠券", path = "/couponToUser/add")
    @Authorization
    public ApiResult addCouponToUser(@Validated(CouponToUser.Add.class) @RequestBody CouponToUser couponToUser) {
        UserVO user = SessionUtil.getCurrentUser();

        CouponToUser added = couponToUserFacade.addCouponToUser(couponToUser.getCouponId(), user.getId(), user.getName());
        if (added == null) {
            return ApiResult.error(-1, "获取优惠券失败");
        }
        return ApiResult.ok(added);
    }

    /**
     * 获取优惠券信息（APP）
     * <p>
     * 扫描使用
     *
     * @param id
     * @return
     */
    @GetMapping(name = "优惠券详情", path = "/couponToUser/{id}/get")
    public ApiResult getCouponToUser(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(couponToUserFacade.findOne(id));
    }

    /**
     * 使用优惠券
     *
     * @param params
     * @return
     */
    @PostMapping(name = "使用优惠券", path = "/couponToUser/use")
    public ApiResult useCoupon(@RequestBody Map<String, Object> params) {
        Object validCode = params.get("validCode");
        Object id = params.get("id");
        if (id == null) {
            return ApiResult.error(-1, "优惠券ID为空");
        }
        if (validCode == null) {
            return ApiResult.error(-1, "请输入校验码");
        }
        CouponToUser edited = couponToUserFacade.useCoupon(new ObjectId(id.toString()), validCode.toString());
        return ApiResult.ok(edited);
    }

    /**
     * 删除优惠券
     *
     * @param id
     * @return
     */
    @GetMapping(name = "删除优惠券", path = "/couponToUser/{id}/delete")
    @Authorization
    public ApiResult deleteCouponToUser(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(couponToUserFacade.deleteCouponToUser(id));
    }

    /**
     * 优惠券分页（APP）
     *
     * @param couponToUser
     * @return
     */
    @PostMapping(name = "优惠券分页", path = "/couponToUser/page")
    @Authorization
    public ApiResult queryCouponToUserPage(@RequestBody CouponToUser couponToUser,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        couponToUser.setPage(couponToUser.getPage() == null ? page : couponToUser.getPage());
        couponToUser.setSize(couponToUser.getSize() == null ? size : couponToUser.getSize());
        couponToUser.setUserId(SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(couponToUserFacade.queryCouponToUserPage(couponToUser));
    }

    // ==================================================[商圈公共接口]======================================================= //

    /**
     * 创建商铺
     * 销售直接创建
     *
     * @param shopApproval
     * @return
     */
    @Authorization
    @PostMapping(name = "创建商铺", path = "/shop/create")
    public ApiResult createShop(@Validated(ShopApproval.Create.class) @RequestBody ShopApproval shopApproval) {
        ShopApprovalDTO shopApprovalDTO = new ShopApprovalDTO();
        BeanUtils.copyProperties(shopApproval, shopApprovalDTO);
        shopApprovalDTO.setCompanyId(companyId);
        shopApprovalDTO.setPartner(shopApproval.getPartner());
        // 邀请人
        User user = commonUserFacade.getUserByPhone(shopApproval.getContactPhone());
        shopApprovalDTO.setProposer(user == null ? null : user.getId());
        shopApprovalDTO.setInviter(user == null ? null : user.getId());
        // 审核人为创建人
        shopApprovalDTO.setApprover(getUserId());
        shopApprovalDTO.setLocation(new Location(shopApproval.getLng(), shopApproval.getLat()));
        return ApiResult.ok(businessFacade.createShop(shopApprovalDTO, user.getName()));
    }

    /**
     * 修改商铺信息（通过审核）
     *
     * @param shopDTO
     * @return
     */
    @Authorization
    @PostMapping(name = "编辑商铺", path = "/shop/modify")
    public ApiResult modifyShop(@Validated(ShopDTO.Modify.class) @RequestBody ShopDTO shopDTO) {
        shopDTO.setManager(getUserId());
        return ApiResult.ok(businessFacade.modifyShop(shopDTO));
    }

    /**
     * 获取未审核的商店信息
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "获取未审核商铺详情", path = "/shop/approval/{id}/detail")
    public ApiResult getShopApprovalById(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(businessFacade.getShopApprovalById(id));
    }

    /**
     * 商家商铺申请
     *
     * @param shopApproval
     * @return
     */
    @Authorization
    @PostMapping(name = "商家申请商铺", path = "/shop/approval/apply")
    public ApiResult applyShop(@Validated(ShopApproval.Create.class) @RequestBody ShopApproval shopApproval) {
        ShopApprovalDTO shopApprovalDTO = new ShopApprovalDTO();
        BeanUtils.copyProperties(shopApproval, shopApprovalDTO);
        shopApprovalDTO.setPartner(SessionUtil.getAppSubject().getPartner());
        shopApprovalDTO.setCompanyId(companyId);
        // 申请人
        shopApprovalDTO.setProposer(getUserId());
        shopApprovalDTO.setInviter(getUserId());
        shopApprovalDTO.setContactPhone(SessionUtil.getCurrentUser().getPhone());
        // 坐标
        shopApprovalDTO.setLocation(new Location(shopApproval.getLng(), shopApproval.getLat()));
        return ApiResult.ok(businessFacade.applyShop(shopApprovalDTO));
    }

    /**
     * 商家店铺审核
     * <p>
     * 参数为：id-商家ID
     *
     * @param shopParams
     * @return
     */
    @Authorization
    @PostMapping(name = "通过商家商铺申请", path = "/shop/approval/approve")
    public ApiResult approveShopApproval(@Validated(ShopParamsDTO.Audit.class) @RequestBody ShopParamsDTO shopParams) {
        return ApiResult.ok(businessFacade.approveShopApprovalByShopApprovalId(shopParams.getId(), getUserId(), getUserName()));
    }

    /**
     * 拒绝商铺申请
     * <p>
     * 参数为：id-商家ID
     *
     * @param shopParams
     * @return
     */
    @Authorization
    @PostMapping(name = "驳回商家商铺申请", path = "/shop/approval/reject")
    public ApiResult rejectShopApproval(@Validated(ShopParamsDTO.Audit.class) @RequestBody ShopParamsDTO shopParams) {
        businessFacade.rejectShopApprovalByShopApprovalId(shopParams.getId(), getUserId());
        return ApiResult.ok();
    }

    /**
     * 从驳回状态再次申请
     * <p>
     * 参数为：id-商家ID
     *
     * @param shopParams
     * @return
     */
    @Authorization
    @PostMapping(name = "从驳回状态再次申请商铺", path = "/shop/approval/reapply")
    public ApiResult reapplyShopApproval(@Validated(ShopParamsDTO.Audit.class) @RequestBody ShopParamsDTO shopParams) {
        businessFacade.reapplyShopByShopApprovalId(shopParams.getId(), getUserId());
        return ApiResult.ok();
    }

    /**
     * 注销已审核的商店
     *
     * @param shopParams
     * @return
     */
    @Authorization
    @PostMapping(name = "注销商铺", path = "/shop/disabled")
    public ApiResult disabledShop(@Validated(ShopParamsDTO.Disabled.class) @RequestBody ShopParamsDTO shopParams) {
        businessFacade.disableShop(shopParams.getId());
        return ApiResult.ok();
    }

    /**
     * 获取审核商铺列表-分页
     *
     * @param pageQuery
     * @return
     */
    @Authorization
    @PostMapping(name = "审核商铺分页", path = "/shop/approval/page")
    public ApiResult listShopApprovalPage(@RequestBody ShopApprovalPageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        pageQuery.setPartner(SessionUtil.getAppSubject().getPartner());
        return ApiResult.ok(businessFacade.listShopApprovals(pageQuery));
    }

    /**
     * 获取店铺（通过审核的店铺）
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "获取已审核通过商铺详情", path = "/shop/{id}/detail2")
    public ApiResult getShopById(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(businessFacade.getShopByShopId(id));
    }

    /**
     * 商店列表
     *
     * @param query
     * @return
     */
    @Authorization
    @PostMapping(name = "商铺列表", path = "/shop/list2")
    public ApiResult listShop(@RequestBody ShopIncrementalQuery query) {
        query.setPartner(SessionUtil.getAppSubject().getPartner());
        return ApiResult.ok(businessFacade.listShops(query));
    }

    /**
     * 商店分页（通过审核的）
     *
     * @param query
     * @return
     */
    @Authorization
    @PostMapping(name = "通过审核的商铺分页", path = "/shop/page2")
    public ApiResult listShopPage(@RequestBody ShopPageQuery query) {
        query.setPartner(SessionUtil.getAppSubject().getPartner());
        return ApiResult.ok(businessFacade.listShops(query));
    }

    /**
     * 根据管理员获取其下商店
     *
     * @return
     */
    @Authorization
    @GetMapping(name = "获取可管理的商铺列表", path = "/shop/manager/list")
    public ApiResult listShopByManager() {
        List<ShopRelationsDTO> shops = businessFacade.listShopsByPartnerAndUserId(
                SessionUtil.getAppSubject().getPartner(), getUserId(), SessionUtil.getCurrentUser().getPhone());
        return ApiResult.ok(shops);
    }

    /**
     * 判断登录用户是否拥有店铺
     *
     * @return
     */
    @Authorization
    @GetMapping(name = "判断登录用户是否拥有商铺", path = "/shop/possess")
    public ApiResult ownShopByUserId() {
        return ApiResult.ok(businessFacade.getOwnShops(getUserId(), SessionUtil.getCurrentUser().getPhone()));
    }

    /**
     * 用户与手机号绑定
     *
     * @param shopParams
     * @return
     */
    @Authorization
    @PostMapping(name = "用户手机号关联商铺", path = "/shop/user/regist")
    public ApiResult registerShopManagerByPhone(@Validated(ShopParamsDTO.Register.class) @RequestBody ShopParamsDTO shopParams) {
        Integer count = businessFacade.registerShopManagerByPhone(shopParams.getContactPhone(), shopParams.getUserId());
        if (count > 0) {
            return ApiResult.ok();
        }
        return ApiResult.error(-1, "用户关联手机号失败");
    }

    /**
     * 小程序[我的店铺]
     *
     * @param shopId
     * @return
     */
    @Authorization
    @GetMapping(name = "商铺详情(小程序)", path = "/shop/{shopId}/getShopTemplate")
    public ApiResult getShopTemplate(@PathVariable("shopId") ObjectId shopId) {
        return ApiResult.ok(businessFacade.getOwnShopDetailByShopId(shopId));
    }

    /**
     * 小程序[商店审核历史搜索]
     *
     * @param query
     * @return
     */
    @PostMapping(name = "商铺审核历史分页(小程序)", path = "/shop/approval/search")
    public ApiResult searchShopApproval(@RequestBody ShopApprovalSubjectPageQuery query) {
        query.setPage(query.getPage() == null ? 1 : query.getPage());
        query.setSize(query.getSize() == null ? 10 : query.getSize());
        query.setPartner(SessionUtil.getAppSubject().getPartner());
        return ApiResult.ok(businessFacade.listShopApprovals(query));
    }

    /**
     * 商铺统计访问量
     *
     * @param accessRecordDTO
     * @return
     */
    @PostMapping(name = "商铺统计访问量", path = "/shop/statistics")
    public ApiResult statisticsShop(@RequestBody AccessRecordDTO accessRecordDTO) {
        return ApiResult.ok(businessFacade.statisticsAccessRecordByShopIdAndActivityIdAndInsertAt(accessRecordDTO));
    }

    /**
     * 优惠券统计
     *
     * @param accessRecordDTO
     * @return
     */
    @PostMapping(name = "优惠券数量统计", path = "/coupon/statistics")
    public ApiResult statisticsCoupon(@RequestBody AccessRecordDTO accessRecordDTO) {
        return ApiResult.ok(businessFacade.statisticsCouponAcquireAndConsume(accessRecordDTO));
    }


    // == 优惠券 ==

    /**
     * 创建优惠券模版
     *
     * @param entity
     * @return
     */
    @Authorization
    @PostMapping(name = "创建优惠券模版", path = "/couponTemplate/create")
    public ApiResult createCouponTemplate(@Validated(CouponTemplate.Create.class) @RequestBody CouponTemplate entity) {
        // 如果是固定时长，将天数转化秒
        if (entity.getTermType().equals(TermTypeEnum.FIXED_TERM.value())) {
            if (entity.getDays() == null) {
                throw new BizException(-1, "有效期限的天数不能为空");
            }
            if (entity.getEndAt() == null) {
                throw new BizException(-1, "结束发放时间不能为空");
            }
            entity.setCouponDelay(entity.getDays() * 24 * 60 * 60L);
        } else {
            if (entity.getCouponEnableAt() == null || entity.getCouponDisableAt() == null) {
                throw new BizException(-1, "生效时间不能为空");
            }
            if (entity.getCouponEnableAt().after(entity.getCouponDisableAt())) {
                throw new BizException(-1, "生效开始时间不能大于生效结束时间");
            }
        }
        CouponTemplateDTO couponTemplateDTO = new CouponTemplateDTO();
        BeanUtils.copyProperties(entity, couponTemplateDTO);
        // 商店ID
        couponTemplateDTO.setShopIds(Collections.singletonList(entity.getShopId()));
        // 创建人ID
        couponTemplateDTO.setCreator(getUserId());
        return ApiResult.ok(businessFacade.createCouponTemplate(couponTemplateDTO));
    }

    /**
     * 根据ID获取优惠券模版信息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "获取优惠券模版详情", path = "/couponTemplate/{id}/detail")
    public ApiResult getCouponTemplateById(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(businessFacade.getCouponTemplateByCouponTemplateId(id, null));
    }

    /**
     * 获取商店的优惠券列表
     *
     * @param shopId
     * @return
     */
    @Authorization
    @GetMapping(name = "获取商铺的优惠券列表", path = "/couponTemplate/{shopId}/list")
    public ApiResult listCouponTemplate(@PathVariable("shopId") ObjectId shopId) {
        return ApiResult.ok(businessFacade.listCouponTemplatesByShopId(shopId));
    }

    /**
     * 结束优惠券模版
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "结束优惠券模版", path = "/couponTemplate/{id}/finish")
    public ApiResult finishCouponTemplate(@PathVariable("id") ObjectId id) {
        businessFacade.finishCouponTemplateByCouponTemplateId(id);
        return ApiResult.ok();
    }

    /**
     * 删除优惠券模版
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "删除优惠券模版", path = "/couponTemplate/{id}/delete")
    public ApiResult deleteCouponTemplate(@PathVariable("id") ObjectId id) {
        businessFacade.deleteCouponTemplateByCouponTemplateId(id);
        return ApiResult.ok();
    }

    /**
     * 获取优惠券模版分页
     *
     * @param pageQuery
     * @return
     */
    @Authorization
    @PostMapping(name = "优惠券模版分页", path = "/couponTemplate/page")
    public ApiResult listCouponTemplate(@RequestBody CouponTemplatePageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        cn.bit.common.facade.data.Page<CouponTemplateDTO> page = businessFacade.listCouponTemplates(pageQuery);
        return ApiResult.ok(page);
    }

    /**
     * 获取优惠券列表
     *
     * @param query
     * @return
     */
    @Authorization
    @PostMapping(name = "优惠券列表", path = "/couponTemplate/list")
    public ApiResult listCouponTemplate(@RequestBody CouponTemplateIncrementalQuery query) {
        return ApiResult.ok(businessFacade.listCouponTemplates(query));
    }

    /**
     * 领取优惠券
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "领取优惠券", path = "/couponTemplate/receive")
    public ApiResult receiveCoupon(@RequestParam("id") ObjectId id, @RequestParam("shopId") ObjectId shopId) {
        return ApiResult.ok(businessFacade.receiveCoupon(id, getUserId(), shopId));
    }

    /**
     * 消费优惠券
     *
     * @param couponParams
     * @return
     */
    @Authorization
    @PostMapping(name = "消费优惠券", path = "/coupon/consume")
    public ApiResult consumeCoupon(@Validated(CouponParamsDTO.Consume.class) @RequestBody CouponParamsDTO couponParams) {
        businessFacade.consumeCoupon(couponParams.getId(), couponParams.getCode(), getUserId(), getUserName(), couponParams.getRemark());
        return ApiResult.ok();
    }

    /**
     * 查看用户领取的优惠券信息
     *
     * @param pageQuery
     * @return
     */
    @Authorization
    @PostMapping(name = "用户领取的优惠券分页", path = "/coupon/page2")
    public ApiResult listCoupons(@RequestBody CouponPageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        pageQuery.setUserId(getUserId());
        return ApiResult.ok(businessFacade.listCoupons(pageQuery));
    }

    /**
     * 获取小程序首页统计数据
     *
     * @param shopId
     * @return
     */
    @GetMapping(name = "小程序首页数据统计", path = "/shop/{shopId}/coupon/statistics")
    public ApiResult appletStatistics(@PathVariable("shopId") ObjectId shopId) {
        return ApiResult.ok(businessFacade.appletStatistics(shopId));
    }

    /**
     * 获取核销列表分页
     *
     * @param pageQuery
     * @return
     */
    @Authorization
    @PostMapping(name = "核销列表分页", path = "/coupon/usage-record")
    public ApiResult getUsedCouponRecordByShopId(@RequestBody CouponPageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        return ApiResult.ok(businessFacade.listUsedCoupons(pageQuery));
    }
    // ====== [商品] ======

    /**
     * 新增商品
     *
     * @param goodsDTO
     * @return
     */
    @Authorization
    @PostMapping(name = "新增商品", path = "/goods/create")
    public ApiResult createGoods(@Validated(GoodsDTO.Create.class) @RequestBody GoodsDTO goodsDTO) {
        goodsDTO.setCreator(getUserId());
        return ApiResult.ok(businessFacade.createGoods(goodsDTO));
    }

    @Authorization
    @PostMapping(name = "编辑商品", path = "/goods/modify")
    public ApiResult modifyGoods(@Validated(GoodsDTO.Modify.class) @RequestBody GoodsDTO goodsDTO) {
        goodsDTO.setCreator(getUserId());
        return ApiResult.ok(businessFacade.modifyGoods(goodsDTO));
    }

    /**
     * 获取商品ID
     *
     * @param id
     * @return
     */
    @GetMapping(name = "商铺详情", path = "/goods/{id}/detail2")
    public ApiResult getGoods(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(businessFacade.getGoodsByGoodsId(id));
    }

    /**
     * 删除商品
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "删除商品", path = "/goods/{id}/delete2")
    public ApiResult deleteGoods(@PathVariable("id") ObjectId id) {
        businessFacade.deleteGoodsByGoodsId(id);
        return ApiResult.ok();
    }

    /**
     * 顶置
     *
     * @param params
     * @return
     */
    @Authorization
    @PostMapping(name = "置顶商品", path = "/goods/stick")
    public ApiResult stickGoods(@RequestBody Map<String, Object> params) {
        Object id = params.get("id");
        if (id == null) {
            throw new BizException(-1, "缺少商品ID");
        }
        return ApiResult.ok(businessFacade.stickGoodsByGoodsId(new ObjectId(id.toString())));
    }

    /**
     * 商品列表分页
     *
     * @param pageQuery
     * @return
     */
    @PostMapping(name = "商品分页", path = "/goods/page2")
    public ApiResult listGoods(@RequestBody GoodsPageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        return ApiResult.ok(businessFacade.listGoods(pageQuery));
    }

    /**
     * 商品列表
     *
     * @param shopId
     * @return
     */
    @GetMapping(name = "商品列表", path = "/goods/{shopId}/list")
    public ApiResult listGoods(@PathVariable("shopId") ObjectId shopId) {
        GoodsIncrementalQuery query = new GoodsIncrementalQuery();
        query.setShopId(shopId);
        return ApiResult.ok(businessFacade.listGoods(query));
    }

    // =======[活动]======

    /**
     * 新增活动
     *
     * @param activityDTO
     * @return
     */
    @Authorization
    @PostMapping(name = "新增活动", path = "/activity/create")
    public ApiResult createActivity(@Validated(ActivityDTO.Create.class) @RequestBody ActivityDTO activityDTO) {
        if (activityDTO.getBeginAt() == null || activityDTO.getEndAt() == null) {
            throw new BizException(-1, "活动时间不能为空");
        }
        if (activityDTO.getBeginAt().after(activityDTO.getEndAt())) {
            throw new BizException(-1, "开始时间不能大于结束时间");
        }
        if (activityDTO.getStatus().equals(ActivityStatusEnum.NOT_STARTED.value()) || activityDTO.getStatus().equals(ActivityStatusEnum.FINISHED.value())) {
            throw new BizException(-1, "发布状态错误");
        }
        activityDTO.setCreator(getUserId());
        return ApiResult.ok(businessFacade.createActivity(activityDTO));
    }

    /**
     * 修改活动
     *
     * @param activityDTO
     * @return
     */
    @Authorization
    @PostMapping(name = "编辑活动", path = "/activity/modify")
    public ApiResult modifyActivity(@Validated(ActivityDTO.Modify.class) @RequestBody ActivityDTO activityDTO) {
        return ApiResult.ok(businessFacade.modifyActivity(activityDTO));
    }

    /**
     * 发布活动
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "发布活动", path = "/activity/{id}/publish")
    public ApiResult publishActivity(@PathVariable("id") ObjectId id) {
        businessFacade.publishActivityByActivityId(id);
        return ApiResult.ok();
    }

    /**
     * 终止活动
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "终止活动", path = "/activity/{id}/finish")
    public ApiResult finishActivity(@PathVariable("id") ObjectId id) {
        businessFacade.finishActivityByActivityId(id);
        return ApiResult.ok();
    }

    /**
     * 删除活动
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "删除活动", path = "/activity/{id}/delete")
    public ApiResult deleteActivity(@PathVariable("id") ObjectId id) {
        businessFacade.deleteActivityByActivityId(id);
        return ApiResult.ok();
    }

    /**
     * 活动列表
     *
     * @param query
     * @return
     */
    @PostMapping(name = "活动列表", path = "/activity/list")
    public ApiResult listActivity(@RequestBody ActivityIncrementalQuery query) {
        return ApiResult.ok(businessFacade.listActivities(query));
    }

    /**
     * 活动列表分页
     *
     * @param pageQuery
     * @return
     */
    @PostMapping(name = "活动分页", path = "/activity/page")
    public ApiResult listActivity(@RequestBody ActivityPageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        return ApiResult.ok(businessFacade.listActivities(pageQuery));
    }

    /**
     * 获取活动详情
     *
     * @param id
     * @return
     */
    @GetMapping(name = "获取活动详情", path = "/activity/{id}/detail")
    public ApiResult getActivityDetail(@PathVariable(value = "id") ObjectId id) {
        return ApiResult.ok(businessFacade.getActivity(id));
    }

    /**
     * 获取商家配置
     *
     * @param type
     * @return
     */
    @GetMapping(name = "商家某类型配置列表", path = "/shop/config/{type}/list")
    public ApiResult listShopConfigOptions(@PathVariable("type") Integer type) {
        return ApiResult.ok(businessFacade.listShopConfigOptions(type));
    }

    /**
     * 获取商家配置
     *
     * @return
     */
    @GetMapping(name = "商家配置列表", path = "/shop/config/list")
    public ApiResult listShopConfigOptions() {
        return ApiResult.ok(businessFacade.listShopConfigOptions());
    }

    // ========== APP ==========

    /**
     * 获取周围的商店-【APP接口】
     *
     * @param pageQuery
     * @return
     */
    @PostMapping(name = "获取周围的商店", path = "/shop/nearby")
    public ApiResult listShopsByLocation(@RequestBody ShopPageQuery pageQuery) {
        pageQuery.setPage(pageQuery.getPage() == null ? 1 : pageQuery.getPage());
        pageQuery.setSize(pageQuery.getSize() == null ? 10 : pageQuery.getSize());
        pageQuery.setPartner(SessionUtil.getAppSubject().getPartner());
        return ApiResult.ok(businessFacade.listShopsByLocation(pageQuery));
    }

    /**
     * 获取商店全部信息（商店信息+优惠券+商品+活动）【APP接口】
     *
     * @param id
     * @return
     */
    @GetMapping(name = "获取商店全部信息(商店信息+优惠券+商品+活动)", path = "/shop/{id}/allDetail")
    public ApiResult getShopDetail(@PathVariable("id") ObjectId id) {
        ShopAppDetailDTO shopAppDetailDTO = businessFacade.getShopDetailByShopId(id, getUserId());
        return ApiResult.ok(shopAppDetailDTO);
    }

    /**
     * 获取周边优惠券【APP接口】
     *
     * @param query
     * @return
     */
    @Authorization
    @PostMapping(name = "获取周边优惠券", path = "/shop/coupon/periphery")
    public ApiResult getLocationCoupon(@RequestBody CouponTemplatePageQuery query) {
        query.setPage(query.getPage() == null ? 1 : query.getPage());
        query.setSize(query.getSize() == null ? 10 : query.getSize());
        query.setUserId(getUserId());
        return ApiResult.ok(businessFacade.listCouponTemplatesByLocation(query));
    }

    /**
     * 获取优惠券信息（包含用户是否已获取或已使用）
     *
     * @param id
     * @return
     */
    @Authorization
    @GetMapping(name = "获取优惠券信息(包含用户是否已获取或已使用)", path = "/couponTemplate/{id}/current/detail")
    public ApiResult getCouponDetail(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(businessFacade.getCouponTemplateByCouponTemplateId(id, getUserId()));
    }

    /**
     * 根据Id获取领取的优惠券，或者code+shopId获取
     *
     * @param id
     * @param code
     * @param shopId
     * @return
     */
    @Authorization
    @GetMapping(name = "根据Id获取领取的优惠券，或者code+shopId获取", path = "/coupon/detail")
    public ApiResult getCouponDetailById(@RequestParam(value = "id", required = false) ObjectId id,
                                         @RequestParam(value = "code", required = false) String code,
                                         @RequestParam(value = "shopId", required = false) ObjectId shopId) {
        if (id == null) {
            if (StringUtil.isBlank(code) || shopId == null) {
                throw new BizException(-1, "参数错误，Id或code+shopId不能为空");
            }
        }
        return ApiResult.ok(businessFacade.findCouponByIdOrCodeAndShopId(id, code, shopId));
    }


    /**
     * 根据店铺ID获取商店员工列表
     *
     * @param shopId
     * @return
     */
    @GetMapping(name = "获取某店铺员工列表", path = "/shop/worker/{shopId}/list")
    public ApiResult listShopWorker(@PathVariable("shopId") ObjectId shopId) {
        List<ShopWorker> shopWorkers = businessFacade.listShopWorker(shopId, null);
        for (ShopWorker shopWorker : shopWorkers) {
            User user = commonUserFacade.getUserByUserId(shopWorker.getUserId());
            if (user != null) {
                shopWorker.setAvatarUrl(user.getAvatar());
            }
        }
        return ApiResult.ok(shopWorkers);
    }

    /**
     * 根据员工表ID获取用户信息
     *
     * @param id
     * @return
     */
    @GetMapping(name = "获取店铺员工信息", path = "/employee/{id}/detail")
    public ApiResult getShopWorker(@PathVariable("id") ObjectId id) {
        return ApiResult.ok(businessFacade.getShopWorkerByShopWorkerId(id));
    }

    /**
     * 获取店员邀请码
     *
     * @param shopId
     * @return
     */
    @Authorization
    @GetMapping(name = "获取店员邀请码", path = "/employee/{shopId}/invite-code")
    public ApiResult getInviteCode(@PathVariable("shopId") ObjectId shopId) {
        String code = businessFacade.getInviteCode(shopId, getUserId());
        return ApiResult.ok(code);
    }

    /**
     * 移除员工
     *
     * @param id
     * @return
     */
    @GetMapping(name = "移除员工", path = "/employee/remove")
    public ApiResult removeShopWorker(@RequestParam(value = "id") ObjectId id) {
        businessFacade.removeShopWorker(id);
        return ApiResult.ok();
    }

    /**
     * 确认邀请
     *
     * @param code
     * @return
     */
    @Authorization
    @GetMapping(name = "确认邀请", path = "/employee/invite/confirm")
    public ApiResult confirmInvitation(@RequestParam(value = "code") String code) {
        businessFacade.confirmInvitation(code, getUserId(), SessionUtil.getCurrentUser().getPhone(), getUserName());
        return ApiResult.ok();
    }

    private String getUserName() {
        return SessionUtil.getCurrentUser().getNickName();
    }

    private ObjectId getUserId() {
        return SessionUtil.getCurrentUser().getId();
    }

}