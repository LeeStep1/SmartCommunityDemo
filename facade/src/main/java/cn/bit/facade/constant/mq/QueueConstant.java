package cn.bit.facade.constant.mq;

public class QueueConstant {

    /* ============================= 交易相关 ============================= */

    /**
     * 支付通知队列
     */
    public static final String QUEUE_TRADE_PAYMENT_NOTIFY = "trade.payment.notify";
    /**
     * 支付通知死信队列（用于阶梯性延时重试）
     */
    public static final String DEAD_QUEUE_TRADE_PAYMENT_NOTIFY = "trade.payment.notify.dead";
    /**
     * 退款通知队列
     */
    public static final String QUEUE_TRADE_REFUND_NOTIFY = "trade.refund.notify";
    /**
     * 退款通知死信队列（用于阶梯性延时重试）
     */
    public static final String DEAD_QUEUE_TRADE_REFUND_NOTIFY = "trade.refund.notify.dead";



    /* ============================= 物业账单相关 ============================= */

    /**
     * 物业账单支付通知队列
     */
    public static final String QUEUE_FEES_PROPERTY_BILL_PAYMENT_NOTIFY = "fees.property.bill.payment.notify";
    /**
     * 物业账单支付通知死信队列（用于阶梯性延时重试）
     */
    public static final String DEAD_QUEUE_FEES_PROPERTY_BILL_PAYMENT_NOTIFY = "fees.property.bill.payment.notify.dead";



    /* ============================= 社区物联网相关 ============================= */

    /**
     * 增加门禁授权队列
     */
    public static final String QUEUE_COMMUNITY_IOT_DOOR_AUTH_ADD = "communityIoT.door.auth.add";
    /**
     * 增加门禁授权死信队列
     */
    public static final String DEAD_QUEUE_COMMUNITY_IOT_DOOR_AUTH_ADD = "communityIoT.door.auth.add.dead";
    /**
     * 删除门禁授权队列
     */
    public static final String QUEUE_COMMUNITY_IOT_DOOR_AUTH_DELETE = "communityIoT.door.auth.delete";
    /**
     * 删除门禁授权死信队列
     */
    public static final String DEAD_QUEUE_COMMUNITY_IOT_DOOR_AUTH_DELETE = "communityIoT.door.auth.delete.dead";
    /**
     * 覆盖门禁授权队列
     */
    public static final String QUEUE_COMMUNITY_IOT_DOOR_AUTH_COVER = "communityIoT.door.auth.cover";
    /**
     * 覆盖门禁授权死信队列
     */
    public static final String DEAD_QUEUE_COMMUNITY_IOT_DOOR_AUTH_COVER = "communityIoT.door.auth.cover.dead";
    /**
     * 增加梯禁授权队列
     */
    public static final String QUEUE_COMMUNITY_IOT_ELEVATOR_AUTH_ADD = "communityIoT.elevator.auth.add";
    /**
     * 增加梯禁授权死信队列
     */
    public static final String DEAD_QUEUE_COMMUNITY_IOT_ELEVATOR_AUTH_ADD = "communityIoT.elevator.auth.add.dead";
    /**
     * 删除梯禁授权队列
     */
    public static final String QUEUE_COMMUNITY_IOTELEVATOR_AUTH_DELETE = "communityIoT.elevator.auth.delete";
    /**
     * 删除梯禁授权死信队列
     */
    public static final String DEAD_QUEUE_COMMUNITY_IOTELEVATOR_AUTH_DELETE = "communityIoT.elevator.auth.delete.dead";
    /**
     * 覆盖梯禁授权队列
     */
    public static final String QUEUE_COMMUNITY_IOT_ELEVATOR_AUTH_COVER = "communityIoT.elevator.auth.cover";
    /**
     * 覆盖梯禁授权死信队列
     */
    public static final String DEAD_QUEUE_COMMUNITY_IOT_ELEVATOR_AUTH_COVER = "communityIoT.elevator.auth.cover.dead";



    /* ============================= 用户相关 ============================= */

    /**
     * IM账号注册队列
     */
    public static final String QUEUE_USER_IM_ACCOUNT_REGISTER = "user.im.account.register";



    /* ============================= 用户相关 ============================= */

    /**
     * 新增用户门禁授权回调队列
     */
    public static final String QUEUE_HOUSEHOLD_NEW_DOOR_AUTH_CALLBACK = "household.new.door.auth.callback";



    /* ============================= 物业人员相关 ============================= */

    /**
     * 物业人员辖区变更门禁授权回调队列
     */
    public static final String QUEUE_PROPERTY_DISTRICT_CHANGE_DOOR_AUTH_CALLBACK = "property.district.change.door.auth.callback";
}
