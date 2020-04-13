package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/6.
 */

import java.io.InputStream;

/**
 * @author terry
 * @create 2016-08-06 16:46
 **/
public class SaveResult extends StorageService.StorageResult {

    /**
     * 资源url
     */
    private String url;
    private String md5;

    /**
     * 远程回调响应内容，用完务必close防止资源泄露
     */
    private InputStream callbackResponseBody;

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

    public InputStream getCallbackResponseBody() {
        return callbackResponseBody;
    }

    public void setCallbackResponseBody(InputStream callbackResponseBody) {
        this.callbackResponseBody = callbackResponseBody;
    }
}
