package cn.bit.facade.vo.business;

import cn.bit.facade.model.business.Coupon;
import cn.bit.facade.model.business.Shop;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Created by fxiao
 * on 2018/4/9
 */
public class ShopVO implements Serializable {

    private Shop shop;

    //----构造函数----
    public ShopVO() {
        this.shop = new Shop();
    }
    public ShopVO(Shop shop) {
        this.shop = shop;
    }

    //----getter/setter---
    public ObjectId getId() {
        return shop.getId();
    }
    public void setId(ObjectId id) {
        shop.setId(id);
    }

    public String getName() {
        return shop.getName();
    }
    public void setName(String name) {
        shop.setName(name);
    }

}
