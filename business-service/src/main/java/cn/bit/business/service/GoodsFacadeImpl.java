package cn.bit.business.service;

import cn.bit.business.dao.GoodsRepository;
import cn.bit.business.dao.ShopRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.business.Goods;
import cn.bit.facade.model.business.Shop;
import cn.bit.facade.service.business.GoodsFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static cn.bit.facade.exception.business.BusinessException.*;

/**
 * Created by fxiao
 * on 2018/4/6
 */
@Service("goodsFacade")
public class GoodsFacadeImpl implements GoodsFacade {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Override
    public Goods addGoods(Goods goods, ObjectId userId) throws BizException {
        Shop shop = shopRepository.findByIdAndDataStatus(goods.getShopId(), DataStatusType.VALID.KEY);
        if (shop == null) {
            throw BIZ_SHOP_NULL;
        }
        goods.setShopsName(shop.getName());
        goods.setCreateAt(new Date());
        goods.setDataStatus(DataStatusType.VALID.KEY);
        return goodsRepository.insert(goods);
    }

    @Override
    public Goods editGoods(Goods goods) throws BizException{
        Goods toGet = goodsRepository.findById(goods.getId());
        if(toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY){
            throw BIZ_GOODS_NULL;
        }
        // 没有图片，则根据商家logo显示，商家没logo，则显示空
        if (goods.getPicture() == null) {
            Shop shop = shopRepository.findById(goods.getShopId());
            if (shop != null && shop.getLogo() != null) {
                goods.setPicture(shop.getLogo());
            } else {
                goods.setPicture(null);
            }
        }
        goods.setId(null);
        goods.setUpdateAt(new Date());
        return goodsRepository.updateByIdAndDataStatus(goods, toGet.getId(), DataStatusType.VALID.KEY);
    }

    @Override
    public Goods findOne(ObjectId id) throws BizException{
        return goodsRepository.findById(id);
    }

    @Override
    public Page<Goods> queryGoodsPage(Goods goods) throws BizException{
        Pageable pageable = new PageRequest(goods.getPage() - 1, goods.getSize(),
                new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Goods> resultPage = goodsRepository.findByNameRegexIgnoreNullAndShopIdIgnoreNullAndDataStatus(
                StringUtil.makeQueryStringAllRegExp(goods.getName()), goods.getShopId(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public List<Goods> getGoodsList(ObjectId shopId) throws BizException{
        return goodsRepository.findByShopIdAndDataStatusOrderByCreateAtAsc(shopId, DataStatusType.VALID.KEY);
    }

    @Override
    public Goods deleteGoods(ObjectId id) throws BizException{
        if(id == null){
            throw BIZ_GOODS_ID_NULL;
        }
        Goods toUpdate = new Goods();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return goodsRepository.updateByIdAndDataStatus(toUpdate, id, DataStatusType.VALID.KEY);
    }
}
