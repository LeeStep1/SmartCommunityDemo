package cn.bit.api.support;

import cn.bit.framework.exceptions.BizException;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by terry on 2018/1/15.
 */
@Data
public class ApiResult<T> implements Serializable {

    private T data;  //获取调用返回值

    private int errorCode; //获取错误码

    private String errorMsg;

    private boolean success = false;

    public ApiResult() {
    }

    public ApiResult(T result) {
        this.data = result;
    }

    public ApiResult(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static ApiResult ok() {
        ApiResult result = new ApiResult(0, "success");
        result.setSuccess(true);
        result.data = null;
        return result;
    }

    public static ApiResult ok(Object data) {
        ApiResult result = new ApiResult(0, "success");
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static ApiResult error(BizException ex) {
        ApiResult result = new ApiResult(ex.getCode(), ex.getMsg());
        result.setSuccess(false);
        return result;
    }

    public static ApiResult error(int errorCode, String errorMsg) {
        ApiResult result = new ApiResult(errorCode, errorMsg);
        result.setSuccess(false);
        return result;
    }
}
