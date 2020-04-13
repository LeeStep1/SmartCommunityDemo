package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/6.
 */

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage 元数据
 *
 * @author terry
 * @create 2016-08-06 17:19
 **/
public class StorageMetadata implements Serializable {

    private String contentEncoding;
    private String cacheControl;
    private String contentType;
    private Long contentLength;
    private String contentMD5;
    private String contentDisposition;
    private Date expires;
    private Date lastModified;
    private Map<String, String> userData = new HashMap<>();
    //资源权限
    private StorageAcl acl;
    //远程回调参数
    private StorageRemoteCallback remoteCallback;

    public StorageMetadata() {
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public StorageAcl getAcl() {
        return acl;
    }

    public void setAcl(StorageAcl acl) {
        this.acl = acl;
    }

    public StorageRemoteCallback getRemoteCallback() {
        return remoteCallback;
    }

    public void setRemoteCallback(StorageRemoteCallback remoteCallback) {
        this.remoteCallback = remoteCallback;
    }

    public Map<String, String> getUserData() {
        return userData;
    }

    public void setUserData(Map<String, String> userData) {
        this.userData = userData;
    }

    private StorageMetadata(Builder b) {
        this.cacheControl = b.cacheControl;
        this.contentType = b.contentType;
        this.contentLength = b.contentLength;
        this.contentEncoding = b.contentEncoding;
        this.contentMD5 = b.contentMD5;
        this.expires = b.expires;
        this.contentDisposition = b.contentDisposition;
        this.lastModified = b.lastModified;
        this.acl = b.acl;
        this.remoteCallback = b.remoteCallback;
        this.userData = b.userData;
    }

    public static class Builder {

        private String contentEncoding;
        private String cacheControl;
        private String contentType;
        private Long contentLength;
        private String contentMD5;
        private String contentDisposition;
        private Date expires;
        private Date lastModified;
        private StorageAcl acl;
        private StorageRemoteCallback remoteCallback;
        private Map<String, String> userData = new HashMap<>();

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
        }

        public Builder cacheControl(String cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        public Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public Builder contentDisposition(String disposition) {
            this.contentDisposition = disposition;
            return this;
        }

        public Builder expires(Date expires) {
            this.expires = expires;
            return this;
        }

        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder acl(StorageAcl acl) {
            this.acl = acl;
            return this;
        }

        public Builder remoteCallback(StorageRemoteCallback remoteCallback) {
            this.remoteCallback = remoteCallback;
            return this;
        }

        public Builder userData(String key, String value) {
            this.userData.put(key, value);
            return this;
        }

        public Builder userData(Map<String, String> userData) {
            if (userData != null && !userData.isEmpty()) {
                this.userData.clear();
                this.userData.putAll(userData);
            }
            return this;
        }

        public StorageMetadata build() {
            return new StorageMetadata(this);
        }

    }
}
