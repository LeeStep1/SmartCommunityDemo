package cn.bit.facade.enums;

/**
 * 轮播图发布状态
 */
public enum SlidePublishType {

    PUBLISH(1, "已发布"), UNPUBLISH(0, "未发布");

    public Integer key;

    public String value;

    SlidePublishType(Integer key, String value){
        this.key = key;
        this.value = value;
    }

}
