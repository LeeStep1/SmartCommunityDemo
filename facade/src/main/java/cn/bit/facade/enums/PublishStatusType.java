package cn.bit.facade.enums;

/**
 * 发布状态
 */
public enum PublishStatusType {

    PUBLISHED(1, "发布中"), UNPUBLISHED(0,"未发布"), REPEAL(-1, "撤销");

    public int key;

    public String value;

    PublishStatusType(int key, String value){
        this.key = key;
        this.value = value;
    }

}
