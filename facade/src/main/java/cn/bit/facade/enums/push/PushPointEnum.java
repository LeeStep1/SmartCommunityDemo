package cn.bit.facade.enums.push;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PushPointEnum {

    NOTICE(0, "社区公告"),
    ALARM(2, "住户报警"),
    FAULT(2, "故障受理"),
    FAULT_ALLOCATED(1, "维修工单"),
    APPLY_ROOM_ATTESTATION(2, "住户申请"),
    AUDIT_ROOM_ATTESTATION(1, "房屋认证审核"),
    APPLY_ROOM_BINDING(1, "申请房间绑定"),
    AUDIT_ROOM_BINDING_BY_OWNER(2, "业主审核房间绑定"),
    AUDIT_ROOM_BINDING_BY_PROPERTY(1, "物业审核房间绑定"),
    UNBINDING_ROOM(1, "解绑房间"),
    ASK_FOR_PAY_BILL(1, "催缴账单"),
    REMINDER_PAY_BILL(1, "提醒缴费"),
    FREE_VIEW_CALLING(1, "全视通呼叫"),
    COMMENT(1, "评论"),
    PRAISE(1, "点赞"),
    REPORT(1, "举报"),
    SHIELDING(1, "屏蔽"),
    SILENT(1, "禁言");

    /**
     * 推送目标类型
     *
     * 0：不限用户ID及物业角色，1：用户ID集合，2：物业角色集合
     */
    private Integer type;

    private String value;

    PushPointEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public Integer type() {
        return this.type;
    }

    public String value() {
        return this.value;
    }

    public static Set<String> getPointIdsByType(Integer type) {
        Set<String> set = new HashSet<>(PushPointEnum.values().length);
        for (PushPointEnum pushPointEnum : PushPointEnum.values()) {
            if (pushPointEnum.type() == type) {
                set.add(pushPointEnum.name());
            }
        }
        return set;
    }

    /**
     * 待配置的功能节点集合
     * @return
     */
    public static Map<String, String> functionPoints() {
        Map<String, String> map = new HashMap<>(4);
        map.put(APPLY_ROOM_ATTESTATION.name(), APPLY_ROOM_ATTESTATION.value);
//        map.put(FAULT.name(), FAULT.value);
        map.put(FAULT_ALLOCATED.name(), FAULT_ALLOCATED.value);
        map.put(ALARM.name(), ALARM.value);
        return map;
    }

    /**
     * 房屋认证相关推送节点集合
     * @return
     */
    public static Set<String> getRoomPointIds() {
        Set<String> set = new HashSet<>(6);
        set.add(APPLY_ROOM_ATTESTATION.name());
        set.add(AUDIT_ROOM_ATTESTATION.name());
        set.add(APPLY_ROOM_BINDING.name());
        set.add(AUDIT_ROOM_BINDING_BY_OWNER.name());
        set.add(AUDIT_ROOM_BINDING_BY_PROPERTY.name());
        set.add(UNBINDING_ROOM.name());
        return set;
    }

}
