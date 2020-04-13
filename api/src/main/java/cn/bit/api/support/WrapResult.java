package cn.bit.api.support;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WrapResult<T> extends ApiResult<T> {

    private ApiResult<T> actualResult;

    private Object[] dataArray;

    public WrapResult(ApiResult<T> actualResult, Object... dataArray) {
        this.actualResult = actualResult;
        this.dataArray = dataArray;
    }

    public static WrapResult create(ApiResult actualResult, Object... dataArray) {
        return new WrapResult(actualResult, dataArray);
    }

    @Override
    public T getData() {
        return actualResult.getData();
    }
}
