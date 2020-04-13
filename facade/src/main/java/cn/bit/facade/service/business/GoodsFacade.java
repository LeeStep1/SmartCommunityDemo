package cn.bit.facade.service.business;

import cn.bit.facade.model.business.Goods;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/6
 * 商品接口
 */
public interface GoodsFacade {

    /**
     * 新增商品
     * @param goods
     * @param userId
     * @return
     */
    Goods addGoods(Goods goods, ObjectId userId) throws BizException;

    /**
     * 修改商品
     * @param goods
     * @return
     */
    Goods editGoods(Goods goods) throws BizException;

    /**
     * 查询详细
     * @param id
     * @return
     */
    Goods findOne(ObjectId id) throws BizException;

    /**
     * 商品分页
     * @param goods
     * @return
     */
    Page<Goods> queryGoodsPage(Goods goods) throws BizException;

    /**
     * 获取列表
     * @param shopsId
     * @return
     */
    List<Goods> getGoodsList(ObjectId shopsId) throws BizException;

    /**
     * 删除商品或者
     * 删除该商家的所有商品
     * @param id
     * @return
     */
    Goods deleteGoods(ObjectId id) throws BizException;
}
