package cn.bit.facade.constant;

import org.bson.types.ObjectId;


/**
 * 角色常量类
 *
 * @author decai.liu
 * @date 2019-06-10
 */
public class RoleConstants {

    /**
     * 物业公司企业管理员角色字符串
     */
    public static final String ROLE_STR_COMPANY_ADMIN = "100000000000000000000001";

    /**
     * 物业公司企业管理员角色ID
     */
    public static final ObjectId ROLE_ID_COMPANY_ADMIN = new ObjectId(ROLE_STR_COMPANY_ADMIN);

    /**
     * 住户角色字符串
     */
    public static final String ROLE_STR_HOUSEHOLD = "000000000000000000000002";

    /**
     * 住户角色字符串ID
     */
    public static final ObjectId ROLE_ID_HOUSEHOLD = new ObjectId(ROLE_STR_HOUSEHOLD);

}
