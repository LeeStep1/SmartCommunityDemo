package cn.bit.facade.service.business;

import cn.bit.facade.model.business.Shop;
import cn.bit.facade.model.business.ShopType;
import cn.bit.facade.vo.business.ShopItem;
import cn.bit.facade.vo.business.ShopRequest;
import cn.bit.facade.vo.business.ShopVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/2
 * 商家接口
 *
 */
public interface ShopFacade {

    /**
     * 新增商家
     * @param shop
     * @return
     */
    Shop addShop(Shop shop);

    /**
     * 更新商家
     * @param shop
     * @return
     */
    Shop editShop(Shop shop) throws BizException;

    /**
     * 根据ID获取商家
     * @param id
     * @return
     */
    Shop findOne(ObjectId id) throws BizException;

    /**
     * 删除ID
     * @param id
     * @return
     */
    Shop deleteShop(ObjectId id) throws BizException;

    /**
     * 商家分页(H5)
     * @param shop
     * @return
     */
    Page queryShopPage(Shop shop) throws BizException;

    /**
     * 获取商家列表
     * @param request
     * @return
     */
    List<ShopVO> getShopList(ShopRequest request) throws BizException;

    // ========================[APP]==========================

    /**
     * 附近商家（APP）
     * @param shop
     * @return
     */
    Page<ShopItem> queryByLocal(ShopRequest shop) throws BizException;

    /**
     * 获取前排的两个商家
     * @param communityId
     * @return
     * @throws BizException
     */
    ShopItem getVIP(ObjectId communityId) throws BizException;

    /**
     * 热度自增
     * @param id
     */
    void increment(ObjectId id);

    // ========================[shopType]==========================
    /**
     * 新增商家类型
     * @param shopType
     * @return
     */
    ShopType addShopType(ShopType shopType);

    /**
     * 查找商家类型
     * @param typeCode
     * @return
     */
    ShopType getShopTypeByCode(Integer typeCode);

    /**
     * 获取商家类型（包含标签）
     * @param shopType
     * @return
     */
    List<ShopType> getShopTypes(ShopType shopType);

    /**
     * 更新过期的优惠券
     * @param id
     * @param couponNames
     */
    Shop updateCouponNamesForShop(ObjectId id, List<String> couponNames);

    Shop findOneWithDetail(ObjectId id, ObjectId userId);
}
