package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/6.
 */

import java.io.InputStream;

/**
 * @author terry
 * @create 2016-08-06 18:20
 **/
public class StorageObject extends StorageService.StorageResult {

    private StorageMetadata metadata;
    private InputStream content;
    private String url;
    private String md5;

    public StorageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(StorageMetadata metadata) {
        this.metadata = metadata;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
