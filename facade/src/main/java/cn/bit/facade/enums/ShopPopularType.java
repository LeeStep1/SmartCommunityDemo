package cn.bit.facade.enums;

/**
 * 商家热度推荐
 */
public enum ShopPopularType {

    Boutique(0, "精品");

    public Integer key;

    public String value;

    ShopPopularType(Integer key, String value){
        this.key = key;
        this.value = value;
    }
}
