package cn.bit.framework.constant;/**
 * Created by terry on 2016/7/29.
 */

/**
 * @author terry
 * @create 2016-07-29 18:00
 **/
public class GlobalConstants {

    /* ========================= 公共属性名称定义 ============================ */
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_GUID = "guid";
    public static final String PROPERTY_IDS = "ids";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_CODE = "code";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_MD5 = "md5";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_DESC = "desc";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_CREATED_AT = "createdAt";
    public static final String PROPERTY_UPDATED_AT = "updatedAt";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_ACCESS_TOKEN = "accessToken";
    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_MOBILE = "mobile";
    public static final String PROPERTY_ACCOUNT_ID = "accountId";
    public static final String PROPERTY_GROUP_ID = "groupId";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_USER_NAME = "username";
    public static final String PROPERTY_ROLE = "role";
    public static final String PROPERTY_GROUP_NICKNAME = "groupNickname";
    public static final String PROPERTY_PARENT_ID = "parentId";
    public static final String PROPERTY_CREATOR_ID = "creatorId";
    public static final String PROPERTY_LEVEL = "level";
    public static final String PROPERTY_REGION_ID = "regionId";
    public static final String PROPERTY_ADDRESS = "address";
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_OWNER_ID = "ownerId";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_SOURCE_TYPE = "sourceType";
    public static final String PROPERTY_RESOURCE_TYPE = "resourceType";
    public static final String PROPERTY_PKS = "pks";
    public static final String PROPERTY_PRODUCT_UNIQUE_ID = "productUniqueId";
    public static final String PROPERTY_PRODUCT_ID = "productId";
    public static final String PROPERTY_ACTIVATION_FRONT_URL = "activationFrontUrl";
    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_TYPE = "type";

    /* ========================= 模板名称 ============================== */
    public static final String TPL_ACCOUNT_ACTIVATION_MAIL = "accountActivationMail.tpl";
    public static final String TPL_RETRIEVE_PASSWORD_MAIL = "retrievePasswordMail.tpl";

    /* ======================== 资源文件 ===============================*/
    public static final String MESSAGE_RESOURCE = "messages/messages";
    public static final String MESSAGE_KEY_ACTIVATION_MAIL_SUBJECT = "activation.mail.subject";
    public static final String MESSAGE_KEY_RETRIEVE_PASSWORD_MAIL_SUBJECT = "retrievePassword.mail.subject";

    /* ======================== others =============================== */
    public static final long ROOT_GROUP_ID = 0L;
    public static final long CONSUMER_CODE_ONYX = 0;

    /* ======================== regex ============================ */
    //手机号
    public static final String REGEX_PHONE = "^1[3456789]\\d{9}$";
    //车牌号规则，仅能识别大部分常规车牌
    public static final String REGEX_CARNO = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[警京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]{0,1}[A-Z0-9]{4,5}[A-Z0-9挂学警港澳]{1}$";

}
