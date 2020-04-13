package cn.bit.facade.constant.mq;

public class RoutingKeyConstant {

    /* ============================= 住户相关 ============================= */

    /**
     * 新增住户路由键
     */
    public static final String ROUTING_KEY_HOUSEHOLD_NEW = "household.new";
    /**
     * 删除住户路由键
     */
    public static final String ROUTING_KEY_HOUSEHOLD_DELETE = "household.delete";



    /* ============================= 物业人员相关相关 ============================= */

    /**
     * 物业辖区变更路由键
     */
    public static final String ROUTING_KEY_PROPERTY_DISTRICT_CHANGE = "property.district.change";

}
