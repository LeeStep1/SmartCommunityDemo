package cn.bit.facade.enums;

/**
 * 社区动态的状态
 */
public enum MomentStatusType {
    // （0：待审核；1：审核通过；2：自动通过；-1：未通过；-2：系统自动屏蔽;-3：管理员屏蔽;）
    UNREVIEWED(0), REVIEWED(1), AUTOREVIEWED(2), REJECT(-1), AUDOSHIELDING(-2), HANDSHIELDING(-3);

    private int key;

    MomentStatusType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
