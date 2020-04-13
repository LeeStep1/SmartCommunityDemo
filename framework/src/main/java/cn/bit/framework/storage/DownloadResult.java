package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/8.
 */

/**
 * @author terry
 * @create 2016-08-08 17:00
 **/
public class DownloadResult extends StorageService.StorageResult {

    private StorageMetadata metadata;

    public StorageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(StorageMetadata metadata) {
        this.metadata = metadata;
    }
}
