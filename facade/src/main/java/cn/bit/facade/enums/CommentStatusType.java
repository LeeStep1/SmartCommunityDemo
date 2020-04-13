package cn.bit.facade.enums;

/**
 * 动态评论的状态
 */
public enum CommentStatusType {
    // （1：正常；0：系统自动屏蔽；-1：管理员屏蔽；）
    NORMAL(1), AUDOSHIELDING(0), HANDSHIELDING(-1);

    private int key;

    CommentStatusType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
