package cn.bit.framework.storage;

/**
 * Created by terry on 2016/8/6.
 */
public enum StorageAcl {

    DEFAULT("default"),
    PRIVATE("private"),
    PUBLIC_READ("public-read"),
    PUBLIC_READ_WRITE("public-read-write");

    private String acl;

    StorageAcl(String acl) {
        this.acl = acl;
    }

    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }
}
