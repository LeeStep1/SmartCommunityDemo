package cn.bit.facade.constant.mq;

public class TopicConstant {

    /* ============================= 社区物联网相关 ============================= */

    /**
     * 门禁授权队列
     */
    public static final String TOPIC_COMMUNITY_IOT_DOOR_AUTH = "communityIoT_door_auth";

    /**
     * 电梯授权队列
     */
    public static final String TOPIC_COMMUNITY_IOT_ELEVATOR_AUTH = "communityIoT_elevator_auth";

    /* ============================= 用户相关 ============================= */

    /**
     * 住户回调队列
     */
    public static final String TOPIC_HOUSEHOLD_NEW_DOOR_AUTH = "household_new_door_auth";

    /* ============================= 物业人员相关 ============================= */

    /**
     * 物业回调队列
     */
    public static final String TOPIC_PROPERTY_CHANGE_DOOR_AUTH = "property_district_change_door_auth";

    /* ============================= 交易相关 ============================= */

    /**
     * 支付通知队列
     */
    public static final String TOPIC_TRADE_PAYMENT = "trade_payment";
    /**
     * 退款通知队列
     */
    public static final String TOPIC_TRADE_REFUND = "trade_refund";

    /* ============================= 物业账单相关 ============================= */

    /**
     * 物业账单支付通知队列
     */
    public static final String TOPIC_FEES_PROPERTY_BILL_PAYMENT = "fees_property_bill_payment";

    /* ============================= 万能凭证相关 ============================= */

    /**
     * 物业账单支付通知队列
     */
    public static final String TOPIC_COMMUNITYIOT_UNIVERSAL_CERTIFICATE = "communityIoT_universal_certificate";

    /* ============================= 住房档案相关 ============================= */

    /**
     * 住房档案excel导入后的业务处理
     */
    public static final String TOPIC_HOUSEHOLD_IMPORT = "household_import";

}
