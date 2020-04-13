package cn.bit.facade.enums;

/**
 * 用户认证审核状态
 */
public enum AuditStatusType {
    // （0：未审核；1：已审核；-1：驳回；-2：违规; 2: 已注销; 3: 已解绑; 4:审核中）
    UNREVIEWED(0), REVIEWED(1), REJECT(-1), VIOLATION(-2),CANCELLED(2),RELEASED(3),REVIEWING(4);

    private int type;

    public static String fromValue(int value) {
        switch (value) {
            case 1:
                return "已通过";
            case -1:
                return "已拒绝";
            case 2:
            case 3:
                return "已注销";
            case 0:
            case 4:
                return "申请";
            default:
                return "正在审核";
        }
    }
    AuditStatusType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
