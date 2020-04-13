package cn.bit.framework.storage;

/**
 * Created by terry on 2016/8/8.
 */
public interface StorageCallback<T extends StorageService.StorageResult> {

    void complete(T result);

}
