package cn.bit.facade.enums;

import cn.bit.framework.utils.string.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置参数key的定义
 * parameter key
 */
public enum ParamKeyType {

    // 物业费配置
    CHARGINGSTANDARDS("收费标准", "chargingStandards"),//1：按月收费，2：按季收费，3：按半年收费，4：按年收费
    PREPAID("预付费", "prepaid"),//true,false
    BILLCREATEDAY("每月账单生成日", "billCreateDay"),
    NEXTEFFECTDATE("下期账单生成时间", "nextEffectDate"),
    LASTEFFECTDATE("最近已生成账单时间", "lastEffectDate"),
    BILLOVERDUE("账单超期日数", "billOverdue"),
    BILLREMARK("收费通知单备注", "billRemark"),
    // 社区动态配置
    REPORTEXPECTNUM("每日可举报上限数", "reportExpectNum"),//平台共享（没有communityId）
    AUTOAUDITMOMENT("动态自动审核", "autoAuditMoment"),
    MOMENTSHIELDINGREPORTNUM("动态屏蔽举报数", "momentShieldingReportNum"),
    MOMENTWARNINGREPORTNUM("动态提醒举报数", "momentWarningReportNum"),
    COMMENTSHIELDINGREPORTNUM("评论屏蔽举报数", "commentShieldingReportNum"),
    COMMENTWARNINGREPORTNUM("评论提醒举报数", "commentWarningReportNum"),
    // 住户认证配置
    LEVEL2AUDIT("是否开启二级用户认证审核", "level2Audit"),
    IDENTITY_CARD("身份证号", "identityCard"),
    HOUSEHOLDADDRESS("户籍地址", "householdAddress"),
    // (1：群众；2：中共党员(包括预备党员)；3：共青团员；4：少先队员；)
    POLITICSSTATUS("政治面貌", "politicsStatus"),
    // 农村户口/城镇户口
    HOUSEHOLD_TYPE("户口类型", "householdType"),
    LANDLINENUMBER("固话号码", "telPhone"),
    WORKUNIT("工作单位", "workUnit"),
    PRESENTADDRESS("现住址", "currentAddress"),
    HOUSECONTRACTNUMBER("住房合同编号", "contract"),
    CHECKINTIME("入户时间", "checkInTime"),
    // 投诉报事配置
    COMPLAIN_AUTO_PENDING("投诉报事自动受理", "complainAutoPending"),
    ;

    private String value;
    private String fieldName;

    private static Map<String,String> map = new HashMap<>();

    static {
        for (ParamKeyType paramKeyType : ParamKeyType.values()) {
            map.put(paramKeyType.name(), paramKeyType.getValue());
        }
    }

    ParamKeyType(String value, String fieldName){
        this.value = value;
        this.fieldName = fieldName;
    }

    public String getValue(){
        return this.value;
    }

    public String getFieldName(){
        return this.fieldName;
    }

    public static boolean checkParamKey(String name) {
        return StringUtil.isNotNull(map.get(name));
    }

    public static Map getParamKeys(){
        return map;
    }
}
