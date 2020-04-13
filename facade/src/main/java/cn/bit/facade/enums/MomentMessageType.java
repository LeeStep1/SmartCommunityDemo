package cn.bit.facade.enums;

/**
 * 消息通知类型
 */
public enum MomentMessageType {
    // （1：评论；2：点赞；3：屏蔽动态；4：屏蔽评论；5：禁言；）
    COMMENT(1), PRAISE(2), SHIELDINGMOMENT(3), SHIELDINGCOMMENT(4), SILENT(5);

    private int key;

    MomentMessageType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
