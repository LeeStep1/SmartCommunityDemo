package cn.bit.facade.enums;

/**
 * 社区角色
 */
public enum RoleType {

    // （0：社区管理员；1：管理员；2：保安；3：保洁；4：维修工；5：住户；6：客服人员；7：企业管理员）
    CM_ADMIN(0,"社区管理员"),
    MANAGER(1,"物业管理员"),
    SECURITY(2,"保安"),
    CLEANER(3,"保洁"),
    SERVICEMAN(4,"维修工"),
    HOUSEHOLD(5,"住户"),
    SUPPORTSTAFF(6,"客服人员"),
    COMPANY_ADMIN(7,"企业管理员");

    private Integer key;

    private String value;

    RoleType(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public int key() {
        return this.key;
    }

    public String value() {
        return this.value;
    }
}
