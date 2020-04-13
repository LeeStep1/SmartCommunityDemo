package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/6.
 */


import cn.bit.framework.exceptions.BizException;

/**
 * @author terry
 * @create 2016-08-06 16:21
 **/
public class StorageException extends BizException {

    public static final int STORAGE_SAVE_FAILD = 59030001;
    public static final int STORAGE_DOWNLOAD_FAILD = 59030002;
    public static final int STORAGE_DELETE_FAILD = 59030003;
    public static final int STORAGE_OPER_FAILD = 59030004;

    public StorageException() {
    }

    public StorageException(String message) {
        super(-1, message);
    }

    public StorageException(Integer code, String message) {
        super(code, message);
        printStackTrace();
    }


}
